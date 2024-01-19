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
package dev.dejvokep.boostedyaml.route.implementation;

import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a multi-key route.
 */
public class MultiKeyRoute implements Route {

    //Route
    private final Object[] route;

    /**
     * Constructs a route from the given <b>non-null</b> keys. The given keys must be <b>immutable</b>; if this cannot
     * be achieved, it is <b>required</b> that the caller never modifies them.
     * <p>
     * No keys or an empty array is considered illegal and will throw an {@link IllegalArgumentException}. As indicated,
     * <code>null</code> keys or attempt to pass a <code>null</code> array (to avoid varargs functionality, e.g.
     * <code>from((Object[]) null)</code>) will throw a {@link NullPointerException}.
     *
     * @param route the route keys
     * @see Route implementation information
     */
    public MultiKeyRoute(@NotNull Object... route) {
        //If empty
        if (Objects.requireNonNull(route, "Route array cannot be null!").length == 0)
            throw new IllegalArgumentException("Empty routes are not allowed!");
        //Validate
        for (Object key : route)
            Objects.requireNonNull(key, "Route cannot contain null keys!");

        //Set
        this.route = route;
    }

    @Override
    @NotNull
    public String join(char separator) {
        //Builder
        StringBuilder builder = new StringBuilder();
        //For each
        for (int i = 0; i < length(); i++)
            builder.append(get(i)).append(i + 1 < length() ? separator : "");
        //Return
        return builder.toString();
    }

    @Override
    public int length() {
        return route.length;
    }

    @Override
    @NotNull
    public Object get(int i) {
        return route[i];
    }

    @Override
    @NotNull
    public Route add(@NotNull Object key) {
        //New route
        Object[] route = Arrays.copyOf(this.route, this.route.length + 1);
        //Set
        route[route.length - 1] = Objects.requireNonNull(key, "Route cannot contain null keys!");
        //Return
        return new MultiKeyRoute(route);
    }

    @Override
    @NotNull
    public Route parent() {
        return route.length == 2 ? Route.from(route[0]) : Route.from(Arrays.copyOf(route, route.length - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route1 = (Route) o;
        if (this.length() != route1.length()) return false;
        if (this.length() == 1 && route1.length() == 1) return Objects.equals(this.get(0), route1.get(0));
        if (!(route1 instanceof MultiKeyRoute)) return false;
        return Arrays.equals(route, ((MultiKeyRoute) route1).route);
    }

    @Override
    public int hashCode() {
        return length() > 1 ? Arrays.hashCode(route) : Objects.hashCode(route[0]);
    }

    @Override
    public String toString() {
        return "MultiKeyRoute{" +
                "route=" + Arrays.toString(route) +
                '}';
    }
}