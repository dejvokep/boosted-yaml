/*
 * Copyright 2022 https://dejvokep.dev/
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
import dev.dejvokep.boostedyaml.dvs.Version;
import dev.dejvokep.boostedyaml.dvs.versioning.Versioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

/**
 * Updater class responsible for executing the whole process:
 * <ol>
 *     <li>loading file version IDs,</li>
 *     <li>comparing IDs (to check if updating, downgrading...),</li>
 *     <li>applying relocations to the document (if the files are not the same version ID) - see {@link Relocator#apply(UpdaterSettings, char)},</li>
 *     <li>marking ignored blocks in the document,</li>
 *     <li>merging both files - see {@link Merger#merge(Section, Section, UpdaterSettings)}.</li>
 * </ol>
 */
public class Updater {

    /**
     * Updater instance for calling non-static methods.
     */
    private static final Updater instance = new Updater();

    /**
     * Updates the given document against the given defaults and settings. The process consists of:
     * <ol>
     *     <li>loading file version IDs,</li>
     *     <li>comparing IDs (to check if updating, downgrading...),</li>
     *     <li>applying relocations to the document (if the files are not the same version ID) - see {@link Relocator#apply(UpdaterSettings, char)},</li>
     *     <li>marking ignored blocks in the document,</li>
     *     <li>merging both files - see {@link Merger#merge(Section, Section, UpdaterSettings)}.</li>
     * </ol>
     *
     * @param document        the document section to update
     * @param defaults        section equivalent in the defaults
     * @param updaterSettings updater settings to use
     * @param generalSettings general settings to use
     * @throws IOException an IO error
     */
    public static void update(@NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings updaterSettings, @NotNull GeneralSettings generalSettings) throws IOException {
        //Apply versioning stuff
        if (instance.runVersionDependent(document, defaults, updaterSettings, generalSettings.getRouteSeparator()))
            return;
        //Merge
        Merger.merge(document, defaults, updaterSettings);
        //If present
        if (updaterSettings.getVersioning() != null)
            //Set the new ID
            updaterSettings.getVersioning().updateVersionID(document, defaults);

        //If auto save is enabled
        if (updaterSettings.isAutoSave())
            document.getRoot().save();
    }

    /**
     * Runs version-dependent mechanics.
     * <ol>
     *     <li>If {@link UpdaterSettings#getVersioning()} is <code>null</code>, does not proceed.</li>
     *     <li>If the version of the document is not provided (is <code>null</code>), assigns the oldest version specified by the underlying pattern
     *     (see {@link Versioning#getFirstVersion()}).</li>
     *     <li>If downgrading and it is disabled, throws an
     *     {@link UnsupportedOperationException}.</li>
     *     <li>If version IDs equal, does not proceed.</li>
     *     <li>If upgrading, applies all relocations needed.</li>
     *     <li>Marks all ignored blocks.</li>
     * </ol>
     *
     * @param document  the document section that's being updated
     * @param defaults  section equivalent in the defaults
     * @param settings  updater settings to use
     * @param separator the route separator, used to split string-based relocations and force copy routes
     * @return if the document is already up-to-date, <code>false</code> otherwise
     */
    private boolean runVersionDependent(@NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings settings, char separator) {
        //Versioning
        Versioning versioning = settings.getVersioning();
        //If the versioning is not set
        if (versioning == null)
            return false;

        //Versions
        Version documentVersion = versioning.getDocumentVersion(document, false), defaultsVersion = versioning.getDocumentVersion(defaults, true);
        //Check default file version
        Objects.requireNonNull(defaultsVersion, "Version ID of the default file cannot be null! Is it malformed or not specified?");
        //If document ID is null
        if (documentVersion == null)
            //Set to the oldest (to go through all relocations supplied)
            documentVersion = versioning.getFirstVersion();

        //Compare
        int compared = documentVersion.compareTo(defaultsVersion);
        //If downgrading
        if (compared > 0 && !settings.isEnableDowngrading())
            //Throw an error
            throw new UnsupportedOperationException(String.format("Downgrading is not enabled (%s > %s)!", defaultsVersion.asID(), documentVersion.asID()));

        //No update needed
        if (compared == 0)
            return true;

        //If not downgrading
        if (compared < 0) {
            //Initialize relocator
            Relocator relocator = new Relocator(document, documentVersion, defaultsVersion);
            //Apply all
            relocator.apply(settings, separator);
        }

        //Ignored routes
        for (Route route : settings.getIgnoredRoutes(defaultsVersion.asID(), separator))
            document.getOptionalBlock(route).ifPresent(block -> block.setIgnored(true));
        return false;
    }

}