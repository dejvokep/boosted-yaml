package com.davidcubesvk.yamlUpdater.core.settings;

public class LoaderSettings {

    private static final char DEFAULT_SEPARATOR = '.';
    /**
     * The string form of the default separator.
     */
    public static final String DEFAULT_STRING_SEPARATOR = String.valueOf(DEFAULT_SEPARATOR);
    /**
     * The escaped form (for regex compatibility at all time) of the default separator.
     */
    public static final String DEFAULT_ESCAPED_SEPARATOR = java.util.regex.Pattern.quote(DEFAULT_STRING_SEPARATOR);
    private static final int DEFAULT_INDENT_SPACES = 2;

    private char separator;
    //The string and quotes separator
    private String stringSeparator = DEFAULT_STRING_SEPARATOR, escapedSeparator = DEFAULT_ESCAPED_SEPARATOR;
    private int indentSpaces;

    public LoaderSettings() {
        this(DEFAULT_SEPARATOR, DEFAULT_INDENT_SPACES);
    }

    public LoaderSettings(char separator, int indentSpaces) {
        setSeparator(separator);
        setIndentSpaces(indentSpaces);
    }

    public void setSeparator(char separator) {
        this.separator = separator;
        this.stringSeparator = String.valueOf(separator);
        this.escapedSeparator = java.util.regex.Pattern.quote(stringSeparator);
    }

    public void setIndentSpaces(int indentSpaces) {
        this.indentSpaces = indentSpaces;
    }

    public char getSeparator() {
        return separator;
    }

    public String getStringSeparator() {
        return stringSeparator;
    }

    public String getEscapedSeparator() {
        return escapedSeparator;
    }

    public int getIndentSpaces() {
        return indentSpaces;
    }
}