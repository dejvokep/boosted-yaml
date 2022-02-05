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
package dev.dejvokep.boostedyaml.updater;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.segment.Segment;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.Version;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RelocatorTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE), Segment.literal("."), Segment.range(0, 10));
    // Versions
    private static final Version VERSION_DOCUMENT = Objects.requireNonNull(PATTERN.getVersion("1.2")), VERSION_DEFAULT = Objects.requireNonNull(PATTERN.getVersion("2.3"));
    // Settings
    private static final UpdaterSettings SETTINGS = UpdaterSettings.builder().setRelocations(new HashMap<String, Map<Route, Route>>(){{
        put("1.0", new HashMap<Route, Route>(){{
            put(Route.from("d"), Route.from("e"));
        }});
        put("1.2", new HashMap<Route, Route>(){{
            put(Route.from("x"), Route.from("f"));
        }});
        put("1.3", new HashMap<Route, Route>(){{
            put(Route.from("x"), Route.from("g"));
            put(Route.from("y"), Route.from("x"));
            put(Route.from("j"), Route.from("k"));
        }});
        put("2.3", new HashMap<Route, Route>(){{
            put(Route.from("g"), Route.from("h"));
            put(Route.from("z"), Route.from("i"));
        }});
    }}).build();

    @Test
    void apply() {
        try {
            // File
            YamlDocument file = YamlDocument.create(new ByteArrayInputStream("x: a\ny: b\nz:\n  a: 1\n  b: 10".getBytes(StandardCharsets.UTF_8)));
            // Create relocator
            Relocator relocator = new Relocator(file, VERSION_DOCUMENT, VERSION_DEFAULT);
            // Apply
            relocator.apply(SETTINGS, '.');
            // Assert
            assertEquals("a", file.get("h", null));
            assertEquals("b", file.get("x", null));
            assertEquals(1, file.get("i.a", null));
            assertEquals(10, file.get("i.b", null));
            assertEquals(3, file.getKeys().size());
            assertEquals(2, file.getSection("i").getKeys().size());
        } catch (IOException ex) {
            fail(ex);
        }
    }
}