package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;

import java.util.regex.Pattern;

/**
 * Factory used to build paths from string paths in bulk.
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
     * Constructs the path array from the given string path, by splitting it by this factory's separator.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING_BASED}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path the string path to split (in format <code>a.b</code> for separator <code>.</code> for example ->
     *             <code>[a, b]</code>)
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