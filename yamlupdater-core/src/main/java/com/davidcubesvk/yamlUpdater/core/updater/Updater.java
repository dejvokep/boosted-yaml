package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.Versioning;

import java.util.Map;
import java.util.Objects;

/**
 * Updater class responsible for executing the whole process:
 * <ol>
 *     <li>loading file version IDs</li>
 *     <li>comparing IDs (to check if updating, downgrading...)</li>
 *     <li>marking force copy blocks in the user section</li>
 *     <li>applying relocations to the user section (if the files are not the same version ID) - see {@link Relocator#apply(Map)}</li>
 *     <li>merging both files - see {@link Merger#merge(Section, Section, UpdaterSettings)}</li>
 * </ol>
 */
public class Updater {

    /**
     * Updater instance for calling non-static methods.
     */
    private static final Updater UPDATER = new Updater();

    /**
     * Updates the given user section using the given default equivalent and settings; with the result reflected in the
     * user section given. The process consists of:
     * <ol>
     *     <li>loading file version IDs,</li>
     *     <li>comparing IDs (to check if updating, downgrading...),</li>
     *     <li>marking force copy blocks in the user section,</li>
     *     <li>applying relocations to the user section (if the files are not the same version ID) - see {@link Relocator#apply(Map)}),</li>
     *     <li>merging both files - see {@link Merger#merge(Section, Section, UpdaterSettings)}.</li>
     * </ol>
     *
     * @param userSection     the user section to update
     * @param defSection      section equivalent in the default file (to update against)
     * @param updaterSettings the updater settings
     * @param generalSettings the general settings used to obtain the path separator, to split string-based relocations and force copy paths
     */
    public static void update(Section userSection, Section defSection, UpdaterSettings updaterSettings, GeneralSettings generalSettings) {
        //Apply versioning stuff
        UPDATER.runVersionDependent(userSection, defSection, updaterSettings, generalSettings.getSeparator());
        //Merge
        Merger.merge(userSection, defSection, updaterSettings);
        //If present
        if (updaterSettings.getVersioning() != null)
            //Set the new ID
            updaterSettings.getVersioning().updateVersionID(userSection, defSection);

        //If auto save is enabled
        if (updaterSettings.isAutoSave())
            userSection.getRoot().save();
    }

    /**
     * Runs version-dependent mechanics.
     * <ol>
     *     <li>If {@link UpdaterSettings#getVersioning()} is <code>null</code>, does not proceed.</li>
     *     <li>If the version of the user (section, file) is not provided (is <code>null</code>;
     *     {@link Versioning#getUserSectionVersion(Section)}), assigns the oldest version specified by the underlying pattern
     *     (see {@link Versioning#getOldest()}). If provided, marks all blocks that should be kept
     *     (determined by the set of paths, see {@link UpdaterSettings#getKeep(char)}).</li>
     *     <li>If downgrading and it is enabled, does not proceed further. If disabled, throws an
     *     {@link UnsupportedOperationException}.</li>
     *     <li>If version IDs equal, does not proceed as well.</li>
     *     <li>Applies all relocations needed.</li>
     * </ol>
     *
     * @param userSection    the user section
     * @param defaultSection the default section equivalent
     * @param settings       updater settings to use
     * @param separator      the path separator, used to split string-based relocations and force copy paths
     */
    private void runVersionDependent(Section userSection, Section defaultSection, UpdaterSettings settings, char separator) {
        //Versioning
        Versioning versioning = settings.getVersioning();
        //If the versioning is not set
        if (versioning == null)
            return;

        //Versions
        Version user = versioning.getUserSectionVersion(userSection), def = versioning.getDefSectionVersion(defaultSection);
        //Check default file version
        Objects.requireNonNull(def, "Version ID of the default file cannot be null!");
        //If user ID is null
        if (user == null)
            //Set to the oldest (to go through all relocations supplied)
            user = versioning.getOldest();

        //Compare
        int compared = user.compareTo(def);
        //If downgrading
        if (compared > 0) {
            //If enabled
            if (settings.isEnableDowngrading())
                return;

            //Throw an error
            throw new UnsupportedOperationException(String.format("Downgrading is not enabled (%s > %s)!", def.asID(), user.asID()));
        }

        //No update needed
        if (compared == 0)
            return;

        //Go through all force copy paths
        for (Path path : settings.getKeep(separator).get(user.asID()))
            //Set
            userSection.getBlockSafe(path).ifPresent(block -> block.setCopy(true));

        //Initialize relocator
        Relocator relocator = new Relocator(userSection, user, def);
        //Apply all
        relocator.apply(settings.getRelocations(separator));
    }

}