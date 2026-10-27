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

import java.util.ArrayList;
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
    //Original styles of the block currently being serialized
    private ScalarStyle originalKeyStyle = null, originalValueStyle = null;
    private FlowStyle originalValueFlowStyle = null;
    //If the original key style should be applied (the entry's key is still the one it was loaded with)
    private boolean applyOriginalKeyStyle = false;

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
        //Use the original style of the block currently being serialized (if enabled and still applicable)
        if (dumperSettings.isPreserveScalarStyle()) {
            ScalarStyle original = nodeRole == NodeRole.KEY ? (applyOriginalKeyStyle ? originalKeyStyle : null) : originalValueStyle;
            if (original != null) {
                //Consume the value style, so it does not leak into nested scalars
                if (nodeRole == NodeRole.VALUE)
                    originalValueStyle = null;
                //Construct directly - the parent represents PLAIN as the configured default style
                return new ScalarNode(tag, value, dumperSettings.getScalarFormatter().format(tag, value, nodeRole, original));
            }
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
     * style preservation is enabled, this is the style the collection was loaded with; otherwise the given configured
     * style is returned.
     *
     * @param configured the configured flow style
     * @return the flow style to use as the default
     */
    private FlowStyle preserveFlowStyle(FlowStyle configured) {
        //Only a block's own value can have an original style (not nested collections)
        if (dumperSettings.isPreserveFlowStyle() && nodeRole == NodeRole.VALUE && originalValueFlowStyle != null) {
            FlowStyle original = originalValueFlowStyle;
            //Consume the style, so it does not leak into nested collections
            originalValueFlowStyle = null;
            return original;
        }
        return configured;
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
        //Block
        Block<?> block = entry.getValue() instanceof Block ? (Block<?>) entry.getValue() : null;
        //Stash original styles for this block; apply the key style only while the key is still the one it was loaded with
        originalKeyStyle = block == null ? null : block.getOriginalKeyStyle();
        applyOriginalKeyStyle = originalKeyStyle != null && block.getOriginalKey() != null && block.getOriginalKey().equals(String.valueOf(entry.getKey()));
        originalValueStyle = block == null ? null : block.getOriginalValueStyle();
        originalValueFlowStyle = block == null ? null : block.getOriginalValueFlowStyle();
        //Represent the key
        Node key = applyComments(block, nodeRole = NodeRole.KEY, representData(entry.getKey()), false);
        //Represent the value
        Node value = applyComments(block, nodeRole = NodeRole.VALUE, representData(block == null ? entry.getValue() : block.getStoredValue()), false);
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