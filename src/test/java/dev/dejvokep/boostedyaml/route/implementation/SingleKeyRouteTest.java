package dev.dejvokep.boostedyaml.route.implementation;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleKeyRouteTest {

    @Test
    void length() {
        Assertions.assertEquals(1, Route.from("a").length());
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