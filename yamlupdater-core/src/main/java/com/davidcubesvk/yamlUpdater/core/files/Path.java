package com.davidcubesvk.yamlUpdater.core.files;

import java.util.Arrays;

public class Path {

    private Object[] path;

    public Path(Object... path) {
        this.path = path;
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