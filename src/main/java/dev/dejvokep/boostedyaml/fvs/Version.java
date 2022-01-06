/*
 * Copyright 2021 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.fvs;

import dev.dejvokep.boostedyaml.fvs.segment.Segment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * In-code representation of version IDs; provides a bunch of useful methods.
 */
public class Version implements Comparable<Version> {

    //Pattern
    private final Pattern pattern;
    //Cursor indexes
    private final int[] cursors;
    //Version string
    private String id;

    /**
     * Initializes the version object with the given ID representation (if known), pattern, and cursors parsed.
     *
     * @param id      the version ID, or <code>null</code> if unknown
     * @param pattern the pattern
     * @param cursors the cursors
     */
    Version(@Nullable String id, @NotNull Pattern pattern, int[] cursors) {
        this.id = id;
        this.pattern = pattern;
        this.cursors = cursors;
        //If null
        if (id == null)
            buildID();
    }

    @Override
    public int compareTo(Version o) {
        //If patterns are not equal
        if (!pattern.equals(o.pattern))
            throw new ClassCastException("Compared versions are not defined by the same pattern!");

        //Go through all indexes
        for (int index = 0; index < cursors.length; index++) {
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
     * Returns the cursor corresponding to pattern's segment at the given index.
     * <p>
     * The given index must be <code>&gt;= 0</code>, simultaneously less than the amount of segments defining the {@link
     * #getPattern() pattern}.
     * <p>
     * The returned cursor is guaranteed to be <code>&gt;= 0</code> and less than {@link Segment#length()}.
     *
     * @param index the index of the cursor to return
     * @return the cursor
     */
    public int getCursor(int index) {
        return cursors[index];
    }

    /**
     * Moves to the next version (accordingly to the {@link #getPattern() pattern}).
     * <p>
     * More formally, shifts the cursor of the least significant segment (on the right). If it is the last element in
     * the segment's definition, shifts the cursor of 2nd least significant part (just to the left), etc.
     * <p>
     * For example, <code>1.2</code> &gt; <code>1.3</code> (depending on the pattern configuration, just for
     * illustration).
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

        //Build ID
        buildID();
    }

    /**
     * Builds the ID for this version.
     */
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
     * Returns the version as an ID - according to the current cursors.
     *
     * @return the version as an ID
     */
    public String asID() {
        return id;
    }

    /**
     * Creates a copy of this version object. The new object does not refer to this one in anything except the pattern,
     * which is shared between them.
     * <p>
     * More formally, only makes a shallow copy of the cursor indexes.
     *
     * @return the new, copied version object
     */
    public Version copy() {
        return new Version(id, pattern, Arrays.copyOf(cursors, cursors.length));
    }

    /**
     * Returns the pattern using which this version was parsed.
     *
     * @return the pattern using which this version was parsed
     */
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return pattern.equals(version.pattern) && Arrays.equals(cursors, version.cursors);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(pattern);
        result = 31 * result + Arrays.hashCode(cursors);
        return result;
    }
}