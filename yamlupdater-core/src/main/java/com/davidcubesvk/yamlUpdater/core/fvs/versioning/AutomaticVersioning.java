package com.davidcubesvk.yamlUpdater.core.fvs.versioning;

import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.fvs.Pattern;
import com.davidcubesvk.yamlUpdater.core.fvs.Version;
import org.jetbrains.annotations.NotNull;

/**
 * Represents automatically supplied versioning information.
 */
public class AutomaticVersioning implements Versioning {

    //Pattern
    private final Pattern pattern;
    //Route
    private final Route route;
    private final String strRoute;

    /**
     * Creates automatically-supplied versioning information, which supplies versions (to the implementing methods)
     * automatically (dynamically) from the given sections using the given route and pattern.
     *
     * @param pattern the pattern used to parse the IDs found in the sections dynamically
     * @param route    the route to find the IDs at in the sections
     */
    public AutomaticVersioning(@NotNull Pattern pattern, @NotNull Route route) {
        this.pattern = pattern;
        this.route = route;
        this.strRoute = null;
    }

    /**
     * Creates automatically-supplied versioning information, which supplies versions (to the implementing methods)
     * automatically (dynamically) from the given sections using the given route and pattern.
     *
     * @param pattern the pattern used to parse the IDs found in the sections dynamically
     * @param route    the route to find the IDs at in the sections
     */
    public AutomaticVersioning(@NotNull Pattern pattern, @NotNull String route) {
        this.pattern = pattern;
        this.route = null;
        this.strRoute = route;
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
        //If routes are used
        if (route != null)
            updated.set(route, def.getString(route));
        else
            updated.set(strRoute, def.getString(strRoute));
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
        return (route != null ? section.getStringSafe(route) : section.getStringSafe(strRoute)).map(pattern::getVersion).orElse(null);
    }

}