package com.davidcubesvk.yamlUpdater.core.block;

public class DirectiveBlock extends Block {

    //If it is a tag directive
    private final boolean tag;
    //The ID and raw variant of the directive
    private final String id, raw;

    public DirectiveBlock(String comments, String raw, boolean tag, String id) {
        super(Type.DIRECTIVE, comments);
        this.raw = raw;
        this.tag = tag;
        this.id = id;
    }

    /**
     * Returns whether this instance represents a <code>%TAG</code> directive. If <code>false</code>, represents a
     * <code>%YAML</code> directive.
     *
     * @return whether <code>%TAG</code> directive is represented
     */
    public boolean isTag() {
        return tag;
    }

    /**
     * Returns the ID of this directive. If {@link #isTag()} returns <code>true</code>, returns the ID of the
     * shortcut <code>%TAG !ID! ...</code>, otherwise, the version of the YAML is returned.
     *
     * @return the ID of this directive
     */
    public String getId() {
        return id;
    }

    /**
     * The formatted version of this directive, as it was specified in the file.
     *
     * @return the formatted version of this directive
     */
    public String getRaw() {
        return raw;
    }

}