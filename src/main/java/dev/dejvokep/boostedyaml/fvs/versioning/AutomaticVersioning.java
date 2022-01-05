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
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.fvs.Pattern;
import dev.dejvokep.boostedyaml.fvs.Version;
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
    public Version getFirstVersion() {
        return pattern.getFirstVersion();
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
        return (route != null ? section.getOptionalString(route) : section.getOptionalString(strRoute)).map(pattern::getVersion).orElse(null);
    }

}