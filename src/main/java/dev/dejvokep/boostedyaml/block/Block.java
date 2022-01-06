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

import dev.dejvokep.boostedyaml.block.implementation.TerminatedBlock;
import dev.dejvokep.boostedyaml.block.implementation.Section;
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
public abstract class Block<T> {

    //Comments
    List<CommentLine> beforeKeyComments = new ArrayList<>(0), inlineKeyComments = null, afterKeyComments = null, beforeValueComments = null, inlineValueComments = null, afterValueComments = null;
    //Value
    private final T value;
    //Keep (updater)
    private boolean keep = false;

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
    protected void init(@Nullable Node key, @Nullable Node value) {
        //If not null
        if (key != null) {
            // Set
            beforeKeyComments = key.getBlockComments() == null ? new ArrayList<>(0) : key.getBlockComments();
            // Manage comments
            if (key.getInLineComments() != null)
                beforeKeyComments.addAll(toBlockComments(key.getInLineComments()));
            if (key.getEndComments() != null)
                beforeKeyComments.addAll(toBlockComments(key.getEndComments()));
            // Collect
            collectComments(key, true);
        }

        //If not null
        if (value != null) {
            // Set
            beforeValueComments = value.getBlockComments();
            // Verify
            if (beforeKeyComments == null)
                beforeKeyComments = new ArrayList<>(0);
            // Manage comments
            if (value.getInLineComments() != null)
                beforeKeyComments.addAll(toBlockComments(value.getInLineComments()));
            if (value.getEndComments() != null)
                beforeKeyComments.addAll(toBlockComments(value.getEndComments()));
            // Collect
            collectComments(value, true);
        }
    }

    /**
     * Collects all comments from this (only if not the initial node) and all sub-nodes; assigns them to {@link
     * Comments.NodeType#KEY} at {@link Comments.Position#BEFORE}.
     *
     * @param node    the node to collect from
     * @param initial if this node is the initial one in the recursive call stack
     */
    private void collectComments(@NotNull Node node, boolean initial) {
        // Add
        if (!initial) {
            if (node.getBlockComments() != null)
                beforeKeyComments.addAll(toBlockComments(node.getBlockComments()));
            if (node.getInLineComments() != null)
                beforeKeyComments.addAll(toBlockComments(node.getInLineComments()));
            if (node.getEndComments() != null)
                beforeKeyComments.addAll(toBlockComments(node.getEndComments()));
        } else {
            // Ensure not null
            if (beforeKeyComments == null)
                beforeKeyComments = new ArrayList<>(0);
        }

        // If is a sequence node
        if (node instanceof SequenceNode) {
            // The node
            SequenceNode sequenceNode = (SequenceNode) node;
            // Iterate
            for (Node sub : sequenceNode.getValue())
                // Collect
                collectComments(sub, false);
        } else if (!initial && node instanceof MappingNode) {
            // The node
            MappingNode mappingNode = (MappingNode) node;
            // Iterate
            for (NodeTuple sub : mappingNode.getValue()) {
                // Collect
                collectComments(sub.getKeyNode(), false);
                collectComments(sub.getValueNode(), false);
            }
        }
    }

    private List<CommentLine> toBlockComments(@NotNull List<CommentLine> commentLines) {
        int i = -1;
        for (CommentLine commentLine : commentLines)
            commentLines.set(++i, commentLine.getCommentType() != CommentType.IN_LINE ? commentLine : new CommentLine(commentLine.getStartMark(), commentLine.getEndMark(), commentLine.getValue(), CommentType.BLOCK));
        return commentLines;
    }

    /**
     * Returns comments at the given position.
     * <p>
     * Expect <code>null</code> or an empty {@link List}.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @return the comments
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
     * Sets the given comments at the given node.
     * <p>
     * To remove comments, use {@link #remove()} instead. Alternatively, pass either
     * <code>null</code> or an empty {@link List} as the parameter.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param comments the comments to set
     */
    public void setComments(@Nullable List<String> comments) {
        Comments.set(this, Comments.NodeType.KEY, Comments.Position.BEFORE, comments == null ? null : comments.stream().map(comment -> Comments.create(comment, Comments.Position.BEFORE)).collect(Collectors.toList()));
    }

    /**
     * Removes all comments at the given node.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     */
    public void remove() {
        Comments.remove(this, Comments.NodeType.KEY, Comments.Position.BEFORE);
    }

    /**
     * Adds the given comments to <i>already existing</i> comments at the given node.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param comments the comments to add
     */
    public void addComments(@NotNull List<String> comments) {
        Comments.add(this, Comments.NodeType.KEY, Comments.Position.BEFORE, comments.stream().map(comment -> Comments.create(comment, Comments.Position.BEFORE)).collect(Collectors.toList()));
    }

    /**
     * Adds the given comment to <i>already existing</i> comments at the given node.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param comment the comment to add
     */
    public void addComment(@NotNull String comment) {
        Comments.add(this, Comments.NodeType.KEY, Comments.Position.BEFORE, Comments.create(comment, Comments.Position.BEFORE));
    }

    /**
     * Returns if the value is a {@link Section section}.
     *
     * @return if the value is a {@link Section section}
     */
    public abstract boolean isSection();

    /**
     * Sets whether to keep this block. Used only internally during updating.
     *
     * @param keep if to keep
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    /**
     * Returns whether to keep this block. Used only internally during updating.
     *
     * @return if to keep this block
     */
    public boolean isKeep() {
        return keep;
    }

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