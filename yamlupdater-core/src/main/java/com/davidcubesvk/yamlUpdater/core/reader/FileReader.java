package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.utils.ParseException;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.block.Block;

import java.util.*;

public class FileReader {

    private static final FileReader FILE_READER = new FileReader();
    private static final BlockReader BLOCK_READER = new BlockReader();

    public static LinkedHashMap<String, Object> load(List<String> lines, Set<String> sectionValues, char keySeparator) throws ParseException {
        //Create a new map
        LinkedHashMap<String, Object> mappings = new LinkedHashMap<>();
        //Load
        int index = FILE_READER.loadSection(lines, new StringBuilder(), mappings, sectionValues, keySeparator);
        if (index < lines.size()) {
            //Put
            mappings.put(null, BLOCK_READER.read(lines, index, keySeparator));
        }
        //Return
        return mappings;
    }

    private int loadSection(List<String> lines, StringBuilder fullKey, Map<String, Object> mappings, Set<String> sectionValues, char keySeparator) throws ParseException {
        //The section value block
        Block section = null;
        //Index and spaces
        int index = 0, spaces = -1;
        //While not at the end of the file
        while (index < lines.size()) {
            //Load
            Block block = BLOCK_READER.read(lines, index, keySeparator);

            //If dangling comment block at the end
            if (block.isComment()) {
                return index;
            }

            //If not set
            if (spaces == -1)
                //Set
                spaces = block.getIndents();
            //If less indents
            if (block.getIndents() < spaces) {
                //End of the section
                return index;
            }

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