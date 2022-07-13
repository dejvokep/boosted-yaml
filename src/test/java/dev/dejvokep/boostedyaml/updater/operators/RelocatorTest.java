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
package dev.dejvokep.boostedyaml.updater.operators;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.Version;
import dev.dejvokep.boostedyaml.dvs.segment.Segment;
import dev.dejvokep.boostedyaml.route.Route;
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

    @Test
    void apply() {
        try {
            YamlDocument document = YamlDocument.create(new ByteArrayInputStream("x: a\ny: b\nz:\n  a: 1\n  b: 10".getBytes(StandardCharsets.UTF_8)));
            Relocator.apply(document, new HashMap<Route, Route>(){{
                put(Route.from("x"), Route.from("g"));
                put(Route.from("y"), Route.from("x"));
                put(Route.from("j"), Route.from("k"));
            }});
            assertEquals("a", document.get("g", null));
            assertEquals("b", document.get("x", null));
            assertEquals(1, document.get("z.a", null));
            assertEquals(10, document.get("z.b", null));
            assertEquals(3, document.getKeys().size());
            assertEquals(2, document.getSection("z").getKeys().size());
        } catch (IOException ex) {
            fail(ex);
        }
    }
}