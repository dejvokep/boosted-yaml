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
package dev.dejvokep.boostedyaml.serialization.standard;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StandardSerializerTest {

    @Test
    void register() {
        // Create serializer
        StandardSerializer serializer = new StandardSerializer("!=");
        // Adapter
        TypeAdapter<CustomType> adapter = new TypeAdapter<CustomType>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull CustomType object) {
                Map<Object, Object> map = new HashMap<>();
                map.put("value", object.getValue());
                return map;
            }

            @NotNull
            @Override
            public CustomType deserialize(@NotNull Map<Object, Object> map) {
                return new CustomType((int) map.get("value"));
            }
        };
        // Register
        serializer.register(CustomType.class, adapter);
        serializer.register("custom", CustomType.class);
        // Assert
        assertNotNull(serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", CustomType.class.getCanonicalName());
            put("value", 5);
        }}));
        assertNotNull(serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", "custom");
            put("value", 7);
        }}));
    }

    @Test
    void deserialize() {
        // Create serializer
        StandardSerializer serializer = new StandardSerializer("!=");
        // Adapter
        TypeAdapter<CustomType> adapter = new TypeAdapter<CustomType>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull CustomType object) {
                Map<Object, Object> map = new HashMap<>();
                map.put("value", object.getValue());
                return map;
            }

            @NotNull
            @Override
            public CustomType deserialize(@NotNull Map<Object, Object> map) {
                return new CustomType((int) map.get("value"));
            }
        };
        // Register
        serializer.register(CustomType.class, adapter);
        serializer.register("custom", CustomType.class);
        // Deserialize
        CustomType deserialized = (CustomType) serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", CustomType.class.getCanonicalName());
            put("value", 5);
        }});
        // Assert
        assertNotNull(deserialized);
        assertEquals(5, deserialized.getValue());
        // Deserialize
        deserialized = (CustomType) serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", "custom");
            put("value", 7);
        }});
        // Assert
        assertNotNull(deserialized);
        assertEquals(7, deserialized.getValue());
    }

    @Test
    void serialize() {
        // Create serializer
        StandardSerializer serializer = new StandardSerializer("!=");
        // Adapter
        TypeAdapter<CustomType> adapter = new TypeAdapter<CustomType>() {
            @NotNull
            @Override
            public Map<Object, Object> serialize(@NotNull CustomType object) {
                Map<Object, Object> map = new HashMap<>();
                map.put("value", object.getValue());
                return map;
            }

            @NotNull
            @Override
            public CustomType deserialize(@NotNull Map<Object, Object> map) {
                return new CustomType((int) map.get("value"));
            }
        };
        // Register
        serializer.register(CustomType.class, adapter);
        // Try to serialize
        assertEquals(new HashMap<Object, Object>(){{
            put("!=", CustomType.class.getCanonicalName());
            put("value", 20);
        }}, serializer.serialize(new CustomType(20), HashMap::new));
        assertEquals(new HashMap<Object, Object>(){{
            put("!=", CustomType.class.getCanonicalName());
            put("value", 50);
        }}, serializer.serialize(new CustomType(50), HashMap::new));
    }

    public static class CustomType {

        private final int value;

        private CustomType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}