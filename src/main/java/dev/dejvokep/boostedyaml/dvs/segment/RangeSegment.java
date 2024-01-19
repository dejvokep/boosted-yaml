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

import java.util.Arrays;

/**
 * Represents an immutable segment constructed from an integer range.
 */
public class RangeSegment implements Segment {

    //Properties
    private final int start, end, step, minStringLength, maxStringLength, fill, length;

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
     */
    public RangeSegment(int start, int end, int step, int fill) {
        this.start = start;
        this.end = end;
        this.step = step;
        this.fill = fill;

        //If step is 0
        if (step == 0)
            throw new IllegalArgumentException("Step cannot be zero!");
        //If invalid step
        if ((start < end && step < 0) || (start > end && step > 0))
            throw new IllegalArgumentException(String.format("Invalid step for the given range! start=%d end=%d step=%d", start, end, step));
        //If an empty range
        if (start == end)
            throw new IllegalArgumentException(String.format("Parameters define an empty range, start=end! start=%d end=%d", start, end));

        //Compute length by dividing the size of the range by the step and add 1 (because start is also part of the range)
        this.length = (int) Math.ceil((double) Math.abs(start - end) / Math.abs(step));
        //Last in the range
        int last = start + step * (length - 1);
        //If negative range
        if (start < 0 || (end < 0 && last < 0))
            throw new IllegalArgumentException(String.format("Range contains negative integers! start=%d end=%d step=%d", start, end, step));
        //If filling and does not apply
        if (fill > 0 && !validateFill(fill, Math.max(start, last)))
            throw new IllegalArgumentException(String.format("Some integer from the range exceeds maximum length defined by the filling parameter! start=%d end=%d last=%d fill=%d", start, end, last, fill));

        //Limits
        this.maxStringLength = fill > 0 ? fill : countDigits(step > 0 ? end : start);
        this.minStringLength = fill > 0 ? fill : countDigits(step > 0 ? start : end);
    }

    /**
     * @deprecated Duplicated internal method subject for removal.
     */
    @Deprecated
    public RangeSegment(int start, int end) {
        this(start, end, start < end ? 1 : -1, 0);
    }

    /**
     * @deprecated Duplicated internal method subject for removal.
     */
    @Deprecated
    public RangeSegment(int start, int end, int step) {
        this(start, end, step, 0);
    }

    /**
     * Validates if the given maximum value in the range fits the maximum allowed by the <code>fill</code> parameter.
     * <p>
     * It must apply that <code>fill > 0</code> and <code>maxValue >= 0</code>.
     *
     * @param fill     the filling parameter used (<code>> 0</code>)
     * @param maxValue max value in the range (<code>>= 0</code>)
     * @return if the value fits the filling parameter
     */
    private boolean validateFill(int fill, int maxValue) {
        //Max value allowed by the filling parameter
        int maxFillValue = 9;
        //Iterate
        for (int i = 0; i < fill; i++) {
            //If fits already
            if (maxFillValue >= maxValue)
                return true;
            //Add 9 at the end (e.g. 9 -&gt; 99)
            maxFillValue *= 10;
            maxFillValue += 9;
        }
        return false;
    }


    @Override
    public int parse(String versionId, int index) {
        //If filling
        if (fill > 0) {
            //If cannot parse
            if (fill > versionId.length() - index)
                return -1;

            try {
                return getRangeIndex(Integer.parseInt(versionId.substring(index, fill)));
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }

        //If no remaining chars
        if (versionId.length() <= index)
            return -1;

        //Value
        int value = 0;
        int digits = 0;
        //Index of the currently processed char
        for (int i = 0; i < maxStringLength; i++) {
            //If out of bounds
            if (i >= versionId.length() - index)
                break;
            //Cannot have more than 1 zero
            if (i == 1 && value == 0 && digits == 1)
                break;

            //Parse digit
            int digit = Character.digit(versionId.charAt(index + i), 10);
            //If invalid
            if (digit == -1)
                break;

            //Shift to the left
            value *= 10;
            //Add
            value += digit;
            digits += 1;
        }

        //No digit
        if (digits == 0)
            return -1;
        //If zero
        if (value == 0)
            return getRangeIndex(0);

        //While greater than 0
        while (value > 0) {
            //If not parsable
            if (digits < minStringLength)
                break;
            //Parse index
            int rangeIndex = getRangeIndex(value);
            //If parsed successfully
            if (rangeIndex != -1)
                return rangeIndex;
            //Divide
            value /= 10;
            //Subtract
            digits -= 1;
        }

        //Could not parse
        return -1;
    }

    /**
     * Returns how many digits the given integer has in its string representation.
     * <p>
     * The given value must be greater than or equal to <code>0</code>.
     *
     * @param value the value to count the digits of (<code>>= 0</code>)
     * @return amount of digits the number has
     */
    private int countDigits(int value) {
        //If zero
        if (value == 0)
            return 1;
        //Digits
        int digits = 0;
        //Iterate while more than 0
        for (; value > 0; digits++)
            value /= 10;
        //Return
        return digits;
    }

    /**
     * Returns the index in the range of the given value. If the value is not contained by this range, returns
     * <code>-1</code>. Otherwise, if it is, method {@link #getElement(int)} with the returned index is guaranteed to
     * return the given value (with additional zeros at the start if filling).
     *
     * @param value the value to get the index for
     * @return the index in the range, or <code>-1</code> if not an element in this range
     */
    private int getRangeIndex(int value) {
        //If out of range
        if (step > 0) {
            if (start > value || end <= value)
                return -1;
        } else {
            if (start < value || end >= value)
                return -1;
        }

        //Difference from the start
        int diff = Math.abs(value - start);
        //If is greater than or equal to 0 and is an element of the range
        if (value >= 0 && diff % step == 0)
            //Return the index
            return diff / Math.abs(step);
        else
            //Not an element of the range
            return -1;
    }

    @Override
    public String getElement(int index) {
        //If out of range
        if (index >= length)
            throw new IndexOutOfBoundsException(String.format("Index out of bounds! i=%d length=%d", index, length));
        //The int value
        String value = Integer.toString(start + step * index, 10);
        //If not filling
        if (fill <= 0 || value.length() == fill)
            return value;

        //Fill char array
        char[] fill = new char[this.fill - value.length()];
        Arrays.fill(fill, '0');
        //Build
        return new StringBuilder(value).insert(0, fill).toString();
    }

    @Override
    public int getElementLength(int index) {
        //If out of range
        if (index >= length)
            throw new IndexOutOfBoundsException(String.format("Index out of bounds! i=%d length=%d", index, length));
        //Calculate
        return fill > 0 ? fill : countDigits(start + step * index);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String toString() {
        return "RangeSegment{" +
                "start=" + start +
                ", end=" + end +
                ", step=" + step +
                ", fill=" + fill +
                ", length=" + length +
                '}';
    }
}