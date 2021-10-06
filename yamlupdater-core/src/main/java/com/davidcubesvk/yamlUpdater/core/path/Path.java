package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Path {

    /**
     * Path with one single key, which is equal to <code>null</code>.
     */
    public static final Path NULL_KEY = Path.fromSingleKey(null);

    //Path
    private final Object[] path;

    /**
     * Constructs path from the given non-empty array/arguments. <b>It is in the caller's best interest to never modify the
     * objects given (and their contents), as it might cause several issues (inequalities between objects...).
     * <p>
     * If the given array/arguments is (one) <code>null</code>, initializes the backing array with that of {@link #NULL_KEY}.
     * <p>
     * Although this is a private constructor, arguments are patched so this constructor won't be able to construct
     * illegal paths (empty ones).
     *
     * @param path the path array
     */
    private Path(@Nullable Object... path) {
        //If null
        if (path == null) {
            //Set
            this.path = NULL_KEY.path;
            return;
        }

        //If empty
        if (path.length == 0)
            throw new IllegalArgumentException("Empty paths are not allowed!");

        //Set
        this.path = path;
    }

    /**
     * Constructs the path array from the given string path, by splitting it by {@link GeneralSettings#DEFAULT_SEPARATOR}.
     * <p>
     * To split using custom separator, please use {@link #Path(String, char)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, to maintain sustainability with
     * Spigot/BungeeCord API. However, if you don't wanna use array-based paths at all, just use any of the
     * {@link Section} methods based on {@link Object} path, which also accept
     * full string paths under some circumstances. Learn more about this behaviour at
     * {@link Section#getBlockSafe(Object)}.</b>
     *
     * @param path the string path to split (in format <code>a.b</code> for separator <code>.</code>)
     */
    private Path(@NotNull String path) {
        this.path = path.split(GeneralSettings.DEFAULT_ESCAPED_SEPARATOR);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by the given separator.
     * <p>
     * As path objects should be created only once and then reused (thanks for immutability and to save resources) - e.g.
     * should be stored in <code>static final</code> fields - it might be fairly disturbing and violating DRY principle
     * to write the same separator everywhere. For that, constructor {@link Path#Path(String, PathFactory)}, or
     * <b>preferably</b>, method {@link PathFactory#create(String)} should be used.
     * <p>
     * <b>This method connects object array oriented paths with string paths, to maintain sustainability with
     * Spigot/BungeeCord API. However, if you don't wanna use array-based paths at all, just use any of the
     * {@link Section} methods based on {@link Object} path, which also accept
     * full string paths under some circumstances. Learn more about this behaviour at
     * {@link Section#getBlockSafe(Object)}.</b>
     *
     * @param path      the string path to split (in format <code>a.b</code> for separator <code>.</code>)
     * @param separator separator to split the path by
     */
    private Path(@NotNull String path, char separator) {
        this.path = path.split(Pattern.quote(String.valueOf(separator)));
    }

    /**
     * Constructs the path array from the given string path, by splitting it by separator provided by the given factory.
     * <p>
     * <b>This method connects object array oriented paths with string paths, to maintain sustainability with
     * Spigot/BungeeCord API. However, if you don't wanna use array-based paths at all, just use any of the
     * {@link Section} methods based on {@link Object} path, which also accept
     * full string paths under some circumstances. Learn more about this behaviour at
     * {@link Section#getBlockSafe(Object)}.</b>
     *
     * @param path        the string path to split
     * @param pathFactory separator provider
     * @see PathFactory#create(String)
     */
    private Path(@NotNull String path, @NotNull PathFactory pathFactory) {
        this.path = path.split(pathFactory.getEscapedSeparator());
    }

    /**
     * Constructs path from the given array/arguments. Empty array/no arguments is considered illegal (might cause
     * serious issues as well) and will throw {@link IllegalArgumentException}. <b>It is in the caller's best interest
     * to never modify the objects given (and their contents), as it might cause several issues (inequalities between objects...).</b>
     * <p>
     * If the given array/arguments is (one) <code>null</code>, returns {@link #NULL_KEY}.
     * <p>
     * <b>If it is known only one argument is supplied and it is <code>null</code>, use {@link #fromSingleKey(Object)} to
     * avoid possible compiler warnings.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and then reuse it.</i>
     *
     * @param path the path array
     */
    public static Path from(@Nullable Object... path) {
        return path == null ? NULL_KEY : new Path(path);
    }

    /**
     * Constructs path from the given single argument. The returned path will, therefore, contain only one key (that one
     * provided). <b>It is in the caller's best interest to never modify the object given (and it's contents), as it might
     * cause several issues (inequalities between objects...).</b>
     * <p>
     * If the given argument is <code>null</code>, returns {@link #NULL_KEY}.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and then reuse it.</i>
     *
     * @param key the only key in the path
     */
    public static Path fromSingleKey(@Nullable Object key) {
        return key == null ? NULL_KEY : new Path(key);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by {@link GeneralSettings#DEFAULT_SEPARATOR}.
     * <p>
     * To split using custom separator, please use {@link #Path(String, char)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, to maintain sustainability with
     * Spigot/BungeeCord API. However, if you don't wanna use array-based paths at all, just use any of the
     * {@link Section} methods based on {@link Object} path, which also accept
     * full string paths under some circumstances. Learn more about this behaviour at
     * {@link Section#getBlockSafe(Object)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and then reuse it.</i>
     *
     * @param path the string path to split (in format <code>a.b</code> for separator <code>.</code>)
     */
    public static Path fromString(@NotNull String path) {
        return new Path(path);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by the given separator.
     * <p>
     * As path objects should be created only once and then reused, it might be fairly disturbing and violating DRY principle
     * to write the same separator everywhere. For that, constructor {@link Path#Path(String, PathFactory)}, or
     * <b>preferably</b>, method {@link PathFactory#create(String)} should be used.
     * <p>
     * <b>This method connects object array oriented paths with string paths, to maintain sustainability with
     * Spigot/BungeeCord API. However, if you don't wanna use array-based paths at all, just use any of the
     * {@link Section} methods based on {@link Object} path, which also accept
     * full string paths under some circumstances. Learn more about this behaviour at
     * {@link Section#getBlockSafe(Object)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and then reuse it.</i>
     *
     * @param path      the string path to split (in format <code>a.b</code> for separator <code>.</code>)
     * @param separator separator to split the path by
     */
    public static Path fromString(@NotNull String path, char separator) {
        return new Path(path, separator);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by separator provided by the given factory.
     * <p>
     * <b>This method connects object array oriented paths with string paths, to maintain sustainability with
     * Spigot/BungeeCord API. However, if you don't wanna use array-based paths at all, just use any of the
     * {@link Section} methods based on {@link Object} path, which also accept
     * full string paths under some circumstances. Learn more about this behaviour at
     * {@link Section#getBlockSafe(Object)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and then reuse it.</i>
     *
     * @param path        the string path to split
     * @param pathFactory separator provider
     * @see PathFactory#create(String)
     */
    public static Path fromString(@NotNull String path, @NotNull PathFactory pathFactory) {
        return new Path(path, pathFactory);
    }

    /**
     * Performs the same operation on the given path as {@link Path#add(Object)} (and returns the result); if the
     * given path is <code>null</code>, creates and returns a single-key path created via {@link Path#fromSingleKey(Object)}
     * from the given key.
     * <p>
     * <b>It is in the caller's best interest to never modify the object given (and their contents), as it might cause
     * several issues (inequalities between objects...).</b>
     *
     * @param path the path to add to, or <code>null</code> to create new
     * @param key  the key to add
     * @return the new path based on the given one (if not <code>null</code>), with added key at the end
     */
    public static Path addTo(@Nullable Path path, @Nullable Object key) {
        return path == null ? Path.from(key) : path.add(key);
    }

    /**
     * Returns the length of the backing array - amount of keys forming this path.
     *
     * @return the length
     */
    public int getLength() {
        return path.length;
    }

    /**
     * Returns key in this path, at the given position (from the backing array).
     *
     * @param i the index
     * @return the key at the given index
     */
    public Object get(int i) {
        return path[i];
    }

    /**
     * Creates a new path, copies this path's backing array, adds the given key at the end and returns the new path itself.
     * <p>
     * <b>It is in the caller's best interest to never modify the object given (and their contents), as it might cause
     * several issues (inequalities between objects...).</b>
     *
     * @param key the key to add
     * @return the new path with same elements of this one, except with the given key added at the end
     */
    public Path add(@Nullable Object key) {
        //New path
        Object[] path = Arrays.copyOf(this.path, this.path.length + 1);
        //Set
        path[path.length - 1] = key;
        //Return
        return new Path(path);
    }

    /**
     * Returns the parent path of this one.
     * <p>
     * More formally, creates a new path and copies this path's backing array, except the last element.
     * <p>
     * Please note that if this path's length is <code>1</code>, invoking this method will create an {@link IllegalArgumentException}, as it is illegal to have empty paths and might cause serious issues as well.
     *
     * @return the parent path of this one
     */
    public Path parent() {
        return Path.from(Arrays.copyOf(this.path, this.path.length - 1));
    }
}