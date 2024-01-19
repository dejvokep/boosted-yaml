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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.implementation.MultiKeyRoute;
import dev.dejvokep.boostedyaml.route.implementation.SingleKeyRoute;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Routes are {@link java.net.URI URI}-like objects used to access and modify data stored in {@link Section sections},
 * backed by a simple array.
 * <p>
 * Routes are immutable objects - it is recommended to create individual objects only once and reuse them. Route's keys
 * are traversed in order as given: assuming route <code>["x", 1]</code>, the processing method ({@link Section}
 * getter/setter...) attempts to obtain {@link Section section} at key <code>"x"</code> in the {@link Section section}
 * from which the method was called; and then value at key <code>1</code> in <b>that</b> {@link Section section}.
 */
public interface Route {

    /**
     * Constructs a route from the given <b>non-null</b> keys. The given keys must be <b>immutable</b>; if this cannot
     * be achieved, it is <b>required</b> that the caller never modifies them.
     * <p>
     * No keys or an empty array is considered illegal and will throw an {@link IllegalArgumentException}. As indicated,
     * <code>null</code> keys or attempt to pass a <code>null</code> array (to avoid varargs functionality, e.g.
     * <code>from((Object[]) null)</code>) will throw a {@link NullPointerException}.
     * <p>
     * <b>If passing an array as the only key,</b> do not forget to cast it to {@link Object}, or use {@link
     * #fromSingleKey(Object)} instead.
     *
     * @param route the route keys
     * @return the route
     * @see Route implementation information
     */
    @NotNull
    static Route from(@NotNull Object... route) {
        // If empty
        if (Objects.requireNonNull(route, "Route array cannot be null!").length == 0)
            throw new IllegalArgumentException("Empty routes are not allowed!");
        // Create
        return route.length == 1 ? new SingleKeyRoute(route[0]) : new MultiKeyRoute(route);
    }

    /**
     * Constructs a route from the given <b>non-null</b> key. The given key must be <b>immutable</b>; if this cannot be
     * achieved, it is <b>required</b> that the caller never modifies it.
     * <p>
     * As indicated, the given key cannot be <code>null</code>. Such attempt will result in a {@link
     * NullPointerException}.
     *
     * @param key the single key in the route
     * @return the single key route
     * @see #fromSingleKey(Object) alias
     * @see Route implementation information
     */
    @NotNull
    static Route from(@NotNull Object key) {
        return new SingleKeyRoute(key);
    }

    /**
     * Constructs a route from the given <b>non-null</b> key. The given key must be <b>immutable</b>; if this cannot be
     * achieved, it is <b>required</b> that the caller never modifies it.
     * <p>
     * As indicated, the given key cannot be <code>null</code>. Such attempt will result in a {@link
     * NullPointerException}.
     *
     * @param key the single key in the route
     * @return the single key route
     * @see Route implementation information
     */
    @NotNull
    static Route fromSingleKey(@NotNull Object key) {
        return new SingleKeyRoute(key);
    }

    /**
     * Constructs a route by splitting the given string route by {@link GeneralSettings#DEFAULT_ROUTE_SEPARATOR}. To
     * split using a custom separator, use {@link #fromString(String, char)} instead.
     * <p>
     * For example, giving string route <code>"a.b"</code> will return the equivalent route containing 2 keys:
     * <code>["a", "b"]</code>.
     * <p>
     * <i>Please note that string routes can also be used as {@link Route} objects, therefore you should not introduce
     * additional overhead by converting them using methods provided by this class, unless necessarily needed.</i>
     *
     * @param route the string route to split
     * @return the route
     * @see #fromString(String, char)
     * @see Route implementation information
     */
    @NotNull
    static Route fromString(@NotNull String route) {
        return fromString(route, GeneralSettings.DEFAULT_ROUTE_SEPARATOR);
    }

