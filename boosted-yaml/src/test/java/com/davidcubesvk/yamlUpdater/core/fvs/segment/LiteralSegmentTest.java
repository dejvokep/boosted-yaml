package com.davidcubesvk.yamlUpdater.core.fvs.segment;

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