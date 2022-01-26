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
 * Represents versioning information supplier.
 */
public interface Versioning {

    /**
     * Returns version of the given user section (file; according to the implementation).
     * <p>
     * If the version ID (to parse from) could not be obtained or parsed, returns <code>null</code>.
     *
     * @param section the user section (file)
     * @return the version of the user section (file)
     */
    @Nullable
    Version getUserSectionVersion(@NotNull Section section);

    /**
     * Returns version of the given default section (file; according to the implementation).
     * <p>
     * As this refers to default (unmodified, directly supplied by the developer) section, if returning
     * <code>null</code>, the section is considered malformed (defaults must have their version IDs properly
     * specified).
     *
     * @param section the default section (file)
     * @return the version of the default section (file)
     */
    @Nullable
    Version getDefSectionVersion(@NotNull Section section);

    /**
     * Returns the first version specified by the used pattern.
     *
     * @return the first version
     * @see Pattern#getFirstVersion()
     */
    @NotNull
    Version getFirstVersion();

    /**
     * Sets version ID of the default section into the updated section content. Called only after successful update.
     * <p>
     * Should be used only if the underlying versioning implementation supports file manipulation ({@link
     * AutomaticVersioning}).
     *
     * @param updated the updated section
     * @param def     the default section equivalent
     */
    default void updateVersionID(@NotNull Section updated, @NotNull Section def) {
    }

}