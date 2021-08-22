package com.davidcubesvk.yamlUpdater.core.block;

public class MappingBlock extends DocumentBlock {

    /**
     * Initializes the block using the given comments, key and value. Whether this block is a section or mapping is
     * determined by the <code>section</code> parameter.
     *
     * @param comments the comments, or an empty string if none
     * @param key      the key object
     * @param value    the value
     * @param size     the amount of lines occupied by the block
     * @param section  whether this block is a section or mapping, see {@link Type}
     */
    public MappingBlock(String comments, Key key, StringBuilder value, int size) {
        super(comments, key, value, size, false);
    }

}