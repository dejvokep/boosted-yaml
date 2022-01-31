/*
 * Copyright 2021 https://dejvokep.dev/
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

class RangeSegmentTest {

    @Test
    void parse() {
        //Create
        Segment segment = new RangeSegment(0, 5, 2, 0);
        //Assert
        assertEquals(-1, segment.parse("B1", 1));
        assertEquals(-1, segment.parse("B", 0));
        assertEquals(-1, segment.parse("1", 0));
        assertEquals(2, segment.parse("4", 0));
        assertEquals(1, segment.parse("2", 0));
        //Create
        segment = new RangeSegment(0, 5, 2, 2);
        //Assert
        assertEquals(-1, segment.parse("01", 1));
        assertEquals(-1, segment.parse("01", 0));
        assertEquals(-1, segment.parse("B", 0));
        assertEquals(-1, segment.parse("1", 0));
        assertEquals(-1, segment.parse("2", 0));
        assertEquals(-1, segment.parse("4", 0));
        assertEquals(2, segment.parse("04", 0));
        assertEquals(1, segment.parse("02", 0));
    }

    @Test
    void getElement() {
        //Create
        Segment segment = new RangeSegment(0, 5, 2, 0);
        //Assert
        assertEquals("0", segment.getElement(0));
        assertEquals("2", segment.getElement(1));
        assertEquals("4", segment.getElement(2));
        //Create
        segment = new RangeSegment(0, 5, 2, 2);
        //Assert
        assertEquals("00", segment.getElement(0));
        assertEquals("02", segment.getElement(1));
        assertEquals("04", segment.getElement(2));
    }

    @Test
    void getElementLength() {
        //Create
        Segment segment = new RangeSegment(0, 12, 2, 0);
        //Assert
        assertEquals(1, segment.getElementLength(0));
        assertEquals(1, segment.getElementLength(2));
        assertEquals(2, segment.getElementLength(5));
        //Create
        segment = new RangeSegment(0, 12, 2, 2);
        //Assert
        assertEquals(2, segment.getElementLength(0));
        assertEquals(2, segment.getElementLength(1));
        assertEquals(2, segment.getElementLength(2));
    }

    @Test
    void length() {
        assertEquals(6, new RangeSegment(0, 12, 2, 0).length());
        assertEquals(6, new RangeSegment(0, 11, 2, 0).length());
        assertEquals(5, new RangeSegment(0, 10, 2, 0).length());
        assertEquals(10, new RangeSegment(0, 10, 1, 0).length());
    }
}