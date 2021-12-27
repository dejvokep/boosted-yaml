package com.davidcubesvk.yamlUpdater.core.fvs.segment;

import java.util.Arrays;

/**
 * Represents an immutable segment constructed from an integer range.
 */
public class RangeSegment implements Segment {

    //Variables
    private final int start, step, minStringLength, maxStringLength, fill, length;

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
     * than defined by the parameter. Set to non-positive (<code><= 0</code>) integer to disable.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>step</code> must not be equal to <code>0</code>,</li>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li><code>step</code> must be specified accordingly to the boundaries, if <code>start < end</code> it must be positive (and vice-versa),</li>
     *     <li>all elements in the range must be <code>>= 0</code>, meaning the same for <code>start</code>, but not for <code>end</code>, which can be negative,</li>
     *     <li>if <code>fill > 0</code>, no element must in it's base representation have more digits than defined by the parameter.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the 1st element in the range
     * @param end   ending boundary (exclusive) of the range
     * @param step difference between each 2 elements in the range (step needed to make to get from element <i>i</i> to
     *             <i>i+1</i>)
     * @param fill filling parameter (or <code><= 0</code> to disable)
     */
    public RangeSegment(int start, int end, int step, int fill) {
        this.start = start;
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
     * Creates a segment from the given range.
     * <p>
     * The range is defined by <code>start</code> (inclusive) and <code>end</code> (exclusive) boundaries. Step is equal
     * to <code>1</code> if <code>start < end</code>, <code>-1</code> otherwise. Filling is disabled.
     * <p>
     * <b>Please mind following:</b>
     * <ul>
     *     <li><code>start</code> cannot be equal to <code>end</code>,</li>
     *     <li>all elements in the range must be <code>>= 0</code>, meaning the same for <code>start</code>, but not for <code>end</code>, which can be negative.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the 1st element in the range
     * @param end   ending boundary (exclusive) of the range
     * @see #RangeSegment(int, int, int, int)
     */
    public RangeSegment(int start, int end) {
        this(start, end, start < end ? 1 : -1, 0);
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
     *     <li><code>step</code> must be specified accordingly to the boundaries, if <code>start < end</code> it must be positive (and vice-versa),</li>
     *     <li>all elements in the range must be <code>>= 0</code>, meaning the same for <code>start</code>, but not for <code>end</code>, which can be negative.</li>
     * </ul>
     * If any of the conditions above is not met, an {@link IllegalArgumentException} is thrown.
     *
     * @param start starting boundary (inclusive) of the range, also the 1st element in the range
     * @param end   ending boundary (exclusive) of the range
     * @param step difference between each 2 elements in the range (step needed to make to get from element <i>i</i> to
     *             <i>i+1</i>)
     * @see #range(int, int, int, int)
     */
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
            //Add 9 at the end (e.g. 9 -> 99)
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

            //Return
            return getRangeIndex(Integer.parseInt(versionId.substring(index, fill)));
        }

        //Current integer value
        int value = 0;
        //Index of the currently processed char
        for (int i = 0; i < maxStringLength; i++) {
            //If out of bounds
            if (i >= versionId.length() - index)
                return -1;
            //Shift to the left
            value *= 10;
            //Parse digit
            int digit = Character.digit(versionId.charAt(index + i), 10);
            //If invalid
            if (digit == -1)
                return -1;
            //Add
            value += digit;

            //If not available to be compared yet
            if (i + 1 < minStringLength)
                continue;

            //Parse index
            int rangeIndex = getRangeIndex(value);
            //If parsed successfully
            if (rangeIndex != -1)
                return rangeIndex;
        }

        //Cannot parse
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
        return fill > 0 ? fill : countDigits(start + step * index);
    }

    @Override
    public int length() {
        return length;
    }
}