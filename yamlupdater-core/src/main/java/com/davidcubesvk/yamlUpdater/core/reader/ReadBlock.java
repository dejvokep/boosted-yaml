package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.Block;

public class ReadBlock {

    private final Block block;
    private final String key;
    private final int size, indents;

    public ReadBlock(Block block, String key, int size, int indents) {
        this.block = block;
        this.key = key;
        this.size = size;
        this.indents = indents;
    }

    public Block getBlock() {
        return block;
    }

    public String getKey() {
        return key;
    }

    public int getSize() {
        return size;
    }

    public int getIndents() {
        return indents;
    }
}