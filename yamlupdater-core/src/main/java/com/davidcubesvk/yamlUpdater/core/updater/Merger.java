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
import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Class responsible for merging the user file with the default file. Merging is the final stage of the updating process.
 */
public class Merger {

    /**
     * Merger instance for calling non-static methods.
     */
    private static final Merger MERGER = new Merger();

    /**
     * Merges the given sections, with the result being the given user section.
     * <p>
     * Merging algorithm consists of iterating through blocks in the default section and for each pair (user-default block)
     * outputs the preserved one (according to the merging rules) into the user section. If the preserved block is the
     * default one, deep copies it (so it is isolated from the defaults). If both blocks represent sections, iterates
     * through these pair of subsections.
     * <p>
     * Additionally, after iteration had finished, deletes all non-processed blocks (those ones which are not contained
     * in the defaults) from the user section, unless {@link UpdaterSettings#isCopyAll()} is enabled or if they are
     * marked as force copy ({@link Block#isCopy()}).
     *
     * @param userSection the user section
     * @param defSection  the default section equivalent to the user section
     * @param settings    updater settings used
     * @see #iterate(Section, Section, UpdaterSettings)
     */
    public static void merge(@NotNull Section userSection, @NotNull Section defSection, @NotNull UpdaterSettings settings) {
        MERGER.iterate(userSection, defSection, settings);
    }

    /**
     * Merges the given sections into the user section.
     * <p>
     * Merging algorithm consists of iterating through blocks in the default section and for each pair (user-default block)
     * outputs the preserved one (according to the merging rules) into the user section. If the preserved block is the
     * default one, deep copies it (so it is isolated from the defaults). If both blocks represent sections, iterates
     * through these pair of subsections.
     * <p>
     * Additionally, after iteration had finished, deletes all non-processed blocks (those ones which are not contained
     * in the defaults) from the user section, unless {@link UpdaterSettings#isCopyAll()} is enabled or if they are
     * marked as force copy ({@link Block#isCopy()}).
     *
     * @param userSection the user section
     * @param defSection  the default section equivalent to the user section
     * @param settings    updater settings used
     */
    private void iterate(Section userSection, Section defSection, UpdaterSettings settings) {
        //Keys
        Set<Object> userKeys = userSection.getKeys();

        //Loop through all default entries
        for (Map.Entry<Object, Block<?>> entry : defSection.getValue().entrySet()) {
            //Key
            Object key = entry.getKey();
            Path path = Path.fromSingleKey(key);
            //Delete
            userKeys.remove(key);
            //Blocks
            Block<?> userBlock = userSection.getBlockSafe(path).orElse(null), defBlock = entry.getValue();
            //If user block is present
            if (userBlock != null) {
                //If are sections
                boolean isUserBlockSection = userBlock instanceof Section, isDefBlockSection = defBlock instanceof Section;
                //If both are sections
                if (isDefBlockSection && isUserBlockSection) {
                    //Iterate
                    iterate((Section) userBlock, (Section) defBlock, settings);
                    continue;
                }

                //Set preserved value
                userSection.set(path, getPreservedValue(settings.getMergeRules(), userBlock, () -> cloneBlock(defBlock, userSection), isUserBlockSection, isDefBlockSection));
                continue;
            }

            //Set cloned
            userSection.set(path, cloneBlock(defBlock, userSection));
        }

        //If copy all is set to true
        if (settings.isCopyAll())
            return;

        //Loop through all default keys
        for (Object userKey : userKeys) {
            //Path
            Path path = Path.fromSingleKey(userKey);
            //If present
            userSection.getBlockSafe(path).ifPresent(block -> {
                //If force copy disabled
                if (!block.isCopy())
                    //Remove
                    userSection.remove(path);
            });
        }
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
     * @see #cloneMapping(Mapping, Section)
     */
    private Block<?> cloneBlock(Block<?> block, Section newParent) {
        return block instanceof Section ? cloneSection((Section) block, newParent) : cloneMapping((Mapping) block, newParent);
    }

    /**
     * Deep clones the given section.
     * <p>
     * More formally, represents the underlying map of the section into nodes and then, constructs them back into Java map.
     *
     * @param section   the section to clone
     * @param newParent new parent section of the section to clone
     * @return the cloned section (with relatives set already)
     */
    private Section cloneSection(Section section, Section newParent) {
        //Root
        YamlFile root = section.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new LibRepresenter(generalSettings, root.getDumperSettings().getSettings());
        //Create the constructor
        LibConstructor constructor = new LibConstructor(root.getLoaderSettings().getSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(section);
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        //Create
        section = new Section(newParent.getRoot(), newParent.isRoot() ? null : newParent.getParent(), section.getName(), section.getPath(), null, (MappingNode) constructor.getConstructed(represented), constructor);
        //Clear
        constructor.clear();
        //Create
        return section;
    }

    /**
     * Deep clones the given mapping.
     * <p>
     * More formally, represents the value of the mapping into nodes and then, constructs them back into Java object.
     *
     * @param mapping   the mapping to clone
     * @param newParent new parent section of the mapping to clone
     * @return the cloned mapping (with relatives set already)
     */
    private Mapping cloneMapping(Mapping mapping, Section newParent) {
        //Root
        YamlFile root = newParent.getRoot();
        //General settings
        GeneralSettings generalSettings = root.getGeneralSettings();

        //Create the representer
        BaseRepresenter representer = new LibRepresenter(generalSettings, root.getDumperSettings().getSettings());
        //Create the constructor
        LibConstructor constructor = new LibConstructor(root.getLoaderSettings().getSettings(generalSettings), generalSettings.getSerializer());
        //Represent
        Node represented = representer.represent(mapping.getValue());
        //Construct
        constructor.constructSingleDocument(Optional.of(represented));

        //Create
        mapping = new Mapping(mapping, constructor.getConstructed(represented));
        //Clear
        constructor.clear();
        //Return
        return mapping;
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
    private Block<?> getPreservedValue(Map<MergeRule, Boolean> rules, Block<?> userBlock, Supplier<Block<?>> defBlock, boolean userBlockIsSection, boolean defBlockIsSection) {
        return rules.get(MergeRule.getFor(userBlockIsSection, defBlockIsSection)) ? userBlock : defBlock.get();
    }


}