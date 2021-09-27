package com.davidcubesvk.yamlUpdater.core.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ListConversions {

    public static Optional<List<String>> toStringList(Optional<List> value) {
        return construct(value, o -> Optional.of(o.toString()));
    }
    public static Optional<List<Integer>> toIntList(Optional<List> value) {
        return construct(value, NumericConversions::toInt);
    }
    public static Optional<List<BigInteger>> toBigIntList(Optional<List> value) {
        return construct(value, NumericConversions::toBigInt);
    }
    public static Optional<List<Byte>> toByteList(Optional<List> value) {
        return construct(value, NumericConversions::toByte);
    }
    public static Optional<List<Long>> toLongList(Optional<List> value) {
        return construct(value, NumericConversions::toLong);
    }
    public static Optional<List<Double>> toDoubleList(Optional<List> value) {
        return construct(value, NumericConversions::toDouble);
    }
    public static Optional<List<Float>> toFloatList(Optional<List> value) {
        return construct(value, NumericConversions::toFloat);
    }
    public static Optional<List<Short>> toShortList(Optional<List> value) {
        return construct(value, NumericConversions::toShort);
    }
    public static Optional<List<Map<?, ?>>> toMapList(Optional<List> value) {
        return construct(value, o -> o instanceof Map ? Optional.of((Map<?, ?>) o) : Optional.empty());
    }

    private static <T> Optional<List<T>> construct(Optional<List> value, Function<Object, Optional<T>> mapper) {
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