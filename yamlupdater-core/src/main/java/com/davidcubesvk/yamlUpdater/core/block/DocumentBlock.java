package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.utils.Constants;

/**
 * Represents one configuration block, which may start with comments, continue with a key and value.
 */
public class DocumentBlock extends Block {

    //Comments
    private final String comments;
    //Keys
    private String key, formattedKey;
    //The value
    private final StringBuilder value;
    //The size and amount of indent spaces determined by the amount of them before the key
    private int size, indents;

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
    public DocumentBlock(String comments, Key key, StringBuilder value, int size, boolean section) {
        super(section ? Type.SECTION : Type.MAPPING, comments, size);
        this.comments = comments;
        this.key = key.getRaw();
        this.formattedKey = key.getFormatted();
        this.value = value;
        this.size = size;
        this.indents = key.getIndents();
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
        this.size += block.getSize();
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
                //Increase size
                size++;
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
     * Sets the raw key. It is recommended, but not required to call {@link #setFormattedKey(String)} afterwards.
     *
     * @param key the new raw key
     */
    public void setRawKey(String key) {
        this.key = key;
    }

    /**
     * Sets the formatted key. It is required to call {@link #setRawKey(String)} afterwards, this key must correspond to
     * the raw key returned by {@link #getRawKey()}.
     *
     * @param formattedKey the new formatted key
     */
    public void setFormattedKey(String formattedKey) {
        this.formattedKey = formattedKey;
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
     * Returns the raw key associated with this block, or <code>null</code> if {@link #isComment()} returns
     * <code>true</code>.
     *
     * @return the raw key
     */
    public String getRawKey() {
        return key;
    }

    /**
     * Returns the formatted key associated with this block, or <code>null</code> if {@link #isComment()} returns
     * <code>true</code>.
     *
     * @return the formatted key
     */
    public String getFormattedKey() {
        return formattedKey;
    }

    /**
     * Returns the value associated with this block, or <code>null</code> if {@link #isComment()} returns
     * <code>true</code>.
     *
     * @return the value
     */
    public StringBuilder getValue() {
        return value;
    }

    /**
     * Returns the amount of lines occupied by the block.
     *
     * @return the amount of lines occupied by the block
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the amount of indents (spaces) this block is indented with.
     *
     * @return the amount of indents
     */
    public int getIndents() {
        return indents;
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
