package com.davidcubesvk.yamlUpdater.core.version;

import java.util.Arrays;

public class Version implements Comparable<Version> {

    //Pattern
    private Pattern pattern;
    //Cursor indexes
    private int[] cursors;
    //Version string
    private String version;

    /**
     * Initializes the version object with the given version string, pattern part cursors, both following the given
     * pattern.
     *
     * @param version the version string
     * @param pattern the pattern
     * @param cursors the cursor indexes
     */
    Version(String version, Pattern pattern, int[] cursors) {
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

    /**
     * Moves to the next version (as per the specified pattern). More formally, shifts the cursor index of the least
     * significant (on the right) version part. If it is the last element in the part's sequence, shifts the cursor of
     * 2nd least significant part (just next to it to the left), etc. Updates the version string.<br>
     * For example, <code>1.2</code> > <code>1.3</code>.
     */
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
            break;
        }


        //The builder
        StringBuilder builder = new StringBuilder();
        //Go through all indexes
        for (int index = 0; index < cursors.length; index++)
            //Append
            builder.append(pattern.getPart(index).getElement(cursors[index]));
        //Set
        version = builder.toString();
    }

    /**
     * Returns the version as string - according to the current part cursors.
     *
     * @return the version as string
     */
    public String asString() {
        return version;
    }

    /**
     * Creates a copy of this version object. The new object does not refer to this one in anything except the pattern,
     * which is common for both of them. More formally, copies cursor indexes only.
     *
     * @return the new, copied version object
     */
    public Version copy() {
        return new Version(version, pattern, Arrays.copyOf(cursors, cursors.length));
    }

}