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
package dev.dejvokep.boostedyaml.fvs.versioning;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.fvs.Pattern;
import dev.dejvokep.boostedyaml.fvs.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents manually supplied versioning information.
 */
public class ManualVersioning implements Versioning {

    //Versions
    private final Version userSectionVersion;
    private final Version defSectionVersion;

    /**
     * Creates manually-supplied versioning information, which parses the given IDs using the pattern straight away, then
     * supplies the (already) parsed versions to the implementing methods.
     *
     * @param pattern              the pattern
     * @param userSectionVersionId the version ID of the user section (file) matching the given pattern
     * @param defSectionVersionId  the version ID of the default section (file) matching the given pattern
     * @throws IllegalArgumentException if either of the given IDs do not match the given pattern (see {@link Pattern#getVersion(String)} for more information)
     */
    public ManualVersioning(@NotNull Pattern pattern, @Nullable String userSectionVersionId, @NotNull String defSectionVersionId) throws IllegalArgumentException {
        this.userSectionVersion = userSectionVersionId == null ? null : pattern.getVersion(userSectionVersionId);
        this.defSectionVersion = pattern.getVersion(defSectionVersionId);
    }

    @Override
    public Version getDefSectionVersion(@NotNull Section section) {
        return defSectionVersion;
    }

    @Override
    public Version getUserSectionVersion(@NotNull Section section) {
        return userSectionVersion;
    }

    @Override
    public Version getFirstVersion() {
        return defSectionVersion.getPattern().getFirstVersion();
    }
}