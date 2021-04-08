package com.davidcubesvk.yamlUpdater;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class YamlUpdater {

    private static final char COMMENT_START = '#';
    private static final String EMPTY_LINE = "\n";
    private static final char KEY_SEPARATOR = '.';
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final char VALUE_LIST = '-';
    private static final char STRING_QUOTE_SURROUNDING = '\"';
    private static final char STRING_APOSTROPHE_SURROUNDING = '\'';

    //The resource streams converted to lists
    private final ArrayList<String> fromLines, toLines;
    //Versions
    private final Version fromVersion, toVersion;
    //Relocations
    private final Map<String, Set<Relocation>> relocations = new HashMap<>();
    //Sections as value
    private final Set<SectionValue> sectionValues = new HashSet<>();

    public YamlUpdater(InputStream fromStream, InputStream toStream, Version fromVersion, Version toVersion) {
        //Call another constructor
        this(new BufferedReader(new InputStreamReader(fromStream)), new BufferedReader(new InputStreamReader(toStream)), fromVersion, toVersion);
    }

    public YamlUpdater(BufferedReader fromReader, BufferedReader toReader, Version fromVersion, Version toVersion) {
        //Collect and convert to linked lists
        this.fromLines = fromReader.lines().collect(Collectors.toCollection(ArrayList::new));
        this.toLines = toReader.lines().collect(Collectors.toCollection(ArrayList::new));
        //Set
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    public List<String> update() {
        //Read all sections
        Map<String, String> fromSections = readSections(fromLines), toSections = readSections(toLines);
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
    private Map<String, String> readSections(List<String> lines) {
        //Sections to be returned
        Map<String, String> sections = new HashMap<>();
        //The key
        StringBuilder key = new StringBuilder();
        //Everything before the value
        StringBuilder section = new StringBuilder();

        //Go through all lines
        for (int index = 0; index < lines.size(); index++) {
            //Trim
            String line = lines.get(index).trim();
            //If a value
            if (!line.startsWith(COMMENT_START) && !line.equals(EMPTY_LINE)) {
                //Read value
                Value value = readValue(lines, index);
                //Put
                sections.put(key.toString() + KEY_SEPARATOR + value.getKey(), section.append(value.getValue()).toString());
                //Reset
                key.setLength(0);
                section.setLength(0);
                //Continue
                index += value.getLinesSize();
                continue;
            }
            //Append the comment (or empty line)
            section.append(line);
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

    private class Value {
        private String key;
        private String value;
        private int linesSize;

        Value(String key) {
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setLinesSize(int linesSize) {
            this.linesSize = linesSize;
        }

        public String getKey() {
            return key;
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
    private Value readValue(List<String> lines, int offset) {
        //The line
        String line = lines.get(offset);
        //Count spaces
        int spaces = countSpaces(line);
        //Substring
        line = line.substring(spaces);
        //The value object to be returned
        Value value = new Value(line.substring(0, line.indexOf(spaces, KEY_VALUE_SEPARATOR)));
        //The actual value
        StringBuilder valueBuilder = new StringBuilder(line);

        //The index
        int index = 0;
        //While there's more than the key's spaces
        while (offset + ++index < lines.size() && countSpaces(line = lines.get(offset + index)) > spaces)
            //Append
            valueBuilder.append(line.substring(spaces));

        //Set
        value.setValue(valueBuilder.toString());
        value.setLinesSize(index);
        //Return
        return value;
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

    public YamlUpdater addRelocations(String version, Relocation... relocations) {
        //Call another method
        return addRelocation(version, new HashSet<>(Arrays.asList(relocations)));
    }

    public YamlUpdater addRelocation(String version, Set<Relocation> relocations) {
        //If there's no relocation for this version
        if (!this.relocations.containsKey(version))
            //Put
            this.relocations.put(version, relocations);
        else
            //Add
            this.relocations.get(version).addAll(relocations);

        //Return
        return this;
    }

    public YamlUpdater addSectionValues(SectionValue... keys) {
        //Add
        sectionValues.addAll(Arrays.asList(keys));
        //Return
        return this;
    }

    public YamlUpdater addSectionValues(Set<SectionValue> keys) {
        //Add
        sectionValues.addAll(keys);
        //Return
        return this;
    }

    public static class Relocation {

        private String from, to;

    }

    public static class SectionValue {
        private Version from, to;
        private String key;
    }

}