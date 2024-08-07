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
package dev.dejvokep.boostedyaml;

import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class YamlDocumentTest {

    @Test
    void isRoot() throws IOException {
        assertTrue(createFile().isRoot());
    }

    @Test
    void load() throws IOException {
        // Create
        YamlDocument file = createFile();
        // Load
        file.reload(createStream("m: 2\nn: x"));
        // Assert
        Assertions.assertEquals(2, file.getInt("m"));
        Assertions.assertEquals("x", file.getString("n"));
        assertFalse(file.reload());
    }

    @Test
    void getDefaults() throws IOException {
        assertNull(YamlDocument.create(createStream("m: 2\nn: x")).getDefaults());
        Assertions.assertEquals(4, YamlDocument.create(createStream("m: 2\nn: x"), createStream("m: 4")).getDefaults().getInt("m"));
    }

    @Test
    void update() throws IOException{
        // Assert
        assertFalse(YamlDocument.create(createStream("m: 2\nn: x")).update());
        // Create
        YamlDocument file = YamlDocument.create(createStream("# a\nkeep: true\n# b\nremove: 1"), createStream("keep: false\n# c\nadd: 2\n# d\nnested:\n  inner: abc"));
        // Assert
        assertTrue(file.update());
        assertEquals("# a\nkeep: true\n# c\nadd: 2\n# d\nnested:\n  inner: abc\n", file.dump());
    }

    @Test
    void getGeneralSettings() throws IOException {
        // Settings
        GeneralSettings settings = GeneralSettings.builder().setDefaultNumber(3).build();
        // Assert
        Assertions.assertEquals(settings, YamlDocument.create(createStream("n: x"), settings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getGeneralSettings());
    }

    @Test
    void getDumperSettings() throws IOException {
        // Settings
        DumperSettings settings = DumperSettings.builder().setLineBreak("\n\n").build();
        // Assert
        Assertions.assertEquals(settings, YamlDocument.create(createStream("n: x"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, settings, UpdaterSettings.DEFAULT).getDumperSettings());
    }

    @Test
    void getUpdaterSettings() throws IOException {
        // Settings
        UpdaterSettings settings = UpdaterSettings.builder().setKeepAll(true).build();
        // Assert
        Assertions.assertEquals(settings, YamlDocument.create(createStream("n: x"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, settings).getUpdaterSettings());
    }

    @Test
    void getLoaderSettings() throws IOException {
        // Settings
        LoaderSettings settings = LoaderSettings.builder().setAutoUpdate(true).build();
        // Assert
        Assertions.assertEquals(settings, YamlDocument.create(createStream("n: x"), GeneralSettings.DEFAULT, settings, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getLoaderSettings());
    }

    @Test
    void getFile() throws IOException {
        // File
        File file = new File("file.yml");
        // Assert
        assertEquals(file, YamlDocument.create(file, GeneralSettings.DEFAULT, LoaderSettings.builder().setCreateFileIfAbsent(false).build(), DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getFile());
    }

    @Test
    void save() throws IOException {
        // Create
        YamlDocument file = YamlDocument.create(createStream("x: y\nb: 5"));
        // Steam
        OutputStream stream = new ByteArrayOutputStream();
        // Save
        file.save(stream, StandardCharsets.UTF_8);
        // Assert
        assertEquals("x: y\nb: 5\n", stream.toString());
    }

    @Test
    void dump() throws IOException {
        assertEquals("x: y\nb: 5\n", YamlDocument.create(createStream("x: y\nb: 5")).dump());
    }

    @Test
    void setSettings() throws IOException {
        // Create
        YamlDocument file = YamlDocument.create(createStream("x: y\nb: 5"));
        // Settings
        GeneralSettings generalSettings = GeneralSettings.builder().setDefaultNumber(3).build();
        LoaderSettings loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build();
        DumperSettings dumperSettings = DumperSettings.builder().setLineBreak("\n\n").build();
        UpdaterSettings updaterSettings = UpdaterSettings.builder().setKeepAll(true).build();
        // Assert
        file.setSettings(generalSettings, loaderSettings, dumperSettings, updaterSettings);
        assertEquals(generalSettings, file.getGeneralSettings());
        assertEquals(loaderSettings, file.getLoaderSettings());
        assertEquals(dumperSettings, file.getDumperSettings());
        assertEquals(updaterSettings, file.getUpdaterSettings());
        assertThrows(IllegalArgumentException.class, () -> file.setSettings(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build()));
    }

    @Test
    void create() throws IOException {
        // Create
        YamlDocument file = YamlDocument.create(createStream("x: y\nb: 5"), createStream("m: 6"));
        // Assert
        Assertions.assertEquals(3, file.getRouteMappedValues(true).size());
        Assertions.assertEquals("y", file.getString("x"));
        Assertions.assertEquals(5, file.getInt("b"));
        Assertions.assertEquals(1, file.getDefaults().getRouteMappedValues(true).size());
        Assertions.assertEquals(6, file.getDefaults().getInt("m"));
    }

    private BufferedInputStream createStream(String content) {
        return new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    private YamlDocument createFile() throws IOException {
        return YamlDocument.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false".getBytes(StandardCharsets.UTF_8)));
    }

}