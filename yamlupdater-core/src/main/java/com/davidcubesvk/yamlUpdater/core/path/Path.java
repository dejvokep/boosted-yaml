package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.implementation.MultiKeyPath;
import com.davidcubesvk.yamlUpdater.core.path.implementation.SingleKeyPath;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Path objects are URIs used to access data in {@link Section sections}.
 * <p>
 * Path objects are immutable, but handful of methods are provided to create derivations.
 */
public interface Path {

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
     * @return the immutable path
     */
    static Path from(@Nullable Object... path) {
        //If null
        if (path == null)
            throw new NullPointerException("Path array cannot be null!");
        //If empty
        if (path.length == 0)
            throw new IllegalArgumentException("Empty paths are not allowed!");
        // Create
        return path.length == 1 ? new SingleKeyPath(path[0]) : new MultiKeyPath(path);
    }

    /**
     * Constructs path from the given single key, enabling usage of wide-range data types as keys. The given key
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create individual paths only once and
     * then reuse them.</i>
     *
     * @param key the single element in the returned path
     * @return the immutable path
     * @see #fromSingleKey(Object) alias
     */
    static Path from(@Nullable Object key) {
        return new SingleKeyPath(key);
    }

    /**
     * Constructs path from the given single key, enabling usage of wide-range data types as keys. The given key
     * <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     * <p>
     * This method is an alias of {@link #from(Object)}.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create individual paths only once and
     * then reuse them.</i>
     *
     * @param key the single element in the returned path
     * @return the immutable path
     */
    static Path fromSingleKey(@Nullable Object key) {
        return new SingleKeyPath(key);
    }

    /**
     * Constructs a path from the given string path, by splitting it by {@link GeneralSettings#DEFAULT_SEPARATOR}.
     * <p>
     * To split using a custom separator, please use {@link #fromString(String, char)}.
     * <p>
     * As string paths can also be used to access data, you should <b>never</b> convert string paths using this
     * method, except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming path <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path the string path to split (in format <code>a.b</code> for separator <code>'.'</code> to create path <code>[a, b]</code>)
     * @return the immutable path
     */
    static Path fromString(@NotNull String path) {
        return fromString(path, GeneralSettings.DEFAULT_SEPARATOR);
    }

    /**
     * Constructs a path from the given string path, by splitting it by the given separator.
     * <p>
     * Specifying the same separator again and again might sometimes violate the DRY principle - if that's the case, use
     * {@link PathFactory} instead.
     * <p>
     * As string paths can also be used to access data, you should <b>never</b> convert string paths using this
     * method, except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming path <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path      the string path to split (in format <code>a.b</code> for separator <code>'.'</code> to create path <code>[a, b]</code>)
     * @param separator separator to split the path by
     * @return the immutable path
     */
    static Path fromString(@NotNull String path, char separator) {
        return path.indexOf(separator) != -1 ? new MultiKeyPath((Object[]) path.split(Pattern.quote(String.valueOf(separator)))) : new SingleKeyPath(path);
    }

    /**
     * Constructs a path from the given string path, by splitting it by separator supplied by the factory.
     * <p>
     * This is an alias, use {@link PathFactory} instead.
     * <p>
     * As string paths can also be used to access data, you should <b>never</b> convert string paths using this
     * method, except some situations where it is allowed.
     * <p>
     * The given keys are traversed in order as they were specified. Assuming path <code>["x", "y"]</code>, processor
     * attempts to get section at key <code>"x"</code> in the section from which the getter/setter... method was called;
     * and then value at key <code>"y"</code> in <b>that</b> section.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path        the string path to split (in format <code>a.b</code> for separator <code>'.'</code> to create path <code>[a, b]</code>)
     * @param pathFactory supplies the separator to split the path by
     * @return the immutable path
     */
    static Path fromString(@NotNull String path, @NotNull PathFactory pathFactory) {
        return path.indexOf(pathFactory.getSeparator()) != -1 ? new MultiKeyPath((Object[]) path.split(pathFactory.getEscapedSeparator())) : new SingleKeyPath(path);
    }

    /**
     * Performs the same operation on the given path as {@link Path#add(Object)} (and returns the result); if the given
     * path is <code>null</code>, creates and returns a single-key path containing only the given key.
     * <p>
     * The given keys <b>should</b> be immutable; otherwise, it is <b>required</b> that the caller never modifies them.
     *
     * @param path the path to add another key (element) to, or <code>null</code> to create new one
     * @param key  the key to add, or create a single key path from
     * @return the new path based on the given one with the given key added at the end, or a single key path according
     * to the documentation above
     * @see Path#add(Object)
     */
    static Path addTo(@Nullable Path path, @Nullable Object key) {
        return path == null ? Path.from(key) : path.add(key);
    }

    /**
     * Returns the length of the path (backing array) - amount of keys forming this path.
     *
     * @return the length
     */
    int length();

    /**
     * Returns key in this path (from the backing array), at the given position. Always verify if the index is within
     * the range of the backing array by calling {@link #length()}.
     *
     * @param i the index
     * @return the key at the given index
     */
    Object get(int i);

    /**
     * Creates a new path, copies this path's backing array, adds the given key at the end and returns the new path
     * created from the new array.
     * <p>
     * <b>It is in the caller's best interest to never modify the objects given (and their contents), as it might cause
     * several issues (inequalities between paths...).</b>
     *
     * @param key the key to add
     * @return the new path with same keys as this one, except with the given key added at the end
     */
    Path add(@Nullable Object key);

    /**
     * Returns the parent path of this one.
     * <p>
     * More formally, creates a new path and copies this path's backing array without the last element.
     * <p>
     * Please note that if this path's {@link #length()} is <code>1</code>, invoking this method will create an
     * {@link IllegalArgumentException}, as it is illegal to have empty paths. See more at {@link Path#from(Object...)}.
     *
     * @return the parent path of this one
     */
    Path parent();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}