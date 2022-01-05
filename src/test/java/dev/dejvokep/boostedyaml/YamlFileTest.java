/*
 * Copyright 2021 https://dejvokep.dev/
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

class YamlFileTest {

    @Test
    void isRoot() throws IOException {
        assertTrue(createFile().isRoot());
    }

    @Test
    void load() throws IOException {
        // Create
        YamlFile file = createFile();
        // Load
        file.reload(createStream("m: 2\nn: x"));
        // Assert
        Assertions.assertEquals(2, file.getInt("m"));
        Assertions.assertEquals("x", file.getString("n"));
        assertFalse(file.reload());
    }

    @Test
    void getDefaults() throws IOException {
        assertNull(YamlFile.create(createStream("m: 2\nn: x")).getDefaults());
        Assertions.assertEquals(4, YamlFile.create(createStream("m: 2\nn: x"), createStream("m: 4")).getDefaults().getInt("m"));
    }

    @Test
    void update() throws IOException{
        // Assert
        assertFalse(YamlFile.create(createStream("m: 2\nn: x")).update());
        // Create
        YamlFile file = YamlFile.create(createStream("n: x"), createStream("m: 4"));
        // Assert
        assertTrue(file.update());
        Assertions.assertEquals(4, file.getInt("m"));
    }

    @Test
    void getGeneralSettings() throws IOException {
        // Settings
        GeneralSettings settings = GeneralSettings.builder().setDefaultNumber(3).build();
        // Assert
        Assertions.assertEquals(settings, YamlFile.create(createStream("n: x"), settings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getGeneralSettings());
    }

    @Test
    void getDumperSettings() throws IOException {
        // Settings
        DumperSettings settings = DumperSettings.builder().setLineBreak("\n\n").build();
        // Assert
        Assertions.assertEquals(settings, YamlFile.create(createStream("n: x"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, settings, UpdaterSettings.DEFAULT).getDumperSettings());
    }

    @Test
    void getUpdaterSettings() throws IOException {
        // Settings
        UpdaterSettings settings = UpdaterSettings.builder().setKeepAll(true).build();
        // Assert
        Assertions.assertEquals(settings, YamlFile.create(createStream("n: x"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, settings).getUpdaterSettings());
    }

    @Test
    void getLoaderSettings() throws IOException {
        // Settings
        LoaderSettings settings = LoaderSettings.builder().setAutoUpdate(true).build();
        // Assert
        Assertions.assertEquals(settings, YamlFile.create(createStream("n: x"), GeneralSettings.DEFAULT, settings, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getLoaderSettings());
    }

    @Test
    void getFile() throws IOException {
        // File
        File file = new File("file.yml");
        // Assert
        assertEquals(file, YamlFile.create(file, GeneralSettings.DEFAULT, LoaderSettings.builder().setCreateFileIfAbsent(false).build(), DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getFile());
    }

    @Test
    void save() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"));
        // Steam
        OutputStream stream = new ByteArrayOutputStream();
        // Save
        file.save(stream, StandardCharsets.UTF_8);
        // Assert
        assertEquals("x: y\nb: 5\n", stream.toString());
    }

    @Test
    void dump() throws IOException {
        assertEquals("x: y\nb: 5\n", YamlFile.create(createStream("x: y\nb: 5")).dump());
    }

    @Test
    void setLoaderSettings() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"));
        // Settings
        LoaderSettings settings = LoaderSettings.builder().setAutoUpdate(true).build();
        // Set
        file.setLoaderSettings(settings);
        // Assert
        Assertions.assertEquals(settings, file.getLoaderSettings());
    }

    @Test
    void setDumperSettings() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"));
        // Settings
        DumperSettings settings = DumperSettings.builder().setLineBreak("\n\n").build();
        // Set
        file.setDumperSettings(settings);
        // Assert
        Assertions.assertEquals(settings, file.getDumperSettings());
    }

    @Test
    void setGeneralSettings() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"));
        // Settings
        GeneralSettings settings = GeneralSettings.builder().setDefaultNumber(3).build();
        // Set
        file.setGeneralSettings(settings);
        // Assert
        Assertions.assertEquals(settings, file.getGeneralSettings());
    }

    @Test
    void setUpdaterSettings() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"));
        // Settings
        UpdaterSettings settings = UpdaterSettings.builder().setKeepAll(true).build();
        // Set
        file.setUpdaterSettings(settings);
        // Assert
        Assertions.assertEquals(settings, file.getUpdaterSettings());
    }

    @Test
    void create() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"), createStream("m: 6"));
        // Assert
        Assertions.assertEquals(2, file.getRouteMappedValues(true).size());
        Assertions.assertEquals("y", file.getString("x"));
        Assertions.assertEquals(5, file.getInt("b"));
        Assertions.assertEquals(1, file.getDefaults().getRouteMappedValues(true).size());
        Assertions.assertEquals(6, file.getDefaults().getInt("m"));
    }

    private BufferedInputStream createStream(String content) {
        return new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    private YamlFile createFile() throws IOException {
        return YamlFile.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false".getBytes(StandardCharsets.UTF_8)));
    }

}