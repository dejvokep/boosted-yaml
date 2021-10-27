package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;
import org.jetbrains.annotations.NotNull;

/**
 * Represents automatically supplied versioning information.
 */
public class AutomaticVersioning implements Versioning {

    //Pattern
    private final Pattern pattern;
    //Path
    private final Path path;
    private final String strPath;

    /**
     * Creates automatically-supplied versioning information, which supplies versions (to the implementing methods)
     * automatically (dynamically) from the given sections using the given path and pattern.
     *
     * @param pattern the pattern used to parse the IDs found in the sections dynamically
     * @param path    the path to find the IDs at in the sections
     */
    public AutomaticVersioning(@NotNull Pattern pattern, @NotNull Path path) {
        this.pattern = pattern;
        this.path = path;
        this.strPath = null;
    }

    /**
     * Creates automatically-supplied versioning information, which supplies versions (to the implementing methods)
     * automatically (dynamically) from the given sections using the given path and pattern.
     *
     * @param pattern the pattern used to parse the IDs found in the sections dynamically
     * @param path    the path to find the IDs at in the sections
     */
    public AutomaticVersioning(@NotNull Pattern pattern, @NotNull String path) {
        this.pattern = pattern;
        this.path = null;
        this.strPath = path;
    }

    @Override
    public Version getDefSectionVersion(@NotNull Section section) {
        return getId(section);
    }

    @Override
    public Version getUserSectionVersion(@NotNull Section section) {
        return getId(section);
    }

    @Override
    public Version getOldest() {
        return pattern.getOldestVersion();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateVersionID(@NotNull Section updated, @NotNull Section def) {
        //If paths are used
        if (path != null)
            updated.set(path, def.getString(path));
        else
            updated.set(strPath, def.getString(strPath));
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
    @SuppressWarnings("ConstantConditions")
    private Version getId(Section section) throws IllegalArgumentException {
        return (path != null ? section.getStringSafe(path) : section.getStringSafe(strPath)).map(pattern::getVersion).orElse(null);
    }

    /**
     * Returns the path to the version ID.
     * @return the path to the version ID
     */
    public Path getPath() {
        return path;
    }
}