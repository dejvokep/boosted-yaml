/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.settings.general;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.serialization.YamlSerializer;
import dev.dejvokep.boostedyaml.utils.supplier.ListSupplier;
import dev.dejvokep.boostedyaml.utils.supplier.MapSupplier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GeneralSettingsTest {

    @Test
    void getKeyMode() {
        assertEquals(GeneralSettings.KeyFormat.OBJECT, GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build().getKeyFormat());
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
        YamlSerializer serializer = new StandardSerializer("!=");
        assertEquals(serializer, GeneralSettings.builder().setSerializer(serializer).build().getSerializer());
    }

    @Test
    void isUseDefaults() {
        assertTrue(GeneralSettings.builder().setUseDefaults(true).build().isUseDefaults());
        assertFalse(GeneralSettings.builder().setUseDefaults(false).build().isUseDefaults());
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
            @NotNull
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