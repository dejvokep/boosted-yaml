package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NumericConversionsTest {

    @Test
    void toInt() {
        assertEquals(Integer.MAX_VALUE, NumericConversions.toInt(Optional.of(Integer.MAX_VALUE)).orElse(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, NumericConversions.toInt(Integer.MAX_VALUE).orElse(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, NumericConversions.toInt(String.valueOf(Integer.MAX_VALUE)).orElse(Integer.MIN_VALUE));
    }

    @Test
    void toBigInt() {
        assertEquals(BigInteger.ONE, NumericConversions.toBigInt(Optional.of(BigInteger.ONE)).orElse(BigInteger.TEN));
        assertEquals(BigInteger.ONE, NumericConversions.toBigInt(BigInteger.ONE).orElse(BigInteger.TEN));
        assertEquals(BigInteger.ONE, NumericConversions.toBigInt(String.valueOf(BigInteger.ONE)).orElse(BigInteger.TEN));
    }

    @Test
    void toByte() {
        assertEquals(Byte.MAX_VALUE, NumericConversions.toByte(Optional.of(Byte.MAX_VALUE)).orElse(Byte.MIN_VALUE));
        assertEquals(Byte.MAX_VALUE, NumericConversions.toByte(Byte.MAX_VALUE).orElse(Byte.MIN_VALUE));
        assertEquals(Byte.MAX_VALUE, NumericConversions.toByte(String.valueOf(Byte.MAX_VALUE)).orElse(Byte.MIN_VALUE));
    }

    @Test
    void toLong() {
        assertEquals(Long.MAX_VALUE, NumericConversions.toLong(Optional.of(Long.MAX_VALUE)).orElse(Long.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, NumericConversions.toLong(Long.MAX_VALUE).orElse(Long.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, NumericConversions.toLong(String.valueOf(Long.MAX_VALUE)).orElse(Long.MIN_VALUE));
    }

    @Test
    void toDouble() {
        assertEquals(Double.MAX_VALUE, NumericConversions.toDouble(Optional.of(Double.MAX_VALUE)).orElse(Double.MIN_VALUE));
        assertEquals(Double.MAX_VALUE, NumericConversions.toDouble(Double.MAX_VALUE).orElse(Double.MIN_VALUE));
        assertEquals(Double.MAX_VALUE, NumericConversions.toDouble(String.valueOf(Double.MAX_VALUE)).orElse(Double.MIN_VALUE));
    }

    @Test
    void toFloat() {
        assertEquals(Float.MAX_VALUE, NumericConversions.toFloat(Optional.of(Float.MAX_VALUE)).orElse(Float.MIN_VALUE));
        assertEquals(Float.MAX_VALUE, NumericConversions.toFloat(Float.MAX_VALUE).orElse(Float.MIN_VALUE));
        assertEquals(Float.MAX_VALUE, NumericConversions.toFloat(String.valueOf(Float.MAX_VALUE)).orElse(Float.MIN_VALUE));
    }

    @Test
    void toShort() {
        assertEquals(Short.MAX_VALUE, NumericConversions.toShort(Optional.of(Short.MAX_VALUE)).orElse(Short.MIN_VALUE));
        assertEquals(Short.MAX_VALUE, NumericConversions.toShort(Short.MAX_VALUE).orElse(Short.MIN_VALUE));
        assertEquals(Short.MAX_VALUE, NumericConversions.toShort(String.valueOf(Short.MAX_VALUE)).orElse(Short.MIN_VALUE));
    }

}