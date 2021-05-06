package com.davidcubesvk.yamlUpdater.block;

import java.util.Map;

public class Section extends Block {
    private final Map<String, Object> mappings;

    public Section(Block block, Map<String, Object> mappings) {
        this(block.getComments(), new Key(block.getKey(), block.getFormattedKey(), block.getIndents()), block.getValue(), mappings, block.getSize());
    }
    public Section(String comments, Key key, StringBuilder value, Map<String, Object> mappings, int size) {
        super(comments, key, value, size, true);
        this.mappings = mappings;
    }

    public Map<String, Object> getMappings() {
        return mappings;
    }
}