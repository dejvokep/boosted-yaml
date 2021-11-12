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
            assertEquals(list.size(), 9);
            //Assert individual elements
            assertEquals(list.get(0), "x");
            assertEquals(list.get(1), "true");
            assertEquals(list.get(2), String.valueOf(Integer.MAX_VALUE));
            assertEquals(list.get(3), String.valueOf(BigInteger.ONE));
            assertEquals(list.get(4), String.valueOf(Byte.MAX_VALUE));
            assertEquals(list.get(5), String.valueOf(Long.MAX_VALUE));
            assertEquals(list.get(6), String.valueOf(Double.MAX_VALUE));
            assertEquals(list.get(7), String.valueOf(Float.MAX_VALUE));
            assertEquals(list.get(8), String.valueOf(Short.MAX_VALUE));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toIntList() {
        assertTrue(ListConversions.toIntList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NUMBER_LIST.get(i).intValue());
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toBigIntList() {
        assertTrue(ListConversions.toBigIntList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NumericConversions.toBigInt(NUMBER_LIST.get(i)).orElse(BigInteger.TEN));
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toByteList() {
        assertTrue(ListConversions.toByteList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NUMBER_LIST.get(i).byteValue());
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toLongList() {
        assertTrue(ListConversions.toLongList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NUMBER_LIST.get(i).longValue());
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toDoubleList() {
        assertTrue(ListConversions.toDoubleList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NUMBER_LIST.get(i).doubleValue());
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toFloatList() {
        assertTrue(ListConversions.toFloatList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NUMBER_LIST.get(i).floatValue());
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toShortList() {
        assertTrue(ListConversions.toShortList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), NUMBER_LIST.size());
            //Assert individual elements
            for (int i = 0; i < list.size(); i++)
                assertEquals(list.get(i), NUMBER_LIST.get(i).shortValue());
            //Return
            return list;
        }).isPresent());
    }

    @Test
    void toMapList() {
        assertTrue(ListConversions.toMapList(LIST).map(list -> {
            //Assert the size
            assertEquals(list.size(), 1);
            //Assert element
            assertEquals(list.get(0), new HashMap<String, String>(){{
                put("x", "y");
            }});
            //Return
            return list;
        }).isPresent());
    }
}