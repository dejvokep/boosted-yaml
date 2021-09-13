package com.davidcubesvk.yamlUpdater.core.block;

/**
 * Covers raw and formatted key, indents.
 */
public class Key {

    //Raw key
    private final String raw;
    //Amount of indents of this key
    private final int indents;

    /**
     * Initializes the key with the given key as both raw and formatted versions and with the specified indents.
     *
     * @param key     the key to use as both raw and formatted version
     * @param indents the amount of indents (spaces) before the key
     * @see #Key(String, String, int) the main constructor
     */
    public Key(String key, int indents) {
        this.raw = key;
        this.indents = indents;
    }

    /**
     * Returns the raw key (used only for outputting the file after update's done), representing the key as it was
     * specified in the input file. It is guaranteed that the raw key corresponds to the formatted key (see
     * {@link #getFormatted()}, it may, however, contain some additional control characters (quotes, apostrophes...)
     * and spaces.
     *
     * @return the raw key
     */
    public String getRaw() {
        return raw;
    }

    /**
     * Returns the amount of indents (spaces) before the key, used to determine the position of the block represented by
     * this key in the block hierarchy.
     *
     * @return the amount of indents (spaces) before the key
     */
    public int getIndents() {
        return indents;
    }
}