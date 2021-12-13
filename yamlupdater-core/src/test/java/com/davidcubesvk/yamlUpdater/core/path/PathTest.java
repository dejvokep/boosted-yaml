package com.davidcubesvk.yamlUpdater.core.path;

import com.davidcubesvk.yamlUpdater.core.path.implementation.SingleKeyPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PathTest {

    @Test
    void from() {
        // Create path
        Path path = Path.from(true, 5);
        // Assert
        assertEquals(2, path.length());
        assertEquals(true, path.get(0));
        assertEquals(5, path.get(1));
        // Create path
        path = Path.from(new Object[]{"a", false});
        // Assert
        assertEquals(2, path.length());
        assertEquals("a", path.get(0));
        assertEquals(false, path.get(1));
        // Create path
        path = Path.from(true);
        // Assert
        assertEquals(SingleKeyPath.class, path.getClass());
        assertEquals(1, path.length());
        assertEquals(true, path.get(0));
        assertEquals(Path.from(true, 7), path.add(7));
        assertEquals(Path.from(true), path);
        assertEquals(Path.from(new Object[]{true}), path);
        assertEquals(path, Path.fromSingleKey(true));

        // Verify
        assertThrows(IllegalArgumentException.class, () -> Path.from(new Object[]{}));
    }

    @Test
    void fromString() {
        assertEquals(SingleKeyPath.class, Path.fromString("a").getClass());
        assertEquals(Path.from("a"), Path.fromString("a"));
        assertPath(Path.fromString("a.true.6"));
        assertPath(Path.fromString("a,true,6", ','));
        assertPath(Path.fromString("a-true-6", new PathFactory('-')));
    }

    private void assertPath(Path path) {
        assertEquals(3, path.length());
        assertEquals("a", path.get(0));
        assertEquals("true", path.get(1));
        assertEquals("6", path.get(2));
    }

    @Test
    void addTo() {
        // Create path
        Path path = Path.addTo(Path.from(true, 7), "d");
        // Assert
        assertEquals(3, path.length());
        assertEquals(true, path.get(0));
        assertEquals(7, path.get(1));
        assertEquals("d", path.get(2));
        assertEquals(Path.from(true, 7), Path.addTo(Path.from(true), 7));
    }

    @Test
    void length() {
        assertEquals(1, Path.from("a").length());
        assertEquals(3, Path.from("c", 7, false).length());
    }

    @Test
    void get() {
        // Create path
        Path path = Path.from(true, 5).add("b");
        // Assert
        assertEquals(3, path.length());
        assertEquals(true, path.get(0));
        assertEquals(5, path.get(1));
        assertEquals("b", path.get(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> Path.from("a").get(1));
    }

    @Test
    void add() {
        // Create path
        Path path = Path.from(true, 5).add("b");
        // Assert
        assertEquals(3, path.length());
        assertEquals(true, path.get(0));
        assertEquals(5, path.get(1));
        assertEquals("b", path.get(2));
        assertEquals(Path.from(true, 7), Path.from(true).add(7));
    }

    @Test
    void parent() {
        assertEquals(Path.from("c", 7, false).parent(), Path.from("c", 7));
    }
}