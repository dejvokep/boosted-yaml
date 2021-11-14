package com.davidcubesvk.yamlUpdater.core.versioning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));

    @Test
    void compareTo() {
        // Compare -1 cases
        assertEquals(-1, PATTERN.getVersion("1.9").compareTo(PATTERN.getVersion("2.1")));
        assertEquals(-1, PATTERN.getVersion("1.4").compareTo(PATTERN.getVersion("1.6")));
        // Compare 0 cases
        assertEquals(0, PATTERN.getVersion("1.2").compareTo(PATTERN.getVersion("1.2")));
        // Compare 1 cases
        assertEquals(1, PATTERN.getVersion("2.1").compareTo(PATTERN.getVersion("1.9")));
        assertEquals(1, PATTERN.getVersion("1.6").compareTo(PATTERN.getVersion("1.4")));
    }

    @Test
    void next() {
        // Shift the most significant part
        Version version = PATTERN.getVersion("1.9");
        version.next();
        // If equals
        assertEquals(PATTERN.getVersion("2.0"), version);

        // Shift the other part
        version = PATTERN.getVersion("1.4");
        version.next();
        // If equals
        assertEquals(PATTERN.getVersion("1.5"), version);
    }

    @Test
    void asID() {
        // Assert directly initialized
        assertEquals("1.4", PATTERN.getVersion("1.4").asID());
        // Assert shifted
        Version version = PATTERN.getVersion("1.5");
        version.next();
        assertEquals("1.6", version.asID());
    }

    @Test
    void copy() {
        // Create a version
        Version version = PATTERN.getVersion("1.2");
        // If equals
        assertEquals(version, version.copy());
    }

    @Test
    void getPattern() {
        assertEquals(PATTERN, PATTERN.getVersion("1.4").getPattern());
    }

    @Test
    void getCursor() {
        assertEquals(2, PATTERN.getVersion("1.2").getCursor(2));
    }

}