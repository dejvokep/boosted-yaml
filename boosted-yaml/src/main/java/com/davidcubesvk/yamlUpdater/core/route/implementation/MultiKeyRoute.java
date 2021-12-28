package com.davidcubesvk.yamlUpdater.core.route.implementation;

import com.davidcubesvk.yamlUpdater.core.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a multi-key route.
 */
public class MultiKeyRoute implements Route {

    //Route
    private final Object[] route;

    /**
     * Constructs route from the given array of keys/key arguments, enabling usage of wide-range data types as keys. The
     * given keys <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * The given array cannot contain <code>null</code> keys.
     * <p>
     * Empty array is considered illegal and will throw an {@link IllegalArgumentException}. Call with <code>null</code>
     * supplied as the route argument (e.g. <code>from((Object[]) null)</code>) is will throw a {@link
     * NullPointerException}.
     * <p>
     * The given keys are traversed in order as they were specified - just like folders: assuming route <code>["x",
     * 1]</code>, processor attempts to get section at key <code>"x"</code> in the section from which the
     * getter/setter... method was called; and then value at key <code>1</code> in <b>that</b> section.
     * <p>
     * If varargs format is used and there is only one argument, it will automatically be interpreted as call to {@link
     * #from(Object)}, saving time and memory consumption.
     * <p>
     * <b>If passing an array as the only key, do not forget to cast it to {@link Object}, otherwise it will be
     * interpreted as multi-key route according to the array's contents. Alternatively, to avoid confusion, use {@link
     * #fromSingleKey(Object)}</b>
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create individual routes only once
     * and then reuse them.</i>
     *
     * @param route the route array
     */
    public MultiKeyRoute(@NotNull Object... route) {
        //Validate
        Objects.requireNonNull(route, "Route array cannot be null!");
        //If empty
        if (route.length == 0)
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
        //Validate
        Objects.requireNonNull(key, "Route cannot contain null keys!");
        //New route
        Object[] route = Arrays.copyOf(this.route, this.route.length + 1);
        //Set
        route[route.length - 1] = key;
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