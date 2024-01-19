/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.dvs.segment;

import dev.dejvokep.boostedyaml.dvs.Pattern;

/**
 * Represents a segment in definition of a {@link Pattern}.
 * <p>
 * Each segment consists of an ordered sequence of elements, either abstract sequence (range), or sequence of literals.
 */
public interface Segment {

    /**
     * Creates a segment from the given range properties.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries and difference
     * between each integer called <code>step</code>. To get <i>i</i>-th element of the range, we can use this formula:
     * <p>
     * <code>start + step*i</code>
     * <p>
     * Filling defines how long (in digits) must each integer in the range be. If an integer in the range has fewer
     * digits than defined by the parameter, additional zeros (<code>0</code>) are prepended to fulfill the requirement
     * (<code>5</code> -&gt; <code>005</code> for <code>fill=3</code>). Set to non-positive (<code>&lt;= 0</code>) value to
     * disable.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>step</code> must not be equal to <code>0</code>,</li>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li><code>step</code> must be specified accordingly to the boundaries, if <code>start &lt; end</code> it must be positive (and vice-versa),</li>
     *     <li>all elements in the range must be <code>&gt;= 0</code>, which implies the same for <code>start</code>, but not necessarily for <code>end</code> (which may be negative),</li>
     *     <li>if <code>fill &gt; 0</code>, each integer in the range must have at most <code>fill</code> digits (e.g. no 3 digit integer for <code>fill=2</code>); mathematical notation: <code>0 &lt;=</code> each range integer <code>&lt;= 10<sup>fill</sup> - 1</code>.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the first element in the range
     * @param end   ending boundary (exclusive) of the range
     * @param step  difference between each neighboring elements in the range (value needed to add to get from element
     *              <i>i</i> to <i>i+1</i>)
     * @param fill  filling parameter (or <code>&lt;= 0</code> to disable)
     * @return the created segment
     */
    static Segment range(int start, int end, int step, int fill) {
        return new RangeSegment(start, end, step, fill);
    }

    /**
     * Creates a segment from the given range properties.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries and difference
     * between each integer called <code>step</code>. To get <i>i</i>-th element of the range, we can use this formula:
     * <p>
     * <code>start + step*i</code>
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>step</code> must not be equal to <code>0</code>,</li>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li><code>step</code> must be specified accordingly to the boundaries, if <code>start &lt; end</code> it must be positive (and vice-versa),</li>
     *     <li>all elements in the range must be <code>&gt;= 0</code>, which implies the same for <code>start</code>, but not necessarily for <code>end</code> (which may be negative),</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the first element in the range
     * @param end   ending boundary (exclusive) of the range
     * @param step  difference between each neighboring elements in the range (value needed to add to get from element
     *              <i>i</i> to <i>i+1</i>)
     * @return the created segment
     * @see #range(int, int, int, int)
     */
    static Segment range(int start, int end, int step) {
        return new RangeSegment(start, end, step, 0);
    }

    /**
     * Creates a segment from the given range properties.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries. Difference
     * between each integer in the range will be <code>1</code> if <code>start &lt; end</code>, <code>-1</code>
     * otherwise.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>step</code> must not be equal to <code>0</code>,</li>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li>all elements in the range must be <code>&gt;= 0</code>, which implies the same for <code>start</code>, but not necessarily for <code>end</code> (which may be negative),</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the first element in the range
     * @param end   ending boundary (exclusive) of the range
     * @return the created segment
     * @see #range(int, int, int, int)
     */
    static Segment range(int start, int end) {
        return new RangeSegment(start, end, start < end ? 1 : -1, 0);
    }

    /**
     * Creates a segment from the given elements.
     *
     * @param elements the elements
     * @return the created segment
     */
    static Segment literal(String... elements) {
        return new LiteralSegment(elements);
    }

    /**
     * Parses the given version ID, starting from the provided index and returns the index of element that was matched.
     * <p>
     * For {@link LiteralSegment}, this is the first element to which {@link String#startsWith(String)} returns
     * <code>true</code>. For {@link RangeSegment}, this is the shortest sequence of characters at the start which
     * represent a number within this range.
     * <p>
     * If no element in the segment is matched, returns <code>-1</code>. This might be the case if the ID is malformed,
     * or if the individual segments overlap each other.
     *
     * @param versionId the version ID to parse
     * @param index     starting index from which to parse the ID
     * @return index of the first matched element (which appears at the provided index of the version ID)
     */
    int parse(String versionId, int index);

    /**
     * Returns <i>i</i>-th element in the segment's definition (the first element having index <code>0</code>).
     * <p>
     * It must apply that <code>0 &lt;= i &lt; length()</code>, otherwise, an {@link IndexOutOfBoundsException} will be
     * thrown.
     *
     * @param index the index
     * @return the element at that index
     */
    String getElement(int index);

    /**
     * Returns length (in characters) of the <i>i</i>-th element in the segment's definition (the first element having
     * index <code>0</code>).
     * <p>
     * It must apply that <code>0 &lt;= i &lt; length()</code>, otherwise, an {@link IndexOutOfBoundsException} will be
     * thrown.
     *
     * @param index the index
     * @return the length of the element at the given index
     */
    int getElementLength(int index);

    /**
     * Returns the length of the segment - amount of elements in the segment's definition.
     *
     * @return the length of the segment
     */
    int length();
}
