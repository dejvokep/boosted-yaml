package com.davidcubesvk.yamlUpdater.core.block;

import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.List;

/**
 * Represents one YAML block, while storing a value and comments. Unless not extended by another class, the object is
 * immutable.
 *
 * @param <T> type of the value stored
 */
public class Block<T> {

    //Comments
    private List<CommentLine> keyBlockComments, keyInlineComments, keyEndComments, valueBlockComments, valueInlineComments, valueEndComments;
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
     * Creates a block with the given value, but no comments.<br>
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
        this.keyBlockComments = previous.keyBlockComments;
        this.keyInlineComments = previous.keyInlineComments;
        this.keyEndComments = previous.keyEndComments;
        this.valueBlockComments = previous.valueBlockComments;
        this.valueInlineComments = previous.valueInlineComments;
        this.valueEndComments = previous.valueEndComments;
    }

    /**
     * Stores comments from the given nodes. Only method which is able to mutate an instance of this class.<br>
     * This method can also be referred to as <i>secondary</i> constructor.
     *
     * @param key   node which represents the key to the block
     * @param value node which represents the value
     */
    protected void init(@Nullable Node key, @Nullable Node value) {
        //If not null
        if (key != null) {
            keyBlockComments = key.getBlockComments();
            keyInlineComments = key.getInLineComments();
            keyEndComments = key.getEndComments();
        }

        //If not null
        if (value != null) {
            valueBlockComments = value.getBlockComments();
            valueInlineComments = value.getInLineComments();
            valueEndComments = value.getEndComments();
        }
    }

    /**
     * Sets whether to keep this block. Used only internally during updater process.
     *
     * @param keep if to keep
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    /**
     * Returns whether to keep this block. Used only internally during updater process.
     *
     * @return if to keep this block
     */
    public boolean isKeep() {
        return keep;
    }

    /**
     * Returns block comments associated with the key node (given on initialization).
     *
     * @return block comments associated with the key node
     */
    public List<CommentLine> getKeyBlockComments() {
        return keyBlockComments;
    }

    /**
     * Returns inline comments associated with the key node (given on initialization).
     *
     * @return inline comments associated with the key node
     */
    public List<CommentLine> getKeyInlineComments() {
        return keyInlineComments;
    }

    /**
     * Returns end comments associated with the key node (given on initialization).
     *
     * @return end comments associated with the key node
     */
    public List<CommentLine> getKeyEndComments() {
        return keyEndComments;
    }

    /**
     * Returns block comments associated with the value node (given on initialization).
     *
     * @return block comments associated with the value node
     */
    public List<CommentLine> getValueBlockComments() {
        return valueBlockComments;
    }

    /**
     * Returns inline comments associated with the value node (given on initialization).
     *
     * @return inline comments associated with the value node
     */
    public List<CommentLine> getValueInlineComments() {
        return valueInlineComments;
    }

    /**
     * Returns end comments associated with the value node (given on initialization).
     *
     * @return end comments associated with the value node
     */
    public List<CommentLine> getValueEndComments() {
        return valueEndComments;
    }

    /**
     * Returns the stored value.
     *
     * @return the stored value
     */
    public T getValue() {
        return value;
    }
}