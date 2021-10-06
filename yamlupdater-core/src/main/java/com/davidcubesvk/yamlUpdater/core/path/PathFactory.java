package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;

import java.util.regex.Pattern;

public class PathFactory {

    //The separator
    private final char separator;
    //Escaped separator
    private final String escapedSeparator;

    /**
     * Creates this factory with the separator specified by the given settings.
     */
    public PathFactory(GeneralSettings generalSettings) {
        this.separator = generalSettings.getSeparator();
        this.escapedSeparator = generalSettings.getEscapedSeparator();
    }

    /**
     * Creates this factory with the given separator.
     */
    public PathFactory(char separator) {
        this.separator = separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
    }


    /**
     * Creates this factory with the defaults specified by {@link GeneralSettings#DEFAULT_SEPARATOR} and
     * {@link GeneralSettings#DEFAULT_ESCAPED_SEPARATOR}.
     */
    public PathFactory() {
        this.separator = GeneralSettings.DEFAULT_SEPARATOR;
        this.escapedSeparator = GeneralSettings.DEFAULT_ESCAPED_SEPARATOR;
    }

    /**
     * Creates and returns a path from the given string path containing individual keys
     * (e.g. <code>a.b</code> for separator <code>.</code>) split using this factory's separator.
     *
     * @param path the string path to create the path from
     * @return the path object
     */
    public Path create(String path) {
        return Path.fromString(path, this);
    }

    public Path create(Object... path) {
        return Path.from(path);
    }

    /**
     * Returns the separator character.
     *
     * @return the separator character
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Returns the escaped version of {@link #getSeparator()}.
     *
     * @return the escaped separator
     */
    public String getEscapedSeparator() {
        return escapedSeparator;
    }
}