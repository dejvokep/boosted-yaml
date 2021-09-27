package com.davidcubesvk.yamlUpdater.core.block;

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.List;

public class Block {

    private Node node;

    public Block(Node node) {
        this.node = node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}