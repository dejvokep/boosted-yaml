package com.davidcubesvk.yamlUpdater.core.block;

/**
 * Covers raw and formatted key, indents.
 */
public class Key {

    //Raw and formatted key
    private final String raw, formatted;
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
        this(key, key, indents);
    }

    /**
     * Initializes the key with the given key versions and amount of indents.
     *
     * @param raw       the raw version of the key
     * @param formatted the formatted version of the key
     * @param indents   the amount of indents (spaces) before the key
     */
    public Key(String raw, String formatted, int indents) {
        this.raw = raw;
        this.formatted = formatted;
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
     * Returns the formatted key (used as the "real" key, as block key, in file representation maps). It is guaranteed
     * that the formatted key corresponds to the raw key (see {@link #getRaw()}).
     *
     * @return the formatted key
     */
    public String getFormatted() {
        return formatted;
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