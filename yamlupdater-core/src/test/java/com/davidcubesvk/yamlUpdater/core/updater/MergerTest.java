package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MergerTest {

    // Settings
    private static final UpdaterSettings SETTINGS = UpdaterSettings.DEFAULT;
    // Files
    private static final YamlFile USER_FILE = new YamlFile(new ByteArrayInputStream("x: 1.2\ny: true\nz:\n  a: 1\n  b: 10\no: \"a: b\"\np: false".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT),
            DEFAULT_FILE = new YamlFile(new ByteArrayInputStream("x: 1.4\ny: false\nz:\n  a: 5\n  b: 10\nm: \"a: c\"".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT);

    @Test
    void merge() {
        // Mark "p" to be kept
        USER_FILE.getBlockSafe("p").orElseThrow(NullPointerException::new).setKeep(true);
        // Merge
        Merger.merge(USER_FILE, DEFAULT_FILE, SETTINGS);
        // Verify all
        assertEquals(USER_FILE.get("x", null), 1.2);
        assertEquals(USER_FILE.get("y", null), true);
        assertEquals(USER_FILE.get("z.a", null), 1);
        assertEquals(USER_FILE.get("z.b", null), 10);
        assertFalse(USER_FILE.contains("o"));
        assertEquals(USER_FILE.get("m", null), "a: c");
        assertEquals(USER_FILE.get("p", null), false);
    }
}