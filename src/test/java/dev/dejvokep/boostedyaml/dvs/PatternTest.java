/*
 * Copyright 2022 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.dvs;

import dev.dejvokep.boostedyaml.dvs.segment.Segment;
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
        // Create a version
        version = PATTERN.getVersion("12.9");
        // Assert all cursors
        assertNotNull(version);
        assertEquals(11, version.getCursor(0));
        assertEquals(0, version.getCursor(1));
        assertEquals(9, version.getCursor(2));
    }

    @Test
    void getFirstVersion() {
        assertEquals(PATTERN.getVersion("1.0"), PATTERN.getFirstVersion());
    }
}