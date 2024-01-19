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
import dev.dejvokep.boostedyaml.dvs.segment.Segment;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.settings.updater.ValueMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class UpdaterTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE), Segment.literal("."), Segment.range(0, 10));
    // Settings
    private static final UpdaterSettings UPDATER_SETTINGS = UpdaterSettings.builder().addRelocations(new HashMap<String, Map<Route, Route>>() {{
        put("1.3", new HashMap<Route, Route>() {{
            put(Route.from("z", "a"), Route.from("r"));
        }});
        put("2.3", new HashMap<Route, Route>() {{
            put(Route.from("o"), Route.from("m"));
            put(Route.from("z"), Route.from("s"));
        }});
    }}).addMappers(new HashMap<String, Map<Route, ValueMapper>>() {{
        put("1.5", new HashMap<Route, ValueMapper>(){{
            put(Route.from("r"), ValueMapper.section((section, route) -> section.getOptionalInt(route).map(value -> value > 0 ? "+" : "else").orElse("unknown")));
        }});
        put("2.3", new HashMap<Route, ValueMapper>(){{
            put(Route.from("m"), ValueMapper.value(value -> value.toString().indexOf(':')));
        }});
    }}).setVersioning(PATTERN, "a").build();

    @Test
    void update() throws IOException {
        // File
        YamlDocument file = YamlDocument.create(
                new ByteArrayInputStream("a: 1.2\ny: true\nz:\n  a: 1\n  b: 15\no: \"a: b\"\np: 50".getBytes(StandardCharsets.UTF_8)),
                new ByteArrayInputStream("a: 2.3\ny: false\ns:\n  a: 5\n  b: 10\nm: \"a: c\"\nr: 20\nt: 100".getBytes(StandardCharsets.UTF_8)),
                GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UPDATER_SETTINGS);
        // Update
        Updater.update(file, Objects.requireNonNull(file.getDefaults()), file.getUpdaterSettings(), file.getGeneralSettings());
        // Assert
        assertEquals("2.3", file.getString("a", null));
        assertEquals(true, file.get("y", null));
        assertEquals(5, file.get("s.a", null));
        assertEquals(15, file.get("s.b", null));
        assertEquals(1, file.get("m", null));
        assertEquals("+", file.get("r", null));
        assertEquals(100, file.get("t", null));
        assertEquals(6, file.getKeys().size());
        assertEquals(2, file.getSection("s").getKeys().size());

        // File
        file = YamlDocument.create(
                new ByteArrayInputStream("x: 1".getBytes(StandardCharsets.UTF_8)),
                new ByteArrayInputStream("a: 1.0\nx: 1\ny: 2".getBytes(StandardCharsets.UTF_8)),
                GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UPDATER_SETTINGS);
        // Update
        Updater.update(file, Objects.requireNonNull(file.getDefaults()), file.getUpdaterSettings(), file.getGeneralSettings());
        // Assert
        assertEquals("1.0", file.getString("a", null));
        assertEquals(1, file.getInt("x", null));
        assertEquals(2, file.getInt("y", null));
        assertEquals(3, file.getKeys().size());
    }
}