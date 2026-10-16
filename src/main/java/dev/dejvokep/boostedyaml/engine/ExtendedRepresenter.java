/*
 * Copyright 2026 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.engine;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.Comments;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.RepresentToNode;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * A custom representer for the SnakeYAML Engine allowing to represent {@link Section} objects, serializing custom
 * objects, all of which while keeping comments and without any additional time consumption.
 */
public class ExtendedRepresenter extends StandardRepresenter {

    //General settings
    private final GeneralSettings generalSettings;
    //Dumper settings
    private final DumperSettings dumperSettings;

    //Currently serialized node role
    private NodeRole nodeRole = NodeRole.KEY;
    //Original key and value scalar styles of the block currently being serialized
    private ScalarStyle originalKeyStyle = null, originalValueStyle = null;
    //Original flow style of a section value currently being serialized
    private FlowStyle originalValueFlowStyle = null;
    //Pre-order styles of the value subtree currently being serialized, replayed for its nested scalars/collections
    private Deque<Enum<?>> valueStyles = null;

    /**
     * Creates an instance of the representer.
     *
     * @param documentType    the represented type of the document
     * @param generalSettings general settings of the root's file, whose contents are going to be represented
     * @param dumperSettings  dumper settings
     * @param engineSettings  engine dump settings already built by the dumper settings
     */
    public ExtendedRepresenter(
            final Class<?> documentType,
            final @NotNull GeneralSettings generalSettings,
            final @NotNull DumperSettings dumperSettings,
            final @NotNull DumpSettings engineSettings
    ) {
        //Call the superclass constructor
        super(engineSettings);
        //Set
        this.generalSettings = generalSettings;
        this.dumperSettings = dumperSettings;

        //Representers
        RepresentToNode representSerializable = new RepresentSerializable();
        //Add representers
        super.representers.put(documentType, new RepresentDocument());
        super.representers.put(Section.class, new RepresentSection());
        super.representers.put(Enum.class, new RepresentEnum());
        super.representers.put(String.class, new RepresentString(super.representers.get(String.class)));
        //Add all types
        for (Class<?> clazz : generalSettings.getSerializer().getSupportedClasses())
            super.representers.put(clazz, representSerializable);
        for (Class<?> clazz : generalSettings.getSerializer().getSupportedParentClasses())
            super.parentClassRepresenters.put(clazz, representSerializable);
    }

    /**
     * Creates an instance of the representer.
     *
     * @param generalSettings general settings of the root's file, whose contents are going to be represented
     * @param dumperSettings  dumper settings
     * @see #ExtendedRepresenter(GeneralSettings, DumperSettings)
     */
    public ExtendedRepresenter(@NotNull GeneralSettings generalSettings, @NotNull DumperSettings dumperSettings) {
        this(YamlDocument.class, generalSettings, dumperSettings, dumperSettings.buildEngineSettings());
    }

    @Override
    protected Node representScalar(Tag tag, String value, ScalarStyle scalarStyle) {
        // Replaying styles of a value subtree (e.g. the elements of a sequence)
        if (valueStyles != null) {
            if (valueStyles.peekFirst() instanceof ScalarStyle) {
                ScalarStyle original = (ScalarStyle) valueStyles.pollFirst();
                if (dumperSettings.isPreserveScalarStyle())
                    return new ScalarNode(tag, value, dumperSettings.getScalarFormatter().format(tag, value, nodeRole, original));
                return super.representScalar(tag, value, dumperSettings.getScalarFormatter().format(tag, value, nodeRole, scalarStyle));
            }
            valueStyles = null;
        }

        // The key, and a value with no node to replay (e.g. one replaced via a set), keep their block-level style here
        if (dumperSettings.isPreserveScalarStyle()) {
            ScalarStyle original = nodeRole == NodeRole.KEY ? originalKeyStyle : originalValueStyle;
            if (original != null)
                return new ScalarNode(tag, value, dumperSettings.getScalarFormatter().format(tag, value, nodeRole, original));
        }

        return super.representScalar(tag, value, dumperSettings.getScalarFormatter().format(tag, value, nodeRole, scalarStyle));
    }

    @Override
    protected Node representSequence(Tag tag, Iterable<?> sequence, FlowStyle flowStyle) {
        return super.representSequence(tag, sequence, dumperSettings.getSequenceFormatter().format(tag, sequence, nodeRole, preserveFlowStyle(flowStyle)));
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
        return super.representMapping(tag, mapping, dumperSettings.getMappingFormatter().format(tag, mapping, nodeRole, preserveFlowStyle(flowStyle)));
    }

