package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.Versioning;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

import java.util.HashSet;
import java.util.Set;

/**
 * Class responsible for reacting everything together.
 */
public class Updater {

    private static final Set<Path> EMPTY_PATH_SET = new HashSet<>();

    /**
     * Reacts and updates a file per the given settings object. Full reacting consists of:
     * <ol>
     *     <li>loading (or creating if does not exist) the disk file,</li>
     *     <li>parsing both files using SnakeYAML,</li>
     *     <li>getting file versions from the files,</li>
     *     <li>parsing both files,</li>
     *     <li>applying relocations (see {@link Relocator}),</li>
     *     <li>merging together (see {@link Merger}),</li>
     * </ol>
     *
     * @param settings     the settings
     * @return the updated file
     * @throws NullPointerException   if disk or resource file path is not set
     * @throws ClassCastException     if an object failed to cast (usually compatibility problem)
     */
    public static void update(YamlFile user, YamlFile def, UpdaterSettings settings) throws NullPointerException, ClassCastException {
        if (user == null || def == null )
            throw new NullPointerException("User, default file or updater settings are null!");

        //Apply versioning stuff
        Version defVersion = applyVersioning(user, def, settings);
        //Merge
        Merger.merge(user, def, settings, defVersion == null ? EMPTY_PATH_SET : settings.getForceCopy().getOrDefault(defVersion.asID(), EMPTY_PATH_SET));
        //If auto save is enabled
        if (settings.isAutoSave())
            user.save();
    }

    private static Version applyVersioning(YamlFile userFile, YamlFile defaultFile, UpdaterSettings settings) {
        //Versioning
        Versioning versioning = settings.getVersioning();
        //If the versioning is not set
        if (versioning == null)
            return null;

        //Versions
        Version user = versioning.getUserFileId(userFile), def = versioning.getDefaultFileId(defaultFile);
        //If user ID is not null
        if (user != null) {
            //Go through all force copy paths
            for (Path path : settings.getForceCopy().get(user.asID()))
                //Set
                userFile.getBlockSafe(path).ifPresent(block -> block.setForceCopy(true));
        } else {
            //Set to oldest (to go through all relocations supplied)
            user = versioning.getOldest();
        }

        //Compare
        int compared = user.compareTo(def);
        //If downgrading
        if (compared > 0) {
            //If enabled
            if (settings.isEnableDowngrading())
                return def;

            throw new UnsupportedOperationException(String.format("Downgrading is not enabled (%s > %s)!", def.asID(), user.asID()));
        }

        //No relocating needed
        if (compared == 0)
            return def;

        //Initialize relocator
        Relocator relocator = new Relocator(userFile, user, def);
        //Apply all
        relocator.apply(settings.getRelocations());
        return def;
    }

}