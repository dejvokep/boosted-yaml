package com.davidcubesvk.yamlUpdater.core.versioning;

import java.util.Arrays;

/**
 * Represents file version IDs, while providing methods to shift to the next ID, or to compare two.
 */
public class Version implements Comparable<Version> {

    //Pattern
    private final Pattern pattern;
    //Cursor indexes
    private final int[] cursors;
    //Version string
    private String id;

    /**
     * Initializes the version object with the given version ID, pattern part cursors, both following the given
     * pattern.
     *
     * @param id      the version string
     * @param pattern the pattern
     * @param cursors the cursor indexes
     */
    Version(String id, Pattern pattern, int[] cursors) {
        this.id = id;
        this.pattern = pattern;
        this.cursors = cursors;
        //If null
        if (id == null)
            buildID();
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
     * Moves to the next version ID (as per the specified pattern).
     * <p>
     * More formally, shifts the cursor index of the least significant (on the right) version part. If it is the last
     * element in the part's sequence, shifts the cursor of 2nd least significant part (just next to it to the left),
     * etc. Updates the version ID string stored.
     * <p>
     * For example, <code>1.2</code> > <code>1.3</code> (depending on the pattern configuration, just for illustration).
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
    }

    private void buildID() {
        //The builder
        StringBuilder builder = new StringBuilder();
        //Go through all indexes
        for (int index = 0; index < cursors.length; index++)
            //Append
            builder.append(pattern.getPart(index).getElement(cursors[index]));
        //Set
        id = builder.toString();
    }

    /**
     * Returns the version as an ID - according to the current part cursors.
     *
     * @return the version as an ID
     */
    public String asID() {
        return id;
    }

    /**
     * Creates a copy of this version object. The new object does not refer to this one in anything except the pattern,
     * which is common for both of them. More formally, copies cursor indexes only.
     *
     * @return the new, copied version object
     */
    public Version copy() {
        return new Version(id, pattern, Arrays.copyOf(cursors, cursors.length));
    }

    /**
     * Returns the underlying pattern.
     *
     * @return the underlying pattern
     */
    public Pattern getPattern() {
        return pattern;
    }
}