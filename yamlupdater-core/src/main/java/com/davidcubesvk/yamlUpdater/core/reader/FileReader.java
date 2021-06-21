package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.utils.ParseException;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.block.Block;

import java.util.*;

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
     * Loads all blocks (mappings, sections) and returns them as a (indirectly) nested map. Assuming we are loading this
     * YAML file:<br>
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
     * An indirectly nested map is a type of map where the values are not actually maps, but the objects ({@link Block}
     * instances) are carrying another map, which make up to a nested map. If there are any dangling comments at the end
     * of the file, they are stored with <code>null</code> key.
     *
     * @param lines the file to read from (a valid YAML)
     * @param sectionValues sections to be loaded as values, with keys separated with the given separator
     * @param keySeparator the separator for keying system
     * @return the loaded file
     * @throws ParseException if the YAML is not properly formatted (see {@link BlockReader#read(List, int)} for more
     * information)
     */
    public static LinkedHashMap<String, Object> load(List<String> lines, Set<String> sectionValues, char keySeparator) throws ParseException {
        //Create a new map
        LinkedHashMap<String, Object> mappings = new LinkedHashMap<>();
        //Load
        int index = FILE_READER.loadSection(lines, new StringBuilder(), mappings, sectionValues, keySeparator);
        //If there are dangling comments
        if (index < lines.size())
            //Put
            mappings.put(null, BLOCK_READER.read(lines, index));
        //Return
        return mappings;
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
    private int loadSection(List<String> lines, StringBuilder fullKey, Map<String, Object> mappings, Set<String> sectionValues, char keySeparator) throws ParseException {
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
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
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