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
package dev.dejvokep.boostedyaml.dvs.versioning;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents versioning information supplier.
 */
public interface Versioning {

    /**
     * Returns version of the given document (section).
     * <p>
     * If obtaining version of the defaults (parameter is set to <code>true</code>), returning <code>null</code> is
     * considered illegal and will throw an exception.
     *
     * @param document the document
     * @param defaults if getting version of the defaults
     * @return the version corresponding to the document, or <code>null</code> if it could not be obtained/parsed
     */
    @Nullable
    Version getDocumentVersion(@NotNull Section document, boolean defaults);

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