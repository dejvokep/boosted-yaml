package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.files.File;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static com.davidcubesvk.yamlUpdater.core.utils.Constants.DOCUMENT_START;

/**
 * Class covering file loading; only one function is public (to be used for this purpose).
 */
public class FileReader {

    /**
     * File reader instance used to call non-static methods.
     */
    private static final FileReader FILE_READER = new FileReader();
    /**
     * Block reader instance used to read blocks.
     */
    private static final BlockReader BLOCK_READER = new BlockReader();

    /**
     * Loads all blocks (mappings, sections) including the stream header.<br>The mappings are represented as a map.
     * Assuming we are loading this YAML file:<br>
     * <code>
     * a:<br>
     * ..b: true<br>
     * ..c: false<br>
     * </code>
     * the map will have structure equal to:
     * <ul>
     *     <li><code>"a"</code>: object of type {@link Section}
     *          <ul>
     *              <li><code>"b"</code>: object of type {@link Block}</li>
     *              <li><code>"c"</code>: object of type {@link Block}</li>
     *          </ul>
     *     </li>
     * </ul>
     * If there are any dangling comments at the end of the file, they are stored with <code>null</code> key.
     *
     * @param streamReader the stream reader (of the file)
     * @param sectionValues section value paths
     * @param settings settings to use to load
     * @return the loaded file
     * @throws ParseException if the YAML is not formatted properly (see {@link BlockReader#read(List, int)} for more
     * information)
     */
    public static File load(InputStreamReader streamReader, Set<String> sectionValues, Settings settings) throws ParseException {
        //The lines
        List<String> lines = new BufferedReader(streamReader).lines().collect(Collectors.toCollection(ArrayList::new));

        //Load the header
        Component<List<Object>> header = FILE_READER.loadHeader(lines);
        //Sublist
        lines = lines.subList(header.getLine(), lines.size());

        //Create a new map
        LinkedHashMap<String, Block> mappings = new LinkedHashMap<>();
        //Load
        int index = FILE_READER.loadSection(lines, new StringBuilder(), mappings, sectionValues, settings.getSeparator());
        //If there are dangling comments
        if (index < lines.size())
            //Put
            mappings.put(null, BLOCK_READER.read(lines, index));
        //Return
        return new File(header.getComponent(), mappings, settings);
    }

    /**
     * Loads header of the file (including the line where
     * {@link com.davidcubesvk.yamlUpdater.core.utils.Constants#DOCUMENT_START} is).
     * @param lines the lines to read from
     * @return the header, or <code>null</code> if there is not any
     */
    public Component<List<Object>> loadHeader(List<String> lines) {
        //The list
        List<Object> header = new ArrayList<>();
        //Comments
        StringBuilder comments = new StringBuilder();
        //Index and size
        int index = -1, size = 0;

        //Loop through all the lines
        for (String line : lines) {
            //Increment
            index++;

            //If it is not a configuration
            if (!BLOCK_READER.isConfiguration(line)) {
                //Add
                comments.append(line);
                continue;
            }

            //If it is the document start
            if (line.startsWith(DOCUMENT_START)) {
                //Add
                header.add(comments.append(line).toString());
                //Reset
                comments.setLength(0);
                //Set
                size = index + 1;
                break;
            }

            //The directive
            Directive directive = Directive.parse(line);
            //If not null
            if (directive != null) {
                //Add
                header.add(directive);
                //Set comments
                directive.setComments(comments.toString());
                //Reset
                comments.setLength(0);
                //Set
                size = index + 1;
                continue;
            }

            //Break
            break;
        }

        //Return
        return header.size() == 0 ? null : new Component<>(size, -1, header);
    }

    /**
     * Loads a configuration section (including all sub-sections) starting from the given line into the given map. That
     * means, reads all sub-blocks while their indentation leveling is at least as large as the first block's
     * indentation.
     * @param lines the lines to read from (a valid YAML)
     * @param fullKey the full key of this section
     * @param mappings the map to load into
     * @param sectionValues sections to be loaded as values, with keys separated with the given separator
     * @param keySeparator the separator for keying system
     * @return how many lines does this section occupy
     * @throws ParseException if the YAML is not formatted properly
     */
    private int loadSection(List<String> lines, StringBuilder fullKey, Map<String, Block> mappings, Set<String> sectionValues, char keySeparator) throws ParseException {
        //The section value block
        Block section = null;
        //Index and spaces
        int index = 0, spaces = -1;
        //While not at the end of the file
        while (index < lines.size()) {
            //Load
            Block block = BLOCK_READER.read(lines, index);

            //If dangling comment block at the end
            if (block.isComment())
                return index;

            //If not set
            if (spaces == -1)
                //Set
                spaces = block.getIndents();
            //If less indents
            if (block.getIndents() < spaces)
                //End of the section
                return index;

            //Increment
            index += block.getSize() + 1;

            //If section value block is set
            if (section != null) {
                //If more indents
                if (section.getIndents() >= block.getIndents()) {
                    //Not part of the section
                    section = null;
                } else {
                    //Attach
                    section.attach(block, block.getIndents() - section.getIndents());
                    continue;
                }
            }

            //If not a section
            if (!block.isSection()) {
                //Put
                mappings.put(block.getFormattedKey(), block);
                continue;
            }

            //If the length is not 0
            if (fullKey.length() > 0)
                //Append the separator
                fullKey.append(keySeparator);

            //If also a value
            if (sectionValues.contains(fullKey.append(block.getFormattedKey()).toString())) {
                //Put
                mappings.put(block.getFormattedKey(), block);
                //Set
                section = block;
            } else {
                //Create a new map
                LinkedHashMap<String, Block> map = new LinkedHashMap<>();
                //Put
                mappings.put(block.getFormattedKey(), new Section(block, map));
                //Load
                index += loadSection(lines.subList(index, lines.size()), fullKey, map, sectionValues, keySeparator);
            }

            //Reset back
            fullKey.setLength(Math.max(0, fullKey.length() - block.getFormattedKey().length() - 1));
        }

        //Finished to the end
        return lines.size();
    }

}