package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;

import java.util.regex.Pattern;

public class PathFactory {

    private final char separator;
    private final String escapedSeparator;

    public PathFactory(GeneralSettings generalSettings) {
        this.separator = generalSettings.getSeparator();
        this.escapedSeparator = generalSettings.getEscapedSeparator();
    }
    public PathFactory(char separator) {
        this.separator = separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
    }
    public PathFactory() {
        this.separator = GeneralSettings.DEFAULT_SEPARATOR;
        this.escapedSeparator = GeneralSettings.DEFAULT_ESCAPED_SEPARATOR;
    }

    public Path create(String path) {
        return new Path((Object[]) path.split(escapedSeparator));
    }
    public Path create(Object... path) {
        return new Path(path);
    }

    public char getSeparator() {
        return separator;
    }

    public String getEscapedSeparator() {
        return escapedSeparator;
    }
}