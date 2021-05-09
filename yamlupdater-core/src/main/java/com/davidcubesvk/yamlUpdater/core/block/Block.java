package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.utils.Constants;

/**
 * Represents one configuration block, which may start with comments, continue with a key and value.
 */
public class Block {

    /**
     * Type of the block.
     * <ul>
     *     <li><code>COMMENT</code>: contains comments only, is not an actual configuration block (mapping). Only used
     *     to reference dangling comments (at the end of the file).</li>
     *     <li><code>SECTION</code>: the block does not contain any actual value - is a section and there are some
     *     sub-mappings.</li>
     *     <li><code>MAPPING</code>: a full sized block which contains a key and value (and might contain comments). Not
     *     a section.</li>
     * </ul>
     */
    private enum Type {
        COMMENT, SECTION, MAPPING
    }

    //The type
    private final Type type;

    //Comments
    private final String comments;
    //Keys
    private String key, formattedKey;
    //The value
    private StringBuilder value;
    //The size and amount of indent spaces determined by the amount of them before the key
    private int size, indents;

    /**
     * Initializes the block using only the comments and size of the block. Used only to represent dangling comments at
     * the end of the file (that means, type is {@link Type#COMMENT}).
     *
     * @param comments the comments
     * @param size     the amount of lines needed to skip to get to the end of the block from the first line (e.g. actual
     *                 block size minus 1)
     */
    public Block(String comments, int size) {
        this.comments = comments;
        this.size = size;
        this.type = Type.COMMENT;
    }

    /**
     * Initializes the block using the given comments, key and value. Whether this block is a section or mapping is
     * determined by the <code>section</code> parameter.
     *
     * @param comments the comments, or an empty string if none
     * @param key      the key object
     * @param value    the value
     * @param size     the amount of lines needed to skip to get to the end of the block from the first line (e.g. actual
     *                 block size minus 1)
     * @param section  whether this block is a section or mapping, see {@link Type}
     */
    public Block(String comments, Key key, StringBuilder value, int size, boolean section) {
        this.comments = comments;
        this.key = key.getRaw();
        this.formattedKey = key.getFormatted();
        this.value = value;
        this.size = size;
        this.indents = key.getIndents();
        this.type = section ? Type.SECTION : Type.MAPPING;
    }

    /**
     * Attaches the given block to this block (e.g. this block will be extended by the given block).
     *
     * @param block   the block to attach
     * @param indents the amount of indents (indent spaces) before the key relative to indents of this block, see
     *                {@link Key#getIndents()} and {@link #getIndents()}
     */
    public void attach(Block block, int indents) {
        //If is a comment
        if (block.isComment()) {
            //Attach comments
            attach(block.comments, indents);
            return;
        }

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
     * Returns the line size of this block <code>- 1</code> - actually returns the amount of lines needed to skip to get
     * to the last character of the block.
     *
     * @return the line size of this block <code>- 1</code>
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
     * Returns whether this block is only a comment, e.g. if it's type is equal to {@link Type#COMMENT}.
     *
     * @return whether this block is only a comment
     */
    public boolean isComment() {
        return type == Type.COMMENT;
    }

    /**
     * Returns whether this block is only a section, e.g. if it's type is equal to {@link Type#SECTION}.
     *
     * @return whether this block is only a section
     */
    public boolean isSection() {
        return type == Type.SECTION;
    }
}
