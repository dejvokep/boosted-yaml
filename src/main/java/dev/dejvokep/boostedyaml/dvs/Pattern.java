/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.dvs;

import dev.dejvokep.boostedyaml.dvs.segment.Segment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Pattern which defines the format of version IDs.
 * <p>
 * Each pattern is composed of {@link Segment segments}, using whose elements IDs can be {@link #getVersion(String)
 * parsed}. Learn more about version IDs, segments and patterns on the <a href="https://dejvokep.gitbook.io/boostedyaml/">wiki</a>.
 * <p>
 * Patterns are immutable - it is recommended to create individual objects only once and reuse them.
 */
public class Pattern {

    //Segments
    private final Segment[] segments;

    /**
     * Creates a pattern composed of the given segments, ordered from the most significant to the least significant
     * (left to right).
     * <p>
     * To avoid any parsing conflicts, make sure that no neighboring segments are overlapping. Here are the two common
     * solutions to that:
     * <ol>
     *     <li>Reserve a character/sequence (e.g. <code>"."</code>) and create it's own segment:
     *     <code>{@link Segment#literal(String...) Segment.literal(".")}</code>. Make sure the reserved sequence does
     *     not appear anywhere else. Then, separate each changing segment
     *     ({@link Segment#range(int, int, int, int) range} with 2+ numbers, {@link Segment#literal(String...) literal}
     *     segment with 2+ elements) with this segment, for example:<br>
     *     <code>new Pattern({@link Segment#range(int, int) Segment.range}(0, {@link Integer#MAX_VALUE}), {@link Segment#literal(String...) Segment.literal(".")}, {@link Segment#range(int, int) Segment.range}(0, 10))</code><br>
     *     This way you will still be able to maintain great version ID semantics: <code>1.0</code> -> <code>1.1</code>...
     *     <li>Ensure all elements within all segments are of the same length in characters. For range segments, you can
     *     use <i>filling</i>.</li>
     * </ol>
     *
     * @param segments the segments, ordered from left (most-significant) to right (least-significant)
     */
    public Pattern(@NotNull Segment... segments) {
        this.segments = segments;
    }


    /**
     * Returns segment at the given index (index <code>0</code> refers to the most significant one on the left).
     *
     * @param index the index
     * @return segment at the given index
     * @deprecated Method with confusing name and subject for removal, use {@link #getSegment(int)} instead.
     */
    @Deprecated
    @NotNull
    public Segment getPart(int index) {
        return segments[index];
    }

    /**
     * Returns segment at the given index (index <code>0</code> refers to the most significant one on the left).
     *
     * @param index the index
     * @return segment at the given index
     */
    @NotNull
    public Segment getSegment(int index) {
        return segments[index];
    }

    /**
     * Parses the given version ID. If the ID does not match this pattern, returns <code>null</code>.
     *
     * @param versionId the version ID to parse
     * @return the version, or <code>null</code> if cannot parse
     */
    @Nullable
    public Version getVersion(@NotNull String versionId) {
        //The cursors
        int[] cursors = new int[segments.length];
        //Start
        int start = 0;
        //Go through all parts
        for (int index = 0; index < segments.length; index++) {
            //Parse
            int cursor = segments[index].parse(versionId, start);
            //If it is -1
            if (cursor == -1)
                return null;
            //Set the cursor
            cursors[index] = cursor;
            //Add
            start += segments[index].getElementLength(cursor);
        }

        //Initialize
        return new Version(versionId, this, cursors);
    }

    /**
     * Builds and returns the first version specified by this pattern, being the version with all cursors set to
     * <code>0</code>.
     *
     * @return the first version
     */
    public Version getFirstVersion() {
        return new Version(null, this, new int[segments.length]);
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "segments=" + Arrays.toString(segments) +
                '}';
    }
}