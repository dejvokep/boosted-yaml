package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NumericConversionsTest {

    @Test
    void toInt() {
        assertEquals(Integer.MAX_VALUE, NumericConversions.toInt(Integer.MAX_VALUE).orElse(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, NumericConversions.toInt(String.valueOf(Integer.MAX_VALUE)).orElse(Integer.MIN_VALUE));
    }

    @Test
    void toBigInt() {
        assertEquals(BigInteger.ONE, NumericConversions.toBigInt(BigInteger.ONE).orElse(BigInteger.TEN));
        assertEquals(BigInteger.ONE, NumericConversions.toBigInt(String.valueOf(BigInteger.ONE)).orElse(BigInteger.TEN));
    }

    @Test
    void toByte() {
        assertEquals(Byte.MAX_VALUE, NumericConversions.toByte(Byte.MAX_VALUE).orElse(Byte.MIN_VALUE));
        assertEquals(Byte.MAX_VALUE, NumericConversions.toByte(String.valueOf(Byte.MAX_VALUE)).orElse(Byte.MIN_VALUE));
    }

    @Test
    void toLong() {
        assertEquals(Long.MAX_VALUE, NumericConversions.toLong(Long.MAX_VALUE).orElse(Long.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, NumericConversions.toLong(String.valueOf(Long.MAX_VALUE)).orElse(Long.MIN_VALUE));
    }

    @Test
    void toDouble() {
        assertEquals(Double.MAX_VALUE, NumericConversions.toDouble(Double.MAX_VALUE).orElse(Double.MIN_VALUE));
        assertEquals(Double.MAX_VALUE, NumericConversions.toDouble(String.valueOf(Double.MAX_VALUE)).orElse(Double.MIN_VALUE));
    }

    @Test
    void toFloat() {
        assertEquals(Float.MAX_VALUE, NumericConversions.toFloat(Float.MAX_VALUE).orElse(Float.MIN_VALUE));
        assertEquals(Float.MAX_VALUE, NumericConversions.toFloat(String.valueOf(Float.MAX_VALUE)).orElse(Float.MIN_VALUE));
    }

    @Test
    void toShort() {
        assertEquals(Short.MAX_VALUE, NumericConversions.toShort(Short.MAX_VALUE).orElse(Short.MIN_VALUE));
        assertEquals(Short.MAX_VALUE, NumericConversions.toShort(String.valueOf(Short.MAX_VALUE)).orElse(Short.MIN_VALUE));
    }

    @Test
    void isNumber() {
        assertTrue(NumericConversions.isNumber(int.class));
        assertTrue(NumericConversions.isNumber(byte.class));
        assertTrue(NumericConversions.isNumber(short.class));
        assertTrue(NumericConversions.isNumber(long.class));
        assertTrue(NumericConversions.isNumber(float.class));
        assertTrue(NumericConversions.isNumber(double.class));
        assertTrue(NumericConversions.isNumber(Integer.class));
        assertTrue(NumericConversions.isNumber(Byte.class));
        assertTrue(NumericConversions.isNumber(Short.class));
        assertTrue(NumericConversions.isNumber(Long.class));
        assertTrue(NumericConversions.isNumber(Float.class));
        assertTrue(NumericConversions.isNumber(Double.class));
        assertFalse(NumericConversions.isNumber(boolean.class));
        assertFalse(NumericConversions.isNumber(Character.class));
    }

    @Test
    void convertNumber() {
        assertEquals(NumericConversions.convertNumber(1, int.class), 1);
        assertEquals(NumericConversions.convertNumber(1, Integer.class), 1);
        assertEquals(NumericConversions.convertNumber(1, double.class), 1D);
        assertEquals(NumericConversions.convertNumber(1, Double.class), 1D);
        assertEquals(NumericConversions.convertNumber(1, float.class), 1F);
        assertEquals(NumericConversions.convertNumber(1, Float.class), 1F);
    }
}