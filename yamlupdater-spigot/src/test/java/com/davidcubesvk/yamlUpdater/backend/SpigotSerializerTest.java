package com.davidcubesvk.yamlUpdater.backend;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpigotSerializerTest {

    @Test
    void deserialize() {
        // Register
        ConfigurationSerialization.registerClass(CustomType.class);
        ConfigurationSerialization.registerClass(CustomType.class, "custom");
        // Deserialize
        CustomType deserialized = (CustomType) ConfigurationSerialization.deserializeObject(new HashMap<String, Object>(){{
            put("==", CustomType.class.getName());
            put("value", 5);
        }});
        // Assert
        assertNotNull(deserialized);
        assertEquals(5, deserialized.getValue());
        // Deserialize
        deserialized = (CustomType) ConfigurationSerialization.deserializeObject(new HashMap<String, Object>(){{
            put("!=", "custom");
            put("value", 7);
        }});
        // Assert
        assertNotNull(deserialized);
        assertEquals(7, deserialized.getValue());
    }

    @Test
    void serialize() {
        // Register
        ConfigurationSerialization.registerClass(CustomType.class);
        // Try to serialize
        assertEquals(new HashMap<Object, Object>(){{
            put("value", 20);
        }}, new CustomType(20).serialize());
        assertEquals(new HashMap<Object, Object>(){{
            put("value", 50);
        }}, new CustomType(50).serialize());
    }

    @Test
    void getSupportedClasses() {
        assertEquals(Collections.emptySet(), SpigotSerializer.INSTANCE.getSupportedClasses());
    }

    @Test
    void getSupportedParentClasses() {
        assertEquals(1, SpigotSerializer.INSTANCE.getSupportedParentClasses().size());
        assertEquals(ConfigurationSerializable.class, SpigotSerializer.INSTANCE.getSupportedParentClasses().iterator().next());
    }

    public static class CustomType implements ConfigurationSerializable {

        private final int value;

        private CustomType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("value", value);
            return map;
        }

        public CustomType deserialize(Map<String, Object> map) {
            System.out.println("DES");
            return new CustomType((int) map.get("value"));
        }
    }

}