package com.davidcubesvk.yamlUpdater.core.route;

import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.route.implementation.MultiKeyRoute;
import com.davidcubesvk.yamlUpdater.core.route.implementation.SingleKeyRoute;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Route objects are URIs used to access data in {@link Section sections}.
 * <p>
 * Route objects are immutable, but handful of methods are provided to create derivations.
 */
public interface Route {

    /**
     * Constructs route from the given array of keys/key arguments, enabling usage of wide-range data types as keys. The
     * given keys
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * Empty array is considered illegal and will throw an {@link IllegalArgumentException}. Call with <code>null</code>
     * supplied as the route argument (e.g. <code>from((Object[]) null)</code>) is will throw a {@link
     * NullPointerException}.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming route <code>["x", 1]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>1</code> in <b>that</b> section.
     * <p>
     * If varargs format is used and there is only one argument, it will automatically be interpreted as call to {@link
     * #from(Object)}, saving time and memory consumption.
     * <p>
     * <b>If passing an array as the only key, do not forget to cast it to {@link Object}, otherwise it will be
     * interpreted as multi-key route according to the array's contents. Alternatively, to avoid confusion, use {@link
     * #fromSingleKey(Object)}.</b>
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create individual routes only once and
     * then reuse them.</i>
     *
     * @param route the route array
     * @return the immutable route
     */
    @NotNull
    static Route from(@NotNull Object... route) {
        //If empty
        if (route.length == 0)
            throw new IllegalArgumentException("Empty routes are not allowed!");
        // Create
        return route.length == 1 ? new SingleKeyRoute(route[0]) : new MultiKeyRoute(route);
    }

    /**
     * Constructs route from the given single key, enabling usage of wide-range data types as keys. The given key
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * Alternatively, to avoid confusion, use {@link #fromSingleKey(Object)}.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create individual routes only once and
     * then reuse them.</i>
     *
     * @param key the single element in the returned route
     * @return the immutable route
     * @see #fromSingleKey(Object) alias
     */
    @NotNull
    static Route from(@NotNull Object key) {
        return new SingleKeyRoute(key);
    }

    /**
     * Constructs route from the given single key, enabling usage of wide-range data types as keys. The given key
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * This method is an alias of {@link #from(Object)}.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create individual routes only once and
     * then reuse them.</i>
     *
     * @param key the single element in the returned route
     * @return the immutable route
     */
    @NotNull
    static Route fromSingleKey(@NotNull Object key) {
        return new SingleKeyRoute(key);
    }

    /**
     * Constructs a route from the given string route, by splitting it by {@link GeneralSettings#DEFAULT_SEPARATOR}.
     * <p>
     * To split using a custom separator, please use {@link #fromString(String, char)}.
     * <p>
     * As string routes can also be used to access data, you should <b>never</b> convert string routes using this method,
     * except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming route <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create that certain route only once
     * and then reuse it.</i>
     *
     * @param route the string route to split (in format <code>a.b</code> for separator <code>'.'</code> to create route
     *             <code>[a, b]</code>)
     * @return the immutable route
     */
    @NotNull
    static Route fromString(@NotNull String route) {
        return fromString(route, GeneralSettings.DEFAULT_SEPARATOR);
    }

    /**
     * Constructs a route from the given string route, by splitting it by the given separator.
     * <p>
     * Specifying the same separator again and again might sometimes violate the DRY principle - if that's the case, use
     * {@link RouteFactory} instead.
     * <p>
     * As string routes can also be used to access data, you should <b>never</b> convert string routes using this method,
     * except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming route <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create that certain route only once
     * and then reuse it.</i>
     *
     * @param route      the string route to split (in format <code>a.b</code> for separator <code>'.'</code> to create
     *                  route <code>[a, b]</code>)
     * @param separator separator to split the route by
     * @return the immutable route
     */
    @NotNull
    static Route fromString(@NotNull String route, char separator) {
        return route.indexOf(separator) != -1 ? new MultiKeyRoute((Object[]) route.split(Pattern.quote(String.valueOf(separator)))) : new SingleKeyRoute(route);
    }

    /**
     * Constructs a route from the given string route, by splitting it by separator supplied by the factory.
     * <p>
     * This is an alias, use {@link RouteFactory} instead.
     * <p>
     * As string routes can also be used to access data, you should <b>never</b> convert string routes using this method,
     * except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming route <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create that certain route only once
     * and then reuse it.</i>
     *
     * @param route        the string route to split (in format <code>a.b</code> for separator <code>'.'</code> to create
     *                    route <code>[a, b]</code>)
     * @param routeFactory supplies the separator to split the route by
     * @return the immutable route
     */
    @NotNull
    static Route fromString(@NotNull String route, @NotNull RouteFactory routeFactory) {
        return route.indexOf(routeFactory.getSeparator()) != -1 ? new MultiKeyRoute((Object[]) route.split(routeFactory.getEscapedSeparator())) : new SingleKeyRoute(route);
    }

    /**
     * Performs the same operation on the given route as {@link Route#add(Object)} (and returns the result); if the given
     * route is <code>null</code>, creates and returns a single-key route containing only the given key.
     * <p>
     * The given keys <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     *
     * @param route the route to add another key (element) to, or <code>null</code> to create new one
     * @param key   the key to add, or create a single key route from
     * @return the new route based on the given one with the given key added at the end, or a single key route according
     * to the documentation above
     * @see Route#add(Object)
     */
    @NotNull
    static Route addTo(@Nullable Route route, @NotNull Object key) {
        return route == null ? Route.from(key) : route.add(key);
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
     * Returns the length of the route (backing array) - amount of keys forming this route.
     *
     * @return the length
     */
    int length();

    /**
     * Returns key in this route (from the backing array), at the given position. Always verify if the index is within
     * the range of the backing array by calling {@link #length()}.
     *
     * @param i the index
     * @return the key at the given index
     */
    @NotNull
    Object get(int i);

    /**
     * Creates a new route, copies this route's backing array, adds the given key at the end and returns the new route
     * created from the new array.
     * <p>
     * <b>It is in the caller's best interest to never modify the objects given (and their contents), as it might cause
     * several issues (inequalities between routes...).</b>
     *
     * @param key the key to add
     * @return the new route with same keys as this one, except with the given key added at the end
     */
    @NotNull
    Route add(@NotNull Object key);

    /**
     * Returns the parent route of this one.
     * <p>
     * More formally, creates a new route and copies this route's backing array without the last element.
     * <p>
     * Please note that if this route's {@link #length()} is <code>1</code>, invoking this method will create an {@link
     * IllegalArgumentException}, as it is illegal to have empty routes. See more at {@link Route#from(Object...)}.
     *
     * @return the parent route of this one
     */
    @NotNull
    Route parent();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}