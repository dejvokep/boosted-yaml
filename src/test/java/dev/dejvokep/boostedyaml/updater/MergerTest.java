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
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MergerTest {

    @Test
    void merge() {
        try {
            // File
            YamlDocument file = YamlDocument.create(
                    new ByteArrayInputStream("x: 1.2\ny: true\nz:\n  a: 1\n  b: 10\no: \"a: b\"\np: false".getBytes(StandardCharsets.UTF_8)),
                    new ByteArrayInputStream("x: 1.4\ny: false\nz:\n  a: 5\n  b: 10\nm: \"a: c\"".getBytes(StandardCharsets.UTF_8)),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);

            // Mark "p" to be ignored
            file.getOptionalBlock("p").orElseThrow(NullPointerException::new).setIgnored(true);
            // Merge
            Merger.merge(file, file.getDefaults(), UpdaterSettings.DEFAULT);
            // Verify all
            assertEquals(1.2, file.get("x", null));
            assertEquals(true, file.get("y", null));
            assertEquals(1, file.get("z.a", null));
            assertEquals(10, file.get("z.b", null));
            assertEquals("a: c", file.get("m", null));
            assertEquals(false, file.get("p", null));
            assertEquals(5, file.getKeys().size());
            assertEquals(2, file.getSection("z").getKeys().size());

            // File
            file = YamlDocument.create(
                    new ByteArrayInputStream("x: 1.2\nz: 1".getBytes(StandardCharsets.UTF_8)),
                    new ByteArrayInputStream("x: 1.4\ny: false\nz: 5".getBytes(StandardCharsets.UTF_8)),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            // Merge
            Merger.merge(file, file.getDefaults(), UpdaterSettings.DEFAULT);
            // Verify
            assertEquals("x: 1.2\ny: false\nz: 1\n", file.dump());

            // Settings
            UpdaterSettings settings = UpdaterSettings.builder().setOptionSorting(UpdaterSettings.OptionSorting.NONE).build();
            // File
            file = YamlDocument.create(
                    new ByteArrayInputStream("x: 1.2\nz: 1".getBytes(StandardCharsets.UTF_8)),
                    new ByteArrayInputStream("x: 1.4\ny: false\nz: 5".getBytes(StandardCharsets.UTF_8)),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, settings);
            // Merge
            Merger.merge(file, file.getDefaults(), settings);
            // Verify
            assertEquals("x: 1.2\nz: 1\ny: false\n", file.dump());
        } catch (
                IOException ex) {
            fail(ex);
        }
    }
}