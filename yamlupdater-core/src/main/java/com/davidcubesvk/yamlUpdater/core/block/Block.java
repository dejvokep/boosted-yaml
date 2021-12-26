package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.block.Comments.NodeType;
import com.davidcubesvk.yamlUpdater.core.block.Comments.Position;
import com.davidcubesvk.yamlUpdater.core.block.implementation.Entry;
import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents one YAML block, while storing its value and comments.
 *
 * @param <T> type of the value stored
 */
public class Block<T> {

    //Comments
    List<CommentLine> beforeKeyComments, inlineKeyComments, afterKeyComments, beforeValueComments, inlineValueComments, afterValueComments;
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
    public Block(@Nullable org.snakeyaml.engine.v2.nodes.Node keyNode, @Nullable org.snakeyaml.engine.v2.nodes.Node valueNode, @Nullable T value) {
        this.value = value;
        init(keyNode, valueNode);
    }

    /**
     * Creates a block with the given value, but no comments.
     * <p>
     * <b>This constructor is only used by extending classes, where the comments (respective nodes) are unknown at the
     * time of initialization. In such a scenario, it is needed to call {@link #init(org.snakeyaml.engine.v2.nodes.Node,
     * org.snakeyaml.engine.v2.nodes.Node)} afterwards.</b>
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
    protected void init(@Nullable org.snakeyaml.engine.v2.nodes.Node key, @Nullable org.snakeyaml.engine.v2.nodes.Node value) {
        //If not null
        if (key != null) {
            // Set
            beforeKeyComments = key.getBlockComments();
            inlineKeyComments = key.getInLineComments();
            afterKeyComments = key.getEndComments();
            // Collect
            collectComments(key, true);
        }

        //If not null
        if (value != null) {
            // Set
            beforeValueComments = value.getBlockComments();
            inlineValueComments = value.getInLineComments();
            afterValueComments = value.getEndComments();
            // Collect
            collectComments(value, true);
        }
    }

    /**
     * Collects all comments from this (only if not the initial node) and all sub-nodes; assigns them to {@link
     * NodeType#KEY} at {@link Position#BEFORE}.
     *
     * @param node    the node to collect from
     * @param initial if this node is the initial one in the recursive call stack
     */
    private void collectComments(@NotNull org.snakeyaml.engine.v2.nodes.Node node, boolean initial) {
        // Add
        if (!initial) {
            beforeKeyComments.addAll(node.getBlockComments());
            beforeKeyComments.addAll(node.getInLineComments());
            beforeKeyComments.addAll(node.getEndComments());
        }

        // If is a sequence node
        if (node instanceof SequenceNode) {
            // The node
            SequenceNode sequenceNode = (SequenceNode) node;
            // Iterate
            for (org.snakeyaml.engine.v2.nodes.Node sub : sequenceNode.getValue())
                // Collect
                collectComments(sub, false);
        } else if (node instanceof MappingNode) {
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

    /**
     * Returns comments at the given position.
     * <p>
     * Please expect <code>null</code> or an empty {@link List}.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param node node from which to retrieve comments
     * @return the comments
     */
    @Nullable
    public List<String> getComments(@NotNull NodeType node) {
        // Comments
        List<CommentLine> comments = Comments.get(this, node, Position.BEFORE);
        // If null
        if (comments == null)
            return null;

        // Map
        return comments.stream().map(CommentLine::getValue).collect(Collectors.toList());
    }

    /**
     * Sets the given comments at the given node.
     * <p>
     * To remove comments, use {@link #remove(NodeType)} instead. Alternatively, pass either
     * <code>null</code> or an empty {@link List} as the parameter.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param node     node to attach to
     * @param comments the comments to set
     */
    public void setComments(@NotNull NodeType node, @Nullable List<String> comments) {
        Comments.set(this, node, Position.BEFORE, comments == null ? null : comments.stream().map(comment -> Comments.create(comment, Position.BEFORE)).collect(Collectors.toList()));
    }

    /**
     * Removes all comments at the given node.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param node node to which the comments are attached
     */
    public void remove(@NotNull NodeType node) {
        Comments.remove(this, node, Position.BEFORE);
    }

    /**
     * Adds the given comments to <i>already existing</i> comments at the given node.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param node     node to which the comments should be added
     * @param comments the comments to add
     */
    public void addComments(@NotNull NodeType node, @NotNull List<String> comments) {
        Comments.add(this, node, Position.BEFORE, comments.stream().map(comment -> Comments.create(comment, Position.BEFORE)).collect(Collectors.toList()));
    }

    /**
     * Adds the given comment to <i>already existing</i> comments at the given node.
     * <p>
     * <i>Use methods provided by {@link Comments} for extensive manipulation.</i>
     *
     * @param node    node to which the comments should be added
     * @param comment the comment to add
     */
    public void addComment(@NotNull NodeType node, @NotNull String comment) {
        Comments.add(this, node, Position.BEFORE, Comments.create(comment, Position.BEFORE));
    }

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
     * For {@link Section sections}, this is a {@link java.util.Map}; for {@link Entry entries} an {@link Object}.
     *
     * @return the stored value
     */
    public T getStoredValue() {
        return value;
    }
}