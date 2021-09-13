package com.davidcubesvk.yamlUpdater.core.utils;

import com.davidcubesvk.yamlUpdater.core.block.Key;
import org.yaml.snakeyaml.Yaml;

/**
 * Constants used to (not only) parse and update a YAML file.
 */
public class Constants {

    /**
     * An empty string.
     */
    public static final String EMPTY_STRING = "";
    /**
     * An empty key used to pass while loading a file (which extends
     * {@link com.davidcubesvk.yamlUpdater.core.block.Section} class requiring a key in constructor).
     */
    public static final Key EMPTY_KEY = new Key("", 0);
    /**
     * An empty string builder.
     */
    public static final StringBuilder EMPTY_STRING_BUILDER = new StringBuilder();
    /**
     * The comment start character sequence.
     */
    public static final String COMMENT_START = " #";
    /**
     * The document start character sequence.
     */
    public static final String DOCUMENT_START = "---";
    /**
     * The document end character sequence.
     */
    public static final String DOCUMENT_END = "...";
    /**
     * Character defining a comment.
     */
    public static final char COMMENT = '#';
    /**
     * Escape character.
     */
    public static final char ESCAPE = '\\';
    /**
     * A space character.
     */
    public static final char SPACE = ' ';
    /**
     * New line character.
     */
    public static final char NEW_LINE = '\n';
    /**
     * YAML tag indicator.
     */
    public static final char TAG_INDICATOR = '!';
    /**
     * YAML directive indicator.
     */
    public static final char DIRECTIVE_INDICATOR = '%';
    /**
     * YAML TAG directive.
     */
    public static final String TAG_DIRECTIVE = "%TAG";
    /**
     * YAML YAML directive.
     */
    public static final String YAML_DIRECTIVE = "%YAML";
    /**
     * YAML mapping separator character sequence.
     */
    public static final String MAPPING_SEPARATOR = ": ";
    /**
     * YAML key end.
     */
    public static final char MAPPING_SEPARATOR_COLON = ':';
    /**
     * YAML list element specification.
     */
    public static final char VALUE_LIST = '-';
    /**
     * Full YAML list element specification.
     */
    public static final String VALUE_LIST_FULL = "- ";
    /**
     * Quote YAML string surrounding.
     */
    public static final char STRING_QUOTE_SURROUNDING = '\"';
    /**
     * Apostrophe YAML string surrounding.
     */
    public static final char STRING_APOSTROPHE_SURROUNDING = '\'';
    /**
     * Square square bracket opening character (YAML arrays).
     */
    public static final char SQUARE_BRACKET_OPENING = '[';
    /**
     * Square square bracket closing character (YAML arrays).
     */
    public static final char SQUARE_BRACKET_CLOSING = ']';
    /**
     * Square curly bracket opening character (YAML keyed branch).
     */
    public static final char CURLY_BRACKET_OPENING = '{';
    /**
     * Square curly bracket closing character (YAML keyed branch).
     */
    public static final char CURLY_BRACKET_CLOSING = '}';
    /**
     * The YAML instance.
     */
    public static final Yaml YAML = new Yaml();

}
