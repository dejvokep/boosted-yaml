package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.block.DirectiveBlock;
import com.davidcubesvk.yamlUpdater.core.block.DocumentBlock;
import com.davidcubesvk.yamlUpdater.core.block.IndicatorBlock;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.files.YamlFile;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.davidcubesvk.yamlUpdater.core.utils.Constants.*;

/**
 * A class responsible for merging the old file with the latest one and therefore, update the file.
 */
public class Merger {

    /**
     * Merger instance for calling non-static methods.
     */
    private static final Merger MERGER = new Merger();

    /**
     * Merges the given files.
     *
     * @param disk        the disk (old, to be updated) file
     * @param resource    the resource (latest) file
     * @param resourceMap YAML map representation of the resource file (must match the resource file)
     * @param indents     amount of indents (spaces) per each hierarchy level
     * @return the merged file as a string
     */
    public static String merge(YamlFile disk, YamlFile resource, Map<Object, Object> resourceMap, Settings settings) {
        //The builder
        StringBuilder builder = new StringBuilder();
        //Merge header
        MERGER.mergeHeader(builder, disk, resource, settings.isKeepFormerDirectives());
        //Merge document
        MERGER.iterate(resource, resourceMap, disk, builder, 0, settings.getIndentSpaces());
        //Merge footer
        MERGER.mergeFooter(builder, disk, resource);

        //Return as string
        return builder.toString();
    }

    private void mergeHeader(StringBuilder builder, YamlFile disk, YamlFile resource, boolean keepFormerDirectives) {
        //Go through all directives inside the resource file
        outer:
        for (DirectiveBlock directive : resource.getDirectives()) {
            //The iterator
            Iterator<DirectiveBlock> iterator = disk.getDirectives().iterator();
            //Go through all directives
            while (iterator.hasNext()) {
                //The former directive
                DirectiveBlock formerDirective = iterator.next();
                //If the same
                if ((!directive.isTag() && !formerDirective.isTag()) || (directive.isTag() && formerDirective.isTag() && directive.getId().equals(formerDirective.getId()))) {
                    //Append
                    builder.append(keepFormerDirectives ? formerDirective.getRaw() : directive.getRaw());

                    //Remove
                    iterator.remove();
                    continue outer;
                }
            }

            //Append
            builder.append(directive.getRaw());
        }

        //Go through all remaining former directives
        for (DirectiveBlock directive : disk.getDirectives())
            //Append
            builder.append(directive.getRaw());

        //Append indicator
        appendIndicator(builder, disk.getDocumentStart(), resource.getDocumentStart());
    }

    private void mergeFooter(StringBuilder builder, YamlFile disk, YamlFile resource) {
        //Append indicator
        appendIndicator(builder, disk.getDocumentEnd(), resource.getDocumentEnd());

        //If there are disk dangling comments
        if (disk.getDanglingComments() != null) {
            //Append
            builder.append(disk.getDanglingComments().getComments());
            return;
        }

        //If there are resource dangling comments
        if (resource.getDanglingComments() != null)
            //Append
            builder.append(resource.getDanglingComments().getComments());
    }

    private void appendIndicator(StringBuilder builder, IndicatorBlock diskIndicator, IndicatorBlock resourceIndicator) {
        //If disk indicator exists
        if (diskIndicator != null) {
            //Append
            builder.append(diskIndicator.getComments()).append(diskIndicator.getSpecification());
            return;
        }

        //If resource indicator exists
        if (resourceIndicator != null)
            //Append
            builder.append(resourceIndicator.getComments()).append(resourceIndicator.getSpecification());
    }


