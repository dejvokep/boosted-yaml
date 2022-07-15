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
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.updater.operators.Mapper;
import dev.dejvokep.boostedyaml.updater.operators.Relocator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A utility class which runs version-dependant operations while updating a {@link Section document}.
 */
public class VersionedOperations {

    /**
     * Runs version-dependent operations.
     * <ol>
     *     <li>If {@link UpdaterSettings#getVersioning()} is <code>null</code>, does not proceed and returns <code>false</code>.</li>
     *     <li>If the version of the document is not provided (is <code>null</code>), assigns the first version specified by the underlying pattern
     *     (see {@link Versioning#getFirstVersion()}) and continues.</li>
     *     <li>If downgrading and it is disabled, throws an {@link UnsupportedOperationException}.</li>
     *     <li>If version IDs equal, it is an indications that the document is already up-to-date. Does not proceed and returns <code>true</code>.</li>
     *     <li>If upgrading, applies all relocations and mappers within the defined version range.</li>
     *     <li>Marks all ignored blocks.</li>
     *     <li>Returns <code>true</code>.</li>
     * </ol>
     *
     * @param document  the document section that's being updated
     * @param defaults  section equivalent in the defaults
     * @param settings  updater settings to use
     * @param separator the route separator, used to parse string routes
     * @return if the document is already up-to-date (does not require further manipulation), <code>false</code>
     * otherwise (further manipulation - merging and postprocessing is mandatory)
     */
    public static boolean run(@NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings settings, char separator) {
        //Versioning
        Versioning versioning = settings.getVersioning();
        //If the versioning is not set
        if (versioning == null)
            return false;

        //Versions
        Version documentVersion = versioning.getDocumentVersion(document, false), defaultsVersion = Objects.requireNonNull(versioning.getDocumentVersion(defaults, true), "Version ID of the defaults cannot be null! Is it malformed or not specified?");

        //Compare (or force update if not found)
        int compared = documentVersion != null ? documentVersion.compareTo(defaultsVersion) : -1;
        //If downgrading
        if (compared > 0 && !settings.isEnableDowngrading())
            //Throw an error
            throw new UnsupportedOperationException(String.format("Downgrading is not enabled (%s > %s)!", defaultsVersion.asID(), documentVersion.asID()));

        //No update needed
        if (compared == 0)
            return true;

        //If not downgrading
        if (compared < 0)
            // Apply relocations and mappings
            iterate(document, documentVersion != null ? documentVersion : versioning.getFirstVersion(), defaultsVersion, settings, separator);

        //Ignored routes
        settings.getIgnoredRoutes(defaultsVersion.asID(), separator).forEach(route ->
                document.getOptionalBlock(route).ifPresent(block -> block.setIgnored(true)));
        return false;
    }

    /**
     * Iterates all versions after the version of the document, but before or the version of the defaults (mathematical
     * notation: <code>(document, defaults></code>); while applying the appropriate relocations and mappers.
     *
     * @param document        the document section that's being updated
     * @param documentVersion version of the document
     * @param defaultsVersion version of the defaults
     * @param settings        updater settings to use
     * @param separator       the route separator, used to parse string routes
     */
    private static void iterate(@NotNull Section document, @NotNull Version documentVersion, @NotNull Version defaultsVersion, @NotNull UpdaterSettings settings, char separator) {
        //Copy
        Version current = documentVersion.copy();
        //While not at the latest version
        while (current.compareTo(defaultsVersion) <= 0) {
            //Move to the next version
            current.next();
            //Apply
            Relocator.apply(document, settings.getRelocations(current.asID(), separator));
            Mapper.apply(document, settings.getMappers(current.asID(), separator));
            //Run logic
            settings.getCustomLogic(current.asID()).forEach(consumer -> consumer.accept(document.getRoot()));
        }
    }

}