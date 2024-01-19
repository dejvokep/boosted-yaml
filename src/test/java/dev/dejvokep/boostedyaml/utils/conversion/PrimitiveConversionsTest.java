/*
 * Copyright 2024 https://dejvokep.dev/
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

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class PrimitiveConversionsTest {

    @Test
    void toInt() {
        assertEquals(Integer.MAX_VALUE, PrimitiveConversions.toInt(Integer.MAX_VALUE).orElse(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, PrimitiveConversions.toInt(String.valueOf(Integer.MAX_VALUE)).orElse(Integer.MIN_VALUE));
    }

    @Test
    void toBigInt() {
        assertEquals(BigInteger.ONE, PrimitiveConversions.toBigInt(BigInteger.ONE).orElse(BigInteger.TEN));
        assertEquals(BigInteger.ONE, PrimitiveConversions.toBigInt(String.valueOf(BigInteger.ONE)).orElse(BigInteger.TEN));
    }

    @Test
    void toByte() {
        assertEquals(Byte.MAX_VALUE, PrimitiveConversions.toByte(Byte.MAX_VALUE).orElse(Byte.MIN_VALUE));
        assertEquals(Byte.MAX_VALUE, PrimitiveConversions.toByte(String.valueOf(Byte.MAX_VALUE)).orElse(Byte.MIN_VALUE));
    }

    @Test
    void toLong() {
        assertEquals(Long.MAX_VALUE, PrimitiveConversions.toLong(Long.MAX_VALUE).orElse(Long.MIN_VALUE));
        assertEquals(Long.MAX_VALUE, PrimitiveConversions.toLong(String.valueOf(Long.MAX_VALUE)).orElse(Long.MIN_VALUE));
    }

    @Test
    void toDouble() {
        assertEquals(Double.MAX_VALUE, PrimitiveConversions.toDouble(Double.MAX_VALUE).orElse(Double.MIN_VALUE));
        assertEquals(Double.MAX_VALUE, PrimitiveConversions.toDouble(String.valueOf(Double.MAX_VALUE)).orElse(Double.MIN_VALUE));
    }

    @Test
    void toFloat() {
        assertEquals(Float.MAX_VALUE, PrimitiveConversions.toFloat(Float.MAX_VALUE).orElse(Float.MIN_VALUE));
        assertEquals(Float.MAX_VALUE, PrimitiveConversions.toFloat(String.valueOf(Float.MAX_VALUE)).orElse(Float.MIN_VALUE));
    }

    @Test
    void toShort() {
        assertEquals(Short.MAX_VALUE, PrimitiveConversions.toShort(Short.MAX_VALUE).orElse(Short.MIN_VALUE));
        assertEquals(Short.MAX_VALUE, PrimitiveConversions.toShort(String.valueOf(Short.MAX_VALUE)).orElse(Short.MIN_VALUE));
    }

    @Test
    void isNumber() {
        assertTrue(PrimitiveConversions.isNumber(int.class));
        assertTrue(PrimitiveConversions.isNumber(byte.class));
        assertTrue(PrimitiveConversions.isNumber(short.class));
        assertTrue(PrimitiveConversions.isNumber(long.class));
        assertTrue(PrimitiveConversions.isNumber(float.class));
        assertTrue(PrimitiveConversions.isNumber(double.class));
        assertTrue(PrimitiveConversions.isNumber(Integer.class));
        assertTrue(PrimitiveConversions.isNumber(Byte.class));
        assertTrue(PrimitiveConversions.isNumber(Short.class));
        assertTrue(PrimitiveConversions.isNumber(Long.class));
        assertTrue(PrimitiveConversions.isNumber(Float.class));
        assertTrue(PrimitiveConversions.isNumber(Double.class));
        assertFalse(PrimitiveConversions.isNumber(boolean.class));
        assertFalse(PrimitiveConversions.isNumber(Character.class));
    }

    @Test
    void convertNumber() {
        assertEquals(PrimitiveConversions.convertNumber(1, int.class), 1);
        assertEquals(PrimitiveConversions.convertNumber(1, Integer.class), 1);
        assertEquals(PrimitiveConversions.convertNumber(1, double.class), 1D);
        assertEquals(PrimitiveConversions.convertNumber(1, Double.class), 1D);
        assertEquals(PrimitiveConversions.convertNumber(1, float.class), 1F);
        assertEquals(PrimitiveConversions.convertNumber(1, Float.class), 1F);
    }
}