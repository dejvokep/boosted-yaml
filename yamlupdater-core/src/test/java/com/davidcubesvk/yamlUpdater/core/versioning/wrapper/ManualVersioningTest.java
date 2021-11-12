package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ManualVersioningTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));
    // Versioning
    private static final ManualVersioning VERSIONING = new ManualVersioning(PATTERN, "1.2", "1.4");
    // File
    private static final YamlFile FILE = new YamlFile(new ByteArrayInputStream("x: 1.2\ny: true".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT);

    @Test
    void getDefSectionVersion() {
        assertEquals(VERSIONING.getDefSectionVersion(FILE).compareTo(PATTERN.getVersion("1.4")), 0);
    }

    @Test
    void getUserSectionVersion() {
        assertEquals(VERSIONING.getUserSectionVersion(FILE).compareTo(PATTERN.getVersion("1.2")), 0);
    }

    @Test
    void getOldest() {
        assertEquals(VERSIONING.getOldest().compareTo(PATTERN.getOldestVersion()), 0);
    }
}