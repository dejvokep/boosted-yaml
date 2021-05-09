package com.davidcubesvk.yamlUpdater.core.block;

/**
 * Covers mapping's value component. This object is usually returned from value-reading method, which calculates
 * and parses everything value-related, including searching for the nearest key to determine whether the block
 * corresponding to the value is section or not. That's why this class also holds information about that.
 */
public class Value {

    //If the block is a section
    private boolean section;
    //The actual value
    private StringBuilder value;

    /**
     * Initializes the value object by the given value builder. When calling this constructor, the block corresponding
     * to the value is automatically determined not to be a section.
     *
     * @param value the value
     * @see #Value(StringBuilder, boolean) the more detailed constructor
     */
    public Value(StringBuilder value) {
        this(value, false);
    }

    /**
     * Initializes the value object by the given value builder and section indicator.
     *
     * @param value   the value
     * @param section whether the block corresponding to the value is a section
     */
    public Value(StringBuilder value, boolean section) {
        this.value = value;
        this.section = section;
    }

    /**
     * Returns the actual value.
     *
     * @return the actual value
     */
    public StringBuilder getValue() {
        return value;
    }

    /**
     * Returns whether the block corresponding to this value object is a section or not.
     *
     * @return if the corresponding block is a section
     */
    public boolean isSection() {
        return section;
    }

}