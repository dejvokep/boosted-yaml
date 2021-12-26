package com.davidcubesvk.yamlUpdater.core.serialization;

import com.davidcubesvk.yamlUpdater.core.serialization.standard.StandardSerializer;
import com.davidcubesvk.yamlUpdater.core.serialization.standard.TypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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