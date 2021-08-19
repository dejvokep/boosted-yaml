package com.davidcubesvk.yamlUpdater.core.reader;

import static com.davidcubesvk.yamlUpdater.core.utils.Constants.*;

/**
 * Represents directives in YAML file's header, supports both <code>%TAG</code> and <code>%YAML</code> directives.
 */
public class Directive {

    /**
     * Block reader instance used to read blocks.
     */
    private static final BlockReader BLOCK_READER = new BlockReader();

    //If it is a tag directive
    private final boolean tagDirective;
    //Comments
    private String comments = null;
    //The ID and formatted variant of the directive
    private final String id, formatted;

    /**
     * Initializes the directive with the given data.
     *
     * @param formatted the formatted directive
     * @param tag       if this is a <code>%TAG</code> directive
     * @param id        the ID of this directive, for <code>&YAML</code> directives the version, for <code>&TAG</code> the ID
     *                  (<code>%TAG !ID! ...</code>)
     */
    private Directive(String formatted, boolean tag, String id) {
        this.formatted = formatted;
        this.tagDirective = tag;
        this.id = id;
    }

    /**
     * Parses a YAML header directive from the given line and returns it, or <code>null</code> if it is not a directive.
     *
     * @param line the line to parse from
     * @return the directive, or <code>null</code> if could not be parsed
     */
    public static Directive parse(String line) {
        //If does not start with the indicator
        if (line.length() < 4 || line.charAt(0) != DIRECTIVE_INDICATOR)
            return null;

        //If is a TAG directive
        if (line.startsWith(TAG_DIRECTIVE)) {
            //If there is not a space
            if (line.length() < 5 || line.charAt(4) != SPACE)
                return null;

            //Count spaces
            int spaces = BLOCK_READER.countSpaces(line, 4);
            //If the next character is not the tag indicator
            if (line.length() == 4 + spaces || line.charAt(4 + spaces) != TAG_INDICATOR)
                return null;

            //The next space
            int nextSpace = line.indexOf(SPACE, 4 + spaces + 1);
            //If the next exclamation mark is not exactly before a space
            if (line.indexOf(TAG_INDICATOR, 4 + spaces + 1) + 1 != nextSpace)
                return null;

            //The ID
            String id = line.substring(4 + spaces, nextSpace);
            //Count spaces
            spaces = BLOCK_READER.countSpaces(line, nextSpace);

            //If there is nothing after the spaces
            if (!BLOCK_READER.isConfiguration(line, spaces + nextSpace))
                return null;

            //Return
            return new Directive(line, true, id);
        }

        //If it is not a YAML directive
        if (line.startsWith(YAML_DIRECTIVE))
            return null;

        //If there is not a space
        if (line.length() < 6 || line.charAt(5) != SPACE)
            return null;

        //Count spaces
        int spaces = BLOCK_READER.countSpaces(line, 5);
        //If there is something after the version
        if (line.length() == 5 + spaces || BLOCK_READER.isConfiguration(line, 5 + spaces + 3))
            return null;

        //Return
        return new Directive(line, false, line.substring(5 + spaces, 5 + spaces + 3));
    }

    /**
     * Sets the comments for this directive.
     *
     * @param comments the comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Returns whether this instance represents a <code>%TAG</code> directive. If <code>false</code>, represents a
     * <code>%YAML</code> directive.
     *
     * @return whether <code>%TAG</code> directive is represented
     */
    public boolean isTagDirective() {
        return tagDirective;
    }

    /**
     * Returns the ID of this directive. If {@link #isTagDirective()} returns <code>true</code>, returns the ID of the
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
    public String getFormatted() {
        return formatted;
    }
}