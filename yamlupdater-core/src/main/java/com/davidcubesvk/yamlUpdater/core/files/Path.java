package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.settings.Settings;

import java.util.Arrays;

public class Path {

    private String stringPath;
    private char lastParsed;

    private Object[] path;

    public Path(Object... path) {
        this.path = path;
    }
    public Path(String stringPath) {
        this.stringPath = stringPath;
    }

    public int getLength(Settings settings) {
        return path.length;
    }

    public Object getKey(int i, Settings settings) {
        return path[i];
    }
    public Object[] getPath(Settings settings) {
        return path;
    }
    public Path add(Object element) {
        //New path
        Path path = new Path();
        //Set
        path.path = Arrays.copyOf(this.path, this.path.length + 1);
        path.path[this.path.length] = element;
        //Return
        return path;
    }

    public static Path from(Object[] path) {
        Path newPath = new Path();
        newPath.path = path;
        return newPath;
    }
}