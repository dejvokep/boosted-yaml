package com.davidcubesvk.yamlUpdater.core.fvs.versioning;

import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.fvs.Version;
import org.jetbrains.annotations.NotNull;

/**
 * Represents versioning information supplier.
 */
public interface Versioning {

    /**
     * Returns version of the given user section (file; according to the implementation).
     * <p>
     * If the version ID (to parse from) is not available in the given file, or just, could not be obtained, returns
     * <code>null</code>. If the ID is present, but cannot be parsed, should throw an {@link IllegalArgumentException}.
     *
     * @param section the user section (file)
     * @return the version of the user section (file)
     */
    Version getUserSectionVersion(@NotNull Section section);

    /**
     * Returns version of the given default section (file; according to the implementation).
     * <p>
     * As this refers to default (unmodified, directly supplied by the developer) section, if returning <code>null</code>,
     * the section is considered malformed (defaults must have their version IDs properly specified).
     * <p>
     * If the ID is present, but cannot be parsed, should throw an {@link IllegalArgumentException} (which is also
     * considered illegal).
     *
     * @param section the default section (file)
     * @return the version of the default section (file)
     */
    Version getDefSectionVersion(@NotNull Section section);

    /**
     * Returns the oldest version specified by the underlying pattern.
     *
     * @return the oldest version
     */
    Version getOldest();

    /**
     * Sets version ID of the default section into the updated section content. Called only after successful update.
     * <p>
     * Should be implemented only if the underlying versioning supports file manipulation ({@link AutomaticVersioning}).
     *
     * @param updated the updated section
     * @param def     the default section equivalent
     */
    default void updateVersionID(@NotNull Section updated, @NotNull Section def) {
    }

}