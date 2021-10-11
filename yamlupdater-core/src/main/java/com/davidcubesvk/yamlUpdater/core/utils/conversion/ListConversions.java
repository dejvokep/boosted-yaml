package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class used to convert raw list optionals into list of target types.
 * <p>
 * Optionals are used as parameters only for sole simplification, as {@link Section} class is built upon optionals -
 * therefore, warnings of this type are suppressed.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
public class ListConversions {

    /**
     * Constructs a list of strings from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getStringSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of strings
     */
    public static Optional<List<String>> toStringList(Optional<List<?>> value) {
        return construct(value, o -> Optional.ofNullable(o instanceof String || o instanceof Number || o instanceof Boolean ? o.toString() : null));
    }

    /**
     * Constructs a list of integers from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getIntSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of integers
     */
    public static Optional<List<Integer>> toIntList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toInt);
    }

    /**
     * Constructs a list of big integers from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getBigIntSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of big integers
     */
    public static Optional<List<BigInteger>> toBigIntList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toBigInt);
    }

    /**
     * Constructs a list of bytes from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getByteSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of bytes
     */
    public static Optional<List<Byte>> toByteList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toByte);
    }

    /**
     * Constructs a list of longs from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getLongSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of longs
     */
    public static Optional<List<Long>> toLongList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toLong);
    }

    /**
     * Constructs a list of doubles from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getDoubleSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of doubles
     */
    public static Optional<List<Double>> toDoubleList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toDouble);
    }

    /**
     * Constructs a list of floats from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getFloatSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of floats
     */
    public static Optional<List<Float>> toFloatList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toFloat);
    }

    /**
     * Constructs a list of shorts from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getShortSafe(Path)}.
     *
     * @param value the list to construct
     * @return list of shorts
     */
    public static Optional<List<Short>> toShortList(Optional<List<?>> value) {
        return construct(value, NumericConversions::toShort);
    }

    /**
     * Constructs a list of maps from the given list of unknown type. The returned optional is never empty, unless
     * the given one is.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an non-map element, it is
     * skipped and will not appear in the returned list.
     *
     * @param value the list to construct
     * @return list of maps
     */
    public static Optional<List<Map<?, ?>>> toMapList(Optional<List<?>> value) {
        return construct(value, o -> o instanceof Map ? Optional.of((Map<?, ?>) o) : Optional.empty());
    }

    /**
     * Constructs a list of the target type (defined by the mapper) from the given list of unknown type, using the given
     * mapper.
     * <p>
     * The mapper should effectively convert object elements from the given list to objects of the target type. If an
     * element is incompatible, the mapper should return an empty optional.
     *
     * @param value the list to construct
     * @return list of the target type
     */
    private static <T> Optional<List<T>> construct(Optional<List<?>> value, Function<Object, Optional<T>> mapper) {
        //If not present
        if (!value.isPresent())
            return Optional.empty();

        //Output
        List<T> list = new ArrayList<>();
        //All elements
        for (Object element : value.get())
            //Add
            mapper.apply(element).ifPresent(list::add);

        //Return
        return Optional.of(list);
    }

}