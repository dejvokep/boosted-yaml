package com.davidcubesvk.yamlUpdater.core.utils.conversion;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ListConversionsTest {

    // List with numbers only
    private static final List<Number> NUMBER_LIST = new ArrayList<Number>(){{
        add(Integer.MAX_VALUE);
        add(BigInteger.ONE);
        add(Byte.MAX_VALUE);
        add(Long.MAX_VALUE);
        add(Double.MAX_VALUE);
        add(Float.MAX_VALUE);
        add(Short.MAX_VALUE);
    }};
    // List
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<List<?>> LIST = Optional.of(new ArrayList<Object>(){{
        add("x");
        add(true);
        addAll(NUMBER_LIST);
        add(new HashMap<String, String>(){{
            put("x", "y");
        }});
    }});

    @Test
    void toStringList() {
        assertFalse(ListConversions.toStringList(Optional.empty()).isPresent());
        assertTrue(ListConversions.toStringList(LIST).map(list -> {
            //Assert the size
            assertEquals(9, list.size());
            //Assert individual elements
            assertEquals("x", list.get(0));
            assertEquals("true", list.get(1));
            assertEquals(String.valueOf(Integer.MAX_VALUE), list.get(2));
            assertEquals(String.valueOf(BigInteger.ONE), list.get(3));
            assertEquals(String.valueOf(Byte.MAX_VALUE), list.get(4));
            assertEquals(String.valueOf(Long.MAX_VALUE), list.get(5));
            assertEquals(String.valueOf(Double.MAX_VALUE), list.get(6));
            assertEquals(String.valueOf(Float.MAX_VALUE), list.get(7));
            assertEquals(String.valueOf(Short.MAX_VALUE), list.get(8));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toIntList() {
        assertTrue(ListConversions.toIntList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NUMBER_LIST.get(i).intValue(), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toBigIntList() {
        assertTrue(ListConversions.toBigIntList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NumericConversions.toBigInt(NUMBER_LIST.get(i)).orElse(BigInteger.TEN), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toByteList() {
        assertTrue(ListConversions.toByteList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NUMBER_LIST.get(i).byteValue(), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toLongList() {
        assertTrue(ListConversions.toLongList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NUMBER_LIST.get(i).longValue(), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toDoubleList() {
        assertTrue(ListConversions.toDoubleList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NUMBER_LIST.get(i).doubleValue(), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toFloatList() {
        assertTrue(ListConversions.toFloatList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NUMBER_LIST.get(i).floatValue(), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toShortList() {
        assertTrue(ListConversions.toShortList(LIST).map(list -> {
            //Assert the size
            assertEquals(NUMBER_LIST.size(), list.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(NUMBER_LIST.get(i).shortValue(), list.get(i));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toMapList() {
        assertTrue(ListConversions.toMapList(LIST).map(list -> {
            //Assert the size
            assertEquals(1, list.size());
            //Assert element
            assertEquals(new HashMap<String, String>(){{
                put("x", "y");
            }}, list.get(0));
            //Return
            return list;
        }).isPresent());
    }
}