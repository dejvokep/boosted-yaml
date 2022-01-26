/*
 * Copyright 2021 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.updater;

import dev.dejvokep.boostedyaml.YamlFile;
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
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;

import java.util.*;
import java.util.function.Supplier;

/**
 * Class responsible for merging the user file with the default file. Merging is the final stage of the updating
 * process.
 */
public class Merger {

    /**
     * Merger instance for calling non-static methods.
     */
    private static final Merger instance = new Merger();

    /**
     * Merges the given sections, with the result being the given user section.
     * <p>
     * Merging algorithm consists of iterating through blocks in the default section and for each pair (user-default
     * block) outputs the preserved one (according to the merging rules) into the user section. If the block is ignored,
     * immediately continues without changing the block. If the preserved block is the default one, deep copies it (so
     * it is isolated from the defaults). If both blocks represent sections, iterates through these pair of
     * subsections.
     * <p>
     * Additionally, after iteration had finished, deletes all non-merged blocks (those which are not contained in the
     * defaults) from the user section, unless {@link UpdaterSettings#isKeepAll()} is enabled.
     *
     * @param userSection the user section
     * @param defSection  the default section equivalent to the user section
     * @param settings    updater settings used
     * @see #iterate(Section, Section, UpdaterSettings)
     */
    public static void merge(@NotNull Section userSection, @NotNull Section defSection, @NotNull UpdaterSettings settings) {
        instance.iterate(userSection, defSection, settings);
    }

    /**
     * Merges the given sections into the user section.
     * <p>
     * Merging algorithm consists of iterating through blocks in the default section and for each pair (user-default
     * block) outputs the preserved one (according to the merging rules) into the user section. If the block is ignored,
     * immediately continues without changing the block. If the preserved block is the default one, deep copies it (so
     * it is isolated from the defaults). If both blocks represent sections, iterates through these pair of
     * subsections.
     * <p>
     * Additionally, after iteration had finished, deletes all non-merged blocks (those which are not contained in the
     * defaults) from the user section, unless {@link UpdaterSettings#isKeepAll()} is enabled.
     *
     * @param userSection the user section
     * @param defSection  the default section equivalent to the user section
     * @param settings    updater settings used
     */
    private void iterate(Section userSection, Section defSection, UpdaterSettings settings) {
        //Keys
        Set<Object> userKeys = userSection.getKeys();

        //Loop through all default entries
        for (Map.Entry<Object, Block<?>> entry : defSection.getStoredValue().entrySet()) {
            //Key
            Object key = entry.getKey();
            Route route = Route.from(key);
            //Delete
            userKeys.remove(key);
            //Blocks
            Block<?> userBlock = userSection.getOptionalBlock(route).orElse(null), defBlock = entry.getValue();
            //If user block is present
            if (userBlock != null) {
                //If ignored
                if (userBlock.isIgnored()) {
                    //Reset
                    userBlock.setIgnored(false);
                    continue;
                }

                //If are sections
                boolean isUserBlockSection = userBlock instanceof Section, isDefBlockSection = defBlock instanceof Section;
                //If both are sections
                if (isDefBlockSection && isUserBlockSection) {
                    //Iterate
                    iterate((Section) userBlock, (Section) defBlock, settings);
                    continue;
                }

                //Set preserved value
                userSection.set(route, getPreservedValue(settings.getMergeRules(), userBlock, () -> cloneBlock(defBlock, userSection), isUserBlockSection, isDefBlockSection));
                continue;
            }

            //Set cloned
            userSection.set(route, cloneBlock(defBlock, userSection));
        }

        //If to keep all
        if (settings.isKeepAll())
            return;

        //Loop through all default keys
        for (Object userKey : userKeys)
            //Remove
            userSection.remove(Route.fromSingleKey(userKey));
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
        YamlFile root = section.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(generalSettings, root.getDumperSettings().buildEngineSettings());
        //Create the constructor
        ExtendedConstructor constructor = new ExtendedConstructor(root.getLoaderSettings().buildEngineSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(section);
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        //Create
        section = new Section(newParent.getRoot(), newParent, section.getRoute(), null, (MappingNode) represented, constructor);
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
        YamlFile root = newParent.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(generalSettings, root.getDumperSettings().buildEngineSettings());
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
     * Returns the preserved block as defined by the given merge rules, blocks and information.
     *
     * @param rules              the merge rules
     * @param userBlock          block in the user file
     * @param defBlock           block equivalent in the default file
     * @param userBlockIsSection if user block is a section
     * @param defBlockIsSection  if default block is a section
     * @return the preserved block
     */
    @NotNull
    private Block<?> getPreservedValue(@NotNull Map<MergeRule, Boolean> rules, @NotNull Block<?> userBlock, @NotNull Supplier<Block<?>> defBlock, boolean userBlockIsSection, boolean defBlockIsSection) {
        return rules.get(MergeRule.getFor(userBlockIsSection, defBlockIsSection)) ? userBlock : defBlock.get();
    }


}