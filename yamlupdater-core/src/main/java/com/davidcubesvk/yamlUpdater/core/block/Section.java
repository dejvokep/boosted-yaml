package com.davidcubesvk.yamlUpdater.core.block;

import java.util.Map;

/**
 * An extension of the {@link Block} class used to represent a section.
 */
public class Section extends Block {

    //Mappings
    private final Map<String, Block> mappings;

    /**
     * Initializes this section using the given block's data and the given sub-mappings.
     *
     * @param block    the block represented
     * @param mappings the sub-mappings of the section
     * @see #Section(String, Key, StringBuilder, Map, int) the detailed constructor
     */
    public Section(Block block, Map<String, Block> mappings) {
        this(block.getComments(), new Key(block.getRawKey(), block.getFormattedKey(), block.getIndents()), block.getValue(), mappings, block.getSize());
    }

    /**
     * Initializes this section using the given data. Read more about parameters except <code>mappings</code>
     * {@link Block#Block(String, Key, StringBuilder, int, boolean) here} (as this represents a section, the boolean
     * parameter is always set to <code>true</code>).
     *
     * @param comments the comments, or an empty string if not any
     * @param key      the key object
     * @param value    the value (does not include the sub-mappings)
     * @param mappings the sub-mappings of this section
     * @param size     amount of lines needed to skip to get to the last line belonging to this section (actual line size
     *                 <code>- 1</code>), not including the sub-mappings
     */
    public Section(String comments, Key key, StringBuilder value, Map<String, Block> mappings, int size) {
        super(comments, key, value, size, true);
        this.mappings = mappings;
    }

    /**
     * Returns the (sub-)mappings represented by this section. This map contains only the nearest sub-mappings, not all,
     * deeper ones. The mappings are represented by the returned map, where the value is not the actual value, but
     * either an instance of {@link Block} or {@link Section} (depending on what is the type of the value, if it is a
     * section, etc.).
     *
     * @return the mappings represented by the section
     */
    public Map<String, Block> getMappings() {
        return mappings;
    }
}