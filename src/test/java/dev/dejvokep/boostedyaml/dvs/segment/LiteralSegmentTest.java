/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.dvs.segment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiteralSegmentTest {

    @Test
    void parse() {
        //Create
        Segment segment = new LiteralSegment("A1", "B", "C2");
        //Assert
        assertEquals(-1, segment.parse("C3", 0));
        assertEquals(-1, segment.parse("B1", 1));
        assertEquals(1, segment.parse("B", 0));
    }

    @Test
    void getElement() {
        //Create
        Segment segment = new LiteralSegment("A1", "B", "C2");
        //Assert
        assertEquals("A1", segment.getElement(0));
        assertEquals("B", segment.getElement(1));
        assertEquals("C2", segment.getElement(2));
    }

    @Test
    void getElementLength() {
        //Create
        Segment segment = new LiteralSegment("A1", "B", "C2");
        //Assert
        assertEquals(2, segment.getElementLength(0));
        assertEquals(1, segment.getElementLength(1));
        assertEquals(2, segment.getElementLength(2));
    }

    @Test
    void length() {
        assertEquals(3, new LiteralSegment("A", "B", "C").length());
    }
}