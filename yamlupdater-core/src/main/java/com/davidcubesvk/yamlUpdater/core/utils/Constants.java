package com.davidcubesvk.yamlUpdater.core.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String COMMENT_START = " #";
    public static final char COMMENT = '#';
    public static final char ESCAPE = '\\';
    public static final char SPACE = ' ';
    public static final char EMPTY_LINE = '\n', NEW_LINE = EMPTY_LINE;
    public static final char KEY_SEPARATOR = '.';
    public static final String MAPPING_SEPARATOR = ": ";
    public static final char MAPPING_SEPARATOR_COLON = ':';
    public static final String MAPPING_SEPARATOR_LINE_END = ":\n";
    public static final char VALUE_LIST = '-';
    public static final String VALUE_LIST_FULL = "- ";
    public static final char STRING_QUOTE_SURROUNDING = '\"';
    public static final char STRING_APOSTROPHE_SURROUNDING = '\'';
    public static final char SQUARE_BRACKET_OPENING = '[';
    public static final char SQUARE_BRACKET_CLOSING = ']';
    public static final char CURLY_BRACKET_OPENING = '{';
    public static final char CURLY_BRACKET_CLOSING = '}';

    public static final Map<Character, String> STRING_SURROUNDING_REPRESENTATIONS = new HashMap<Character, String>() {{
        put(STRING_QUOTE_SURROUNDING, "" + STRING_QUOTE_SURROUNDING + STRING_QUOTE_SURROUNDING + STRING_QUOTE_SURROUNDING);
        put(STRING_APOSTROPHE_SURROUNDING, get(STRING_QUOTE_SURROUNDING).replace(STRING_QUOTE_SURROUNDING, STRING_APOSTROPHE_SURROUNDING));
    }};

}
