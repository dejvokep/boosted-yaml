package com.davidcubesvk.yamlUpdater.core.reader;

import com.davidcubesvk.yamlUpdater.core.block.*;
import com.davidcubesvk.yamlUpdater.core.files.YamlFile;
import com.davidcubesvk.yamlUpdater.core.settings.LoaderSettings;
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
    private final List<String> lines;
    //Settings
    private final LoaderSettings settings;

    //The directives
    private final List<DirectiveBlock> directives = new ArrayList<>();
    //The document start and end blocks
    private IndicatorBlock documentStart, documentEnd;
    //Mappings
    private final Map<String, DocumentBlock> mappings = new HashMap<>();
    //Dangling comments
    private CommentBlock danglingComments;

    //Reading index
    private int index = 0;
    //Temp block
    private ReadBlock nextBlock = null;

    public FileReader(InputStreamReader streamReader, LoaderSettings settings) {
        this.lines = new BufferedReader(streamReader).lines().collect(Collectors.toCollection(ArrayList::new));
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
     * @return the loaded file
     * @throws ParseException if the YAML is not formatted properly (see {@link BlockReader#read(List, int)} for more
     *                        information)
     */
    public YamlFile load() throws ParseException {
        //Load the header
        loadHeader();

        //If there is not anything else
        if (nextBlock == null)
            throw new ParseException("No document content was found!");

        //If it is footer content
        if (nextBlock.getBlock().isFooterContent())
            throw new ParseException("No document content was found!");

        //Load
        loadSection(lines.subList(index, lines.size()), new StringBuilder(), mappings);

        //If there is not anything else
        if (nextBlock == null)
            return new YamlFile(directives, documentStart, mappings, documentEnd, danglingComments, settings);

        //Load the footer
        loadFooter();
        //Return
        return new YamlFile(directives, documentStart, mappings, documentEnd, danglingComments, settings);
    }

    /**
     * Loads header of the file (including the line where
     * {@link com.davidcubesvk.yamlUpdater.core.utils.Constants#DOCUMENT_START} is).
     *
     * @return the header, or <code>null</code> if there is not any
     */
    public int loadHeader() throws ParseException {
        //The block
        ReadBlock block = null;
        //Index
        int index = this.index;

        //While there is something to read and it is part of the header
        while (index < lines.size() && (block = BLOCK_READER.read(lines, index)).getBlock().isHeaderContent()) {
            //If a directive
            if (block.getBlock().getType() == Block.Type.DIRECTIVE) {
                //Add
                directives.add((DirectiveBlock) block.getBlock());
                return index;
            }

            //Set
            documentStart = (IndicatorBlock) block.getBlock();
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
        ReadBlock block = nextBlock;

        //While there is something to read and it is part of the header
        while (block != null || index < lines.size()) {
            //If null
            if (block == null)
                //Read
                block = BLOCK_READER.read(lines, index);
            //Add
            index += block.getSize();

            //If is an indicator
            if (block.getBlock().getType() == Block.Type.INDICATOR)
                //Set
                documentEnd = (IndicatorBlock) block.getBlock();
            else
                //Set
                danglingComments = (CommentBlock) block.getBlock();

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
     * @return how many lines does this section occupy
     * @throws ParseException if the YAML is not formatted properly
     */
    private int loadSection(List<String> lines, StringBuilder fullKey, Map<String, DocumentBlock> mappings) throws ParseException {
        //Index and spaces
        int index = 0, spaces = -1;
        //While not at the end of the file
        while (index < lines.size()) {
            //Load
            DocumentBlock block;
            String key;
            int size, indents;
            //If not loading the first block in the document
            if (nextBlock == null) {
                //Read
                ReadBlock readBlock = BLOCK_READER.read(lines, index);
                //If is part of the footer
                if (readBlock.getBlock().isFooterContent()) {
                    //Set
                    nextBlock = readBlock;
                    //Finished
                    return index;
                }
                //Set
                block = (DocumentBlock) readBlock.getBlock();
                key = readBlock.getKey();
                size = readBlock.getSize();
                indents = readBlock.getIndents();
            } else {
                //Set
                block = (DocumentBlock) nextBlock.getBlock();
                key = nextBlock.getKey();
                size = nextBlock.getSize();
                indents = nextBlock.getIndents();
                //Set to null
                nextBlock = null;
            }

            //If not set
            if (spaces == -1)
                //Set
                spaces = indents;
            //If less indents
            if (indents < spaces)
                //End of the section
                return index;

            //Increment
            index += size + 1;

            //If not a section
            if (!block.isSection()) {
                //Put
                mappings.put(key, block);
                continue;
            }

            //If the length is not 0
            if (fullKey.length() > 0)
                //Append the separator
                fullKey.append(settings.getSeparator());

            //Create a new map
            LinkedHashMap<String, DocumentBlock> map = new LinkedHashMap<>();
            //Put
            mappings.put(key, new Section(block, map));
            //Load
            index += loadSection(lines.subList(index, lines.size()), fullKey, map);

            //Reset back
            fullKey.setLength(Math.max(0, fullKey.length() - key.length() - 1));
        }

        //Finished to the end
        return lines.size();
    }

}