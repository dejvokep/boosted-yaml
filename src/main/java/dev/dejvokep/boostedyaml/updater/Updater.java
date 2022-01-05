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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.fvs.Version;
import dev.dejvokep.boostedyaml.fvs.versioning.Versioning;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
     * @param generalSettings the general settings used to obtain the route separator, to split string-based relocations and force copy routes
     */
    public static void update(@NotNull Section userSection, @NotNull Section defSection, @NotNull UpdaterSettings updaterSettings, @NotNull GeneralSettings generalSettings) throws IOException {
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
     *     (see {@link Versioning#getFirstVersion()}). If provided, marks all blocks that should be kept
     *     (determined by the set of routes, see {@link UpdaterSettings#getKeep(char)}).</li>
     *     <li>If downgrading and it is enabled, does not proceed further. If disabled, throws an
     *     {@link UnsupportedOperationException}.</li>
     *     <li>If version IDs equal, does not proceed as well.</li>
     *     <li>Applies all relocations needed.</li>
     * </ol>
     *
     * @param userSection    the user section
     * @param defaultSection the default section equivalent
     * @param settings       updater settings to use
     * @param separator      the route separator, used to split string-based relocations and force copy routes
     */
    private void runVersionDependent(@NotNull Section userSection, @NotNull Section defaultSection, @NotNull UpdaterSettings settings, char separator) {
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
            user = versioning.getFirstVersion();

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

        // Keep routes
        Set<Route> keepRoutes = settings.getKeep(separator).getOrDefault(user.asID(), null);
        //If there are any keep routes
        if (keepRoutes != null)
            //Go through all keep routes
            for (Route route : keepRoutes)
                //Set
                userSection.getOptionalBlock(route).ifPresent(block -> block.setKeep(true));

        //Initialize relocator
        Relocator relocator = new Relocator(userSection, user, def);
        //Apply all
        relocator.apply(settings.getRelocations(separator));
    }

}