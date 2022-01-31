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
package dev.dejvokep.boostedyaml.dvs.segment;

import dev.dejvokep.boostedyaml.dvs.Pattern;

/**
 * Represents a segment (= part) in definition of a {@link Pattern}.
 */
public interface Segment {

    /**
     * Creates a segment from the given range.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries and difference
     * between each element called <code>step</code>. To get <i>i</i>-th element of the range, we can use this formula:
     * <p>
     * <code>start + step*i</code>
     * <p>
     * Filling defines how long (in digits) must each element (= integer) in the range be. If an element in the range
     * has fewer digits than defined by the parameter, additional zeros (<code>0</code>) are appended in front of the
     * number to fulfill the requirement. It must, however, be guaranteed that no number from the range has more digits
     * than defined by the parameter. Set to non-positive (<code>&lt;= 0</code>) integer to disable.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>step</code> must not be equal to <code>0</code>,</li>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li><code>step</code> must be specified accordingly to the boundaries, if <code>start &lt; end</code> it must be positive (and vice-versa),</li>
     *     <li>all elements in the range must be <code>&gt;= 0</code>, meaning the same for <code>start</code>, but not for <code>end</code>, which can be negative,</li>
     *     <li>if <code>fill &gt; 0</code>, no element must in it's base representation have more digits than defined by the parameter.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the 1st element in the range
     * @param end   ending boundary (exclusive) of the range
     * @param step  difference between each 2 elements in the range (step needed to make to get from element <i>i</i>
     *              to
     *              <i>i+1</i>)
     * @param fill  filling parameter (or <code>&lt;= 0</code> to disable)
     * @return the created segment
     */
    static Segment range(int start, int end, int step, int fill) {
        return new RangeSegment(start, end, step, fill);
    }

    /**
     * Creates a segment from the given range.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries and difference
     * between each element called <code>step</code>. To get <i>i</i>-th element of the range, we can use this formula:
     * <p>
     * <code>start + step*i</code>
     * <p>
     * Filling is disabled.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>step</code> must not be equal to <code>0</code>,</li>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li><code>step</code> must be specified accordingly to the boundaries, if <code>start &lt; end</code> it must be positive (and vice-versa),</li>
     *     <li>all elements in the range must be <code>&gt;= 0</code>, meaning the same for <code>start</code>, but not for <code>end</code>, which can be negative.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the 1st element in the range
     * @param end   ending boundary (exclusive) of the range
     * @param step  difference between each 2 elements in the range (step needed to make to get from element <i>i</i>
     *              to
     *              <i>i+1</i>)
     * @return the created segment
     * @see #range(int, int, int, int)
     */
    static Segment range(int start, int end, int step) {
        return new RangeSegment(start, end, step);
    }

    /**
     * Creates a segment from the given range.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries. Step is equal
     * to <code>1</code> if <code>start &lt; end</code>, <code>-1</code> otherwise. Filling is disabled.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li>all elements in the range must be <code>&gt;= 0</code>, meaning the same for <code>start</code>, but not for <code>end</code>, which can be negative.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the 1st element in the range
     * @param end   ending boundary (exclusive) of the range
     * @return the created segment
     * @see #range(int, int, int, int)
     */
    static Segment range(int start, int end) {
        return new RangeSegment(start, end);
    }

    /**
     * Creates a segment with the given elements.
     *
     * @param elements the elements
     * @return the created segment
     */
    static Segment literal(String... elements) {
        return new LiteralSegment(elements);
    }

    /**
     * Parses the given version ID from the given index and returns which element (represented by the index) was
     * matched.
     * <p>
     * For {@link LiteralSegment}, this is the first element to which {@link String#startsWith(String)} returns
     * <code>true</code>. For {@link RangeSegment}, this is the shortest sequence of characters at the start which
     * represent a number within this range.
     * <p>
     * If no element in the segment matches the start of the ID, returns <code>-1</code>. This might be the case if the
     * ID is malformed, or if the individual parts overlapping each other.
     *
     * @param versionId the version ID to parse
     * @param index     the index in the ID from which to parse
     * @return index of the first matched element at the start of the version ID
     */
    int parse(String versionId, int index);

    /**
     * Returns i-th element in the segment's definition (1st element being represented by <code>0</code>).
     * <p>
     * It must apply that <code>0 &lt;= i &lt; length()</code>, otherwise, an {@link IndexOutOfBoundsException} will be
     * thrown.
     *
     * @param index the index
     * @return the element at that index
     */
    String getElement(int index);

    /**
     * Returns length (in characters) of the i-th element in the segment's definition (1st element being represented by
     * <code>0</code>).
     * <p>
     * It must apply that <code>0 &lt;= i &lt; length()</code>, otherwise, an {@link IndexOutOfBoundsException} will be
     * thrown.
     *
     * @param index the index
     * @return the length of the element at the given index
     */
    int getElementLength(int index);

    /**
     * The length of the segment - amount of elements in the segment's definition.
     *
     * @return the length of the segment
     */
    int length();
}
