/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.block;

import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Comment manager for {@link Block blocks}.
 * <p>
 * <b>Please note</b> that the methods provided here support all implemented {@link Position positions} by the engine
 * as defined by the YAML spec.
 * <p>
 * However, when dumping the document, depending on the
 * {@link dev.dejvokep.boostedyaml.settings.dumper.DumperSettings.Builder#setFlowStyle(FlowStyle) flow style} currently
 * in use, comments at some positions will be lost. That is a limitation imposed by the engine and might be patched in
 * future releases. Always refer to the corresponding position documentation for detailed information regarding these
 * requirements and compatibility notes.
 */
public class Comments {

    /**
     * Comment position relative to the key/value node.
     */
    public enum Position {
        /**
         * Puts the comments before the key/value node.
         * <p>
         * <b>Serialization:</b>
         * <ul>
         *     <li>{@link FlowStyle#BLOCK}: always serialized</li>
         *     <li>{@link FlowStyle#FLOW}: serialized only with the root section</li>
         * </ul>
         * An attempt to dump a document with comments set at this position will cause them to permanently be lost.
         * <p>
         * <b>Compatibility:</b>
         * <p>{@link CommentType#IN_LINE} is not allowed to be used at this position. You can use {@link #create(String, Position)} to create comments with guaranteed compatibility.</p>
         */
        BEFORE,

        /**
         * Puts the comments inline with the key/value node.
         * <p>
         * <b>Serialization:</b>
         * <ul>
         *     <li>{@link FlowStyle#BLOCK}: serialized only with keys and values that are represented as a scalar (<b>not</b> a {@link java.util.Map map}, {@link dev.dejvokep.boostedyaml.block.implementation.Section section}, or an array-like object)</li>
         *     <li>{@link FlowStyle#FLOW}: serialized only with keys and values that are represented as a {@link java.util.Map map}, {@link dev.dejvokep.boostedyaml.block.implementation.Section section} or an array-like object</li>
         * </ul>
         * An attempt to dump a document with comments set at this position will cause them to permanently be lost.
         * <p>
         * <b>Compatibility:</b>
         * <p>{@link CommentType#IN_LINE} is the only comment type allowed at this position. You can use {@link #create(String, Position)} to create comments with guaranteed compatibility.</p>
         */
        INLINE,

        /**
         * Puts the comments after the key/value node.
         * <p>
         * <b>Serialization:</b>
         * <ul>
         *     <li>{@link FlowStyle#BLOCK}: always serialized</li>
         *     <li>{@link FlowStyle#FLOW}: serialized only with the root section</li>
         * </ul>
         * <b>If there is any following block in the document, comments at this position might be de-aligned and loaded
         * as {@link Position#BEFORE} comments for that block.</b> An attempt to dump a document with comments set at
         * this position will cause them to permanently be lost.
         * <p>
         * <b>Compatibility:</b>
         * <p>{@link CommentType#IN_LINE} is the only comment type allowed at this position. You can use {@link #create(String, Position)} to create comments with guaranteed compatibility.</p>
         */
        AFTER
    }

    /**
     * Represents to which node the comments are attached in a <i>mapping</i>.
     *
     * @deprecated Use {@link NodeRole} instead.
     */
    public enum NodeType {
        /**
         * The comments are attached to the key node of the mapping.
         */
        KEY,
        /**
         * The comments are attached to the value node of the mapping.
         */
        VALUE;

        /**
         * Returns the type as a role.
         *
         * @return the type as a role
         */
        public NodeRole toRole() {
            return this == KEY ? NodeRole.KEY : NodeRole.VALUE;
        }
    }

    /**
     * Comment representing a blank line. Cannot be used with {@link Position#INLINE}.
     */
    public static final CommentLine BLANK_LINE = new CommentLine(Optional.empty(), Optional.empty(), "", CommentType.BLANK_LINE);

    /**
     * Returns comments at the given position.
     * <p>
     * This method will return <code>null</code> or an empty {@link List}, indicating there are no comments at the
     * position.
     * <p>
     * <b>The returned list can be modified with changes reflected in the document tree immediately. Please note that
     * not all comment types are allowed at all positions. Always refer to the {@link Position} documentation when
     * managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block used to retrieve comments
     * @param node     node from which to retrieve comments
     * @param position position of the retrieved comments
     * @return the comments
     */
    @Nullable
    public static List<CommentLine> get(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Position position) {
        switch (position) {
            case BEFORE:
                return node == NodeRole.KEY ? block.beforeKeyComments : block.beforeValueComments;
            case INLINE:
                return node == NodeRole.KEY ? block.inlineKeyComments : block.inlineValueComments;
            case AFTER:
                return node == NodeRole.KEY ? block.afterKeyComments : block.afterValueComments;
            default:
                return null;
        }
    }

    /**
     * Returns comments at the given position.
     * <p>
     * This method will return <code>null</code> or an empty {@link List}, indicating there are no comments at the
     * position.
     * <p>
     * <b>The returned list can be modified with changes reflected in the document tree immediately. Please note that
     * not all comment types are allowed at all positions. Always refer to the {@link Position} documentation when
     * managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block used to retrieve comments
     * @param node     node from which to retrieve comments
     * @param position position of the retrieved comments
     * @return the comments
     * @deprecated Replaced by {@link #get(Block, NodeRole, Position)} and subject for removal.
     */
    @Deprecated
    @Nullable
    public static List<CommentLine> get(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position) {
        return get(block, node.toRole(), position);
    }

    /**
     * Sets the given comments at the given position.
     * <p>
     * To remove comments, use {@link #remove(Block, NodeType, Position)} instead. Alternatively, pass either
     * <code>null</code> or an empty {@link List} as the parameter.
     * <p>
     * Setting a list returned by {@link #get(Block, NodeType, Position)} is not necessary as the changes made to that
     * list are automatically reflected in the document structure.
     * <p>
     * <b>Please note that not all comment types are allowed at all positions. Always refer to the {@link Position}
     * documentation when managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block for which to set
     * @param node     node to attach to
     * @param position position at which to set
     * @param comments the comments to set
     * @see #create(String, Position)
     */
    public static void set(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Position position, @Nullable List<CommentLine> comments) {
        //Replace
        if (comments != null)
            comments = new ArrayList<>(comments);

        switch (position) {
            case BEFORE:
                if (node == NodeRole.KEY)
                    block.beforeKeyComments = comments;
                else
                    block.beforeValueComments = comments;
                break;
            case INLINE:
                if (node == NodeRole.KEY)
                    block.inlineKeyComments = comments;
                else
                    block.inlineValueComments = comments;
                break;
            case AFTER:
                if (node == NodeRole.KEY)
                    block.afterKeyComments = comments;
                else
                    block.afterValueComments = comments;
                break;
        }
    }

    /**
     * Sets the given comments at the given position.
     * <p>
     * To remove comments, use {@link #remove(Block, NodeType, Position)} instead. Alternatively, pass either
     * <code>null</code> or an empty {@link List} as the parameter.
     * <p>
     * Setting a list returned by {@link #get(Block, NodeType, Position)} is not necessary as the changes made to that
     * list are automatically reflected in the document structure.
     * <p>
     * <b>Please note that not all comment types are allowed at all positions. Always refer to the {@link Position}
     * documentation when managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block for which to set
     * @param node     node to attach to
     * @param position position at which to set
     * @param comments the comments to set
     * @see #create(String, Position)
     * @deprecated Replaced by {@link #set(Block, NodeRole, Position, List)} and subject for removal.
     */
    @Deprecated
    public static void set(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @Nullable List<CommentLine> comments) {
        set(block, node.toRole(), position, comments);
    }

    /**
     * Removes all comments at the given position.
     *
     * @param block    the block for which to remove
     * @param node     node to which the comments are attached
     * @param position position of the comments to remove
     */
    public static void remove(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Position position) {
        set(block, node, position, null);
    }

    /**
     * Removes all comments at the given position.
     *
     * @param block    the block for which to remove
     * @param node     node to which the comments are attached
     * @param position position of the comments to remove
     * @deprecated Replaced by {@link #remove(Block, NodeRole, Position)} and subject for removal.
     */
    @Deprecated
    public static void remove(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position) {
        set(block, node.toRole(), position, null);
    }

    /**
     * Adds the given comments to <i>already existing</i> comments at the given position.
     * <p>
     * <b>Please note that not all comment types are allowed at all positions. Always refer to the {@link Position}
     * documentation when managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block to add to
     * @param node     node to which the comments should be added
     * @param position position at which the comments should be added
     * @param comments the comments to add
     * @see #create(String, Position)
     */
    public static void add(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Position position, @NotNull List<CommentLine> comments) {
        comments.forEach(comment -> add(block, node, position, comment));
    }

    /**
     * Adds the given comments to <i>already existing</i> comments at the given position.
     * <p>
     * <b>Please note that not all comment types are allowed at all positions. Always refer to the {@link Position}
     * documentation when managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block to add to
     * @param node     node to which the comments should be added
     * @param position position at which the comments should be added
     * @param comments the comments to add
     * @see #create(String, Position)
     * @deprecated Replaced by {@link #add(Block, NodeRole, Position, List)} and subject for removal.
     */
    @Deprecated
    public static void add(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @NotNull List<CommentLine> comments) {
        comments.forEach(comment -> add(block, node.toRole(), position, comment));
    }

    /**
     * Adds the given comment to <i>already existing</i> comments at the given position.
     * <p>
     * <b>Please note that not all comment types are allowed at all positions. Always refer to the {@link Position}
     * documentation when managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block to add to
     * @param node     node to which the comments should be added
     * @param position position at which the comments should be added
     * @param comment  the comment to add
     * @see #create(String, Position)
     */
    public static void add(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Position position, @NotNull CommentLine comment) {
        switch (position) {
            case BEFORE:
                if (node == NodeRole.KEY) {
                    //Might be null
                    if (block.beforeKeyComments == null)
                        block.beforeKeyComments = new ArrayList<>();
                    //Add
                    block.beforeKeyComments.add(comment);
                } else {
                    //Might be null
                    if (block.beforeValueComments == null)
                        block.beforeValueComments = new ArrayList<>();
                    //Add
                    block.beforeValueComments.add(comment);
                }
                break;
            case INLINE:
                if (node == NodeRole.KEY) {
                    //Might be null
                    if (block.inlineKeyComments == null)
                        block.inlineKeyComments = new ArrayList<>();
                    //Add
                    block.inlineKeyComments.add(comment);
                } else {
                    //Might be null
                    if (block.inlineValueComments == null)
                        block.inlineValueComments = new ArrayList<>();
                    //Add
                    block.inlineValueComments.add(comment);
                }
                break;
            case AFTER:
                if (node == NodeRole.KEY) {
                    //Might be null
                    if (block.afterKeyComments == null)
                        block.afterKeyComments = new ArrayList<>();
                    //Add
                    block.afterKeyComments.add(comment);
                } else {
                    //Might be null
                    if (block.afterValueComments == null)
                        block.afterValueComments = new ArrayList<>();
                    //Add
                    block.afterValueComments.add(comment);
                }
                break;
        }
    }

    /**
     * Adds the given comment to <i>already existing</i> comments at the given position.
     * <p>
     * <b>Please note that not all comment types are allowed at all positions. Always refer to the {@link Position}
     * documentation when managing comments in order to avoid content loss and runtime exceptions.</b>
     *
     * @param block    the block to add to
     * @param node     node to which the comments should be added
     * @param position position at which the comments should be added
     * @param comment  the comment to add
     * @see #create(String, Position)
     * @deprecated Replaced by {@link #add(Block, NodeRole, Position, CommentLine)} and subject for removal.
     */
    @Deprecated
    public static void add(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @NotNull CommentLine comment) {
        add(block, node.toRole(), position, comment);
    }

    /**
     * Creates a comment. For blank lines, please use {@link #BLANK_LINE}.
     *
     * @param comment  the actual comment
     * @param position position at which the block will be placed
     * @return the comment line
     */
    @NotNull
    public static CommentLine create(@NotNull String comment, @NotNull Position position) {
        return new CommentLine(Optional.empty(), Optional.empty(), comment, position == Position.INLINE ? CommentType.IN_LINE : CommentType.BLOCK);
    }

}