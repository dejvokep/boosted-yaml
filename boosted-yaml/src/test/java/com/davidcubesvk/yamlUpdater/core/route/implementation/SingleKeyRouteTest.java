package com.davidcubesvk.yamlUpdater.core.route.implementation;

import com.davidcubesvk.yamlUpdater.core.route.Route;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleKeyRouteTest {

    @Test
    void length() {
        assertEquals(1, Route.from("a").length());
    }

    @Test
    void get() {
        // Create route
        Route route = Route.from("a");
        // Assert
        assertEquals("a", route.get(0));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> route.get(1));
    }

    @Test
    void parent() {
        assertThrows(IllegalArgumentException.class, () -> Route.from("a").parent());
    }

    @Test
    void add() {
        assertEquals(Route.from("a", 5), Route.from("a").add(5));
    }
}