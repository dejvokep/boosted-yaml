package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import java.math.BigInteger;
import java.util.Optional;

public class NumericConversions {

    public static Optional<Integer> toInt(Optional<Number> value) {
        return value.map(Number::intValue);
    }
    public static Optional<BigInteger> toBigInt(Optional<Number> value) {
        return value.isPresent() ? value.get() instanceof BigInteger ? Optional.of((BigInteger) value.get()) : toBigInt(value.get()) : Optional.empty();
    }
    public static Optional<Byte> toByte(Optional<Number> value) {
        return value.map(Number::byteValue);
    }
    public static Optional<Long> toLong(Optional<Number> value) {
        return value.map(Number::longValue);
    }
    public static Optional<Double> toDouble(Optional<Number> value) {
        return value.map(Number::doubleValue);
    }
    public static Optional<Float> toFloat(Optional<Number> value) {
        return value.map(Number::floatValue);
    }
    public static Optional<Short> toShort(Optional<Number> value) {
        return value.map(Number::shortValue);
    }

    public static Optional<Integer> toInt(Object value) {
        //If a number
        if (value instanceof Number)
            return Optional.of(((Number) value).intValue());

        //Try to parse
        try {
            return Optional.of(Integer.valueOf(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    public static Optional<Byte> toByte(Object value) {
        //If a number
        if (value instanceof Number)
            return Optional.of(((Number) value).byteValue());

        //Try to parse
        try {
            return Optional.of(Byte.valueOf(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    public static Optional<Long> toLong(Object value) {
        //If a number
        if (value instanceof Number)
            return Optional.of(((Number) value).longValue());

        //Try to parse
        try {
            return Optional.of(Long.valueOf(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    public static Optional<Double> toDouble(Object value) {
        //If a number
        if (value instanceof Number)
            return Optional.of(((Number) value).doubleValue());

        //Try to parse
        try {
            return Optional.of(Double.valueOf(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    public static Optional<Float> toFloat(Object value) {
        //If a number
        if (value instanceof Number)
            return Optional.of(((Number) value).floatValue());

        //Try to parse
        try {
            return Optional.of(Float.valueOf(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    public static Optional<Short> toShort(Object value) {
        //If a number
        if (value instanceof Number)
            return Optional.of(((Number) value).shortValue());

        //Try to parse
        try {
            return Optional.of(Short.valueOf(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
    public static Optional<BigInteger> toBigInt(Object value) {
        //If a big integer
        if (value instanceof BigInteger)
            return Optional.of((BigInteger) value);
        //If a number
        if (value instanceof Number)
            return Optional.of(BigInteger.valueOf(((Number) value).longValue()));

        //Try to parse
        try {
            return Optional.of(new BigInteger(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

}