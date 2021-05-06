package com.davidcubesvk.yamlUpdater.version;

import java.util.Arrays;

public class Version implements Comparable<Version> {

    private Pattern pattern;
    private int[] cursors;
    private String version;

    public Version(String version, Pattern pattern, int[] cursors) {
        this.version = version;
        this.pattern = pattern;
        this.cursors = cursors;
    }

    @Override
    public int compareTo(Version o) {
        //If parts don't equal
        if (!pattern.equals(o.pattern))
            throw new ClassCastException("Versions to compare do not have the same part pattern.");
        //Go through all indexes
        for (int index = cursors.length - 1; index >= 0; index--) {
            //Compare
            int compared = Integer.compare(cursors[index], o.cursors[index]);
            //If 0
            if (compared == 0)
                continue;
            //Return
            return compared;
        }

        //Same
        return 0;
    }

    public void next() {
        //Go through all indexes
        for (int index = cursors.length - 1; index >= 0; index--) {
            //The cursor
            int cursor = cursors[index];
            //If out of range
            if (cursor + 1 >= pattern.getPart(index).length()) {
                //Reset
                cursors[index] = 0;
                //Continue and update the next cursor
                continue;
            }

            //Increase
            cursors[index] = cursor + 1;
            return;
        }
    }

    public String asString() {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < cursors.length; index++)
            builder.append(pattern.getPart(index).getElement(cursors[index]));
        return builder.toString();
    }

    public Version copy() {
        return new Version(version, pattern, Arrays.copyOf(cursors, cursors.length));
    }

}