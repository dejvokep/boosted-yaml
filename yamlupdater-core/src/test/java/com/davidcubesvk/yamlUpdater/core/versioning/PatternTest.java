package com.davidcubesvk.yamlUpdater.core.versioning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    // Parts
    private static final Pattern.Part PART_FIRST = new Pattern.Part(1, 100), PART_SECOND = new Pattern.Part("."), PART_THIRD = new Pattern.Part(0, 10);
    // Pattern
    private static final Pattern PATTERN = new Pattern(PART_FIRST, PART_SECOND, PART_THIRD);

    @Test
    void getPart() {
        // Assert all
        assertEquals(PATTERN.getPart(0), PART_FIRST);
        assertEquals(PATTERN.getPart(1), PART_SECOND);
        assertEquals(PATTERN.getPart(2), PART_THIRD);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> PATTERN.getPart(3));
    }

    @Test
    void getVersion() {
        // Create a version
        Version version = PATTERN.getVersion("1.4");
        // Assert all cursors
        assertEquals(version.getCursor(0), 0);
        assertEquals(version.getCursor(1), 0);
        assertEquals(version.getCursor(2), 4);
    }

    @Test
    void getOldestVersion() {
        assertEquals(PATTERN.getOldestVersion(), PATTERN.getVersion("1.0"));
    }
}