package com.davidcubesvk.yamlUpdater.core.path.implementation;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleKeyPathTest {

    @Test
    void length() {
        assertEquals(1, Path.from("a").length());
    }

    @Test
    void get() {
        // Create path
        Path path = Path.from("a");
        // Assert
        assertEquals("a", path.get(0));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> path.get(1));
    }

    @Test
    void parent() {
        assertThrows(IllegalArgumentException.class, () -> Path.from("a").parent());
    }

    @Test
    void add() {
        assertEquals(Path.from("a", 5), Path.from("a").add(5));
    }
}