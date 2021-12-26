package com.davidcubesvk.yamlUpdater.core.route.implementation;

import com.davidcubesvk.yamlUpdater.core.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a single-key route.
 */
public class SingleKeyRoute implements Route {

    //The key
    private final Object key;

    /**
     * Constructs route from the given single key, enabling usage of wide-range data types as keys. The given key
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * The given key cannot be <code>null</code>.
     * <p>
     * <i>As routes are immutable objects, to save resources, it is recommended to create individual routes only once and
     * then reuse them.</i>
     *
     * @param key the single element in the route
     */
    public SingleKeyRoute(@NotNull Object key) {
        //Validate
        Objects.requireNonNull(key, "Route cannot contain null keys!");
        //Set
        this.key = key;
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
        //Validate
        Objects.requireNonNull(key, "Route cannot contain null keys!");
        //Return
        return Route.from(this.key, key);
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
}