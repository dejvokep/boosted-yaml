package com.davidcubesvk.yamlUpdater.core.versioning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));

    @Test
    void compareTo() {
        // Compare -1 cases
        assertEquals(PATTERN.getVersion("1.9").compareTo(PATTERN.getVersion("2.1")), -1);
        assertEquals(PATTERN.getVersion("1.4").compareTo(PATTERN.getVersion("1.6")), -1);
        // Compare 0 cases
        assertEquals(PATTERN.getVersion("1.2").compareTo(PATTERN.getVersion("1.2")), 0);
        // Compare 1 cases
        assertEquals(PATTERN.getVersion("2.1").compareTo(PATTERN.getVersion("1.9")), 1);
        assertEquals(PATTERN.getVersion("1.6").compareTo(PATTERN.getVersion("1.4")), 1);
    }

    @Test
    void next() {
        // Shift the most significant part
        Version version = PATTERN.getVersion("1.9");
        version.next();
        // If equals
        assertEquals(version.asID(), "2.0");

        // Shift the other part
        version = PATTERN.getVersion("1.4");
        version.next();
        // If equals
        assertEquals(version.asID(), "1.5");
    }

    @Test
    void asID() {
        assertEquals(PATTERN.getVersion("1.4").asID(), "1.4");
    }

    @Test
    void copy() {
        // Create a version
        Version version = PATTERN.getVersion("1.2");
        // If equals
        assertEquals(version.copy().compareTo(version), 0);
    }

    @Test
    void getPattern() {
        assertEquals(PATTERN.getVersion("1.4").getPattern(), PATTERN);
    }

    @Test
    void getCursor() {
        assertEquals(PATTERN.getVersion("1.2").getCursor(2), 2);
    }
}