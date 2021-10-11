package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import com.davidcubesvk.yamlUpdater.core.block.Section;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Utility class used to convert number optionals (or objects) into numbers of target types.
 * <p>
 * Optionals are used as parameters only for sole simplification, as {@link Section} class is built upon optionals -
 * therefore, warnings of this type are suppressed.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
public class NumericConversions {

    /**
     * Converts the given number to integer, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the integer
     */
    public static Optional<Integer> toInt(Optional<Number> value) {
        return value.map(Number::intValue);
    }

    /**
     * Converts the given number to big integer, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the big integer
     */
    public static Optional<BigInteger> toBigInt(Optional<Number> value) {
        return value.isPresent() ? value.get() instanceof BigInteger ? Optional.of((BigInteger) value.get()) : toBigInt(value.get()) : Optional.empty();
    }

    /**
     * Converts the given number to byte, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the byte
     */
    public static Optional<Byte> toByte(Optional<Number> value) {
        return value.map(Number::byteValue);
    }

    /**
     * Converts the given number to long, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the long
     */
    public static Optional<Long> toLong(Optional<Number> value) {
        return value.map(Number::longValue);
    }

    /**
     * Converts the given number to double, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the double
     */
    public static Optional<Double> toDouble(Optional<Number> value) {
        return value.map(Number::doubleValue);
    }

    /**
     * Converts the given number to float, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the float
     */
    public static Optional<Float> toFloat(Optional<Number> value) {
        return value.map(Number::floatValue);
    }

    /**
     * Converts the given number to short, returns an empty optional if an only the given one is empty.
     *
     * @param value the number
     * @return the short
     */
    public static Optional<Short> toShort(Optional<Number> value) {
        return value.map(Number::shortValue);
    }

    /**
     * Converts the given value to integer.
     * <p>
     * If the given value is not an instance of {@link Number} or could not be converted by parsing from string via
     * {@link Integer#valueOf(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the integer
     */
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

    /**
     * Converts the given value to byte.
     * <p>
     * If the given value is not an instance of {@link Number} or could not be converted by parsing from string via
     * {@link Byte#valueOf(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the byte
     */
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

    /**
     * Converts the given value to long.
     * <p>
     * If the given value is not an instance of {@link Number} or could not be converted by parsing from string via
     * {@link Long#valueOf(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the long
     */
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

    /**
     * Converts the given value to double.
     * <p>
     * If the given value is not an instance of {@link Number} or could not be converted by parsing from string via
     * {@link Double#valueOf(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the double
     */
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

    /**
     * Converts the given value to float.
     * <p>
     * If the given value is not an instance of {@link Number} or could not be converted by parsing from string via
     * {@link Float#valueOf(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the float
     */
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

    /**
     * Converts the given value to short.
     * <p>
     * If the given value is not an instance of {@link Number} or could not be converted by parsing from string via
     * {@link Short#valueOf(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the short
     */
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

    /**
     * Converts the given value to big integer.
     * <p>
     * If the given value is not an instance of {@link BigInteger}, {@link Number} or could not be converted by parsing
     * from string via {@link BigInteger#BigInteger(String)}, returns an empty optional.
     *
     * @param value the number
     * @return the big integer
     */
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