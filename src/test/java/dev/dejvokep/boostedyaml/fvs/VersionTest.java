package dev.dejvokep.boostedyaml.fvs;

import dev.dejvokep.boostedyaml.fvs.segment.Segment;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE), Segment.literal("."), Segment.range(0, 10));

    @Test
    void compareTo() {
        // Compare -1 cases
        assertEquals(-1, Objects.requireNonNull(PATTERN.getVersion("1.9")).compareTo(Objects.requireNonNull(PATTERN.getVersion("2.1"))));
        assertEquals(-1, Objects.requireNonNull(PATTERN.getVersion("1.4")).compareTo(Objects.requireNonNull(PATTERN.getVersion("1.6"))));
        // Compare 0 cases
        assertEquals(0, Objects.requireNonNull(PATTERN.getVersion("1.2")).compareTo(Objects.requireNonNull(PATTERN.getVersion("1.2"))));
        // Compare 1 cases
        assertEquals(1, Objects.requireNonNull(PATTERN.getVersion("2.1")).compareTo(Objects.requireNonNull(PATTERN.getVersion("1.9"))));
        assertEquals(1, Objects.requireNonNull(PATTERN.getVersion("1.6")).compareTo(Objects.requireNonNull(PATTERN.getVersion("1.4"))));
    }

    @Test
    void next() {
        // Shift the most significant part
        Version version = PATTERN.getVersion("1.9");
        // Assert
        assertNotNull(version);
        // Next
        version.next();
        // If equals
        assertEquals(PATTERN.getVersion("2.0"), version);

        // Shift the other part
        version = PATTERN.getVersion("1.4");
        // Assert
        assertNotNull(version);
        // Next
        version.next();
        // If equals
        assertEquals(PATTERN.getVersion("1.5"), version);
    }

    @Test
    void asID() {
        // Assert directly initialized
        assertEquals("1.4", Objects.requireNonNull(PATTERN.getVersion("1.4")).asID());
        // Version
        Version version = PATTERN.getVersion("1.5");
        // Assert
        assertNotNull(version);
        // Next
        version.next();
        // Assert
        assertEquals("1.6", version.asID());
    }

    @Test
    void copy() {
        // Create a version
        Version version = PATTERN.getVersion("1.2");
        // Assert
        assertNotNull(version);
        // If equals
        assertEquals(version, version.copy());
    }

    @Test
    void getPattern() {
        assertEquals(PATTERN, Objects.requireNonNull(PATTERN.getVersion("1.4")).getPattern());
    }

    @Test
    void getCursor() {
        assertEquals(2, Objects.requireNonNull(PATTERN.getVersion("1.2")).getCursor(2));
    }

}