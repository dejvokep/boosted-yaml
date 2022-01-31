/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.route;

import dev.dejvokep.boostedyaml.route.implementation.SingleKeyRoute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteTest {

    @Test
    void from() {
        // Create route
        Route route = Route.from(true, 5);
        // Assert
        assertEquals(2, route.length());
        assertEquals(true, route.get(0));
        assertEquals(5, route.get(1));
        // Create route
        route = Route.from(new Object[]{"a", false});
        // Assert
        assertEquals(2, route.length());
        assertEquals("a", route.get(0));
        assertEquals(false, route.get(1));
        // Create route
        route = Route.from(true);
        // Assert
        assertEquals(SingleKeyRoute.class, route.getClass());
        assertEquals(1, route.length());
        assertEquals(true, route.get(0));
        assertEquals(Route.from(true, 7), route.add(7));
        assertEquals(Route.from(true), route);
        assertEquals(Route.from(new Object[]{true}), route);
        assertEquals(route, Route.fromSingleKey(true));

        // Verify
        assertThrows(IllegalArgumentException.class, () -> Route.from(new Object[]{}));
    }

    @Test
    void fromString() {
        assertEquals(SingleKeyRoute.class, Route.fromString("a").getClass());
        assertEquals(Route.from("a"), Route.fromString("a"));
        assertRoute(Route.fromString("a.true.6"));
        assertRoute(Route.fromString("a,true,6", ','));
        assertRoute(Route.fromString("a-true-6", new RouteFactory('-')));
    }

    private void assertRoute(Route route) {
        assertEquals(3, route.length());
        assertEquals("a", route.get(0));
        assertEquals("true", route.get(1));
        assertEquals("6", route.get(2));
    }

    @Test
    void addTo() {
        // Create route
        Route route = Route.addTo(Route.from(true, 7), "d");
        // Assert
        assertEquals(3, route.length());
        assertEquals(true, route.get(0));
        assertEquals(7, route.get(1));
        assertEquals("d", route.get(2));
        assertEquals(Route.from(true, 7), Route.addTo(Route.from(true), 7));
    }

    @Test
    void length() {
        assertEquals(1, Route.from("a").length());
        assertEquals(3, Route.from("c", 7, false).length());
    }

    @Test
    void get() {
        // Create route
        Route route = Route.from(true, 5).add("b");
        // Assert
        assertEquals(3, route.length());
        assertEquals(true, route.get(0));
        assertEquals(5, route.get(1));
        assertEquals("b", route.get(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> Route.from("a").get(1));
    }

    @Test
    void add() {
        // Create route
        Route route = Route.from(true, 5).add("b");
        // Assert
        assertEquals(3, route.length());
        assertEquals(true, route.get(0));
        assertEquals(5, route.get(1));
        assertEquals("b", route.get(2));
        assertEquals(Route.from(true, 7), Route.from(true).add(7));
    }

    @Test
    void parent() {
        assertEquals(Route.from("c", 7, false).parent(), Route.from("c", 7));
    }
}