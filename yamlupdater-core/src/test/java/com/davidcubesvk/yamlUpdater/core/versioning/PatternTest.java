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
        assertEquals(PART_FIRST, PATTERN.getPart(0));
        assertEquals(PART_SECOND, PATTERN.getPart(1));
        assertEquals(PART_THIRD, PATTERN.getPart(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> PATTERN.getPart(3));
    }

    @Test
    void getVersion() {
        // Create a version
        Version version = PATTERN.getVersion("2.4");
        // Assert all cursors
        assertEquals(1, version.getCursor(0));
        assertEquals(0, version.getCursor(1));
        assertEquals(4, version.getCursor(2));
    }

    @Test
    void getOldestVersion() {
        assertEquals(PATTERN.getVersion("1.0"), PATTERN.getOldestVersion());
    }
}