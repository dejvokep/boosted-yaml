package com.davidcubesvk.yamlUpdater.core.path.implementation;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a multi-key path.
 */
public class MultiKeyPath implements Path {

    //Path
    private final Object[] path;

    /**
     * Constructs path from the given array of keys/key arguments, enabling usage of wide-range data types as keys. The given keys
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * Empty array is considered illegal and will throw an {@link IllegalArgumentException}. Call with <code>null</code>
     * supplied as the path argument (e.g. <code>from((Object[]) null)</code>) is will throw a {@link NullPointerException}.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming path <code>["x", 1]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>1</code> in <b>that</b> section.
     * <p>
     * If varargs format is used and there is only one argument, it will automatically be interpreted as call to
     * {@link #from(Object)}, saving time and memory consumption.
     * <p>
     * <b>If passing an array as the only key, do not forget to cast it to {@link Object}, otherwise it will be
     * interpreted as multi-key path according to the array's contents. Alternatively, to avoid confusion, use {@link #fromSingleKey(Object)}</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create individual paths only once and
     * then reuse them.</i>
     *
     * @param path the path array
     */
    public MultiKeyPath(@Nullable Object... path) {
        //If null
        if (path == null)
            throw new NullPointerException("Path array cannot be null!");
        //If empty
        if (path.length == 0)
            throw new IllegalArgumentException("Empty paths are not allowed!");

        //Set
        this.path = path;
    }

    @Override
    public int length() {
        return path.length;
    }

    @Override
    public Object get(int i) {
        return path[i];
    }

    @Override
    public Path add(@Nullable Object key) {
        //New path
        Object[] path = Arrays.copyOf(this.path, this.path.length + 1);
        //Set
        path[path.length - 1] = key;
        //Return
        return new MultiKeyPath(path);
    }

    @Override
    public Path parent() {
        return path.length == 2 ? Path.from(path[0]) : Path.from(Arrays.copyOf(path, path.length - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;
        Path path1 = (Path) o;
        if (this.length() != path1.length()) return false;
        if (this.length() == 1 && path1.length() == 1) return Objects.equals(this.get(0), path1.get(0));
        if (!(path1 instanceof MultiKeyPath)) return false;
        return Arrays.equals(path, ((MultiKeyPath) path1).path);
    }

    @Override
    public int hashCode() {
        return length() > 1 ? Arrays.hashCode(path) : Objects.hashCode(path[0]);
    }

}