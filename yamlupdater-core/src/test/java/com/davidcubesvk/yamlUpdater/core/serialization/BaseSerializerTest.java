package com.davidcubesvk.yamlUpdater.core.serialization;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseSerializerTest {

    @Test
    void register() {
        // Create serializer
        BaseSerializer serializer = new BaseSerializer("!=");
        // Register
        assertTrue(serializer.register(CustomSerializable.class));
        assertTrue(serializer.register(CustomSerializable.class, "custom"));
        assertFalse(serializer.register(IncorrectSerializable.class));
        // Try to deserialize
        assertNotNull(serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", CustomSerializable.class.getCanonicalName());
            put("value", 5);
        }}));
        assertNotNull(serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", "custom");
            put("value", 5);
        }}));
    }

    @Test
    void deserialize() {
        // Create serializer
        BaseSerializer serializer = new BaseSerializer("!=");
        // Register
        serializer.register(CustomSerializable.class);
        serializer.register(CustomSerializable.class, "custom");
        // Try to deserialize
        assertEquals(10, ((CustomSerializable) serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", CustomSerializable.class.getCanonicalName());
            put("value", 10);
        }})).value);
        assertEquals(15, ((CustomSerializable) serializer.deserialize(new HashMap<Object, Object>(){{
            put("!=", "custom");
            put("value", 15);
        }})).value);
    }

    @Test
    void serialize() {
        // Create serializer
        BaseSerializer serializer = new BaseSerializer("!=");
        // Register
        serializer.register(CustomSerializable.class);
        // Try to serialize
        assertEquals(new HashMap<Object, Object>(){{
            put("!=", CustomSerializable.class.getCanonicalName());
            put("value", 20);
        }}, serializer.serialize(new CustomSerializable(20), HashMap::new));
        assertEquals(new HashMap<Object, Object>(){{
            put("!=", CustomSerializable.class.getCanonicalName());
            put("value", 50);
        }}, serializer.serialize(new CustomSerializable(50), HashMap::new));
    }

    @Test
    void getSerializableClass() {
        assertEquals(Serializable.class, new BaseSerializer("!=").getSerializableClass());
    }

    @Test
    void getClassIdentifierKey() {
        assertEquals("!=", new BaseSerializer("!=").getClassIdentifierKey());
    }

    public static class IncorrectSerializable implements Serializable {

        private final int value;

        private IncorrectSerializable(int value) {
            this.value = value;
        }

        public Object deserialize(Map<Object, Object> map) {
            return new IncorrectSerializable((int) map.get("value"));
        }

        @Override
        public Map<Object, Object> serialize() {
            return new HashMap<Object, Object>(){{
                put("value", value);
            }};
        }
    }

    public static class CustomSerializable implements Serializable {

        private final int value;

        private CustomSerializable(int value) {
            this.value = value;
        }

        public static Object deserialize(Map<Object, Object> map) {
            return new CustomSerializable((int) map.get("value"));
        }

        @Override
        public Map<Object, Object> serialize() {
            return new HashMap<Object, Object>(){{
                put("value", value);
            }};
        }

    }
}