package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ManualVersioningTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));
    // Versioning
    private static final ManualVersioning VERSIONING = new ManualVersioning(PATTERN, "1.2", "1.4");

    @Test
    void getDefSectionVersion() throws IOException {
        assertEquals(PATTERN.getVersion("1.4"), VERSIONING.getDefSectionVersion(createFile()));
    }

    @Test
    void getUserSectionVersion() throws IOException {
        assertEquals(PATTERN.getVersion("1.2"), VERSIONING.getUserSectionVersion(createFile()));
    }

    @Test
    void getOldest() {
        assertEquals(PATTERN.getOldestVersion(), VERSIONING.getOldest());
    }

    private YamlFile createFile() throws IOException {
        return YamlFile.create(new ByteArrayInputStream("x: 1.2\ny: true".getBytes(StandardCharsets.UTF_8)));
    }

}