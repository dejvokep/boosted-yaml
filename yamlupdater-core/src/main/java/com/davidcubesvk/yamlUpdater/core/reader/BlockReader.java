package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Key;
import com.davidcubesvk.yamlUpdater.core.block.Value;
import com.davidcubesvk.yamlUpdater.core.utils.Constants;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;
import com.davidcubesvk.yamlUpdater.core.utils.Primitive;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

import static com.davidcubesvk.yamlUpdater.core.utils.Constants.*;

public class BlockReader {

    private static final Yaml YAML = new Yaml();

    /**
     * Reads one configuration block. It must be guaranteed the given YAML is valid and that the first configuration
     * line starting from the given offset contains a key. If any of these conditions is not met, an exception is
     * thrown.
     *
     * @param lines  lines to read from (must be representing a valid YAML configuration), there must not be an EOL
     *               {@link Constants#NEW_LINE} character at the end of each line
     * @param offset index of the first line to read
     * @return the configuration block
     * @throws ParseException if the given YAML is invalid, or invalid offset was specified
     */
    public Block read(List<String> lines, int offset, char keySeparator) throws ParseException {
        //Comments, key and value
        Component<StringBuilder> comments = getComments(lines.subList(offset, lines.size()));
        //If at the end
        if (offset + comments.getLine() >= lines.size())
            //Return
            return new Block(comments.getComponent().toString(), comments.getLine());

        //Try
        try {
            //Key
            Component<Key> key = getKey(lines.get(offset + comments.getLine()));
            //If null
            if (key == null)
                //Throw exception
                throw new ParseException(String.format("Could not parse the key in at line %d.", offset + comments.getLine() + 1));

            //Value
            Component<Value> value = getValue(lines.subList(offset + comments.getLine(), lines.size()), key);
            //Return
            return new Block(comments.getComponent().toString(), key.getComponent(), value.getComponent().getValue(), comments.getLine() + value.getLine(), value.getComponent().isSection());
        } catch (Exception ex) {
            //Throw wrapper exception
            throw new ParseException(String.format("Failed to parse configuration block starting at line %d. Is the YAML formatted properly?", offset + 1), ex);
        }
    }

    /**
     * Reads a simple value component into the given string builder and returns the index of the last line of the value.
     * A value component is considered simple if it is not a:
     * <ul>
     *     <li>list,</li>
     *     <li>string enclosed in quote, or apostrophes,</li>
     *     <li>keyed branch (another configuration section).</li>
     * </ul>
     * It must be guaranteed the value starts at the first line of the given list at the given offset. Amount of key
     * spaces (e.g. amount of whitespace characters before mapping key) is used to determine which lines to read, for
     * reference and to reformat wrong (weird) indentations - from the beginning of each value line, all whitespaces are
     * trimmed, but not more than amount of key spaces.
     *
     * @param lines     the lines to read from (a valid YAML)
     * @param offset    value offset at the first line
     * @param keySpaces amount of spaces at the start of the key line
     * @param builder   a string builder to append to
     * @return the last line of the value (in the given line list)
     */
    private int readSimple(List<String> lines, int offset, int keySpaces, StringBuilder builder) {
        //Go through all lines
        for (int index = 0; index < lines.size(); index++) {
            //The line
            String line = lines.get(index);
            //Spaces here
            offset = index == 0 ? offset : countSpaces(line);

            //If less spaces
            if (offset <= keySpaces)
                //Previous line was the last
                return index - 1;
            else
                //Append
                builder.append(line, index == 0 ? offset : keySpaces, line.length()).append(NEW_LINE);
        }

        //Return the last line
        return lines.size() - 1;
    }

