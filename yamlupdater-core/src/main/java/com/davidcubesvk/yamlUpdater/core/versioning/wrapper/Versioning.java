package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

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
    Version getUserSectionVersion(Section section);

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
    Version getDefSectionVersion(Section section);

    /**
     * Returns the oldest version specified by the underlying pattern.
     *
     * @return the oldest version
     */
    Version getOldest();

}