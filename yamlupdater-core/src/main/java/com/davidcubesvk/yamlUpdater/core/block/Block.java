package com.davidcubesvk.yamlUpdater.core.block;

public abstract class Block {

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
    public enum Type {
        COMMENT, SECTION, MAPPING, DIRECTIVE, INDICATOR;
    }

    private Type type;
    private String comments;

    public Block(Type type, String comments) {
        this.type = type;
        this.comments = comments;
    }

    public Type getType() {
        return type;
    }

    public String getComments() {
        return comments;
    }

    void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isHeaderContent() {
        return type == Type.DIRECTIVE;
    }

    public boolean isFooterContent() {
        return type == Type.COMMENT;
    }
}