/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.route;

import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Factory used to build routes from string routes in bulk.
 * <p>
 * As string routes can also be used to access data, you should <b>never</b> convert string routes using this class,
 * except some situations where it is allowed.
 */
public class RouteFactory {

    //The separator
    private final char separator;
    //Escaped separator
    private final String escapedSeparator;

    /**
     * Creates a factory with the separator specified by the given settings.
     *
     * @param generalSettings settings from which to get the separator
     */
    public RouteFactory(@NotNull GeneralSettings generalSettings) {
        this.separator = generalSettings.getSeparator();
        this.escapedSeparator = generalSettings.getEscapedSeparator();
    }

    /**
     * Creates a factory with the given separator.
     *
     * @param separator separator to use
     */
    public RouteFactory(char separator) {
        this.separator = separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
    }


    /**
     * Creates a factory with the defaults specified by {@link GeneralSettings#DEFAULT_SEPARATOR} and {@link
     * GeneralSettings#DEFAULT_ESCAPED_SEPARATOR}.
     */
    public RouteFactory() {
        this.separator = GeneralSettings.DEFAULT_SEPARATOR;
        this.escapedSeparator = GeneralSettings.DEFAULT_ESCAPED_SEPARATOR;
    }

    /**
     * Constructs a route from the given string route, by splitting it by the factory's separator.
     * <p>
     * As string routes can also be used to access data, you should <b>never</b> convert string routes using this
     * method, except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming route <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create that certain route only once
     * and
     * then reuse it.</i>
     *
     * @param route the string route to split (in format <code>a.b</code> for factory separator <code>'.'</code> to
     *              create route <code>[a, b]</code>)
     * @return the immutable route
     */
    @NotNull
    public Route create(String route) {
        return Route.fromString(route, this);
    }

    /**
     * Returns the separator character.
     *
     * @return the separator character
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Returns the escaped version of {@link #getSeparator()}.
     *
     * @return the escaped separator
     */
    @NotNull
    public String getEscapedSeparator() {
        return escapedSeparator;
    }
}