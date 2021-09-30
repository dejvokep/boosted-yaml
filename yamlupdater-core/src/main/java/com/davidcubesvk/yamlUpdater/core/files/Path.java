package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Path {

    private Object[] path;

    public Path(Object... path) {
        this.path = path;
    }
    public Path(String path) {
        this.path = path.split(GeneralSettings.DEFAULT_ESCAPED_SEPARATOR);
    }
    public Path(String path, char separator) {
        this.path = path.split(Pattern.quote(String.valueOf(separator)));
    }
    public Path(String path, PathFactory pathFactory) {
        this.path = path.split(pathFactory.getEscapedSeparator());
    }

    public int getLength() {
        return path.length;
    }

    public Object getKey(int i) {
        return path[i];
    }
    public Object[] getPath() {
        return path;
    }
    public Path add(Object element) {
        //New path
        Object[] path = Arrays.copyOf(this.path, this.path.length + 1);
        //Set
        path[path.length - 1] = element;
        //Return
        return new Path(path);
    }
}