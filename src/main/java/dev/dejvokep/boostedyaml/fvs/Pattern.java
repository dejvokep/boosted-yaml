package dev.dejvokep.boostedyaml.fvs;

import dev.dejvokep.boostedyaml.fvs.segment.Segment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an immutable pattern using which version IDs can be parsed into {@link Version versions}.
 */
public class Pattern {

    //Segments
    private final Segment[] segments;

    /**
     * Initializes the pattern with the given segments.
     * <p>
     * The given segments should be ordered from the most significant to the least significant (left to right). Please
     * make sure that no segments are overlapping. There are 2 common solutions to that:
     * <ol>
     *     <li><b>RECOMMENDED:</b> Reserve a character (e.g. <code>'.'</code>) and create a static segment with it.
     *     Define the pattern so each segment is differentiated from another with this static segment and make sure no
     *     segment contains the reserved character in any of its elements. This way you can prevent overlapping and
     *     simultaneously keep standard version specification (e.g. <code>1.2</code>). Same applies if you would like to
     *     use character <i>sequence</i> instead of only one character.</li>
     *     <li>Ensure all elements within all segments are of the same length in characters. For range segments, you can
     *     use <i>filling</i>.</li>
     * </ol>
     * Please note that patterns and segments are immutable, and therefore, you can reuse them.
     * <p>
     * <b>For more information</b> please visit the {wiki}.
     *
     * @param segments the segments, ordered from left (most-significant) to right (least-significant)
     */
    public Pattern(@NotNull Segment... segments) {
        this.segments = segments;
    }

    /**
     * Returns segment at the given index (from the most significant one on the left).
     *
     * @param index the index
     * @return the segment at the given index
     */
    @NotNull
    public Segment getPart(int index) {
        return segments[index];
    }

    /**
     * Parses the given version ID. If the ID does not match this pattern, returns <code>null</code>.
     *
     * @param versionId the version ID
     * @return the version
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
     * Builds and returns the first version specified by this pattern.
     * <p>
     * More formally, returns version represented by all cursors set to the lowest index - <code>0</code>.
     *
     * @return the first version
     */
    public Version getFirstVersion() {
        return new Version(null, this, new int[segments.length]);
    }

}