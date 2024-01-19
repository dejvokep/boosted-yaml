/*
 * Copyright 2024 https://dejvokep.dev/
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