    /**
     * Constructs a route by splitting the given string route by the provided separator.
     * <p>
     * For example, giving string route <code>"a/b"</code> and separator <code>/</code> will return the equivalent route
     * containing 2 keys - <code>["a", "b"]</code>.
     * <p>
     * <i>Please note that string routes can also be used as {@link Route} objects, therefore you should not introduce
     * additional overhead by converting them using methods provided by this class, unless necessarily needed. If that's
     * the case for multiple routes, you can use {@link RouteFactory} to abstract the separator.</i>
     *
     * @param route     the string route to split
     * @param separator separator to split the route by
     * @return the route
     * @see #fromString(String, char)
     * @see Route implementation information
     */
    @NotNull
    static Route fromString(@NotNull String route, char separator) {
        return route.indexOf(separator) != -1 ? new MultiKeyRoute((Object[]) route.split(Pattern.quote(String.valueOf(separator)))) : new SingleKeyRoute(route);
    }

    /**
     * Constructs a route by splitting the given string route by the provided factory's {@link
     * RouteFactory#getSeparator() separator}.
     * <p>
     * For example, giving string route <code>"a/b"</code> and separator <code>/</code> will return the equivalent route
     * containing 2 keys - <code>["a", "b"]</code>.
     * <p>
     * <i>Please note that string routes can also be used as {@link Route} objects, therefore you should not introduce
     * additional overhead by converting them using methods provided by this class, unless necessarily needed.</i>
     *
     * @param route        the string route to split
     * @param routeFactory provider of the separator
     * @return the route
     * @see #fromString(String, char)
     * @see Route implementation information
     */
    @NotNull
    static Route fromString(@NotNull String route, @NotNull RouteFactory routeFactory) {
        return route.indexOf(routeFactory.getSeparator()) != -1 ? new MultiKeyRoute((Object[]) route.split(routeFactory.getEscapedSeparator())) : new SingleKeyRoute(route);
    }

    /**
     * Adds the given <code>non-null</code> key to the route and returns the new object, if the route is
     * <code>null</code>, creates and returns a single key route containing only the key. The given key must be
     * <b>immutable</b>; if this cannot be achieved, it is <b>required</b> that the caller never modifies it.
     * <p>
     * For example, if you add key <code>1</code> to route <code>["a", "b"]</code>, the resulting route will be
     * represented by <code>["a", "b", 1]</code>. However, if you add the same key to a <code>null</code> route, the
     * result will be a single key route equivalent to <code>[1]</code>.
     * <p>
     * More formally, if a <code>non-null</code> route is given, copies this route's backing array (keys), adds the
     * given key at the end and returns the new route.
     *
     * @param route route to add the key to, or <code>null</code> to create a single key route
     * @param key   the key to add
     * @return the new route with same keys as this one, except with the given key added as the last one
     * @see Route#addTo(Route, Object)
     */
    @NotNull
    static Route addTo(@Nullable Route route, @NotNull Object key) {
        return route == null ? Route.fromSingleKey(key) : route.add(key);
    }

    /**
     * Joins the route's keys with the given separator.
     *
     * @param separator the separator to join with
     * @return the joined route
     */
    @NotNull
    String join(char separator);

    /**
     * Returns the length of the route (backing array) - amount of keys forming the route.
     *
     * @return the length
     */
    int length();

    /**
     * Returns key at the given index. Always verify if the requested index is within the range of the backing array's
     * {@link #length() length}.
     *
     * @param i the index
     * @return the key at the given index
     */
    @NotNull
    Object get(int i);

    /**
     * Adds the given <code>non-null</code> key to the route and returns the new object. The given key must be
     * <b>immutable</b>; if this cannot be achieved, it is <b>required</b> that the caller never modifies it.
     * <p>
     * For example, if you add key <code>1</code> to route <code>["a", "b"]</code>, the resulting route will be
     * represented by <code>["a", "b", 1]</code>.
     * <p>
     * More formally, copies this route's backing array (keys), adds the given key at the end and returns the new
     * route.
     *
     * @param key the key to add
     * @return the new route with same keys as this one, except with the given key added as the last one
     */
    @NotNull
    Route add(@NotNull Object key);

    /**
     * Returns the parent route of this one. More formally, creates a new route and copies this route's backing array
     * without the last element.
     * <p>
     * Per documentation of {@link Route#from(Object...)}, invoking this method if <code>{@link #length()} == 1</code>
     * will throw an {@link IllegalArgumentException}.
     *
     * @return the parent route
     */
    @NotNull
    Route parent();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}