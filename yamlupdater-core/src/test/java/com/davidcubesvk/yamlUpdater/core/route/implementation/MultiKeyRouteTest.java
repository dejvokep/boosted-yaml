package com.davidcubesvk.yamlUpdater.core.route.implementation;

import com.davidcubesvk.yamlUpdater.core.route.Route;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiKeyRouteTest {

    @Test
    void length() {
        assertEquals(2, Route.from("a", "b").length());
    }

    @Test
    void get() {
        // Create route
        Route route = Route.from("a", "b");
        // Assert
        assertEquals("a", route.get(0));
        assertEquals("b", route.get(1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> route.get(2));
    }

    @Test
    void add() {
        // Create route
        Route route = Route.from("a").add("c");
        // Assert
        assertEquals(Route.from("a", "c"), route);
        assertEquals(2, route.length());
        assertEquals(MultiKeyRoute.class, route.getClass());
    }

    @Test
    void parent() {
        // Create route
        Route route = Route.from("a", "b");
        // Assert
        assertEquals(Route.from("a"), route.parent());
        assertEquals(SingleKeyRoute.class, route.parent().getClass());
    }
}