package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Path objects are used as URIs to represent and properly locate values in sections (see {@link Section}) by using
 * an object array to store the individual keys, allowing for better accessibility and possibilities.
 * <p>
 * Path objects are immutable, but handful of methods are provided to create derived paths.
 */
public class Path {

    /**
     * Path with one single key, which is equal to <code>null</code>.
     */
    public static final Path NULL_KEY = Path.fromSingleKey(null);

    //Path
    private final Object[] path;

    /**
     * Constructs path from the given non-empty array/arguments. <b>It is in the caller's best interest to never modify
     * the objects given (and their contents), as it might cause several issues (inequalities between paths...).</b>
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
     * To split using a custom separator, please use {@link #Path(String, char)}.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     *
     * @param path the string path to split (in format <code>a.b</code> for separator <code>.</code> for example ->
     *             <code>[a, b]</code>)
     */
    private Path(@NotNull String path) {
        this.path = path.split(GeneralSettings.DEFAULT_ESCAPED_SEPARATOR);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by the given separator.
     * <p>
     * As path objects should be created only once and then reused (thanks for immutability and to save resources) - e.g.
     * should be stored in <code>static final</code> fields - it might be fairly disturbing and violating the DRY principle
     * to write the same separator everywhere. For that, constructor {@link Path#Path(String, PathFactory)}, or
     * <b>preferably</b>, method {@link PathFactory#create(String)} should be used.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented-paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     *
     * @param path      the string path to split (in format <code>a.b</code> for separator <code>.</code> for example ->
     *                  <code>[a, b]</code>)
     * @param separator separator to split the path by
     */
    private Path(@NotNull String path, char separator) {
        this.path = path.split(Pattern.quote(String.valueOf(separator)));
    }

    /**
     * Constructs the path array from the given string path, by splitting it by the separator provided by the factory.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     *
     * @param path        the string path to split
     * @param pathFactory separator provider
     * @see PathFactory#create(String)
     */
    private Path(@NotNull String path, @NotNull PathFactory pathFactory) {
        this.path = path.split(pathFactory.getEscapedSeparator());
    }

    /**
     * Constructs path from the given array/arguments, enabling usage of wide-range data types as keys.
     * <p>
     * Empty array/no argument is considered illegal (might cause serious issues as well) and will throw an
     * {@link IllegalArgumentException}. <b>It is in the caller's best interest to never modify the objects given (and
     * their contents), as it might cause several issues (inequalities between paths...).</b>
     * <p>
     * The given array represents individuals keys to traverse sequentially when looking for value at the path. For
     * example, for array <code>["x", 1]</code>, processor attempts to get section at key <code>"x"</code> in the
     * section from which we are looking for the value (where we called get/set/remove method) and then value at key
     * <code>1</code> in <b>that</b> section. Just like string paths.
     * <p>
     * If the given array/arguments is (one) <code>null</code>, initializes the backing array with that of {@link #NULL_KEY}.
     * To avoid compiler warnings in such cases, it is recommended to use {@link #fromSingleKey(Object)}.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path the path array
     * @return the immutable path
     */
    public static Path from(@Nullable Object... path) {
        return path == null ? NULL_KEY : new Path(path);
    }

    /**
     * Constructs path from the given single argument. The returned path will, therefore, contain only one key (that one
     * provided). <b>It is in the caller's best interest to never modify the objects given (and their contents), as it
     * might cause several issues (inequalities between paths...).</b>
     * <p>
     * If the given argument is <code>null</code>, returns {@link #NULL_KEY}.
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param key the only key in the path
     * @return the immutable path
     * @see #from(Object...)
     */
    public static Path fromSingleKey(@Nullable Object key) {
        return key == null ? NULL_KEY : new Path(key);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by {@link GeneralSettings#DEFAULT_SEPARATOR}.
     * <p>
     * To split using custom separator, please use {@link #Path(String, char)}.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path the string path to split (in format <code>a.b</code> for separator <code>.</code> for example ->
     *             <code>[a, b]</code>)
     * @return the immutable path
     */
    public static Path fromString(@NotNull String path) {
        return new Path(path);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by the given separator.
     * <p>
     * As path objects should be created only once and then reused (thanks for immutability and to save resources) - e.g.
     * should be stored in <code>static final</code> fields - it might be fairly disturbing and violating the DRY principle
     * to write the same separator everywhere. For that, constructor {@link Path#Path(String, PathFactory)}, or
     * <b>preferably</b>, method {@link PathFactory#create(String)} should be used.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path      the string path to split (in format <code>a.b</code> for separator <code>.</code> for example ->
     *                  <code>[a, b]</code>)
     * @param separator separator to split the path by
     * @return the immutable path
     */
    public static Path fromString(@NotNull String path, char separator) {
        return new Path(path, separator);
    }

    /**
     * Constructs the path array from the given string path, by splitting it by the separator supplied by the factory.
     * <p>
     * As path objects should be created only once and then reused (thanks for immutability and to save resources) - e.g.
     * should be stored in <code>static final</code> fields - it might be fairly disturbing and violating the DRY principle
     * to write the same factory everywhere. For that <b>preferably</b>, method {@link PathFactory#create(String)} should
     * be used.
     * <p>
     * <b>Please note</b> that string paths can only contain string keys. Therefore, you will not be able to refer to
     * <code>null</code> keys, or any other datatype (unless {@link GeneralSettings#getKeyMode()} is set to
     * {@link GeneralSettings.KeyMode#STRING}). Please learn more at {@link Section#getBlockSafe(String)}.
     * <p>
     * <b>This method connects object array oriented paths with string paths, enabling usage of path objects while
     * maintaining sustainability with Spigot/BungeeCord API. However, string path related methods should be used for
     * that manner. Please learn more at {@link Section#getBlockSafe(String)}.</b>
     * <p>
     * <i>As paths are immutable objects, to save resources, it is recommended to create that certain path only once and
     * then reuse it.</i>
     *
     * @param path        the string path to split (in format <code>a.b</code> for separator <code>.</code> for example ->
     *                    <code>[a, b]</code>)
     * @param pathFactory factory used to supply the separator by which to split the given string path
     * @return the immutable path
     */
    static Path fromString(@NotNull String path, @NotNull PathFactory pathFactory) {
        return new Path(path, pathFactory);
    }

    /**
     * Performs the same operation on the given path as {@link Path#add(Object)} (and returns the result); if the given
     * path is <code>null</code>, creates and returns a single-key path created via {@link Path#fromSingleKey(Object)}
     * from the given key.
     * <p>
     * <b>It is in the caller's best interest to never modify the objects given (and their contents), as it might cause
     * several issues (inequalities between paths...).</b>
     *
     * @param path the path to add another key (element) to, or <code>null</code> to create new one
     * @param key  the key to add, or create a single key path from
     * @return the new path based on the given one with the given key added at the end, or a single key path according
     * to the documentation above
     */
    public static Path addTo(@Nullable Path path, @Nullable Object key) {
        return path == null ? Path.from(key) : path.add(key);
    }

    /**
     * Returns the length of the path (backing array) - amount of keys forming this path.
     *
     * @return the length
     */
    public int length() {
        return path.length;
    }

    /**
     * Returns key in this path (from the backing array), at the given position.
     *
     * @param i the index
     * @return the key at the given index
     */
    public Object get(int i) {
        return path[i];
    }

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
     * More formally, creates a new path and copies this path's backing array without the last element.
     * <p>
     * Please note that if this path's length is <code>1</code>, invoking this method will create an
     * {@link IllegalArgumentException}, as it is illegal to have empty paths. See more at {@link Path#from(Object...)}.
     *
     * @return the parent path of this one
     */
    public Path parent() {
        return Path.from(Arrays.copyOf(this.path, this.path.length - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;
        Path path1 = (Path) o;
        return Arrays.equals(path, path1.path);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }
}