    /**
     * Reads an enclosed value component into the given string builder and returns a position of character just after
     * the last (enclosing) character. A component is considered enclosed if it is enclosed in:
     * <ul>
     *     <li>quotes or apostrophes - is a string,</li>
     *     <li>square brackets - is a list,</li>
     *     <li>curly brackets - is a keyed branch (configuration section).</li>
     * </ul>
     * It must be guaranteed that character at the offset position in the line list returns true when
     * {@link #isEnclosedComponent(char)} is called. Amount of key spaces (e.g. amount of whitespace characters before
     * mapping key) is used for reference and to reformat wrong indentations - from the beginning of each value line,
     * all whitespaces are trimmed, but not more than amount of key spaces.<br>
     * If end of the list was reached but there is still any component-enclosing character remaining to be closed
     * (quote, apostrophe, square/curly bracket), returns <code>null</code> (however, appended content to the given
     * string builder is not removed). This case should be handled by the caller method.
     *
     * @param lines   the lines to read from (a valid YAML)
     * @param offset  value offset at the first line
     * @param spaces  amount of spaces at the start of the key line
     * @param builder a string builder to append to
     * @return the position of character just after the last value (enclosing) character (in the given line list), or
     * <code>null</code> if failed to enclose
     */
    private Position readEnclosed(List<String> lines, int offset, int spaces, StringBuilder builder) {
        //Amount of brackets
        Primitive<Integer> squareBrackets = new Primitive<>(0), curlyBrackets = new Primitive<>(0);
        //If we are in a string
        Primitive<Boolean> quote = new Primitive<>(false), apostrophe = new Primitive<>(false);

        //Go through all lines
        for (int index = 0; index < lines.size(); index++) {
            //The line
            String line = lines.get(index);
            //Spaces here
            offset = index == 0 ? offset : Math.min(countSpaces(line), spaces);

            //Append
            builder.append(line, offset, line.length()).append(NEW_LINE);
            //Read
            int end = processEnclosedChars(line, offset, quote, apostrophe, squareBrackets, curlyBrackets);
            //If found
            if (end != -1)
                //Return
                return new Position(index, end);
        }

        return null;
    }

    /**
     * Reads an enclosed value component into the given string builder and returns a position of character just after
     * the last (enclosing) character. A component is considered enclosed if it is enclosed in:
     * <ul>
     *     <li>quotes or apostrophes - is a string,</li>
     *     <li>square brackets - is a list,</li>
     *     <li>curly brackets - is a keyed branch (configuration section).</li>
     * </ul>
     * It must be guaranteed that character at the offset position in the line returns true when
     * {@link #isEnclosedComponent(char)} is called.
     * If end of the line was reached but there is still any component-enclosing character remaining to be closed
     * (quote, apostrophe, square/curly bracket), returns <code>-1</code> (however, appended content to the given string
     * builder is not removed). This case should be handled by the caller method.
     *
     * @param line    the line to read from (a valid YAML)
     * @param offset  value offset at the line
     * @param builder a string builder to append to
     * @return the <code>index+1</code> of the last value (enclosing) character, or <code>-1</code> if failed to enclose
     */
    private int readEnclosed(String line, int offset, StringBuilder builder) {
        //Amount of brackets
        Primitive<Integer> squareBrackets = new Primitive<>(0), curlyBrackets = new Primitive<>(0);
        //If we are in a string
        Primitive<Boolean> quote = new Primitive<>(false), apostrophe = new Primitive<>(false);

        //Read
        int end = processEnclosedChars(line, offset, quote, apostrophe, squareBrackets, curlyBrackets);
        //If not found
        if (end == -1)
            return -1;

        //Append
        builder.append(line, offset, end);
        //Return
        return end;
    }

    /**
     * Processes all characters in the given line from the given offset. If there's a character, after whose processing
     * there are no quotes, apostrophes, square/curly brackets remaining, returns <code>index+1</code> of that
     * character, <code>-1</code> otherwise.
     *
     * @param line           the line to process characters in
     * @param offset         offset of the first character to read
     * @param quote          a boolean indicating if there is any quote to close
     * @param apostrophe     a boolean indicating if there is any apostrophe to close
     * @param squareBrackets number of square brackets to close
     * @param curlyBrackets  number of curly brackets to close
     * @return the <code>index+1</code> of the last value (enclosing) character (e.g. there is no quote, apostrophe,
     * square/curly bracket remaining), otherwise, <code>-1</code>
     */
    private int processEnclosedChars(String line, int offset, Primitive<Boolean> quote, Primitive<Boolean> apostrophe, Primitive<Integer> squareBrackets, Primitive<Integer> curlyBrackets) {
        //Go through all characters
        for (int i = offset; i < line.length(); i++) {
            //Process
            processCharacter(line, i, quote, apostrophe, squareBrackets, curlyBrackets);

            //If not a string and
            if (!quote.get() && !apostrophe.get()) {
                //If everything is closed
                if (squareBrackets.get() == 0 && curlyBrackets.get() == 0)
                    return i + 1;
                //It is a comment start
                if (line.startsWith(COMMENT_START, i))
                    break;
            }
        }

        return -1;
    }

