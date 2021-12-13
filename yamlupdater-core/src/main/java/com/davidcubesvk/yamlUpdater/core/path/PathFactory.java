package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;

import java.util.regex.Pattern;

/**
 * Factory used to build paths from string paths in bulk.
 * <p>
 * As string paths can also be used to access data, you should <b>never</b> convert string paths using this class,
 * except some situations where it is allowed.
 */
public class PathFactory {

    //The separator
    private final char separator;
    //Escaped separator
    private final String escapedSeparator;

    /**
     * Creates a factory with the separator specified by the given settings.
     */
    public PathFactory(GeneralSettings generalSettings) {
        this.separator = generalSettings.getSeparator();
        this.escapedSeparator = generalSettings.getEscapedSeparator();
    }

    /**
     * Creates a factory with the given separator.
     */
    public PathFactory(char separator) {
        this.separator = separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
    }


    /**
     * Creates a factory with the defaults specified by {@link GeneralSettings#DEFAULT_SEPARATOR} and
     * {@link GeneralSettings#DEFAULT_ESCAPED_SEPARATOR}.
     */
    public PathFactory() {
        this.separator = GeneralSettings.DEFAULT_SEPARATOR;
        this.escapedSeparator = GeneralSettings.DEFAULT_ESCAPED_SEPARATOR;
    }

    /**
     * Constructs a path from the given string path, by splitting it by the factory's separator.
     * <p>
     * As string paths can also be used to access data, you should <b>never</b> convert string paths using this
     * method, except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming path <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path      the string path to split (in format <code>a.b</code> for factory separator <code>'.'</code> to create path <code>[a, b]</code>)
     * @return the immutable path
     */
    public Path create(String path) {
        return Path.fromString(path, this);
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