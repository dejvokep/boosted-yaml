package com.davidcubesvk.yamlUpdater.core.path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathTest {

    @Test
    void from() {
        // Create path
        Path path = Path.from(true, 5);
        // Assert
        assertEquals(true, path.get(0));
        assertEquals(5, path.get(1));
        assertEquals(2, path.length());
        // Create path
        path = Path.from(new Object[]{"a", false});
        // Assert
        assertEquals("a", path.get(0));
        assertEquals(false, path.get(1));
        assertEquals(2, path.length());
        // Verify
        assertThrows(IllegalArgumentException.class, () -> Path.from(new Object[]{}));
    }

    @Test
    void fromSingleKey() {
        // Create path
        Path path = Path.fromSingleKey(false);
        // Assert
        assertEquals(false, path.get(0));
        assertEquals(1, path.length());
    }

    @Test
    void fromString() {
        assertContents(Path.fromString("a.true.6"));
        assertContents(Path.fromString("a,true,6", ','));
        assertContents(Path.fromString("a-true-6", new PathFactory('-')));
    }

    private void assertContents(Path path) {
        assertEquals("a", path.get(0));
        assertEquals("true", path.get(1));
        assertEquals("6", path.get(2));
        assertEquals(3, path.length());
    }

    @Test
    void addTo() {
        // Create path
        Path path = Path.addTo(Path.from(true, 7), "d");
        // Assert
        assertEquals(true, path.get(0));
        assertEquals(7, path.get(1));
        assertEquals("d", path.get(2));
        assertEquals(3, path.length());
    }

    @Test
    void length() {
        assertEquals(Path.from("c", 7, false).length(), 3);
    }

    @Test
    void get() {
        // Create path
        Path path = Path.from(true, 5).add("b");
        // Assert
        assertEquals(true, path.get(0));
        assertEquals(5, path.get(1));
        assertEquals("b", path.get(2));
    }

    @Test
    void add() {
        // Create path
        Path path = Path.from(true, 5).add("b");
        // Assert
        assertEquals(true, path.get(0));
        assertEquals(5, path.get(1));
        assertEquals("b", path.get(2));
        assertEquals(3, path.length());
    }

    @Test
    void parent() {
        assertEquals(Path.from("c", 7, false).parent(), Path.from("c", 7));
    }
}