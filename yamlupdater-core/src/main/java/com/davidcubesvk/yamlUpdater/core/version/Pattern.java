package com.davidcubesvk.yamlUpdater.core.version;

import java.util.stream.IntStream;

/**
 * Class used to specify file versioning pattern. It is strongly recommended to see the API wiki for more information
 * and explanations on this topic.
 */
public class Pattern {

    /**
     * Stream generating endless zeros, used to fill strings to desired length.
     */
    private static final IntStream ZERO_FILL_STREAM = IntStream.generate(() -> 0);

    //Parts of the pattern
    private Part[] parts;

    /**
     * Initializes the pattern with the given parts. The given parts must be ordered from left to right, as specified in
     * an example version ID (e.g. <code>1.2</code>).
     *
     * @param parts the parts, ordered from left (most-significant) to right (less-significant)
     */
    public Pattern(Part[] parts) {
        this.parts = parts;
    }

    /**
     * Returns a part at the given index of this pattern.
     *
     * @param index the index to get
     * @return the part at the given index
     */
    public Part getPart(int index) {
        return parts[index];
    }

    /**
     * Parses and returns a version object from the given version ID. The given ID must match the pattern, otherwise,
     * unexpected results may occur.
     *
     * @param versionId the version ID
     * @return the version object
     * @throws IllegalArgumentException if failed to parse the string (does not match the pattern)
     */
    public Version getVersion(String versionId) throws IllegalArgumentException {
        //Copy reference
        String edited = versionId;
        //The cursors
        int[] cursors = new int[parts.length];
        //Go through all parts
        for (int index = 0; index < parts.length; index++) {
            //Set the cursor
            cursors[index] = parts[index].parse(edited);
            //Cut out the start
            edited = edited.substring(parts[index].getElement(cursors[index]).length());
        }

        return new Version(versionId, this, cursors);
    }

    /**
     * Class used to specify a part of versioning pattern.
     */
    public static class Part {

        /**
         * The max length of the ordered sequence.
         */
        public static final int MAX_SEQUENCE_LENGTH = 1000;

        //Array of elements (from the first to last, ordered)
        private final String[] elements;

        /**
         * Initializes the part by the given elements. These elements should be ordered by the order they are changed
         * when version ID changes, starting from the first element in the first version, till the last element (after
         * which the next version ID will use the first element again).
         * <br>For example, assuming we have the first version string (which has ever been used) <code>1.A</code>, next
         * <code>1.B</code>... till <code>1.E</code>, where next version is <code>2.A</code>, for the letters we would
         * create a part using <code>{"A", "B", "C", "D", "E"}</code>. <strong>Please see the API wiki or
         * {@link #Part(int, int)} for more details.</strong>
         *
         * @param elements all possible values this part can mean, ordered from the first one to last
         * @throws IllegalArgumentException if the given length of varargs is longer than {@link #MAX_SEQUENCE_LENGTH}
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
         * ordered sequence will just <b>descend</b> one-by-one). The constructor actually creates a string array
         * representing this range (from the first to the last number), which if used irresponsibly, will occupy a lot
         * of environment's memory (RAM). Therefore, there is a limit enforced - {@link #MAX_SEQUENCE_LENGTH}.
         *
         * @param from the first integer in the ordered sequence
         * @param to   the (exclusive) last integer
         * @throws IllegalArgumentException if the generated sequence is longer than {@link #MAX_SEQUENCE_LENGTH}
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
         * The constructor actually creates a string array representing this range (from the first to the last number),
         * which if used irresponsibly, will occupy a lot of environment's memory (RAM). Therefore, there is a limit
         * enforced - {@link #MAX_SEQUENCE_LENGTH}.<br>
         * The fill-to parameter indicates <b>exactly</b> how much digits each number must have. If some number has
         * amount of digits less than the value of fill-to, additional <code>0</code>s will be appended before, so the
         * number has the specified number of digits (without changing the mathematical value of the number).<br>
         * If any of the numbers generated from the given boundaries already have more digits than fill-to value, or the
         * boundaries given represent a sequence longer than {@link #MAX_SEQUENCE_LENGTH}, an
         * {@link IllegalArgumentException} will be thrown. To not use the filling feature, set it to <code>0</code>.
         *
         * @param from   the first integer in the ordered sequence
         * @param to     the (exclusive) last integer
         * @param fillTo indicates how many digits must a number have in string form, or <code>0</code> to disable
         * @throws IllegalArgumentException if <code>fillTo</code> parameter is less than <code>0</code> or a number
         *                                  generated from the given boundaries has more digits than the
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
         * Parses the given version ID. Finds the first element in the part's element sequence for which applies: the
         * given ID must start with that element to whose call to {@link String#startsWith(String)} (as parameter)
         * returns <code>true</code>.
         *
         * @param versionId the version ID to parse
         * @return the first found element which is also contained in the start of the version ID
         * @throws IllegalArgumentException if no element matches the version string
         */
        private int parse(String versionId) throws IllegalArgumentException {
            //Go through all indexes
            for (int index = 0; index < elements.length; index++) {
                //If the same
                if (versionId.startsWith(elements[index]))
                    //Set
                    return index;
            }
            //Not found
            throw new IllegalArgumentException("The given version part \"" + versionId + "\" does not suit any of this part's possible variations!");
        }

        /**
         * Returns n-th part element. It must apply that <code>0 <= i < length()</code>.
         * @param index the index
         * @return the element at that index
         */
        public String getElement(int index) {
            return elements[index];
        }

        /**
         * The length of the element array.
         * @return the length of the elements
         */
        public int length() {
            return elements.length;
        }
    }

}