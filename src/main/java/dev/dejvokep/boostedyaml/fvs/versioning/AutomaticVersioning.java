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
import org.jetbrains.annotations.Nullable;

/**
 * Represents automatically supplied versioning information.
 */
public class AutomaticVersioning implements Versioning {

    //Pattern
    private final Pattern pattern;
    //Routes
    private final Route route;
    private final String strRoute;

    /**
     * Creates automatically-supplied versioning information.
     * <p>
     * The versions of the respective documents will be obtained and parsed at runtime.
     *
     * @param pattern the pattern used to parse the IDs
     * @param route   the route at which the IDs are
     */
    public AutomaticVersioning(@NotNull Pattern pattern, @NotNull Route route) {
        this.pattern = pattern;
        this.route = route;
        this.strRoute = null;
    }

    /**
     * Creates automatically-supplied versioning information.
     * <p>
     * The versions of the respective documents will be obtained and parsed at runtime.
     *
     * @param pattern the pattern used to parse the IDs
     * @param route   the route at which the IDs are
     */
    public AutomaticVersioning(@NotNull Pattern pattern, @NotNull String route) {
        this.pattern = pattern;
        this.route = null;
        this.strRoute = route;
    }

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public Version getDocumentVersion(@NotNull Section document, boolean defaults) {
        return (route != null ? document.getOptionalString(route) : document.getOptionalString(strRoute)).map(pattern::getVersion).orElse(null);

    }

    @NotNull
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

}