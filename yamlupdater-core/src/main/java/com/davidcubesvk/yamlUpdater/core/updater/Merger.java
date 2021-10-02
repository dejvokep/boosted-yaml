package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Mapping;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.engine.LibConstructor;
import com.davidcubesvk.yamlUpdater.core.engine.LibRepresenter;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.MergeRule;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

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
     * @param userFile        the disk (old, to be updated) file
     * @param resourceFile    the resource (latest) file
     * @return the merged file as a string
     */
    public static void merge(YamlFile userFile, YamlFile resourceFile, UpdaterSettings settings, Set<Path> forceCopy) {
        MERGER.iterate(userFile, resourceFile, settings, forceCopy);
    }

    private void iterate(Section userSection, Section defSection, UpdaterSettings settings, Set<Path> forceCopy) {
        //Keys
        Set<Object> userKeys = userSection.getKeys();
        System.out.println("DEFAULT KEYS " + defSection.getKeys());

        //Loop through all default entries
        for (Map.Entry<Object, Block<?>> entry : defSection.getValue().entrySet()) {
            System.out.println("MERGER key:" + entry.getKey());
            //Key
            Object key = entry.getKey();
            //Delete
            userKeys.remove(key);
            //Blocks
            Block<?> userBlock = userSection.getBlockSafe(key).orElse(null), defBlock = entry.getValue();
            //If user block is present
            if (userBlock != null) {
                //If are sections
                boolean isUserBlockSection = userBlock instanceof Section, isDefBlockSection = defBlock instanceof Section;
                //If both are sections
                if (isDefBlockSection && isUserBlockSection) {
                    //Iterate
                    iterate((Section) userBlock, (Section) defBlock, settings, forceCopy);
                    continue;
                }

                System.out.println(entry.getKey() + ": USER=" + userBlock + " (value=" + userBlock.getValue() + "), DEFAULT=" + defBlock + " (value=" + defBlock.getValue() + ")");
                //Set preserved value
                userSection.set(key, getPreservedValue(settings.getMergeRules(), userBlock, () -> cloneBlock(defBlock, userSection), isUserBlockSection, isDefBlockSection));
                System.out.println("merge rule preserve user=" + (settings.getMergeRules().get(MergeRule.getFor(isUserBlockSection, isDefBlockSection))));
                System.out.println(entry.getKey() + " preserving " + userSection.get(key) + userSection.getBlockSafe(key).get().getValue());
                System.out.println();
                continue;
            }

            //Set cloned
            userSection.set(key, cloneBlock(defBlock, userSection));
        }

        //If copy all is set to true
        if (settings.isForceCopyAll())
            return;

        //Loop through all default keys
        for (Object userKey : userKeys) {
            //If force copy disabled
            if (!forceCopy.contains(userSection.getPath().add(userKey)))
                //Remove
                userSection.remove(userKey);
        }
    }

    private Block<?> cloneBlock(Block<?> block, Section newParent) {
        return block instanceof Section ? cloneSection((Section) block, newParent) : cloneMapping((Mapping) block, newParent);
    }

    private Section cloneSection(Section section, Section newParent) {
        //Root
        YamlFile root = section.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new LibRepresenter(root.getDumperSettings().getSettings(), generalSettings.getSerializer());
        //Create the constructor
        LibConstructor constructor = new LibConstructor(root.getLoaderSettings().getSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(section);
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        //Create
        return new Section(newParent.getRoot(), newParent.isRoot() ? null : newParent.getParent(), section.getName(), section.getPath(), null, (MappingNode) constructor.getConstructed().get(represented), constructor);
    }

    private Mapping cloneMapping(Mapping mapping, Section newParent) {
        //Root
        YamlFile root = newParent.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new LibRepresenter(root.getDumperSettings().getSettings(), generalSettings.getSerializer());
        //Create the constructor
        LibConstructor constructor = new LibConstructor(root.getLoaderSettings().getSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(mapping.getValue());
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        System.out.println("Cloning Mapping: " + mapping + " with value=" + mapping.getValue() + " to=" + constructor.getConstructed().get(represented));
        //Create
        return new Mapping(mapping, constructor.getConstructed().get(represented));
    }

    private Block<?> getPreservedValue(Map<MergeRule, Boolean> rules, Block<?> userValue, Supplier<Block<?>> defaultValue, boolean userValueIsSection, boolean defaultValueIsSection) {
        return rules.get(MergeRule.getFor(userValueIsSection, defaultValueIsSection)) ? userValue : defaultValue.get();
    }


}