    /**
     * Returns the default flow style to hand to the formatter for the collection currently being serialized. When flow
     * style preservation is enabled, this is the style the collection was loaded with (taken from the value subtree
     * replay, or from a section's stored style); otherwise the given configured style is returned.
     *
     * @param configured the configured flow style
     * @return the flow style to use as the default
     */
    private FlowStyle preserveFlowStyle(FlowStyle configured) {
        // Replaying styles of a value subtree
        if (valueStyles != null) {
            if (valueStyles.peekFirst() instanceof FlowStyle) {
                FlowStyle original = (FlowStyle) valueStyles.pollFirst();
                return dumperSettings.isPreserveFlowStyle() ? original : configured;
            }
            // The value no longer matches the loaded node - stop replaying and fall back
            valueStyles = null;
        }

        // Section values keep their flow style here, since they are not replayed from a node
        if (dumperSettings.isPreserveFlowStyle() && originalValueFlowStyle != null) {
            FlowStyle original = originalValueFlowStyle;
            originalValueFlowStyle = null;
            return original;
        }
        return configured;
    }

    /**
     * Collects the scalar and flow styles of the given node and all its descendants in pre-order (the same order in
     * which they are serialized), so they can be replayed onto the produced nodes.
     *
     * @param node the value node to collect styles from
     * @return the collected styles, oldest first
     */
    private Deque<Enum<?>> collectStyles(@NotNull Node node) {
        Deque<Enum<?>> styles = new ArrayDeque<>();
        collectStyles(node, styles);
        return styles;
    }

    /**
     * Returns whether the given value node still matches the given value, i.e. they are the same kind and, for
     * collections, the same size. Used to decide whether a value replaced via a set can have its per-element styles
     * replayed - a replacement of a different shape cannot be aligned positionally.
     *
     * @param node  the original value node
     * @param value the current value
     * @return whether the styles of the node can be replayed onto the value
     */
    private boolean nodeMatchesValue(@NotNull Node node, @Nullable Object value) {
        if (node instanceof ScalarNode)
            return !(value instanceof Collection) && !(value instanceof Map);
        if (node instanceof SequenceNode)
            return value instanceof Collection && ((Collection<?>) value).size() == ((SequenceNode) node).getValue().size();
        if (node instanceof MappingNode)
            return value instanceof Map && ((Map<?, ?>) value).size() == ((MappingNode) node).getValue().size();
        return false;
    }

    private void collectStyles(@NotNull Node node, @NotNull Deque<Enum<?>> styles) {
        if (node instanceof ScalarNode) {
            styles.addLast(((ScalarNode) node).getScalarStyle());
        } else if (node instanceof SequenceNode) {
            styles.addLast(((SequenceNode) node).getFlowStyle());
            for (Node element : ((SequenceNode) node).getValue())
                collectStyles(element, styles);
        } else if (node instanceof MappingNode) {
            styles.addLast(((MappingNode) node).getFlowStyle());
            for (NodeTuple tuple : ((MappingNode) node).getValue()) {
                collectStyles(tuple.getKeyNode(), styles);
                collectStyles(tuple.getValueNode(), styles);
            }
        }
    }

    /**
     * Node representer implementation for serializable objects.
     */
    private class RepresentSerializable implements RepresentToNode {

        @Override
        public Node representData(Object data) {
            //Serialize
            Object serialized = generalSettings.getSerializer().serialize(data, generalSettings.getDefaultMapSupplier());
            //Return
            return ExtendedRepresenter.this.representData(serialized == null ? data : serialized);
        }

    }

    /**
     * Node representer implementation for {@link YamlDocument documents}.
     */
    private class RepresentDocument implements RepresentToNode {

        @Override
        public Node representData(Object data) {
            //Cast
            Section section = (Section) data;
            //Return
            return applyComments(section, NodeRole.VALUE, ExtendedRepresenter.this.representData(section.getStoredValue()), section.isRoot());
        }

    }

    /**
     * Node representer implementation for {@link Section sections}.
     * <p>
     * Used only when cloning during updating, all other cases are already handled by
     * {@link #representMappingEntry(Map.Entry)}.
     */
    private class RepresentSection implements RepresentToNode {

        @Override
        public Node representData(Object data) {
            //Cast
            Section section = (Section) data;
            //Return
            return applyComments(section, NodeRole.KEY, ExtendedRepresenter.this.representData(section.getStoredValue()), section.isRoot());
        }

    }

    /**
     * Node representer implementation for {@link Enum enums}.
     */
    private class RepresentEnum implements RepresentToNode {

        @Override
        public Node representData(Object data) {
            return ExtendedRepresenter.this.representData(((Enum<?>) data).name());
        }

    }

    /**
     * Node representer implementation for {@link String strings}.
     */
    private class RepresentString implements RepresentToNode {

        // Previous representer
        private final RepresentToNode previous;

