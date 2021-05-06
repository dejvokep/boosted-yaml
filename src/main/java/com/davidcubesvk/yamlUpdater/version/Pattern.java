package com.davidcubesvk.yamlUpdater.version;

import java.util.stream.IntStream;

public class Pattern {

    private static final IntStream ZERO_FILL_STREAM = IntStream.generate(() -> 0);

    private Part[] parts;

    public Pattern(Part[] parts) {
        this.parts = parts;
    }

    public Part getPart(int index) {
        return parts[index];
    }

    public Version getVersion(String version) {
        //Copy
        String edited = version;
        //The cursors
        int[] cursors = new int[parts.length];
        //Go through all parts
        for (int index = 0; index < parts.length; index++) {
            //Set the cursor
            cursors[index] = parts[index].parse(edited);
            //Cut out the start
            edited = edited.substring(parts[index].getElement(cursors[index]).length());
        }

        return new Version(version, this, cursors);
    }

    /**
     * Class used to specify a part of versioning pattern.
     */
    public static class Part {

        /**
         * The maximal length of the ordered sequence.
         */
        public static final int MAX_SEQUENCE_LENGTH = 1000;

        //Array of elements (from the first to last, ordered)
        private final String[] elements;

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
                    number.insert(0, ZERO_FILL_STREAM.limit(fillTo - number.length()).toArray());
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
        private int parse(String version) throws IllegalArgumentException {
            //Go through all indexes
            for (int index = 0; index < elements.length; index++) {
                //If the same
                if (version.startsWith(elements[index]))
                    //Set
                    return index;
            }
            //Not found
            throw new IllegalArgumentException("The given version part \"" + version + "\" does not suit any of this part's possible variations!");
        }

        public String getElement(int index) {
            return elements[index];
        }
        public int length() {
            return elements.length;
        }
    }

}