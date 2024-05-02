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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.block.implementation.TerminatedBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents one YAML block, while storing its value and comments.
 *
 * @param <T> type of the value stored
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class Block<T> {

    //Comments
    @Nullable
    List<CommentLine> beforeKeyComments = null, inlineKeyComments = null, afterKeyComments = null, beforeValueComments = null, inlineValueComments = null, afterValueComments = null;
    //Value
    private T value;
    //If to ignore
    private boolean ignored;

    /**
     * Creates a block using the given parameters; while storing references to comments from the given nodes.
     *
     * @param keyNode   node which represents the key to the block
     * @param valueNode node which represents the value
     * @param value     the value to store
     */
    public Block(@Nullable Node keyNode, @Nullable Node valueNode, @Nullable T value) {
        this.value = value;
        init(keyNode, valueNode);
    }

    /**
     * Creates a block with the given value, but no comments.
     * <p>
     * <b>This constructor is only used by extending classes, where the comments (respective nodes) are unknown at the
     * time of initialization. In such a scenario, it is needed to call {@link #init(Node, Node)} afterwards.</b>
     *
     * @param value the value to store
     */
    public Block(@Nullable T value) {
        this(null, null, value);
    }

    /**
     * Creates a block with the same comments as the provided previous block, with the given value. If given block is
     * <code>null</code>, creates a block with no comments.
     *
     * @param previous the previous block to reference comments from
     * @param value    the value to store
     */
    public Block(@Nullable Block<?> previous, @Nullable T value) {
        //Set
        this.value = value;
        //If null
        if (previous == null)
            return;

        //Set
        this.beforeKeyComments = previous.beforeKeyComments;
        this.inlineKeyComments = previous.inlineKeyComments;
        this.afterKeyComments = previous.afterKeyComments;
        this.beforeValueComments = previous.beforeValueComments;
        this.inlineValueComments = previous.inlineValueComments;
        this.afterValueComments = previous.afterValueComments;
    }

    /**
     * Stores comments from the given nodes. Only method which is able to mutate an instance of this class.
     * <p>
     * This method can also be referred to as the <i>secondary</i> constructor.
     *
     * @param key   node which represents the key to the block
     * @param value node which represents the value
     */
    @SuppressWarnings("ConstantConditions")
    protected void init(@Nullable Node key, @Nullable Node value) {
        //If not null
        if (key != null) {
            // Set
            beforeKeyComments = key.getBlockComments() == null ? new ArrayList<>(0) : key.getBlockComments();
            inlineKeyComments = key.getInLineComments();
            afterKeyComments = key.getEndComments();
            // Collect
            collectComments(key, beforeKeyComments, true);
        }

        //If not null
        if (value != null) {
            // Set
            beforeValueComments = value.getBlockComments() == null ? new ArrayList<>(0) : value.getBlockComments();
            inlineValueComments = value.getInLineComments();
            afterValueComments = value.getEndComments();
            // Collect
            collectComments(value, beforeValueComments, true);
        }
    }

    /**
     * Collects all comments from this (only if not the initial node) and all sub-nodes and assigns them to the provided
     * destination list. Inline comments are automatically converted to block comments.
     *
     * @param node        the node to collect from
     * @param destination the destination list
     * @param initial     if this node is the initial one in the recursive call stack
     */
    private void collectComments(@NotNull Node node, @NotNull List<CommentLine> destination, boolean initial) {
        // Add
        if (!initial) {
            if (node.getBlockComments() != null)
                destination.addAll(toBlockComments(node.getBlockComments()));
            if (node.getInLineComments() != null)
                destination.addAll(toBlockComments(node.getInLineComments()));
            if (node.getEndComments() != null)
                destination.addAll(toBlockComments(node.getEndComments()));
        }

        // If is a sequence node
        if (node instanceof SequenceNode) {
            // The node
            SequenceNode sequenceNode = (SequenceNode) node;
            // Iterate
            for (Node sub : sequenceNode.getValue())
                // Collect
                collectComments(sub, destination, false);
        } else if (!initial && node instanceof MappingNode) {
            // The node
            MappingNode mappingNode = (MappingNode) node;
            // Iterate
            for (NodeTuple sub : mappingNode.getValue()) {
                // Collect
                collectComments(sub.getKeyNode(), destination, false);
                collectComments(sub.getValueNode(), destination, false);
            }
        }
    }

    /**
     * Converts all comments of type {@link CommentType#IN_LINE} within this list to their {@link CommentType#BLOCK}
     * equivalent.
     * <p>
     * This method mutates the given list and returns it.
     *
     * @param commentLines lines to convert
     * @return the given list
     */
    private List<CommentLine> toBlockComments(@NotNull List<CommentLine> commentLines) {
        // Index
        int i = -1;
        // Convert
        for (CommentLine commentLine : commentLines)
            commentLines.set(++i, commentLine.getCommentType() != CommentType.IN_LINE ? commentLine : new CommentLine(commentLine.getStartMark(), commentLine.getEndMark(), commentLine.getValue(), CommentType.BLOCK));
        // Return
        return commentLines;
    }

    /**
     * Sets a new value.
     *
     * @param value the new value
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Returns comments (at {@link Comments.NodeType#KEY} node at {@link Comments.Position#BEFORE} position).
     * <p>
     * This method will return <code>null</code> or an empty {@link List}, indicating there are no comments.
     *
     * @return the comments
     * @see Comments#get(Block, Comments.NodeType, Comments.Position)
     */
    @Nullable
    public List<String> getComments() {
        // Comments
        List<CommentLine> comments = Comments.get(this, Comments.NodeType.KEY, Comments.Position.BEFORE);
        // If null
        if (comments == null)
            return null;

        // Map
        return comments.stream().map(CommentLine::getValue).collect(Collectors.toList());
    }

    /**
     * Sets the given comments (at {@link Comments.NodeType#KEY} node at {@link Comments.Position#BEFORE} position).
     * <p>
     * To remove comments, use {@link #removeComments()} instead. Alternatively, pass either
     * <code>null</code> or an empty {@link List} as the parameter.
     *
     * @param comments the comments to set
     * @see Comments#set(Block, Comments.NodeType, Comments.Position, List)
     */
    public void setComments(@Nullable List<String> comments) {
        Comments.set(this, Comments.NodeType.KEY, Comments.Position.BEFORE, comments == null ? null : comments.stream().map(comment -> Comments.create(comment, Comments.Position.BEFORE)).collect(Collectors.toList()));
    }

    /**
     * Removes all comments (at {@link Comments.NodeType#KEY} node at {@link Comments.Position#BEFORE} position).
     *
     * @see Comments#remove(Block, Comments.NodeType, Comments.Position)
     */
    public void removeComments() {
        Comments.remove(this, Comments.NodeType.KEY, Comments.Position.BEFORE);
    }

    /**
     * Adds the given comments to <i>already existing</i> comments (at {@link Comments.NodeType#KEY} node at
     * {@link Comments.Position#BEFORE} position).
     *
     * @param comments the comments to add
     * @see Comments#add(Block, Comments.NodeType, Comments.Position, List)
     */
    public void addComments(@NotNull List<String> comments) {
        Comments.add(this, Comments.NodeType.KEY, Comments.Position.BEFORE, comments.stream().map(comment -> Comments.create(comment, Comments.Position.BEFORE)).collect(Collectors.toList()));
    }

    /**
     * Adds the given comment to <i>already existing</i> comments (at {@link Comments.NodeType#KEY} node at
     * {@link Comments.Position#BEFORE} position).
     *
     * @param comment the comment to add
     * @see Comments#add(Block, Comments.NodeType, Comments.Position, CommentLine)
     */
    public void addComment(@NotNull String comment) {
        Comments.add(this, Comments.NodeType.KEY, Comments.Position.BEFORE, Comments.create(comment, Comments.Position.BEFORE));
    }

    /**
     * Sets if to ignore this block. Used only internally while updating.
     *
     * @param ignored if to ignore this block
     */
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    /**
     * Returns if this block is ignored. Used only internally while updating.
     *
     * @return if this block is ignored
     */
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * Returns if this block represents a {@link Section section}.
     *
     * @return if this block represents a {@link Section section}
     */
    public abstract boolean isSection();

    /**
     * Returns the stored value.
     * <p>
     * For {@link Section sections}, this is a {@link java.util.Map}; for {@link TerminatedBlock terminated blocks} an
     * {@link Object}.
     *
     * @return the stored value
     */
    public T getStoredValue() {
        return value;
    }
}