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
package dev.dejvokep.boostedyaml.engine;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendedConstructorTest {

    @Test
    void construct() {
        assertEquals(1.234, buildConstructor().construct(new ScalarNode(Tag.FLOAT, "1.234", ScalarStyle.PLAIN)));
        assertEquals(1, ((CustomType) buildConstructor().construct(new MappingNode(Tag.MAP, Arrays.asList(
                new NodeTuple(new ScalarNode(Tag.STR, "!=", ScalarStyle.PLAIN), new ScalarNode(Tag.STR, CustomType.class.getCanonicalName(), ScalarStyle.PLAIN)),
                new NodeTuple(new ScalarNode(Tag.STR, "value", ScalarStyle.PLAIN), new ScalarNode(Tag.INT, "1", ScalarStyle.PLAIN))),
                FlowStyle.BLOCK))).value);
    }

    @Test
    void constructObjectNoCheck() {
        assertEquals(false, buildConstructor().construct(new ScalarNode(Tag.BOOL, "false", ScalarStyle.PLAIN)));
    }

    @Test
    void getConstructed() {
        ExtendedConstructor constructor = buildConstructor();
        Node node = new ScalarNode(Tag.INT, "123", ScalarStyle.PLAIN);
        constructor.construct(node);
        assertEquals(123, constructor.getConstructed(node));
    }

    private ExtendedConstructor buildConstructor() {
        StandardSerializer serializer = new StandardSerializer("!=");
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
        serializer.register(CustomType.class, adapter);
        return new ExtendedConstructor(LoadSettings.builder().build(), serializer);
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