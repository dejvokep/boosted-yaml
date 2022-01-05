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

import dev.dejvokep.boostedyaml.fvs.Pattern;
import dev.dejvokep.boostedyaml.fvs.segment.Segment;
import org.jetbrains.annotations.NotNull;

/**
 * Basic automatic versioning.
 */
public class BasicVersioning extends AutomaticVersioning {

    /**
     * Pattern used, only one segment from 1 to infinity.
     */
    public static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE));

    /**
     * Initializes the versioning with the given route.
     *
     * @param route the route at which version IDs can be found
     */
    public BasicVersioning(@NotNull String route) {
        super(PATTERN, route);
    }
}