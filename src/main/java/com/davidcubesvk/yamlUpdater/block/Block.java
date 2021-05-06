package com.davidcubesvk.yamlUpdater.block;

import com.davidcubesvk.yamlUpdater.Constants;

public class Block {

    private enum Type {
        COMMENT, SECTION, MAPPING
    }

    private Type type;

    private String comments, key, formattedKey, fullKey = null;
    private StringBuilder value;
    private int size, indents;

    public Block(String comments, int size) {
        this.comments = comments;
        this.size = size;
        this.type = Type.COMMENT;
    }
    public Block(String comments, Key key, StringBuilder value, int size, boolean section) {
        this.comments = comments;
        this.key = key.getRaw();
        this.formattedKey = key.getFormatted();
        this.value = value;
        this.size = size;
        this.indents = key.getIndents();
        this.type = section ? Type.SECTION : Type.MAPPING;
    }

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
                if (index < sequence.length()-1) {
                    //Append the indentation prefix
                    for (int count = 0; count < indents; count++)
                        value.append(Constants.SPACE);
                }
            }
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getComments() {
        return comments;
    }

    public String getKey() {
        return key;
    }

    public String getFormattedKey() {
        return formattedKey;
    }

    public String getFullKey() {
        return fullKey;
    }

    public StringBuilder getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }

    public int getIndents() {
        return indents;
    }

    public void setFullKey(String fullKey) {
        this.fullKey = fullKey;
    }

    public boolean isComment() {
        return type == Type.COMMENT;
    }

    public boolean isSection() {
        return type == Type.SECTION;
    }
}
