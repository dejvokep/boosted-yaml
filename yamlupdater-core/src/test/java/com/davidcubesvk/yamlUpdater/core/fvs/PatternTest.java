package com.davidcubesvk.yamlUpdater.core.fvs;

import com.davidcubesvk.yamlUpdater.core.fvs.segment.Segment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    // Segments
    private static final Segment SEGMENT_FIRST = Segment.range(1, Integer.MAX_VALUE), SEGMENT_SECOND = Segment.literal("."), SEGMENT_THIRD = Segment.range(0, 10);
    // Pattern
    private static final Pattern PATTERN = new Pattern(SEGMENT_FIRST, SEGMENT_SECOND, SEGMENT_THIRD);

    @Test
    void getPart() {
        // Assert all
        assertEquals(SEGMENT_FIRST, PATTERN.getPart(0));
        assertEquals(SEGMENT_SECOND, PATTERN.getPart(1));
        assertEquals(SEGMENT_THIRD, PATTERN.getPart(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> PATTERN.getPart(3));
    }

    @Test
    void getVersion() {
        // Create a version
        Version version = PATTERN.getVersion("2.4");
        // Assert all cursors
        assertNotNull(version);
        assertEquals(1, version.getCursor(0));
        assertEquals(0, version.getCursor(1));
        assertEquals(4, version.getCursor(2));
    }

    @Test
    void getFirstVersion() {
        assertEquals(PATTERN.getVersion("1.0"), PATTERN.getFirstVersion());
    }
}