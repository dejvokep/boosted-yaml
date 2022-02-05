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
package dev.dejvokep.boostedyaml.dvs.versioning;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents manually supplied versioning information.
 */
public class ManualVersioning implements Versioning {

    //Versions
    private final Version documentVersion;
    private final Version defaultsVersion;

    /**
     * Creates manually supplied versioning information.
     * <p>
     * The given IDs are parsed immediately using the pattern; then supplied by the appropriate method implementations.
     * <p>
     * If the version ID of the defaults is invalid, it is considered illegal and will throw a {@link
     * NullPointerException}. For the document itself, if the version returned is <code>null</code>, the updater
     * assigns {@link #getFirstVersion()} to it.
     *
     * @param pattern           the pattern
     * @param documentVersionId the version ID of the document
     * @param defaultsVersionId the version ID of the defaults
     */
    public ManualVersioning(@NotNull Pattern pattern, @Nullable String documentVersionId, @NotNull String defaultsVersionId) {
        this.documentVersion = documentVersionId == null ? null : pattern.getVersion(documentVersionId);
        this.defaultsVersion = pattern.getVersion(defaultsVersionId);
    }

    @Nullable
    @Override
    public Version getDocumentVersion(@NotNull Section document, boolean defaults) {
        return defaults ? defaultsVersion : documentVersion;
    }

    @NotNull
    @Override
    public Version getFirstVersion() {
        return defaultsVersion.getPattern().getFirstVersion();
    }

    @Override
    public String toString() {
        return "ManualVersioning{" +
                "documentVersion=" + documentVersion +
                ", defaultsVersion=" + defaultsVersion +
                '}';
    }
}