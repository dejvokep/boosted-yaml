package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NumericConversionsTest {

    @Test
    void toInt() {
        assertEquals(NumericConversions.toInt(Optional.of(Integer.MAX_VALUE)).orElse(Integer.MIN_VALUE), Integer.MAX_VALUE);
        assertEquals(NumericConversions.toInt(Integer.MAX_VALUE).orElse(Integer.MIN_VALUE), Integer.MAX_VALUE);
        assertEquals(NumericConversions.toInt(String.valueOf(Integer.MAX_VALUE)).orElse(Integer.MIN_VALUE), Integer.MAX_VALUE);
    }

    @Test
    void toBigInt() {
        assertEquals(NumericConversions.toBigInt(Optional.of(BigInteger.ONE)).orElse(BigInteger.TEN), BigInteger.ONE);
        assertEquals(NumericConversions.toBigInt(BigInteger.ONE).orElse(BigInteger.TEN), BigInteger.ONE);
        assertEquals(NumericConversions.toBigInt(String.valueOf(BigInteger.ONE)).orElse(BigInteger.TEN), BigInteger.ONE);
    }

    @Test
    void toByte() {
        assertEquals(NumericConversions.toByte(Optional.of(Byte.MAX_VALUE)).orElse(Byte.MIN_VALUE), Byte.MAX_VALUE);
        assertEquals(NumericConversions.toByte(Byte.MAX_VALUE).orElse(Byte.MIN_VALUE), Byte.MAX_VALUE);
        assertEquals(NumericConversions.toByte(String.valueOf(Byte.MAX_VALUE)).orElse(Byte.MIN_VALUE), Byte.MAX_VALUE);
    }

    @Test
    void toLong() {
        assertEquals(NumericConversions.toLong(Optional.of(Long.MAX_VALUE)).orElse(Long.MIN_VALUE), Long.MAX_VALUE);
        assertEquals(NumericConversions.toLong(Long.MAX_VALUE).orElse(Long.MIN_VALUE), Long.MAX_VALUE);
        assertEquals(NumericConversions.toLong(String.valueOf(Long.MAX_VALUE)).orElse(Long.MIN_VALUE), Long.MAX_VALUE);
    }

    @Test
    void toDouble() {
        assertEquals(NumericConversions.toDouble(Optional.of(Double.MAX_VALUE)).orElse(Double.MIN_VALUE), Double.MAX_VALUE);
        assertEquals(NumericConversions.toDouble(Double.MAX_VALUE).orElse(Double.MIN_VALUE), Double.MAX_VALUE);
        assertEquals(NumericConversions.toDouble(String.valueOf(Double.MAX_VALUE)).orElse(Double.MIN_VALUE), Double.MAX_VALUE);
    }

    @Test
    void toFloat() {
        assertEquals(NumericConversions.toFloat(Optional.of(Float.MAX_VALUE)).orElse(Float.MIN_VALUE), Float.MAX_VALUE);
        assertEquals(NumericConversions.toFloat(Float.MAX_VALUE).orElse(Float.MIN_VALUE), Float.MAX_VALUE);
        assertEquals(NumericConversions.toFloat(String.valueOf(Float.MAX_VALUE)).orElse(Float.MIN_VALUE), Float.MAX_VALUE);
    }

    @Test
    void toShort() {
        assertEquals(NumericConversions.toShort(Optional.of(Short.MAX_VALUE)).orElse(Short.MIN_VALUE), Short.MAX_VALUE);
        assertEquals(NumericConversions.toShort(Short.MAX_VALUE).orElse(Short.MIN_VALUE), Short.MAX_VALUE);
        assertEquals(NumericConversions.toShort(String.valueOf(Short.MAX_VALUE)).orElse(Short.MIN_VALUE), Short.MAX_VALUE);
    }

}