    /**
     * Iterates and appends all key-value pairs, iterating from the view of the resource file, to the given builder.
     * <ul>
     *     <li>If a pair is a section pair (e.g. the value of the pair is a section), comments and key of the section
     *     block are appended (also the value in terms of the colon and spaces around) and this function is called
     *     recursively on the sub-section. If there is not any block present in the disk section, the full section
     *     (including all sub-mappings) is automatically appended and recursive call is not made. If there is a mapping,
     *     but is a block (not a section too), it is considered a change of mapping interest and therefore, will be
     *     taken like it did not exist and the section from the resource section is automatically appended (and again,
     *     no recursive call is made).</li>
     *     <li>If it is a block (not a section) and block with the same key is present in the disk section, the
     *     comments, raw key specification and the value are appended. If it is a section, the full section (from the
     *     disk file) is automatically appended. If there is not a key present in the disk section, the block is
     *     automatically appended.</li>
     * </ul>
     * All changes are reflected in the resource map.
     * <b>Please note that during loading, if a section is configured to be a section value, it no longer is a section,
     * it is considered to be a normal mapping block.</b>
     *
     * @param resourceSection the section from the resource file
     * @param resourceMap     the YAML representation of the section from the resource file (must match the
     *                        <code>resourceSection</code> parameter)
     * @param diskSection     the section from the disk file, must match to <code>resourceSection</code> section (must have
     *                        the same depth and full key)
     * @param builder         a builder to which will the function append
     * @param depth           the depth in (level of) the file's hierarchy
     * @param indentSpaces    spaces per each indent differentiating each level (depth) of the hierarchy
     */
    private void iterate(Section resourceSection, Map<Object, Object> resourceMap, Section diskSection, StringBuilder builder, int depth, int indentSpaces) {
        //Go through all entries
        for (Map.Entry<String, DocumentBlock> entry : resourceSection.getMappings().entrySet()) {
            //If a block
            if (entry.getValue() instanceof Section) {
                //The section
                Section section = (Section) entry.getValue();
                //If there is not such a key in the current map
                if (diskSection == null || diskSection.getMappings() == null || !diskSection.getMappings().containsKey(entry.getKey())) {
                    //Append
                    appendSection(builder, depth, indentSpaces, (Map<Object, Object>) resourceMap.get(entry.getKey()), section, entry.getKey());
                    continue;
                }

                //Disk object
                DocumentBlock diskBlock = diskSection.getMappings().get(entry.getKey());
                //Append
                appendBlock(builder, depth, indentSpaces, diskBlock, entry.getKey());
                //If a section
                if (diskBlock instanceof Section) {
                    //Create a new map
                    Map<Object, Object> map = new HashMap<>();
                    //Update the map
                    resourceMap.put(entry.getKey(), map);
                    //Iterate
                    iterate(section, map, (Section) diskBlock, builder, depth + 1, indentSpaces);
                } else
                    //Update the map
                    resourceMap.put(entry.getKey(), YAML.load(diskBlock.getValue().toString().trim().substring(1)));
                continue;
            }

            //The block
            DocumentBlock resourceBlock = entry.getValue();
            //If there is not such a key in the current map
            if (diskSection == null || diskSection.getMappings() == null || !diskSection.getMappings().containsKey(entry.getKey())) {
                //Append
                appendBlock(builder, depth, indentSpaces, resourceBlock, entry.getKey());
                continue;
            }

            //Disk object
            DocumentBlock diskBlock = diskSection.getMappings().get(entry.getKey());
            //If a block
            if (diskBlock instanceof Section) {
                //Create a new map
                Map<Object, Object> map = new HashMap<>();
                //Update the map
                resourceMap.put(entry.getKey(), map);
                //Append
                appendSection(builder, depth, indentSpaces, map, (Section) diskBlock, entry.getKey());
            } else {
                //Update the map
                resourceMap.put(entry.getKey(), YAML.load(diskBlock.getValue().toString().trim().substring(1)));
                //Append
                appendBlock(builder, depth, indentSpaces, diskBlock, entry.getKey());
            }
        }
    }

    /**
     * Appends the given section (including all the sub-sections or sub-blocks). If at any point the YAML map does not
     * contain a certain object which the section does, that object (not the block itself, but the key and value) is
     * automatically inserted into the map. That means, at the end of this function, the given map matches the given
     * section.
     *
     * @param builder the builder to append to
     * @param depth   the depth of the section in (level of) the file's hierarchy
     * @param indents indents (spaces) differentiating each level (depth) of the hierarchy
     * @param map     the YAML representation of the given section
     * @param section the section to append
     * @throws IllegalArgumentException if there is not instance of type {@link DocumentBlock} somewhere in the section's
     *                                  mapping values
     */
    private void appendSection(StringBuilder builder, int depth, int indents, Map<Object, Object> map, Section section, String key) throws IllegalArgumentException {
        //Append
        appendBlock(builder, depth, indents, section, key);
        //Go through all entries
        for (Map.Entry<String, DocumentBlock> entry : section.getMappings().entrySet()) {
            //If null
            if (entry.getValue() == null)
                throw new IllegalArgumentException(String.format("Object at path ...%s is not a block object. Please report the problem to our support.", entry.getKey()));

            //If a section
            if (entry.getValue() instanceof Section) {
                //Create a new map
                Map<Object, Object> newMap = new HashMap<>();
                //Update the map
                map.put(entry.getKey(), newMap);
                //Append
                appendSection(builder, depth + 1, indents, newMap, (Section) entry.getValue(), entry.getKey());
            } else {
                //Update the map
                map.put(entry.getKey(), YAML.load(entry.getValue().getValue().toString().trim().substring(1)));
                //Append
                appendBlock(builder, depth + 1, indents, entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * Appends the given block to the given builder. If the given block is an instance of {@link Section}, only block-
     * available components are appended - comments, key and value. Does not append all the sub-mappings of the section
     * (use {@link #appendSection(StringBuilder, int, int, Map, Section)} instead.
     *
     * @param builder the builder to append to
     * @param depth   the depth of the block in (level of) the file's hierarchy
     * @param indents indents (spaces) differentiating each level (depth) of the hierarchy
     * @param block   the block to append
     */
    private void appendBlock(StringBuilder builder, int depth, int indents, DocumentBlock block, String key) {
        //Calculate spaces
        int spaces = depth * indents;
        //Append comments
        appendSequence(builder, block.getComments(), spaces);
        //Append key
        appendSequence(builder, key, spaces);
        //Append value
        appendSequence(builder, block.getValue(), spaces);
    }

    /**
     * Appends the given sequence to the given builder. When on a new line and not at the end of the sequence, spaces
     * are appended before as specified by the <code>spaces</code> parameter.
     *
     * @param builder  the builder to append to
     * @param sequence the sequence to append
     * @param spaces   amount of spaces to append at the start of each line in the sequence
     */
    public void appendSequence(StringBuilder builder, CharSequence sequence, int spaces) {
        //Go through all characters
        for (int index = 0; index < sequence.length(); index++) {
            //If previous character was a new line char
            if (builder.length() >= 1 && builder.charAt(builder.length() - 1) == NEW_LINE)
                //Append the indentation prefix
                for (int count = 0; count < spaces; count++)
                    builder.append(SPACE);

            //Char
            char c = sequence.charAt(index);
            //Append
            builder.append(c);
        }
    }

}