package com.davidcubesvk.yamlUpdater.core.path.implementation;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a single-key path.
 */
public class SingleKeyPath implements Path {

    //The key
    private final Object key;

    /**
     * Constructs path from the given single key, enabling usage of wide-range data types as keys. The given key
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create individual paths only once and
     * then reuse them.</i>
     *
     * @param key the single element in the path
     */
    public SingleKeyPath(@Nullable Object key) {
        this.key = key;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Object get(int i) {
        //If out of range
        if (i != 0)
            throw new ArrayIndexOutOfBoundsException("Index " + i + " for single key path!");
        return key;
    }

    @Override
    public Path parent() {
        throw new IllegalArgumentException("Empty paths are not allowed!");
    }

    @Override
    public Path add(@Nullable Object key) {
        return Path.from(this.key, key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;
        Path that = (Path) o;
        if (that.length() != 1) return false;
        return Objects.equals(key, that.get(0));
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}