package com.davidcubesvk.yamlUpdater.core.fvs.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.fvs.Pattern;
import com.davidcubesvk.yamlUpdater.core.fvs.segment.Segment;
import com.davidcubesvk.yamlUpdater.core.fvs.versioning.ManualVersioning;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ManualVersioningTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE), Segment.literal("."), Segment.range(0, 10));
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