/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.utils.conversion;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class used to convert raw lists to list of target types.
 */
public class ListConversions {

    /**
     * Constructs a list of strings from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalString(Route)}.
     *
     * @param value the list to construct
     * @return list of strings
     */
    @NotNull
    public static Optional<List<String>> toStringList(@Nullable List<?> value) {
        return construct(value, o -> Optional.ofNullable(o.toString()));
    }

    /**
     * Constructs a list of integers from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalInt(Route)}.
     *
     * @param value the list to construct
     * @return list of integers
     */
    @NotNull
    public static Optional<List<Integer>> toIntList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toInt);
    }

    /**
     * Constructs a list of big integers from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalBigInt(Route)}.
     *
     * @param value the list to construct
     * @return list of big integers
     */
    @NotNull
    public static Optional<List<BigInteger>> toBigIntList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toBigInt);
    }

    /**
     * Constructs a list of bytes from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalByte(Route)}.
     *
     * @param value the list to construct
     * @return list of bytes
     */
    @NotNull
    public static Optional<List<Byte>> toByteList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toByte);
    }

    /**
     * Constructs a list of longs from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalLong(Route)}.
     *
     * @param value the list to construct
     * @return list of longs
     */
    @NotNull
    public static Optional<List<Long>> toLongList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toLong);
    }

    /**
     * Constructs a list of doubles from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalDouble(Route)}.
     *
     * @param value the list to construct
     * @return list of doubles
     */
    @NotNull
    public static Optional<List<Double>> toDoubleList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toDouble);
    }

    /**
     * Constructs a list of floats from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalFloat(Route)}.
     *
     * @param value the list to construct
     * @return list of floats
     */
    @NotNull
    public static Optional<List<Float>> toFloatList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toFloat);
    }

    /**
     * Constructs a list of shorts from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is
     * skipped and will not appear in the returned list. Please learn more about compatible types at the main content
     * method {@link Section#getOptionalShort(Route)}.
     *
     * @param value the list to construct
     * @return list of shorts
     */
    @NotNull
    public static Optional<List<Short>> toShortList(@Nullable List<?> value) {
        return construct(value, PrimitiveConversions::toShort);
    }

    /**
     * Constructs a list of maps from the given list of unknown type. The returned optional is never empty, unless
     * the given value is <code>null</code>.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an non-map element, it is
     * skipped and will not appear in the returned list.
     *
     * @param value the list to construct
     * @return list of maps
     */
    @NotNull
    public static Optional<List<Map<?, ?>>> toMapList(@Nullable List<?> value) {
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
    @NotNull
    private static <T> Optional<List<T>> construct(@Nullable List<?> value, @NotNull Function<Object, Optional<T>> mapper) {
        //If null
        if (value == null)
            return Optional.empty();

        //Output
        List<T> list = new ArrayList<>();
        //All elements
        for (Object element : value) {
            //If null
            if (element == null)
                continue;

            //Add
            mapper.apply(element).ifPresent(list::add);
        }

        //Return
        return Optional.of(list);
    }

}