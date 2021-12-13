package com.davidcubesvk.yamlUpdater.core.settings.general;

import com.davidcubesvk.yamlUpdater.core.serialization.BaseSerializer;
import com.davidcubesvk.yamlUpdater.core.serialization.YamlSerializer;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.ListSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GeneralSettingsTest {

    @Test
    void getKeyMode() {
        assertEquals(GeneralSettings.KeyMode.OBJECT, GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build().getKeyMode());
    }

    @Test
    void getSeparator() {
        assertEquals(',', GeneralSettings.builder().setSeparator(',').build().getSeparator());
    }

    @Test
    void getEscapedSeparator() {
        assertEquals(Pattern.quote(","), GeneralSettings.builder().setSeparator(',').build().getEscapedSeparator());
    }

    @Test
    void getSerializer() {
        YamlSerializer serializer = new BaseSerializer("!=");
        assertEquals(serializer, GeneralSettings.builder().setSerializer(serializer).build().getSerializer());
    }

    @Test
    void getDefaultObject() {
        Object o = new Object();
        assertEquals(o, GeneralSettings.builder().setDefaultObject(o).build().getDefaultObject());
    }

    @Test
    void getDefaultString() {
        assertEquals("a", GeneralSettings.builder().setDefaultString("a").build().getDefaultString());
    }

    @Test
    void getDefaultChar() {
        assertEquals('b', GeneralSettings.builder().setDefaultChar('b').build().getDefaultChar());
    }

    @Test
    void getDefaultNumber() {
        assertEquals(5, GeneralSettings.builder().setDefaultNumber(5).build().getDefaultNumber());
    }

    @Test
    void getDefaultBoolean() {
        assertTrue(GeneralSettings.builder().setDefaultBoolean(true).build().getDefaultBoolean());
        assertFalse(GeneralSettings.builder().setDefaultBoolean(false).build().getDefaultBoolean());
    }

    @Test
    void getDefaultList() {
        // Build
        GeneralSettings settings = GeneralSettings.builder().setDefaultList(new ListSupplier() {
            @Override
            public <T> List<T> supply(int size) {
                return new LinkedList<>();
            }
        }).build();
        // Assert
        assertTrue(settings.getDefaultList(1) instanceof LinkedList);
        assertTrue(settings.getDefaultList() instanceof LinkedList);
    }

    @Test
    void getDefaultSet() {
        // Build
        GeneralSettings settings = GeneralSettings.builder().setDefaultSet(LinkedHashSet::new).build();
        // Assert
        assertTrue(settings.getDefaultSet(1) instanceof LinkedHashSet);
        assertTrue(settings.getDefaultSet() instanceof LinkedHashSet);
    }

    @Test
    void getDefaultMap() {
        // Build
        GeneralSettings settings = GeneralSettings.builder().setDefaultMap(LinkedHashMap::new).build();
        // Assert
        assertTrue(settings.getDefaultMap(1) instanceof LinkedHashMap);
        assertTrue(settings.getDefaultMap() instanceof LinkedHashMap);
    }

    @Test
    void getDefaultMapSupplier() {
        MapSupplier supplier = LinkedHashMap::new;
        assertEquals(supplier, GeneralSettings.builder().setDefaultMap(supplier).build().getDefaultMapSupplier());
    }
}