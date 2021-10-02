package com.davidcubesvk.yamlUpdater.core.block;

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.List;

public abstract class Block<T> {

    private List<CommentLine> keyBlockComments, keyInlineComments, keyEndComments, valueBlockComments, valueInlineComments, valueEndComments;
    private final T value;

    public Block(Node keyNode, Node valueNode, T value) {
        this.value = value;
        init(keyNode, valueNode);
    }

    /**
     * Call #init afterwards!
     * @param value
     */
    public Block(T value) {
        this(null, null, value);
    }
    public Block(Block<?> previous, T value) {
        //Set
        this.value = value;
        //If null
        if (previous == null)
            return;

        if (value instanceof Mapping) {
            System.out.println("MAPPING AS VALUE! " + previous + " " + value);
            throw new IllegalArgumentException("Mapping as value!");
        }

        //Set
        this.keyBlockComments = previous.keyBlockComments;
        this.keyInlineComments = previous.keyInlineComments;
        this.keyEndComments = previous.keyEndComments;
        this.valueBlockComments = previous.valueBlockComments;
        this.valueInlineComments = previous.valueInlineComments;
        this.valueEndComments = previous.valueEndComments;
    }

    protected void init(Node key, Node value) {
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

    public void resetComments() {}

    public List<CommentLine> getKeyBlockComments() {
        return keyBlockComments;
    }

    public List<CommentLine> getKeyInlineComments() {
        return keyInlineComments;
    }

    public List<CommentLine> getKeyEndComments() {
        return keyEndComments;
    }

    public List<CommentLine> getValueBlockComments() {
        return valueBlockComments;
    }

    public List<CommentLine> getValueInlineComments() {
        return valueInlineComments;
    }

    public List<CommentLine> getValueEndComments() {
        return valueEndComments;
    }

    public T getValue() {
        return value;
    }
}