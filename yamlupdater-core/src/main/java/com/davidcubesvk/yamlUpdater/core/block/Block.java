package com.davidcubesvk.yamlUpdater.core.block;

import org.snakeyaml.engine.v2.nodes.Node;

public class Block<T> {

    private Node node;
    private T value;

    public Block(Node keyNode, Node valueNode, T value) {
        this.node = node;
        this.value = value;
    }
    public Block(T value) {
        this(null, null, value);
    }
    public Block(Block<?> previous, T value) {}

    protected void init(Node key, Node value) {
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Node getNode() {
        return node;
    }

    public T getValue() {
        return value;
    }
}