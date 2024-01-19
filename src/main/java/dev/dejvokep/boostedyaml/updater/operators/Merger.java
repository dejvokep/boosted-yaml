/*
 * Copyright 2024 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.updater.operators;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.implementation.TerminatedBlock;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.engine.ExtendedConstructor;
import dev.dejvokep.boostedyaml.engine.ExtendedRepresenter;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.updater.MergeRule;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;

import java.util.*;
import java.util.function.Supplier;

/**
 * Merger is the last of the updating process operators, responsible for merging the document with the defaults.
 */
public class Merger {

    /**
     * Instance for calling non-static methods.
     */
    private static final Merger INSTANCE = new Merger();

    /**
     * Merges the given document with the defaults.
     * <p>
     * Merging algorithm consists of iterating through blocks in the defaults and for each pair (document+default block)
     * outputs the preserved one (according to the merging rules) into the document. If the block is ignored,
     * immediately continues without changing the block. If the preserved block is the default one, deep copies it (so
     * it is isolated from the defaults). If both blocks represent sections, iterates through these pair of
     * subsections.
     * <p>
     * Additionally, after iteration had finished, deletes all non-merged blocks (those which are not contained in the
     * defaults) from the document, unless {@link UpdaterSettings#isKeepAll()} is enabled.
     *
     * @param document the document
     * @param defaults the default equivalent to the document
     * @param settings updater settings to use
     * @see #iterate(Section, Section, UpdaterSettings)
     */
    public static void merge(@NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings settings) {
        INSTANCE.iterate(document, defaults, settings);
    }

    /**
     * Merges the given document with the defaults.
     * <p>
     * Merging algorithm consists of iterating through blocks in the defaults and for each pair (document+default block)
     * outputs the preserved one (according to the merging rules) into the document. If the block is ignored,
     * immediately continues without changing the block. If the preserved block is the default one, deep copies it (so
     * it is isolated from the defaults). If both blocks represent sections, iterates through these pair of
     * subsections.
     * <p>
     * Additionally, after iteration had finished, deletes all non-merged blocks (those which are not contained in the
     * defaults) from the document, unless {@link UpdaterSettings#isKeepAll()} is enabled.
     *
     * @param document the document
     * @param defaults the default equivalent to the document
     * @param settings updater settings to use
     */
    private void iterate(Section document, Section defaults, UpdaterSettings settings) {
        //Keys
        Set<Object> documentKeys = new HashSet<>(document.getStoredValue().keySet());
        //Sorting
        boolean sort = settings.getOptionSorting() == UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS;
        Map<Object, Block<?>> sorted = sort ? document.getRoot().getGeneralSettings().getDefaultMap() : null;

        //Loop through all default entries
        for (Map.Entry<Object, Block<?>> entry : defaults.getStoredValue().entrySet()) {
            //Key
            Object key = entry.getKey();
            Route route = Route.from(key);
            //Delete
            documentKeys.remove(key);
            //Blocks
            Block<?> documentBlock = document.getOptionalBlock(route).orElse(null), defaultBlock = entry.getValue();
            //If document block is present
            if (documentBlock != null) {
                //If ignored
                if (documentBlock.isIgnored()) {
                    //Reset
                    documentBlock.setIgnored(false);
                    //If a section
                    if (documentBlock instanceof Section)
                        resetIgnored((Section) documentBlock);

                    //If sorting
                    if (sort)
                        sorted.put(key, documentBlock);
                    continue;
                }

                //If are sections
                boolean isDocumentBlockSection = documentBlock instanceof Section, isDefaultBlockSection = defaultBlock instanceof Section;
                //If both are sections
                if (isDefaultBlockSection && isDocumentBlockSection) {
                    //Iterate
                    iterate((Section) documentBlock, (Section) defaultBlock, settings);

                    //If sorting
                    if (sort)
                        sorted.put(key, documentBlock);
                    continue;
                }

                //Set preserved value
                if (sort)
                    sorted.put(key, getPreservedValue(settings.getMergeRules(), documentBlock, () -> cloneBlock(defaultBlock, document), isDocumentBlockSection, isDefaultBlockSection));
                else
                    document.set(route, getPreservedValue(settings.getMergeRules(), documentBlock, () -> cloneBlock(defaultBlock, document), isDocumentBlockSection, isDefaultBlockSection));
                continue;
            }

            //Set cloned
            if (sort)
                sorted.put(key, cloneBlock(defaultBlock, document));
            else
                document.set(route, cloneBlock(defaultBlock, document));
        }

        //If to keep all
        if (settings.isKeepAll()) {
            //If sorting
            if (sort) {
                //Add remaining
                documentKeys.forEach(key -> sorted.put(key, document.getStoredValue().get(key)));
                //Repopulate
                document.repopulate(sorted);
            }
            return;
        }

        //Loop through all document keys
        for (Object key : documentKeys) {
            //Route
            Route route = Route.fromSingleKey(key);
            //Block
            Block<?> block = document.getOptionalBlock(route).orElse(null);
            //If ignored
            if (block != null && block.isIgnored()) {
                //Reset
                block.setIgnored(false);
                //If a section
                if (block instanceof Section)
                    resetIgnored((Section) block);

                //If sorting
                if (sort)
                    sorted.put(key, block);
                continue;
            }

            //Remove if not sorting
            if (!sort)
                document.remove(route);
        }

        //Repopulate
        if (sort)
            document.repopulate(sorted);
    }

