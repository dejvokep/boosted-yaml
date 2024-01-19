/*
 * Copyright 2024 https://dejvokep.dev/
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
 * Factory used to build {@link Route routes} from string routes with the same separator in bulk.
 * <p>
 * <i>Please note that string routes can also be used as {@link Route} objects, therefore you should not introduce
 * additional overhead by converting them using methods provided by this class, unless necessarily needed.</i>
 */
public class RouteFactory {

    //Separator
    private final char separator;
    private final String escapedSeparator;

    /**
     * Creates a factory with the given setting's {@link GeneralSettings#getRouteSeparator() separator}.
     *
     * @param generalSettings provider of the separator to use
     */
    public RouteFactory(@NotNull GeneralSettings generalSettings) {
        this.separator = generalSettings.getRouteSeparator();
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
     * Creates a factory with the default separator defined by {@link GeneralSettings#DEFAULT_ROUTE_SEPARATOR}.
     */
    public RouteFactory() {
        this.separator = GeneralSettings.DEFAULT_ROUTE_SEPARATOR;
        this.escapedSeparator = GeneralSettings.DEFAULT_ESCAPED_SEPARATOR;
    }

    /**
     * Constructs a route by splitting the given string route by the {@link #getSeparator() factory's separator}.
     * <p>
     * For example, giving string route <code>"a.b"</code> will return the equivalent route containing 2 keys:
     * <code>["a", "b"]</code>.
     * <p>
     * <i>Please note that string routes can also be used as {@link Route} objects, therefore you should not introduce
     * additional overhead by converting them using methods provided by this class, unless necessarily needed.</i>
     *
     * @param route the string route to split
     * @return the route
     * @see Route implementation information
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
     * Returns the escaped {@link #getSeparator() separator}.
     *
     * @return the escaped {@link #getSeparator() separator}
     */
    @NotNull
    public String getEscapedSeparator() {
        return escapedSeparator;
    }
}