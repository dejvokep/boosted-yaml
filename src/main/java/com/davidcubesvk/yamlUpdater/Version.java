package com.davidcubesvk.yamlUpdater;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Version implements Comparable<Version> {

    //The version pattern (parts)
    private final ArrayList<Part> pattern = new ArrayList<>();

    /**
     * Initializes the versioning system by the current version of the language files and a pattern, which corresponds
     * to the format of the given version. Calls {@link #Version(String, List)}.
     *
     * @param version the (normally specified) current plugin's language file version (e.g. <code>1.2</code>)
     * @param pattern pattern corresponding to the given version - array of parts forming the pattern
     * @throws IllegalArgumentException if the version or pattern array is <code>null</code>, or if the array has length
     *                                  0.
     * @see #Version(String, List) the main constructor
     */
    public Version(String version, Part... pattern) throws IllegalArgumentException {
        this(version, pattern == null ? null : Arrays.asList(pattern));
    }

    /**
     * Initializes the versioning system by the current version of the language files and a pattern, which corresponds
     * to the format of the given version.<br>
     * Let's say the pattern for your plugin version is <code>X.Y</code>, where <code>X</code> is an integer from
     * <code>1</code> to possible infinity and <code>Y</code> an integer from range of <code>0-9</code>. The first part
     * would be representing <code>X</code>, where you would use constructor {@link Part#Part(int, int)} with parameters
     * <code>1</code> and {@link Integer#MAX_VALUE} as <code>from</code> and <code>to</code>, respectively. The second
     * part will be specified using constructor {@link Part#Part(String...)} with the only element <code>"."</code>,
     * because the dot does not change, that part will never represent anything else than a dot. Now there comes the 3rd
     * part, where you'd use constructor {@link Part#Part(int, int)} again, but with parameters <code>0</code> as
     * <code>from</code> and <code>10</code> as <code>to</code>. This allocates an ordered sequence from 0 to 9, which
     * indicates that after for example, version <code>X.3</code> goes version <code>X.4</code>.<br>
     * For more information about how versions are compared, or shifted to find the nearest newer version, please see
     * {@link Part#next()}.
     *
     * @param version the (normally specified) current plugin's language file version (e.g. <code>1.2</code>)
     * @param pattern pattern corresponding to the given version - list of parts forming the pattern
     * @throws IllegalArgumentException if the version or pattern list is <code>null</code>, or if the list size is 0.
     */
    public Version(String version, List<Part> pattern) throws IllegalArgumentException {
        //If null or empty
        if (version == null || pattern == null || pattern.size() == 0)
            throw new IllegalArgumentException("Version or pattern list is null, or there are no parts available.");

        //Add all
        this.pattern.addAll(pattern);
        //Go through all parts
        for (Part part : pattern)
            //Parse
            version = version.substring(part.parse(version).length());
    }

    @Override
    public int compareTo(@NotNull Version o) {
        //If parts don't equal
        if (!pattern.equals(o.pattern))
            throw new ClassCastException("Versions to compare do not have the same part pattern.");
        //Go through all indexes
        for (int index = 0; index < pattern.size(); index++) {
            //Compare
            int compared = pattern.get(index).compareTo(o.pattern.get(index));
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
     * Class used to specify a part of versioning pattern.
     */
    private static class Part implements Comparable<Part> {

        /**
         * The maximal length of the ordered sequence.
         */
        public static final int MAX_SEQUENCE_LENGTH = 1000;

        //Array of elements (from the first to last, ordered)
        private final String[] elements;
        //The cursor
        private int cursor;

        /**
         * Initializes the part by the given elements. These elements should be ordered by the order they are changed
         * when version changes. Please see {@link #Part(int, int)} for more details.
         *
         * @param elements all possible values this part can mean, ordered from the first one to last
         * @throws IllegalArgumentException if the given array is longer than {@link #MAX_SEQUENCE_LENGTH}
         * @see #Part(int, int, int) for more information
         */
        public Part(String... elements) throws IllegalArgumentException {
            //If length is greater than the maximum allowed
            if (elements.length > MAX_SEQUENCE_LENGTH)
                throw new IllegalArgumentException("Maximum allowed sequence length is " + MAX_SEQUENCE_LENGTH + ", but sequence of length " + elements.length + " was given!");
            //Set
            this.elements = elements;
        }

        /**
         * Initializes the part by the given integer boundaries (from - inclusive, to - exclusive). All the integers
         * within this range will each be taken as a value this part can represent, ordered from the first one to last.<br>
         * Please note that <code>from</code> does not necessarily have to be less than <code>to</code> (otherwise, the
         * ordered sequence will just <b>descend</b> one-by-one).
         *
         * @param from the first integer in the ordered sequence
         * @param to   the (exclusive) last integer
         * @throws IllegalArgumentException if <code>fillTo</code> parameter is less than <code>0</code> or greater than
         *                                  and a number generated from the given boundaries has more digits than the
         *                                  <code>fillTo</code> permits, or the generated sequence is longer than
         *                                  {@link #MAX_SEQUENCE_LENGTH}
         * @see #Part(int, int, int) for more information
         */
        public Part(int from, int to) throws IllegalArgumentException {
            this(from, to, 0);
        }

        /**
         * Initializes the part by the given integer boundaries (from - inclusive, to - exclusive). All the integers
         * within this range will each be taken as a value this part can represent, ordered from the first one to last.<br>
         * Please note that <code>from</code> does not necessarily have to be less than <code>to</code> (otherwise, the
         * ordered sequence will just <b>descend</b> one-by-one).<br>
         * The fill-to parameter indicates how much digits each number must have. If some number has amount of digits
         * less than the value of fill-to, additional <code>0</code>s will be appended before, so the number has
         * the specified number of digits (without changing the order of integers). Please note that appending
         * <code>0</code> before numbers in math is completely useless, but here integers are stored as a string with
         * filling used to ensure that each of them (strings) is at exactly that long. If any of the numbers generated
         * from the given boundaries already have more digits than fill-to value, or the boundaries given represent a
         * sequence longer than {@link #MAX_SEQUENCE_LENGTH}, an {@link IllegalArgumentException} will be thrown. To not
         * use the filling feature, set it to <code>0</code>.
         *
         * @param from   the first integer in the ordered sequence
         * @param to     the (exclusive) last integer
         * @param fillTo indicates how many digits must a number have in string form, or <code>0</code> to disable
         * @throws IllegalArgumentException if <code>fillTo</code> parameter is less than <code>0</code> or greater than
         *                                  and a number generated from the given boundaries has more digits than the
         *                                  <code>fillTo</code> permits, or the generated sequence is longer than
         *                                  {@link #MAX_SEQUENCE_LENGTH}
         */
        public Part(int from, int to, int fillTo) throws IllegalArgumentException {
            //If less than 0
            if (fillTo < 0)
                throw new IllegalArgumentException("Fill-to parameter can't be less than 0!");
            //Compute the step
            int step = from > to ? -1 : 1;
            //Create the array
            elements = new String[Math.abs(from - to)];
            //Go through all numbers
            for (int index = 0; index < elements.length; index++) {
                //Create the number
                StringBuilder number = new StringBuilder().append(from + (index * step));
                //If filling parameter is outside the bounds
                if (fillTo != 0 && number.length() > fillTo)
                    throw new IllegalArgumentException("Number already has " + number.length() + " digits, can't fill to " + fillTo + ".");
                //If we need to fill some zeros
                if (number.length() < fillTo)
                    //Insert
                    number.insert(0, IntStream.generate(() -> 0).limit(fillTo - number.length()).toArray());
                //Add
                elements[index] = number.toString();
            }
        }

        /**
         * Parses the given version. Finds the first element in the part's element sequence for which applies: the given
         * version must start with that element, or {@link String#startsWith(String)} called on the version with the
         * element as parameter must return <code>true</code>.
         *
         * @param version the version to parse
         * @return the first found element which is also contained in the start of the version string
         * @throws IllegalArgumentException if no element matches the version string
         */
        private String parse(String version) throws IllegalArgumentException {
            //Go through all indexes
            for (int index = 0; index < elements.length; index++) {
                //If the same
                if (version.startsWith(elements[index])) {
                    //Set
                    this.cursor = index;
                    return elements[index];
                }
            }
            //Not found
            throw new IllegalArgumentException("The given version part \"" + version + "\" does not suit any of this part's possible variations!");
        }

        /**
         * Shifts the cursor to the next value, or if on the last, returns back to first value (and returns
         * <code>true</code> to indicate, that the part before this one to the left in the pattern should also be
         * shifted).
         *
         * @return if the cursor returned back to the first value
         */
        private boolean next() {
            //If on the last element
            if (cursor == elements.length - 1)
                cursor = 0;

            //If returned back to the start
            return cursor == 0;
        }

        @Override
        public int compareTo(Part o) {
            //If elements don't equal
            if (!Arrays.equals(elements, o.elements))
                throw new ClassCastException("Parts do not have the same elements.");

            return Integer.compare(cursor, o.cursor);
        }
    }

}