        /**
         * Creates an instance of the custom string representer.
         *
         * @param previous the previous representer, used to represent the string itself
         */
        private RepresentString(@NotNull RepresentToNode previous) {
            this.previous = previous;
        }

        @Override
        public Node representData(Object data) {
            // Update the style
            ScalarStyle previousStyle = defaultScalarStyle;
            defaultScalarStyle = dumperSettings.getStringStyle();
            // Represent
            Node node = previous.representData(data);
            // Revert back
            defaultScalarStyle = previousStyle;
            return node;
        }

    }

    /**
     * Applies (sets) comments of the block, at the given position to a node. This method overwrites comments previously
     * associated with the node.
     *
     * @param block    the block whose comments to apply
     * @param nodeRole comments of node with the role to apply
     * @param node     the node to set the comments to
     * @param isRoot   if the provided node is the root node - represents the root section
     * @return the provided node, now with set comments
     */
    private Node applyComments(@Nullable Block<?> block, @NotNull NodeRole nodeRole, @NotNull Node node, boolean isRoot) {
        // No comments to apply
        if (block == null)
            return node;

        // Apply block comments (before+after)
        if (allowBlockComments(isRoot)) {
            node.setBlockComments(Comments.get(block, nodeRole, Comments.Position.BEFORE));
            node.setEndComments(Comments.get(block, nodeRole, Comments.Position.AFTER));
        }

        List<CommentLine> inline = Comments.get(block, nodeRole, Comments.Position.INLINE);
        if (inline != null && !inline.isEmpty()) {
            // If allowed
            if (allowInlineComments(node)) {
                node.setInLineComments(inline);
            } else if (allowBlockComments(isRoot)) {
                // Add to before block comments
                List<CommentLine> before = node.getBlockComments() == null ? new ArrayList<>(inline.size()) : new ArrayList<>(node.getBlockComments());
                for (CommentLine line : inline)
                    before.add(new CommentLine(line.getStartMark(), line.getEndMark(), line.getValue(), line.getCommentType() == CommentType.IN_LINE ? CommentType.BLOCK : line.getCommentType()));
                node.setBlockComments(before);
            }
            // Drop the comments
        }

        return node;
    }

    @Override
    protected NodeTuple representMappingEntry(Map.Entry<?, ?> entry) {
        // Entry of a plain map nested within a value subtree - styles are supplied by the active replay
        if (valueStyles != null) {
            Node replayedKey = representData(entry.getKey());
            Node replayedValue = representData(entry.getValue());
            return new NodeTuple(replayedKey, replayedValue);
        }

        //Block
        Block<?> block = entry.getValue() instanceof Block ? (Block<?>) entry.getValue() : null;
        //Stash original styles for this block
        originalKeyStyle = block == null ? null : block.getOriginalKeyStyle();
        originalValueStyle = block == null ? null : block.getOriginalValueStyle();
        originalValueFlowStyle = block == null ? null : block.getOriginalValueFlowStyle();
        //Represent the key (before the value subtree replay is active)
        Node key = applyComments(block, nodeRole = NodeRole.KEY, representData(entry.getKey()), false);
        boolean replaying = block != null && block.getOriginalValueNode() != null
                && (dumperSettings.isPreserveScalarStyle() || dumperSettings.isPreserveFlowStyle())
                && nodeMatchesValue(block.getOriginalValueNode(), block.getStoredValue());
        if (replaying)
            valueStyles = collectStyles(block.getOriginalValueNode());
        //Represent the value
        Node value = applyComments(block, nodeRole = NodeRole.VALUE, representData(block == null ? entry.getValue() : block.getStoredValue()), false);
        if (replaying)
            valueStyles = null;
        //Create
        return new NodeTuple(key, value);
    }

    /**
     * Returns whether block comments ({@link CommentType#BLOCK} and {@link CommentType#BLANK_LINE}) are allowed to be
     * serialized within the document. The result does not depend on the {@link Node node} that is being serialized.
     *
     * @param isRoot if the node is the root node - represents the root section
     * @return if to allow block comment serialization for this node
     */
    private boolean allowBlockComments(boolean isRoot) {
        return isRoot || settings.getDefaultFlowStyle() == FlowStyle.BLOCK;
    }

    /**
     * Returns whether inline comments ({@link CommentType#IN_LINE}) are allowed to be serialized with this node.
     *
     * @param node the node whose comments are being serialized
     * @return if to allow block comment serialization for this node
     */
    private boolean allowInlineComments(@NotNull Node node) {
        return (settings.getDefaultFlowStyle() == FlowStyle.BLOCK && node instanceof ScalarNode) || (settings.getDefaultFlowStyle() == FlowStyle.FLOW && (node instanceof SequenceNode || node instanceof MappingNode));
    }

}