package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.Block;

public class ReadBlock {

    private final Block block;
    private final int size, indents;

    public ReadBlock(Block block, int size, int indents) {
        this.block = block;
        this.size = size;
        this.indents = indents;
    }

    public Block getBlock() {
        return block;
    }

    public int getSize() {
        return size;
    }

    public int getIndents() {
        return indents;
    }
}