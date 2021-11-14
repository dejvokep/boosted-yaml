package com.davidcubesvk.yamlUpdater.core.settings.general;

import com.davidcubesvk.yamlUpdater.core.serialization.Serializer;
import com.davidcubesvk.yamlUpdater.core.serialization.YamlSerializer;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.ListSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.SetSupplier;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GeneralSettingsTest {

    @Test
    void getKeyMode() {
        assertEquals(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build().getKeyMode(), GeneralSettings.KeyMode.OBJECT);
    }

    @Test
    void getSeparator() {
        assertEquals(GeneralSettings.builder().setSeparator(',').build().getSeparator(), ',');
    }

    @Test
    void getEscapedSeparator() {
        assertEquals(GeneralSettings.builder().setSeparator(',').build().getEscapedSeparator(), Pattern.quote(","));
    }

    @Test
    void getSerializer() {
        YamlSerializer serializer = new Serializer("!=");
        assertEquals(GeneralSettings.builder().setSerializer(serializer).build().getSerializer(), serializer);
    }

    @Test
    void getDefaultObject() {
        Object o = new Object();
        assertEquals(GeneralSettings.builder().setDefaultObject(o).build().getDefaultObject(), o);
    }

    @Test
    void getDefaultString() {
        assertEquals(GeneralSettings.builder().setDefaultString("a").build().getDefaultString(), "a");
    }

    @Test
    void getDefaultChar() {
        assertEquals(GeneralSettings.builder().setDefaultChar('b').build().getDefaultChar(), 'b');
    }

    @Test
    void getDefaultNumber() {
        assertEquals(GeneralSettings.builder().setDefaultNumber(5).build().getDefaultNumber(), 5);
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
        assertEquals(GeneralSettings.builder().setDefaultMap(supplier).build().getDefaultMapSupplier(), supplier);
    }
}