    /**
     * Processes character at the given index in the given line and modifies the given primitives.
     *
     * @param line           the line to process in
     * @param index          the index of the character to process
     * @param quote          a boolean indicating if there is any quote to close
     * @param apostrophe     a boolean indicating if there is any apostrophe to close
     * @param squareBrackets number of square brackets to close
     * @param curlyBrackets  number of curly brackets to close
     */
    private void processCharacter(String line, int index, Primitive<Boolean> quote, Primitive<Boolean> apostrophe, Primitive<Integer> squareBrackets, Primitive<Integer> curlyBrackets) {
        //Switch
        switch (line.charAt(index)) {
            case STRING_QUOTE_SURROUNDING:
                //If escaped or we are in apostrophe surrounded string
                if (apostrophe.get() || (index >= 1 && line.charAt(index - 1) == ESCAPE))
                    break;
                //Change
                quote.set(!quote.get());
                break;

            case STRING_APOSTROPHE_SURROUNDING:
                //If escaped or we are in quote surrounded string
                if (quote.get() || (index >= 1 && line.charAt(index - 1) == ESCAPE))
                    break;
                //Change
                apostrophe.set(!apostrophe.get());
                break;

            case SQUARE_BRACKET_OPENING:
                //If we are in a string
                if (quote.get() || apostrophe.get())
                    break;
                //Increase
                squareBrackets.set(squareBrackets.get() + 1);
                break;

            case SQUARE_BRACKET_CLOSING:
                //If we are in a string
                if (quote.get() || apostrophe.get())
                    break;
                //Decrease
                squareBrackets.set(squareBrackets.get() - 1);
                break;

            case CURLY_BRACKET_OPENING:
                //If we are in a string
                if (quote.get() || apostrophe.get())
                    break;
                //Decrease
                curlyBrackets.set(curlyBrackets.get() + 1);
                break;

            case CURLY_BRACKET_CLOSING:
                //If we are in a string
                if (quote.get() || apostrophe.get())
                    break;
                //Decrease
                curlyBrackets.set(curlyBrackets.get() - 1);
                break;
        }
    }

    /**
     * Reads normal, non branched (not specified using square brackets) list into the given string builder and returns
     * the index of the last list line.<br>
     * It must be guaranteed that character at the offset position in the first line is equal to
     * {@link Constants#VALUE_LIST} and if it is not the last character in the line, the next one must be a whitespace.
     * Amount of key spaces (e.g. amount of whitespace characters before mapping key) is used to determine which lines
     * to read, for reference and to reformat wrong (weird) indentations - from the beginning of each value line, all
     * whitespaces are trimmed, but not more than amount of key spaces.
     *
     * @param lines   the line to read from (a valid YAML)
     * @param offset  value offset at the line
     * @param spaces  amount of spaces at the start of the key line
     * @param builder a string builder to append to
     * @return the index of the last list line
     */
    private int readList(List<String> lines, int offset, int spaces, StringBuilder builder) {
        //If we are in a string
        Primitive<Boolean> quote = new Primitive<>(false), apostrophe = new Primitive<>(false);
        //Amount of brackets
        Primitive<Integer> squareBrackets = new Primitive<>(0), curlyBrackets = new Primitive<>(0);

        //Read to next index and the current index
        int toNext = -1, index;
        //Go through all lines
        for (index = 0; index < lines.size(); index++) {
            //The line
            String line = lines.get(index);
            //The offset
            offset = index == 0 ? offset : countSpaces(line);

            //If reading till the next configuration
            if (toNext > index) {
                //Append
                builder.append(line, Math.min(spaces, offset), line.length()).append(NEW_LINE);
                continue;
            }

            //If not a configuration
            if (!isConfiguration(line, offset)) {
                //If not everything is closed
                if (quote.get() || apostrophe.get() || squareBrackets.get() > 0 || curlyBrackets.get() > 0) {
                    //Append
                    builder.append(line, Math.min(spaces, offset), line.length()).append(NEW_LINE);
                    continue;
                }

                //The next configuration
                Component<String> nextConfiguration = nextConfiguration(lines.subList(index + 1, lines.size()));
                //If there is less spaces
                if (nextConfiguration.getIndex() <= spaces)
                    break;

                //Read till the next configuration
                toNext = index + 1 + nextConfiguration.getLine();
                continue;
            }

            //If everything is closed and there are less spaces
            if (offset <= spaces && !quote.get() && !apostrophe.get() && squareBrackets.get() == 0 && curlyBrackets.get() == 0)
                break;

            //Go through all characters
            for (int i = 0; i < line.length(); i++) {
                //Process
                processCharacter(line, i, quote, apostrophe, squareBrackets, curlyBrackets);

                //If not a string and it is a comment start
                if (!quote.get() && !apostrophe.get() && line.startsWith(COMMENT_START, i))
                    break;
            }

            //Append
            builder.append(line, Math.min(spaces, offset), line.length()).append(NEW_LINE);
        }

        //Return
        return index - 1;
    }

