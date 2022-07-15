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
package dev.dejvokep.boostedyaml.route.implementation;

import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a single-key route.
 */
public class SingleKeyRoute implements Route {

    //The key
    private final Object key;

    /**
     * Constructs a route from the given <b>non-null</b> key. The given key must be <b>immutable</b>; if this cannot be
     * achieved, it is <b>required</b> that the caller never modifies it.
     * <p>
     * As indicated, the given key cannot be <code>null</code>. Such attempt will result in a {@link
     * NullPointerException}.
     *
     * @param key the single key in the route
     * @see Route implementation information
     */
    public SingleKeyRoute(@NotNull Object key) {
        this.key = Objects.requireNonNull(key, "Route cannot contain null keys!");
    }

    @Override
    @NotNull
    public String join(char separator) {
        return key.toString();
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    @NotNull
    public Object get(int i) {
        //If out of range
        if (i != 0)
            throw new ArrayIndexOutOfBoundsException("Index " + i + " for single key route!");
        return key;
    }

    @Override
    @NotNull
    public Route parent() {
        throw new IllegalArgumentException("Empty routes are not allowed!");
    }

    @Override
    @NotNull
    public Route add(@NotNull Object key) {
        return Route.from(this.key, Objects.requireNonNull(key, "Route cannot contain null keys!"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route that = (Route) o;
        if (that.length() != 1) return false;
        return Objects.equals(key, that.get(0));
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "SingleKeyRoute{" +
                "key=" + key +
                '}';
    }
}