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