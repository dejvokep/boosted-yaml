package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.utils.Constants;

/**
 * Represents one configuration block, which may start with comments, continue with a key and value.
 */
public class DocumentBlock extends Block {

    //Comments
    private final String comments;
    //Key
    private String key;
    //The value
    private final StringBuilder value;

    /**
     * Initializes the block using the given comments, key and value. Whether this block is a section or mapping is
     * determined by the <code>section</code> parameter.
     *
     * @param comments the comments, or an empty string if none
     * @param key      the key object
     * @param value    the value
     * @param section  whether this block is a section or mapping, see {@link Type}
     */
    public DocumentBlock(String comments, String key, StringBuilder value, boolean section) {
        super(section ? Type.SECTION : Type.MAPPING, comments);
        this.comments = comments;
        this.key = key;
        this.value = value;
    }

    /**
     * Attaches the given block to this block (e.g. this block will be extended by the given block).
     *
     * @param block   the block to attach
     * @param indents the amount of indents (indent spaces) before the key relative to indents of this block, see
     *                {@link Key#getIndents()} and {@link #getIndents()}
     */
    public void attach(DocumentBlock block, int indents) {
        //Attach comments
        attach(block.comments, indents);
        //Attach key
        attach(block.key, indents);
        //Attach value
        attach(block.value, indents);
    }

    /**
     * Attaches the given sequence to this block (e.g. this block will be extended by the given sequence).
     *
     * @param sequence the sequence to attach
     * @param indents  the amount of indents (indent spaces) before the key relative to indents of this block, see
     *                 {@link Key#getIndents()} and {@link #getIndents()}
     */
    public void attach(CharSequence sequence, int indents) {
        //Go through all characters
        for (int index = 0; index < sequence.length(); index++) {
            //Char
            char c = sequence.charAt(index);
            //Append
            value.append(c);
            //If a newline character
            if (c == Constants.NEW_LINE) {
                //If not at the end
                if (index < sequence.length() - 1) {
                    //Append the indentation prefix
                    for (int count = 0; count < indents; count++)
                        value.append(Constants.SPACE);
                }
            }
        }
    }

    /**
     * Sets the raw key. It is recommended, but not required to call afterwards.
     *
     * @param key the new raw key
     */
    public void setRawKey(String key) {
        this.key = key;
    }

    /**
     * Returns the comments associated with this block, or an empty string.
     *
     * @return the comments associated with this block, or an empty string
     */
    public String getComments() {
        return comments;
    }

    /**
     * Returns the raw key associated with this block, or <code>null</code> if returns
     * <code>true</code>.
     *
     * @return the raw key
     */
    public String getRawKey() {
        return key;
    }

    /**
     * Returns the value associated with this block, or <code>null</code> if returns
     * <code>true</code>.
     *
     * @return the value
     */
    public StringBuilder getValue() {
        return value;
    }

    /**
     * Returns whether this block is only a section, e.g. if it's type is equal to {@link Type#SECTION}.
     *
     * @return whether this block is only a section
     */
    public boolean isSection() {
        return getType() == Type.SECTION;
    }
}