    /**
     * Reads all lines unless a line with configuration.
     *
     * @param lines the lines to read from
     * @return the comment component
     */
    private Component<StringBuilder> getComments(List<String> lines) {
        //Comments
        StringBuilder comments = new StringBuilder();
        //Index
        int index = 0;

        //Go through all lines
        for (String line : lines) {
            //Trim
            line = trim(line);
            //If a configuration
            if (isConfiguration(line))
                break;

            //Append
            comments.append(line).append(NEW_LINE);
            //Increment
            index++;
        }

        //Return
        return new Component<>(index, -1, comments);
    }

    /**
     * Parses and returns key on the given line. If there's not a key, returns <code>null</code>. This method can be
     * used only if not in an enclosed component (e.g. at the start of the line there is no remaining quote, apostrophe,
     * square/curly bracket to close) and the line can not be an expanded list's line.<br>
     * This is an exception to the returned component object, line is always equal to <code>0</code> (as keys can only
     * be specified in one line), index is equal to <code>index+1</code> of index of the
     * {@link Constants#MAPPING_SEPARATOR_COLON}. The string contained in the component is the key.
     *
     * @param line the line to parse the key from
     * @return the key, or <code>null</code> otherwise
     */
    private Component<Key> getKey(String line) {
        //Count spaces
        int spaces = countSpaces(line);
        //If not a configuration
        if (!isConfiguration(line, spaces) || line.charAt(spaces) == VALUE_LIST)
            return null;

        //The first char
        char c = line.charAt(spaces);
        //If it is an enclosed key
        if (isEnclosedComponent(c)) {
            //Read
            int keyEnd = readEnclosed(line, spaces, new StringBuilder());
            //If not found
            if (keyEnd == -1)
                return null;
            //Spaces past the key
            int spacesPastKey = countSpaces(line, keyEnd);
            //If there is not any mapping colon
            if (line.charAt(keyEnd + spacesPastKey) != MAPPING_SEPARATOR_COLON &&
                    (line.length() != keyEnd + spacesPastKey + 1 && line.charAt(keyEnd + spacesPastKey + 1) != SPACE))
                return null;
            //The key
            String key = line.substring(spaces, keyEnd);
            //If a string
            if (c == STRING_QUOTE_SURROUNDING || c == STRING_APOSTROPHE_SURROUNDING)
                return new Component<>(keyEnd, keyEnd + spacesPastKey + 1, new Key(key, spaces));
            //Parse and return
            return new Component<>(keyEnd, keyEnd + spacesPastKey + 1, new Key(key, YAML.load(key).toString(), spaces));
        }

        //The index of the colon
        int index = line.indexOf(MAPPING_SEPARATOR, spaces);
        //If not found
        if (index == -1) {
            //Set to the last character
            index = line.length() - 1;
            //If is not a colon
            if (line.charAt(index) != MAPPING_SEPARATOR_COLON)
                return null;
        }
        //The key
        String key = line.substring(spaces, index);

        //Return the key
        return new Component<>(0, index + 1, new Key(key, key.trim(), spaces));
    }

