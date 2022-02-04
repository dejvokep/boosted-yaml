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
package dev.dejvokep.boostedyaml.serialization.standard;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TypeAdapterTest {

    @Test
    void toStringKeyedMap() {
        // Dummy adapter
        TypeAdapter<Object> adapter = new TypeAdapter<Object>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull Object object) {
                return Collections.emptyMap();
            }

            @NotNull
            @Override
            public Object deserialize(@NotNull Map<Object, Object> map) {
                return "";
            }
        };

        // Assert
        assertEquals(new HashMap<String, Object>(){{
            put("a", 5);
            put("true", new HashMap<String, Object>(){{
                put("4", true);
                put("y", new ArrayList<Object>(){{
                    add(7);
                    add(false);
                }});
            }});
        }}, adapter.toStringKeyedMap(new HashMap<Object, Object>(){{
            put("a", 5);
            put(true, new HashMap<Object, Object>(){{
                put(4, true);
                put("y", new ArrayList<Object>(){{
                    add(7);
                    add(false);
                }});
            }});
        }}));
    }
}