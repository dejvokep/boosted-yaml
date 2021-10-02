package com.davidcubesvk.yamlUpdater.core.block;

import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.List;

public class Mapping extends Block<Object> {

    public Mapping(Node keyNode, Node valueNode, Object value) {
        super(keyNode, valueNode, value);
    }

    public Mapping(Block<?> previous, Object value) {
        super(previous, value);
        System.out.println("Mapping.java constructor 2: " + previous + " val=" + value);
    }
}