    /**
     * Reads and returns value of the mapping from the given list. A value starts exactly where the key ends, e.g. the
     * first character in the output value that is not a {@link Constants#SPACE} is always a
     * {@link Constants#MAPPING_SEPARATOR_COLON}. It must be guaranteed that the first line in the given list is the
     * line with the mapping key, to which the value belongs. The given key component is used to determine all the
     * offsets.<br>
     * If something failed to read (only if the value is an enclosed component), a {@link ParseException} is thrown.
     *
     * @param lines the lines to read from
     * @param key   the key component
     * @return the value
     * @throws ParseException if the value is an enclosed component and it was failed to be enclosed within the given
     *                        line list
     */
    private Component<Value> getValue(List<String> lines, Component<Key> key) throws ParseException {
        //The line
        String line = lines.get(0);
        //Spaces
        int spaces = countSpaces(line);
        //The value
        StringBuilder valueBuilder = new StringBuilder().append(line, spaces + key.getComponent().getRaw().length(), line.length()).append(NEW_LINE);

        //If is a configuration
        if (isConfiguration(line, key.getIndex())) {
            //Count spaces between the key and value
            int spacesBeforeValue = countSpaces(line, key.getIndex());
            //Remove the appended content (value part only)
            valueBuilder.setLength(key.getIndex() - key.getComponent().getRaw().length() - spaces + spacesBeforeValue);

            //If is enclosed
            if (isEnclosedComponent(line.charAt(key.getIndex() + spacesBeforeValue))) {
                //Read
                Position position = readEnclosed(lines, key.getIndex() + spacesBeforeValue, spaces, valueBuilder);
                //If null
                if (position == null)
                    throw new ParseException("Failed to parse enclosed value. Is everything closed properly? Check quotes, apostrophes, square and curly brackets.");

                //Return
                return new Component<>(position.getLine(), -1, new Value(valueBuilder));
            }

            //Read and return
            return new Component<>(readSimple(lines, key.getIndex() + spacesBeforeValue, spaces, valueBuilder), -1, new Value(valueBuilder));
        }

        //If at the end
        if (lines.size() == 1)
            return new Component<>(0, -1, new Value(valueBuilder));

        //Next configuration
        Component<String> nextConfiguration = nextConfiguration(lines.subList(1, lines.size()));
        //Current spaces
        int currentSpaces = countSpaces(nextConfiguration.getComponent());
        //If a key
        if (getKey(nextConfiguration.getComponent()) != null) {
            //If less or equal spaces
            if (currentSpaces <= spaces)
                //Null value
                return new Component<>(0, -1, new Value(valueBuilder, false));
            else
                //A section
                return new Component<>(0, -1, new Value(valueBuilder, true));
        }

        //The index
        int index;
        //Go through all lines
        for (index = 1; index < nextConfiguration.getLine(); index++)
            //Append
            valueBuilder.append(trim(lines.get(index), spaces)).append(NEW_LINE);

        //The line
        line = nextConfiguration.getComponent();

        //If is enclosed
        if (isEnclosedComponent(line.charAt(currentSpaces))) {
            //Read
            Position position = readEnclosed(lines.subList(index, lines.size()), currentSpaces, spaces, valueBuilder);
            //If null
            if (position == null)
                throw new ParseException("Failed to parse enclosed value. Is everything closed properly? Check quotes, apostrophes, square and curly brackets.");

            //The line
            line = lines.get(index + position.getLine());
            //Return
            return new Component<>(index + position.getLine(), -1, new Value(valueBuilder.append(line, position.getIndex(), line.length())));
        }

        //If is a list
        if (isList(line, currentSpaces))
            //Read and return
            return new Component<>(index + readList(lines.subList(index, lines.size()), currentSpaces, spaces, valueBuilder), -1, new Value(valueBuilder));

        //Read and return
        return new Component<>(index + readSimple(lines.subList(index, lines.size()), currentSpaces, spaces, valueBuilder), -1, new Value(valueBuilder));
    }

    /**
     * Returns if the given line contains any configuration.
     *
     * @param line the line to check
     * @return if the given line contains any configuration
     * @see #isConfiguration(String, int)
     */
    private boolean isConfiguration(String line) {
        return isConfiguration(line, 0);
    }

