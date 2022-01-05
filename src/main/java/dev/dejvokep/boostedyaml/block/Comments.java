/*
 * Copyright 2021 https://dejvokep.dev/
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Comment manager for {@link Block blocks}, providing additional methods on top of those already provided by {@link
 * Block}.
 * <p>
 * <b>Please note</b> that the methods provided here add possibilities for all implemented {@link Position positions}.
 * However, using positions other than {@link Position#BEFORE} might lead to comment de-alignment. Please read more
 * information at the enum constants.
 */
public class Comments {

    /**
     * Comment position relative to the node (scalar; see {@link NodeType}) to which the comment is attached.
     */
    public enum Position {
        /**
         * Puts the comments before the node.
         */
        BEFORE,

        /**
         * Puts the comments inline with the node.
         * <p>
         * <b>Please note</b> that such comments will be dumped after the node. That means, after they are loaded
         * again, they will be attached to the <b>next</b> node in the document (leading to de-alignment of the comments
         * - they will no longer be part of the same mapping). Therefore, you should <b>never</b> use this position.
         */
        INLINE,

        /**
         * Puts the comments after the node.
         * <p>
         * <b>Please note</b> that such, after they are loaded again, will be attached to the <b>next</b> node in the
         * document (leading to de-alignment of the comments - they will no longer be part of the same mapping).
         * Therefore, you should <b>never</b> use this position.
         */
        AFTER
    }

    /**
     * Represents to which node the comments are attached in a <i>mapping</i>.
     */
    public enum NodeType {
        /**
         * The comments are attached to the key node of the mapping.
         */
        KEY,
        /**
         * The comments are attached to the value node of the mapping.
         */
        VALUE
    }

    /**
     * Comment representing a blank line.
     */
    public static final CommentLine BLANK_LINE = new CommentLine(Optional.empty(), Optional.empty(), null, CommentType.BLANK_LINE);

    /**
     * Returns comments at the given position.
     * <p>
     * Please expect <code>null</code> or an empty {@link List}, representing there are no comments at the position.
     *
     * @param block    the block used to retrieve comments
     * @param node     node from which to retrieve comments
     * @param position position of the retrieved comments
     * @return the comments
     */
    @Nullable
    public static List<CommentLine> get(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position) {
        switch (position) {
            case BEFORE:
                return node == NodeType.KEY ? block.beforeKeyComments : block.beforeValueComments;
            case INLINE:
                return node == NodeType.KEY ? block.inlineKeyComments : block.inlineValueComments;
            case AFTER:
                return node == NodeType.KEY ? block.afterKeyComments : block.afterValueComments;
            default:
                return null;
        }
    }

    /**
     * Sets the given comments at the given position.
     * <p>
     * To remove comments, use {@link #remove(Block, NodeType, Position)} instead. Alternatively, pass either
     * <code>null</code> or an empty {@link List} as the parameter.
     *
     * @param block    the block for which to set
     * @param node     node to attach to
     * @param position position at which to set
     * @param comments the comments to set
     */
    public static void set(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @Nullable List<CommentLine> comments) {
        switch (position) {
            case BEFORE:
                if (node == NodeType.KEY)
                    block.beforeKeyComments = comments;
                else
                    block.beforeValueComments = comments;
                break;
            case INLINE:
                if (node == NodeType.KEY)
                    block.inlineKeyComments = comments;
                else
                    block.inlineValueComments = comments;
                break;
            case AFTER:
                if (node == NodeType.KEY)
                    block.afterKeyComments = comments;
                else
                    block.afterValueComments = comments;
                break;
        }
    }

    /**
     * Removes all comments at the given position.
     *
     * @param block    the block for which to remove
     * @param node     node to which the comments are attached
     * @param position position of the comments to remove
     */
    public static void remove(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position) {
        set(block, node, position, null);
    }

    /**
     * Adds the given comments to <i>already existing</i> comments at the given position.
     *
     * @param block    the block to add to
     * @param node     node to which the comments should be added
     * @param position position at which the comments should be added
     * @param comments the comments to add
     */
    public static void add(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @NotNull List<CommentLine> comments) {
        comments.forEach(comment -> add(block, node, position, comment));
    }

    /**
     * Adds the given comment to <i>already existing</i> comments at the given position.
     *
     * @param block    the block to add to
     * @param node     node to which the comments should be added
     * @param position position at which the comments should be added
     * @param comment  the comment to add
     */
    public static void add(@NotNull Block<?> block, @NotNull NodeType node, @NotNull Position position, @NotNull CommentLine comment) {
        switch (position) {
            case BEFORE:
                if (node == NodeType.KEY) {
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
                if (node == NodeType.KEY) {
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
                if (node == NodeType.KEY) {
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