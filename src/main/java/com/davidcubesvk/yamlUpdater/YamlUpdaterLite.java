package com.davidcubesvk.yamlUpdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class YamlUpdaterLite {

    private static final char COMMENT_START = '#';
    private static final String EMPTY_LINE = "\n", NEW_LINE = EMPTY_LINE;
    private static final char KEY_SEPARATOR = '.';
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final char VALUE_LIST = '-';
    private static final char STRING_QUOTE_SURROUNDING = '\"';
    private static final char STRING_APOSTROPHE_SURROUNDING = '\'';
    private static final char ARRAY_START = '[';
    private static final char ARRAY_END = ']';

    private static final Map<Character, String> representations = new HashMap<Character, String>() {{
        put(STRING_QUOTE_SURROUNDING, "" + STRING_QUOTE_SURROUNDING + STRING_QUOTE_SURROUNDING + STRING_QUOTE_SURROUNDING);
        put(STRING_APOSTROPHE_SURROUNDING, get(STRING_QUOTE_SURROUNDING).replace(STRING_QUOTE_SURROUNDING, STRING_APOSTROPHE_SURROUNDING));
    }};

    //The resource streams converted to lists
    private final ArrayList<String> fromLines, toLines;
    //Sections as value
    private final Set<SectionValue> sectionValues = new HashSet<>();

    public YamlUpdaterLite(InputStream fromStream, InputStream toStream) {
        //Call another constructor
        this(new BufferedReader(new InputStreamReader(fromStream)), new BufferedReader(new InputStreamReader(toStream)));
    }

    public YamlUpdaterLite(BufferedReader fromReader, BufferedReader toReader) {
        //Collect and convert to linked lists
        this.fromLines = fromReader.lines().collect(Collectors.toCollection(ArrayList::new));
        this.toLines = toReader.lines().collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> update() {
        //Read all sections
        Map<String, String> fromSections = readSections(fromLines), toSections = readSections(toLines);
        return null;
    }

    /**
     * Reads all sections from the given file listing in a key-value version whereas key is the actual key to which the
     * section belongs with the section as the value. A section is considered as a block of sequential lines optionally
     * starting with comments and then value. If there is any dangling comment, it is loaded with a <code>null</code>
     * key.
     *
     * @param lines the file lines to read from
     * @return all sections including dangling comments (if there are any present)
     */
    private int readSections(Map<String, String> load, List<String> lines, String parentKey, int spaces) {
        //The key
        StringBuilder key = new StringBuilder(parentKey);
        //Everything before the value
        StringBuilder section = new StringBuilder();

        //Go through all lines
        for (int index = 0; index < lines.size(); index++) {
            //Trim
            String line = lines.get(index).trim();
            //If is not a configuration
            if (!isConfiguration(line)) {
                //Append the comment (or empty line)
                section.append(line);
                continue;
            }

            //If a section
            if (isSection(lines, index))
                //Read and return
                return ++index + readSections(load, lines, key.append(KEY_SEPARATOR).append(getKey(line)).toString(), countSpaces(lines.get(index)));

            //Read value
            Mapping mapping = getValue(lines, index);
            //Put
            sections.put(key.toString() + KEY_SEPARATOR + mapping.getKey(), section.append(mapping.getValue()).toString());
            //Reset
            key.setLength(0);
            section.setLength(0);
            //Continue
            index += mapping.getLinesSize();
            continue;
        }
        //If not empty
        if (section.length() != 0)
            //Add as dangling comment (at the end)
            sections.put(null, section.toString());
        //Return
        return sections;
    }

    private boolean isSection(List<String> lines, int offset) {
        //The key line
        String line = lines.get(offset);
        //Trim so there is only the value left
        line = line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim();
        //If there is something and it is not a comment
        if (line.length() > 0 && line.charAt(0) != COMMENT_START)
            return false;

        //Go through all following lines
        for (String next : lines.subList(offset + 1, lines.size())) {
            //If not a configuration
            if (!isConfiguration(next))
                continue;

            //Return if there is a key
            return getKey(next) != null;
        }

        //Will never happen with properly formatted YAML
        return false;
    }

    private boolean isBlankLine(String line) {
        //Trim and return
        return line.trim().length() == 0;
    }

    private boolean isConfiguration(String line) {
        //Trim
        line = line.trim();
        //Return
        return line.length() != 0 && line.charAt(0) != COMMENT_START;
    }

    private String getKey(String line) {
        //Trim
        line = line.trim();
        //If not a configuration or if is a list
        if (!isConfiguration(line) || line.charAt(0) == VALUE_LIST)
            return null;
        //Key surrounding character (quote or apostrophe)
        Character surrounding = line.charAt(0) == STRING_QUOTE_SURROUNDING || line.charAt(0) == STRING_APOSTROPHE_SURROUNDING ? line.charAt(0) : null;
        //The index of the colon
        int index = line.indexOf(KEY_VALUE_SEPARATOR);
        //If there is not any
        if (surrounding == null)
            //Return the key, if is a mapping
            return index == -1 ? null : line.substring(0, index);

        //Construct string that can be used to represent the surrounding character validly, without closing the string (''' or """)
        String validRepresentation = surrounding + "" + surrounding + "" + surrounding;
        //Go through all characters to find the end surrounding
        for (index = 1; index < line.length(); index++) {
            //If not a surrounding char
            if (line.charAt(index) != surrounding)
                continue;

            //If a valid in-string surrounding representation
            if (line.startsWith(validRepresentation, index))
                //Skip 2 more indexes
                index += 2;
            else
                //A surrounding char
                break;
        }
        //If at the end
        if (index >= line.length())
            return null;
        //If there is a colon
        return line.substring(index).trim().charAt(0) == KEY_VALUE_SEPARATOR ? line.substring(0, index) : null;
    }

    private class Mapping {
        private String comments, key, value;
        private int linesSize;

        Mapping(String key, String value, int linesSize) {
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setLinesSize(int linesSize) {
            this.linesSize = linesSize;
        }

        public String getValue() {
            return value;
        }

        public int getLinesSize() {
            return linesSize;
        }
    }

    /**
     * Reads value from the given file lines and offset indicating the start of the reading.
     *
     * @param lines  file representation to read from
     * @param offset index to start from (index at which the value starts)
     * @return the read value
     */
    private Mapping getValue(List<String> lines, String key) {
        //The line
        String line = lines.get(0);
        //Count spaces
        int spaces = countSpaces(line);
        //Substring
        line = line.substring(spaces);

        //Remove the key
        line = line.substring(key.length());
        //The value start offset
        int valueStartOffset = key.length() + line.indexOf(KEY_VALUE_SEPARATOR);
        //Just so only the value remains
        line = line.substring(line.indexOf(KEY_VALUE_SEPARATOR));

        //Count spaces to the value
        int spacesToValue = countSpaces(line);
        //Add to the offset
        valueStartOffset += spacesToValue;
        //Trim
        line = line.substring(spacesToValue);

        //The value
        StringBuilder value = new StringBuilder(line);
        //The line size of the value
        int linesSize;
        //If in this line
        if (isConfiguration(line)) {
            //The first character
            char c = line.charAt(0);
            //Is a string surrounding character
            if (isStringSurrounding(c))
                //Read
                linesSize = getStringLineSize(lines, valueStartOffset, c);
            else if (isArrayOpening(c))
                //Read
                linesSize = getArrayLineSize(lines, valueStartOffset);
            else
                return new Mapping(key, line, 1);
        } else {
            //Go through all lines
            for (linesSize = 1; linesSize < lines.size(); linesSize++)
                //If it is a configuration
                if (isConfiguration(lines.get(linesSize)))
                    break;
            //The line
            line = lines.get(linesSize);
            //Count spaces to the value
            spacesToValue = countSpaces(line);
            //The character
            char c = line.charAt(spacesToValue);
            //Is a string surrounding character
            if (isStringSurrounding(c))
                //Read
                linesSize += getStringLineSize(lines.subList(linesSize, lines.size()), spacesToValue, c);
            else if (isArrayOpening(c))
                //Read
                linesSize += getArrayLineSize(lines.subList(linesSize, lines.size()), spacesToValue);
            else
                //Just a one-line value
                linesSize += 1;
        }

        //Go through all lines
        for (int index = 1; index < linesSize; index++) {
            //The line
            line = lines.get(index);
            //Count spaces
            int lineSpaces = countSpaces(line);
            //Append the line
            value.append(line.substring(Math.min(lineSpaces, spaces))).append(NEW_LINE);
        }

        //Return the mapping
        return new Mapping(key, value.toString(), linesSize);
    }

    /**
     * Parses a string and returns it's length in terms of how many lines it occupies. It must be guaranteed that the
     * string specification starts at the first line, e.g. <code>lines.get(0)</code> at character index specified by the
     * offset parameter (where the value of the surrounding parameter must equal the character on the offset index),
     * otherwise an {@link IllegalArgumentException} exception is thrown. Same happens if call to
     * {@link #isStringSurrounding(char)} with the given surrounding character returns <code>false</code>.
     *
     * @param lines       lines of YAML mappings starting from the string opening character (surrounding, specification)
     * @param offset      the index of the string-surrounding character
     * @param surrounding the string-surrounding character on the given offset index
     * @return how many lines the string occupies (<code><i>line index with ending string surrounding</i> + 1</code>)
     * @throws ParseException           if the end of the string could not be found - the YAML is not formatted properly
     * @throws IllegalArgumentException if the character at the given offset on the first line is not equal to neither
     *                                  of {@link #STRING_QUOTE_SURROUNDING} or {@link #STRING_APOSTROPHE_SURROUNDING},
     *                                  or if the given surrounding character is neither of these
     */
    private int getStringLineSize(List<String> lines, int offset, char surrounding) throws
            ParseException, IllegalArgumentException {
        //If it is not a string surrounding character
        if (!isStringSurrounding(surrounding))
            throw new IllegalArgumentException("The given surrounding character is not a string surrounding character!");
        //If there is no opening bracket
        if (lines.get(0).charAt(offset) != surrounding)
            throw new IllegalArgumentException("Character on the given offset does not equal the given string surrounding character!");

        //Set the valid representation
        String validRepresentation = representations.get(surrounding);
        //Go through all following lines
        for (int index = 0; index < lines.size(); index++) {
            //Change the offset if at the end of the second line
            if (index == 1)
                offset = 0;

            //Find the surrounding in the current line
            if (findStringSurrounding(lines.get(index), offset, surrounding, validRepresentation) != -1)
                //Return
                return index;
        }

        //Would never happen with properly formatted YAML
        throw new ParseException("Could not find the end of a string! Is the YAML formatted properly?", -1);
    }

    /**
     * Finds a string surrounding in the given line starting from the given offset. Returns the index, or
     * <code>-1</code> if not found.
     *
     * @param line                the line to search in
     * @param offset              starting index
     * @param surrounding         the string surrounding to find
     * @param validRepresentation a valid representation of the surrounding character that can be used inside the string
     *                            (without actually closing the string)
     * @return the index of the closing surrounding, or <code>-1</code> of not found
     * @throws IllegalArgumentException if the given surrounding character is neither of
     *                                  {@link #STRING_QUOTE_SURROUNDING} or {@link #STRING_APOSTROPHE_SURROUNDING}
     */
    private int findStringSurrounding(String line, int offset, char surrounding, String validRepresentation) throws
            IllegalArgumentException {
        //If not a string surrounding character
        if (!isStringSurrounding(surrounding))
            throw new IllegalArgumentException("The given surrounding character is not a string surrounding character!");

        //Go through all characters to find the end surrounding
        for (; offset < line.length(); offset++) {
            //If not a surrounding char
            if (line.charAt(offset) != surrounding)
                continue;

            //If a valid in-string surrounding representation
            if (line.startsWith(validRepresentation, offset))
                //Skip 2 more indexes
                offset += 2;
            else
                //A surrounding char
                return offset;
        }

        //Not found
        return -1;
    }

    /**
     * Parses an array and returns it's length in terms of how many lines it occupies. It must be guaranteed that the
     * array specification starts at the first line, e.g. <code>lines.get(0)</code> at character index specified by the
     * offset parameter (where {@link #ARRAY_START} must equal the character on the offset index), otherwise an
     * {@link IllegalArgumentException} exception is thrown.<br>
     * Supports nested arrays.
     *
     * @param lines  lines of YAML mappings starting from the array opening (specification)
     * @param offset the index of the {@link #ARRAY_START} bracket in the first line
     * @return how many lines the array occupies (<code><i>line index with {@link #ARRAY_END}</i> + 1</code>)
     * @throws ParseException           if the end of the array could not be found - the YAML is not formatted properly
     * @throws IllegalArgumentException if the character at the given offset on the first line is not equal to
     *                                  {@link #ARRAY_START}
     */
    private int getArrayLineSize(List<String> lines, int offset) throws ParseException, IllegalArgumentException {
        //If there is no opening bracket
        if (lines.get(0).charAt(offset) != ARRAY_START)
            throw new IllegalArgumentException("Character on the given offset is not array-opening bracket!");

        //The string surrounding (or null if not in a string)
        Character surrounding = null;
        //The valid representation of the surrounding
        String validRepresentation = null;
        //Number of closing brackets to be found
        int brackets = 1;

        //Go through all following lines
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            //Change the offset if at the end of the second line
            if (lineIndex == 1)
                offset = -1;

            //The line
            String line = lines.get(lineIndex);
            //Go through all characters
            for (int index = offset + 1; index < line.length(); index++) {
                //The character
                char c = line.charAt(index);
                //If in a string
                if (surrounding != null) {
                    //Find the end
                    int occurrence = findStringSurrounding(line, index, surrounding, validRepresentation);
                    //If found
                    if (occurrence != -1) {
                        //Move to the end
                        lineIndex = occurrence;
                        //Reset
                        surrounding = null;
                        validRepresentation = null;
                    }
                } else if (isStringSurrounding(c)) {
                    //Set the surrounding
                    surrounding = c;
                    //Set the valid representation
                    validRepresentation = representations.get(c);
                } else {
                    //If a comment start
                    if (c == COMMENT_START)
                        //Skip
                        continue;
                    if (c == ARRAY_START)
                        //Add bracket
                        brackets++;
                    else if (c == ARRAY_END)
                        //Remove bracket
                        if (--brackets == 0)
                            return lineIndex;
                }
            }
        }

        //Would never happen with properly formatted YAML
        throw new ParseException("Could not find the end of an array! Is the YAML formatted properly?", -1);
    }

    /**
     * Returns <code>true</code> if the given character is equal to either {@link #STRING_QUOTE_SURROUNDING} or
     * {@link #STRING_APOSTROPHE_SURROUNDING}.
     *
     * @param c the character to compare
     * @return if the given character is a string surrounding character
     */
    private boolean isStringSurrounding(char c) {
        return c == STRING_QUOTE_SURROUNDING || c == STRING_APOSTROPHE_SURROUNDING;
    }

    private boolean isArrayOpening(char c) {
        return c == ARRAY_START;
    }

    /**
     * Counts how many spaces (whitespace characters) there are at the start of the given string.
     *
     * @param line the string to count in
     * @return amount of spaces at the start of the given string
     */
    private int countSpaces(String line) {
        //Count
        int count = 0;
        //While the character is not a space
        while (line.charAt(count) == ' ')
            //Increment
            count++;
        //Return
        return count;
    }

    public void update(File file) {

    }

    public YamlUpdaterLite addSectionValues(SectionValue... keys) {
        //Add
        sectionValues.addAll(Arrays.asList(keys));
        //Return
        return this;
    }

    public YamlUpdaterLite addSectionValues(Set<SectionValue> keys) {
        //Add
        sectionValues.addAll(keys);
        //Return
        return this;
    }

    public static class SectionValue {
        private Version from, to;
        private String key;
    }

}