    /**
     * Returns if the given line starting from the offset contains any configuration - e.g.:
     * <ul>
     *     <li>it is not an empty line,</li>
     *     <li>does not contain {@link Constants#SPACE} characters only,</li>
     *     <li>the first non-space character is not a {@link Constants#COMMENT} character.</li>
     * </ul>
     *
     * @param line   the line to check
     * @param offset the starting offset
     * @return if the given line starting from the offset contains any configuration
     */
    private boolean isConfiguration(String line, int offset) {
        //If the length is the offset
        if (line.length() == offset)
            return false;

        //Count spaces
        int spaces = countSpaces(line, offset);
        //Return
        return line.length() > spaces && ((spaces == 0 && line.charAt(0) != '#') || (spaces > 0 && line.charAt(spaces) != '#'));
    }

    /**
     * Finds the first line in the given list on whose call to {@link #isConfiguration(String)} returns
     * <code>true</code>. Returns the line, it's index (relative to the given list) and index of the first configuration
     * character (equal to amount of spaces at the start of the line). If not found, returns <code>null</code>.
     *
     * @param lines the list of lines to find in
     * @return the first configuration line, <code>null</code> otherwise
     */
    public Component<String> nextConfiguration(List<String> lines) {
        //The index and spaces
        int index = 0, spaces;
        //Go through all lines
        for (String line : lines) {
            //If a configuration
            if (isConfiguration(line, spaces = countSpaces(line)))
                return new Component<>(index, spaces, line);

            //Increment
            index++;
        }

        //Not found
        return null;
    }

    /**
     * Counts how many spaces (whitespace characters, {@link Constants#SPACE}) there are at the start of the given
     * string.
     *
     * @param line the string to count in
     * @return amount of spaces at the start of the given string
     * @see #countSpaces(String, int)
     */
    private int countSpaces(String line) {
        return countSpaces(line, 0);
    }

    /**
     * Counts how many spaces (whitespace characters, {@link Constants#SPACE}) there are at the start of the given
     * string, from the given offset.
     *
     * @param line the string to count in
     * @return amount of spaces at the start of the given string
     */
    private int countSpaces(String line, int offset) {
        //If the length is the offset
        if (line.length() == offset)
            return 0;

        //Count
        int count = 0;
        //While the character is a space
        while (count < line.length() - offset && line.charAt(offset + count) == SPACE)
            //Increment
            count++;
        //Return
        return count;
    }


    /**
     * Trims all {@link Constants#SPACE} characters from the start of the given string.
     *
     * @param line the string to trim
     * @return the trimmed string
     */
    private String trim(String line) {
        return trim(line, Integer.MAX_VALUE);
    }

    /**
     * Trims all {@link Constants#SPACE} characters from the start of the given string, but not more than specified by
     * the <code>max</code> parameter.
     *
     * @param line the string to trim
     * @param max  maximum characters to be trimmed
     * @return the trimmed string
     */
    private String trim(String line, int max) {
        return line.substring(Math.min(countSpaces(line), max));
    }

    /**
     * Returns if the given character is equal to opening of any enclosed component:
     * <ul>
     *     <li>string - {@link Constants#STRING_QUOTE_SURROUNDING} and {@link Constants#STRING_APOSTROPHE_SURROUNDING},</li>
     *     <li>list - {@link Constants#SQUARE_BRACKET_OPENING},</li>
     *     <li>keyed branch (configuration section) - {@link Constants#CURLY_BRACKET_OPENING}.</li>
     * </ul>
     *
     * @param c the character to check
     * @return if the given character is equal to opening of any enclosed component
     */
    private boolean isEnclosedComponent(char c) {
        return c == STRING_QUOTE_SURROUNDING || c == STRING_APOSTROPHE_SURROUNDING || c == SQUARE_BRACKET_OPENING || c == CURLY_BRACKET_OPENING;
    }

    /**
     * Returns whether normal (non-branched, not specified using square brackets) list element specification is present
     * starting from the given index in the line. Returns <code>true</code> only if the character at the given index is
     * equal to {@link Constants#VALUE_LIST} and it is the last character, or if the next character is equal to
     * {@link Constants#SPACE}.
     *
     * @param line  the line to check
     * @param index the index to check
     * @return if a normal list element specification is present at the given index
     */
    private boolean isList(String line, int index) {
        return index + 1 < line.length() ? line.startsWith(VALUE_LIST_FULL, index) : line.charAt(index) == VALUE_LIST;
    }

}