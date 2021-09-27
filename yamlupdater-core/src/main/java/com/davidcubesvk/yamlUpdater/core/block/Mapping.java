package com.davidcubesvk.yamlUpdater.core.block;

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.List;

public class Mapping extends Block {

    private Object value;

    public Mapping(Node node, Object value) {
        super(node);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}