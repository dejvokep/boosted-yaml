package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Loader;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.Charset;
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
        file.load(createStream("m: 2\nn: x"));
        // Assert
        assertEquals(2, file.getInt("m"));
        assertEquals("x", file.getString("n"));
        assertFalse(file.load());
    }

    @Test
    void getDefaults() throws IOException {
        assertNull(YamlFile.create(createStream("m: 2\nn: x")).getDefaults());
        assertEquals(4, YamlFile.create(createStream("m: 2\nn: x"), createStream("m: 4")).getDefaults().getInt("m"));
    }

    @Test
    void update() throws IOException{
        // Assert
        assertFalse(YamlFile.create(createStream("m: 2\nn: x")).update());
        // Create
        YamlFile file = YamlFile.create(createStream("n: x"), createStream("m: 4"));
        // Assert
        assertTrue(file.update());
        assertEquals(4, file.getInt("m"));
    }

    @Test
    void getGeneralSettings() throws IOException {
        // Settings
        GeneralSettings settings = GeneralSettings.builder().setDefaultNumber(3).build();
        // Assert
        assertEquals(settings, YamlFile.create(createStream("n: x"), settings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getGeneralSettings());
    }

    @Test
    void getDumperSettings() throws IOException {
        // Settings
        DumperSettings settings = DumperSettings.builder().setLineBreak("\n\n").build();
        // Assert
        assertEquals(settings, YamlFile.create(createStream("n: x"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, settings, UpdaterSettings.DEFAULT).getDumperSettings());
    }

    @Test
    void getUpdaterSettings() throws IOException {
        // Settings
        UpdaterSettings settings = UpdaterSettings.builder().setKeepAll(true).build();
        // Assert
        assertEquals(settings, YamlFile.create(createStream("n: x"), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, settings).getUpdaterSettings());
    }

    @Test
    void getLoaderSettings() throws IOException {
        // Settings
        LoaderSettings settings = LoaderSettings.builder().setAutoUpdate(true).build();
        // Assert
        assertEquals(settings, YamlFile.create(createStream("n: x"), GeneralSettings.DEFAULT, settings, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT).getLoaderSettings());
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
    void setDefaults() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"));
        // Set
        file.setDefaults(createStream("m: 4"));
        // Assert
        assertNotNull(file.getDefaults());
        assertEquals(4, file.getDefaults().getInt("m"));
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
        assertEquals(settings, file.getLoaderSettings());
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
        assertEquals(settings, file.getDumperSettings());
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
        assertEquals(settings, file.getGeneralSettings());
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
        assertEquals(settings, file.getUpdaterSettings());
    }

    @Test
    void create() throws IOException {
        // Create
        YamlFile file = YamlFile.create(createStream("x: y\nb: 5"), createStream("m: 6"));
        // Assert
        assertEquals(2, file.getRouteMappedValues(true).size());
        assertEquals("y", file.getString("x"));
        assertEquals(5, file.getInt("b"));
        assertEquals(1, file.getDefaults().getRouteMappedValues(true).size());
        assertEquals(6, file.getDefaults().getInt("m"));
    }

    private BufferedInputStream createStream(String content) {
        return new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    private YamlFile createFile() throws IOException {
        return YamlFile.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false".getBytes(StandardCharsets.UTF_8)));
    }

}