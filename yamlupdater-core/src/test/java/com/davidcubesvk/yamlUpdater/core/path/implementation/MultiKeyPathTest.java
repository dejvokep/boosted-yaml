package com.davidcubesvk.yamlUpdater.core.path.implementation;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiKeyPathTest {

    @Test
    void length() {
        assertEquals(2, Path.from("a", "b").length());
    }

    @Test
    void get() {
        // Create path
        Path path = Path.from("a", "b");
        // Assert
        assertEquals("a", path.get(0));
        assertEquals("b", path.get(1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> path.get(2));
    }

    @Test
    void add() {
        // Create path
        Path path = Path.from("a").add("c");
        // Assert
        assertEquals(Path.from("a", "c"), path);
        assertEquals(2, path.length());
        assertEquals(MultiKeyPath.class, path.getClass());
    }

    @Test
    void parent() {
        // Create path
        Path path = Path.from("a", "b");
        // Assert
        assertEquals(Path.from("a"), path.parent());
        assertEquals(SingleKeyPath.class, path.parent().getClass());
    }
}