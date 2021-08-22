package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.*;
import com.davidcubesvk.yamlUpdater.core.files.File;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class covering file loading; only one function is public (to be used for this purpose).
 */
public class FileReader {

    /**
     * Block reader instance used to read blocks.
     */
    private static final BlockReader BLOCK_READER = new BlockReader();

    //The lines
    private List<String> lines;
    //Section values
    private Set<String> sectionValues;
    //Settings
    private Settings settings;

    //The directives
    private List<DirectiveBlock> directives = new ArrayList<>();
    //The document start and end blocks
    private IndicatorBlock documentStart, documentEnd;
    //Mappings
    private Map<String, DocumentBlock> mappings = new HashMap<>();
    //Dangling comments
    private CommentBlock danglingComments;

    //Reading index
    private int index = 0;
    //Temp block
    private Block nextBlock = null;

    public FileReader(InputStreamReader streamReader, Set<String> sectionValues, Settings settings) {
        this.lines = new BufferedReader(streamReader).lines().collect(Collectors.toCollection(ArrayList::new));
        this.sectionValues = sectionValues;
        this.settings = settings;
    }

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
     *              <li><code>"b"</code>: object of type {@link DocumentBlock}</li>
     *              <li><code>"c"</code>: object of type {@link DocumentBlock}</li>
     *          </ul>
     *     </li>
     * </ul>
     * If there are any dangling comments at the end of the file, they are stored with <code>null</code> key.
     *
     * @param streamReader  the stream reader (of the file)
     * @param sectionValues section value paths
     * @param settings      settings to use to load
     * @return the loaded file
     * @throws ParseException if the YAML is not formatted properly (see {@link BlockReader#read(List, int)} for more
     *                        information)
     */
    public File load() throws ParseException {
        //Load the header
        loadHeader();

        //If there is not anything else
        if (nextBlock == null)
            throw new ParseException("No document content was found!");

        //If it is footer content
        if (nextBlock.isFooterContent())
            throw new ParseException("No document content was found!");

        //Load
        loadSection(lines.subList(index, lines.size()), new StringBuilder(), mappings);

        //If there is not anything else
        if (nextBlock == null)
            return new File(directives, documentStart, mappings, documentEnd, danglingComments, settings);

        //Load the footer
        loadFooter();
        //Return
        return new File(directives, documentStart, mappings, documentEnd, danglingComments, settings);
    }

    /**
     * Loads header of the file (including the line where
     * {@link com.davidcubesvk.yamlUpdater.core.utils.Constants#DOCUMENT_START} is).
     *
     * @param lines the lines to read from
     * @return the header, or <code>null</code> if there is not any
     */
    public int loadHeader() throws ParseException {
        //The block
        Block block = null;
        //Index
        int index = this.index;

        //While there is something to read and it is part of the header
        while (index < lines.size() && (block = BLOCK_READER.read(lines, index)).isHeaderContent()) {
            //If a directive
            if (block.getType() == Block.Type.DIRECTIVE) {
                //Add
                directives.add((DirectiveBlock) block);
                return index;
            }

            //Set
            documentStart = (IndicatorBlock) block;
            //Reset
            block = null;
        }

        //Set
        nextBlock = block;
        //Return
        return index;
    }

    public void loadFooter() throws ParseException {
        //The block
        Block block = nextBlock;

        //While there is something to read and it is part of the header
        while (block != null || index < lines.size()) {
            //If null
            if (block == null)
                //Read
                block = BLOCK_READER.read(lines, index);
            //Add
            index += block.getSize();

            //If is an indicator
            if (block.getType() == Block.Type.INDICATOR)
                //Set
                documentEnd = (IndicatorBlock) block;
            else
                //Set
                danglingComments = (CommentBlock) block;

            //Reset
            block = null;
        }
    }

    /**
     * Loads a configuration section (including all sub-sections) starting from the given line into the given map. That
     * means, reads all sub-blocks while their indentation leveling is at least as large as the first block's
     * indentation.
     *
     * @param lines         the lines to read from (a valid YAML)
     * @param fullKey       the full key of this section
     * @param mappings      the map to load into
     * @param sectionValues sections to be loaded as values, with keys separated with the given separator
     * @param keySeparator  the separator for keying system
     * @return how many lines does this section occupy
     * @throws ParseException if the YAML is not formatted properly
     */
    private int loadSection(List<String> lines, StringBuilder fullKey, Map<String, DocumentBlock> mappings) throws ParseException {
        //The section value block
        MappingBlock section = null;
        //Index and spaces
        int index = 0, spaces = -1;
        //While not at the end of the file
        while (index < lines.size()) {
            //Load
            DocumentBlock block;
            //If not loading the first block in the document
            if (nextBlock == null) {
                //Read
                Block readBlock = BLOCK_READER.read(lines, index);
                //If is part of the footer
                if (readBlock.isFooterContent()) {
                    //Set
                    nextBlock = readBlock;
                    //Finished
                    return index;
                }
                //Set
                block = (DocumentBlock) readBlock;
            } else {
                //Set
                block = (DocumentBlock) nextBlock;
                //Set to null
                nextBlock = null;
            }

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
                fullKey.append(settings.getSeparator());

            //If also a value
            if (sectionValues.contains(fullKey.append(block.getFormattedKey()).toString())) {
                //Put
                mappings.put(block.getFormattedKey(), block);
                //Set
                section = new MappingBlock(block.getComments(), new Key(block.getRawKey(), block.getFormattedKey(), block.getIndents()), block.getValue(), block.getSize());
            } else {
                //Create a new map
                LinkedHashMap<String, DocumentBlock> map = new LinkedHashMap<>();
                //Put
                mappings.put(block.getFormattedKey(), new Section(block, map));
                //Load
                index += loadSection(lines.subList(index, lines.size()), fullKey, map);
            }

            //Reset back
            fullKey.setLength(Math.max(0, fullKey.length() - block.getFormattedKey().length() - 1));
        }

        //Finished to the end
        return lines.size();
    }

}