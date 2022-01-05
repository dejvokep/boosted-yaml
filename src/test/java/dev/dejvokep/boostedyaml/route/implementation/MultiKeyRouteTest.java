/*
 * Copyright 2021 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.route.implementation;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiKeyRouteTest {

    @Test
    void length() {
        Assertions.assertEquals(2, Route.from("a", "b").length());
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