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
package dev.dejvokep.boostedyaml.updater;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.settings.updater.ValueMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class VersionedOperationsTest {

    @Test
    void run() {
        try {
            // Downgrading enabled
            assertDoesNotThrow(() -> VersionedOperations.run(YamlDocument.create(new ByteArrayInputStream("v: 2".getBytes(StandardCharsets.UTF_8))), YamlDocument.create(new ByteArrayInputStream("v: 1".getBytes(StandardCharsets.UTF_8))), UpdaterSettings.builder().setEnableDowngrading(true).setVersioning(new BasicVersioning("v")).build(), '.'));
            // Downgrading disabled
            assertThrows(UnsupportedOperationException.class, () -> VersionedOperations.run(YamlDocument.create(new ByteArrayInputStream("v: 2".getBytes(StandardCharsets.UTF_8))), YamlDocument.create(new ByteArrayInputStream("v: 1".getBytes(StandardCharsets.UTF_8))), UpdaterSettings.builder().setEnableDowngrading(false).setVersioning(new BasicVersioning("v")).build(), '.'));
            // No versioning
            assertFalse(VersionedOperations.run(YamlDocument.create(new ByteArrayInputStream(new byte[0])), YamlDocument.create(new ByteArrayInputStream(new byte[0])), UpdaterSettings.DEFAULT, '.'));
            // Up to date
            assertTrue(VersionedOperations.run(YamlDocument.create(new ByteArrayInputStream("v: 1".getBytes(StandardCharsets.UTF_8))), YamlDocument.create(new ByteArrayInputStream("v: 1".getBytes(StandardCharsets.UTF_8))), UpdaterSettings.builder().setEnableDowngrading(true).setVersioning(new BasicVersioning("v")).build(), '.'));

            // Operator application
            YamlDocument document = YamlDocument.create(new ByteArrayInputStream("v: 1\na: 1".getBytes(StandardCharsets.UTF_8)));
            assertFalse(VersionedOperations.run(document, YamlDocument.create(new ByteArrayInputStream("v: 3".getBytes(StandardCharsets.UTF_8))),
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("v")).addRelocations(new HashMap<String, Map<Route, Route>>(){{
                        put("1", Collections.singletonMap(Route.from("a"), Route.from("d")));
                        put("2", Collections.singletonMap(Route.from("a"), Route.from("b")));
                        put("3", Collections.singletonMap(Route.from("b"), Route.from("c")));
                    }}).addMappers(new HashMap<String, Map<Route, ValueMapper>>(){{
                        put("1", Collections.singletonMap(Route.from("d"), ValueMapper.value(value -> -1)));
                        put("2", Collections.singletonMap(Route.from("b"), ValueMapper.value(value -> 2)));
                        put("3", Collections.singletonMap(Route.from("c"), ValueMapper.value(value -> ((Integer) value) + 1)));
                    }}).addCustomLogic(new HashMap<String, List<Consumer<YamlDocument>>>(){{
                        put("1", Collections.singletonList(document -> document.set("e", -1)));
                        put("2", Collections.singletonList(document -> document.set("e", 1)));
                        put("3", Collections.singletonList(document -> document.set("e", 2)));
                    }}).build(), '.'));
            assertEquals(3, document.get("c"));
            assertEquals(2, document.get("e"));
            assertTrue(document.contains("v"));
            assertEquals(3, document.getKeys().size());
        } catch (IOException ex) {
            fail(ex);
        }
    }

}