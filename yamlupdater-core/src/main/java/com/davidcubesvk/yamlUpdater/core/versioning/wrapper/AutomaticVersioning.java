package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

/**
 * Represents automatically supplied versioning information.
 */
public class AutomaticVersioning implements Versioning {

    //Pattern
    private final Pattern pattern;
    //Path
    private final Path path;

    /**
     * Creates automatically-supplied versioning information, which supplies versions (to the implementing methods)
     * automatically (dynamically) from the given sections using the given path and pattern.
     *
     * @param pattern the pattern used to parse the IDs found in the sections dynamically
     * @param path    the path to find the IDs at in the sections
     */
    public AutomaticVersioning(Pattern pattern, Path path) {
        this.pattern = pattern;
        this.path = path;
    }

    @Override
    public Version getDefSectionVersion(Section section) {
        return getId(section);
    }

    @Override
    public Version getUserSectionVersion(Section section) {
        return getId(section);
    }

    @Override
    public Version getOldest() {
        return pattern.getOldestVersion();
    }

    /**
     * Returns the parsed version from the given section, at path and parsed using pattern given in the constructor.
     * <p>
     * If not a string is present at the path, returns <code>null</code>. If the found ID cannot be parsed using the
     * pattern, an {@link IllegalArgumentException} is thrown (see {@link Pattern#getVersion(String)}).
     *
     * @param section the section to get the version from
     * @return the version, or <code>null</code> if not found
     * @throws IllegalArgumentException if failed to parse the ID
     */
    private Version getId(Section section) throws IllegalArgumentException {
        return section.getStringSafe(path).map(pattern::getVersion).orElse(null);
    }

}