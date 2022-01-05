package dev.dejvokep.boostedyaml.route;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class RouteFactoryTest {

    @Test
    void create() {
        assertEquals(Route.from("a", "b"), new RouteFactory('.').create("a.b"));
    }

    @Test
    void getSeparator() {
        assertEquals(',', new RouteFactory(',').getSeparator());
    }

    @Test
    void getEscapedSeparator() {
        assertEquals(Pattern.quote(","), new RouteFactory(',').getEscapedSeparator());
    }
}