package com.davidcubesvk.yamlUpdater.reactor;

import com.davidcubesvk.yamlUpdater.Constants;
import com.davidcubesvk.yamlUpdater.block.Block;
import com.davidcubesvk.yamlUpdater.block.Section;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

public class Merger {

    private static final Merger MERGER = new Merger();
    private static final Yaml YAML = new Yaml();

    public static String merge(File current, File latest, Map<Object, Object> yamlMap, int indentationSpaces) {
        StringBuilder builder = new StringBuilder();
        Block oldDangling = (Block) current.getMappings().remove(null);
        Block newDangling = (Block) latest.getMappings().remove(null);
        MERGER.iterate(latest, yamlMap, current, builder, 0, indentationSpaces);
        if (oldDangling != null || newDangling != null) {
            if (oldDangling == null) {
                MERGER.appendBlock(builder, 0, indentationSpaces, newDangling);
            } else {
                MERGER.appendBlock(builder, 0, indentationSpaces, oldDangling);
            }
        }
        return builder.toString();
    }

    private void iterate(Section latestSection, Map<Object, Object> latestMap, Section currentSection, StringBuilder builder, int depth, int indentationSpaces) {
        //Go through all entries
        for (Map.Entry<String, Object> entry : latestSection.getMappings().entrySet()) {
            //If a block
            if (entry.getValue() instanceof Section) {
                //The section
                Section section = (Section) entry.getValue();
                //If there is not such a key in the current map
                if (currentSection == null || currentSection.getMappings() == null || !currentSection.getMappings().containsKey(entry.getKey())) {
                    //Append
                    appendBlock(builder, depth, indentationSpaces, section);
                    appendSection(builder, depth, indentationSpaces, (Map<Object, Object>) latestMap.get(entry.getKey()), section);
                    continue;
                }

                //Current object
                Block currentBlock = (Block) currentSection.getMappings().get(entry.getKey());
                //Append
                appendBlock(builder, depth, indentationSpaces, currentBlock);
                //If a section
                if (currentBlock instanceof Section) {
                    //Create a new map
                    Map<Object, Object> map = new HashMap<>();
                    //Update the map
                    latestMap.put(entry.getKey(), map);
                    //Iterate
                    iterate(section, map, (Section) currentBlock, builder, depth + 1, indentationSpaces);
                }
                else
                    //Update the map
                    latestMap.put(entry.getKey(), YAML.load(currentBlock.getValue().toString().trim().substring(1)));
                continue;
            }

            //The block
            Block latestBlock = (Block) entry.getValue();
            //If there is not such a key in the current map
            if (currentSection == null || currentSection.getMappings() == null || !currentSection.getMappings().containsKey(entry.getKey())) {
                //Append
                appendBlock(builder, depth, indentationSpaces, latestBlock);
                continue;
            }

            //Current object
            Block currentBlock = (Block) currentSection.getMappings().get(entry.getKey());
            //If a block
            if (currentBlock instanceof Section) {
                //Append
                appendBlock(builder, depth, indentationSpaces, currentBlock);
                //Create a new map
                Map<Object, Object> map = new HashMap<>();
                //Update the map
                latestMap.put(entry.getKey(), map);
                //Append
                appendSection(builder, depth, indentationSpaces, map, (Section) currentBlock);
            } else {
                //Update the map
                latestMap.put(entry.getKey(), YAML.load(currentBlock.getValue().toString().trim().substring(1)));
                //Append
                appendBlock(builder, depth, indentationSpaces, currentBlock);
            }
        }
    }

    private void appendSection(StringBuilder builder, int depth, int indentationSpaces, Map<Object, Object> map, Section section) {
        //Append comments
        appendSequence(builder, section.getComments(), depth * indentationSpaces);
        //Go through all entries
        for (Map.Entry<String, Object> entry : section.getMappings().entrySet()) {
            //If a section
            if (entry.getValue() instanceof Section) {
                //Create a new map
                Map<Object, Object> newMap = new HashMap<>();
                //Update the map
                map.put(entry.getKey(), newMap);
                //Append
                appendSection(builder, depth + 1, indentationSpaces, newMap, (Section) entry.getValue());
            } else if (entry.getValue() instanceof Block) {
                //Update the map
                map.put(entry.getKey(), YAML.load(((Block) entry.getValue()).getValue().toString().trim().substring(1)));
                //Append
                appendBlock(builder, depth + 1, indentationSpaces, (Block) entry.getValue());
            } else
                throw new IllegalArgumentException(String.format("Key object %s at path ...%s is not a block object. Please report the problem to our support.", entry.getValue().toString(), entry.getKey()));
        }
    }

    private void appendBlock(StringBuilder builder, int depth, int indentationSpaces, Block block) {
        //Calculate spaces
        int spaces = depth * indentationSpaces;
        //Append comments
        appendSequence(builder, block.getComments(), spaces);
        //If is a comment
        if (block.isComment())
            return;

        //Append key
        appendSequence(builder, block.getKey(), spaces);
        //Append value
        appendSequence(builder, block.getValue(), spaces);
    }

    public void appendSequence(StringBuilder builder, CharSequence sequence, int spaces) {
        //Go through all characters
        for (int index = 0; index < sequence.length(); index++) {
            //If previous character was a new line char
            if (builder.length() >= 1 && builder.charAt(builder.length() - 1) == Constants.NEW_LINE)
                //Append the indentation prefix
                for (int count = 0; count < spaces; count++)
                    builder.append(Constants.SPACE);

            //Char
            char c = sequence.charAt(index);
            //Append
            builder.append(c);
        }
    }

}