    /**
     * Resets ignored setting for sub-blocks of the given section.
     *
     * @param section the section
     */
    private void resetIgnored(@NotNull Section section) {
        //Iterate
        section.getStoredValue().values().forEach(block -> {
            //Reset
            block.setIgnored(false);
            //If a section
            if (block instanceof Section)
                resetIgnored((Section) block);
        });
    }

    /**
     * Deep clones the given block.
     * <p>
     * More formally, represents the value of the block into nodes and then, constructs them back into Java objects.
     *
     * @param block     the block to clone
     * @param newParent new parent section of the block to clone
     * @return the cloned block (with relatives set already)
     * @see #cloneSection(Section, Section)
     * @see #cloneTerminated(TerminatedBlock, Section)
     */
    @NotNull
    private Block<?> cloneBlock(@NotNull Block<?> block, @NotNull Section newParent) {
        return block instanceof Section ? cloneSection((Section) block, newParent) : cloneTerminated((TerminatedBlock) block, newParent);
    }

    /**
     * Deep clones the given section.
     * <p>
     * More formally, represents the underlying map of the section into nodes and then, constructs them back into Java
     * map.
     *
     * @param section   the section to clone
     * @param newParent new parent section of the section to clone
     * @return the cloned section (with relatives set already)
     */
    @NotNull
    private Section cloneSection(@NotNull Section section, @NotNull Section newParent) {
        //If is the root
        if (section.getRoute() == null)
            throw new IllegalArgumentException("Cannot clone the root!");
        //Root
        YamlDocument root = section.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(generalSettings, root.getDumperSettings());
        //Create the constructor
        ExtendedConstructor constructor = new ExtendedConstructor(root.getLoaderSettings().buildEngineSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(section);
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        //Create
        section = new Section(newParent.getRoot(), newParent, section.getRoute(), moveComments(represented), (MappingNode) represented, constructor);
        //Clear
        constructor.clear();
        //Create
        return section;
    }

    /**
     * Deep clones the given terminated block.
     * <p>
     * More formally, represents the value of the entry into nodes and then, constructs them back into a Java object.
     *
     * @param entry     the entry to clone
     * @param newParent new parent section of the entry to clone
     * @return the cloned entry (with relatives set already)
     */
    @NotNull
    private TerminatedBlock cloneTerminated(@NotNull TerminatedBlock entry, @NotNull Section newParent) {
        //Root
        YamlDocument root = newParent.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(generalSettings, root.getDumperSettings());
        //Create the constructor
        ExtendedConstructor constructor = new ExtendedConstructor(root.getLoaderSettings().buildEngineSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(entry.getStoredValue());
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        //Create
        entry = new TerminatedBlock(entry, constructor.getConstructed(represented));
        //Clear
        constructor.clear();
        //Return
        return entry;
    }

    /**
     * Creates a dummy node to which comments from the given node are moved. That is, the comments attached to the given
     * node will be removed.
     *
     * @param node the node to move comments from
     * @return a new, dummy node with comments from the given node
     */
    private Node moveComments(@NotNull Node node) {
        // Dummy node
        ScalarNode scalarNode = new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN);
        // Move
        scalarNode.setBlockComments(node.getBlockComments());
        scalarNode.setInLineComments(node.getInLineComments());
        scalarNode.setEndComments(node.getEndComments());
        // Delete
        node.setBlockComments(Collections.emptyList());
        node.setInLineComments(null);
        node.setEndComments(null);
        // Return
        return scalarNode;
    }

    /**
     * Returns the preserved block as defined by the given merge rules, blocks and information.
     *
     * @param rules                  the merge rules
     * @param documentBlock          block in the document which is merged
     * @param defaultBlock           block equivalent in the defaults
     * @param documentBlockIsSection if block in the document is a section
     * @param defaultBlockIsSection  if block in the defaults is a section
     * @return the preserved block
     */
    @NotNull
    private Block<?> getPreservedValue(@NotNull Map<MergeRule, Boolean> rules, @NotNull Block<?> documentBlock, @NotNull Supplier<Block<?>> defaultBlock, boolean documentBlockIsSection, boolean defaultBlockIsSection) {
        return rules.get(MergeRule.getFor(documentBlockIsSection, defaultBlockIsSection)) ? documentBlock : defaultBlock.get();
    }


}