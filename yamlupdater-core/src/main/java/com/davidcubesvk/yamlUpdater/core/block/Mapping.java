package com.davidcubesvk.yamlUpdater.core.block;

import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.nodes.Node;

/**
 * Represents one YAML mapping (key=value pair), while storing the mapping's value and comments.
 */
public class Mapping extends Block<Object> {

    /**
     * Creates a mapping using the given parameters; while storing references to comments from the given nodes.
     *
     * @param keyNode   node which represents the mapping key
     * @param valueNode node which represents the mapping value
     * @param value     the value to store
     */
    public Mapping(@Nullable Node keyNode, @Nullable Node valueNode, @Nullable Object value) {
        super(keyNode, valueNode, value);
    }

    /**
     * Creates a mapping with the same comments as the provided previous block, with the given value. If given block is
     * <code>null</code>, creates a mapping with no comments.
     *
     * @param previous the previous block to reference comments from
     * @param value    the value to store
     */
    public Mapping(@Nullable Block<?> previous, @Nullable Object value) {
        super(previous, value);
    }
}