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

import dev.dejvokep.boostedyaml.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface used to (de-)serialize custom types with {@link StandardSerializer}.
 * <p>
 * Please note that classes implementing this interface must also be registered at the serializer instance used. See
 * {@link StandardSerializer#register(Class, TypeAdapter)}.
 *
 * @param <T> type of the adapter
 */
public interface TypeAdapter<T> {

    /**
     * Serializes the given instance into a map.
     * <p>
     * The returned map does not need to (but may) contain the type identifier <a href="https://dejvokep.gitbook.io/boostedyaml/">wiki</a>.
     * Type identifier is one entry in the top-level map (the one returned), where the key is defined by the serializer
     * (<code>==</code> for {@link StandardSerializer#getDefault()}) and the value identifies the serialized type - either by
     * the full canonical classname (e.g. <code>me.name.project.objects.CustomObject</code>) or it's alias. <b>Both must
     * also be registered</b>.
     * <p>
     * If the returned map does not contain the identifier, the {@link StandardSerializer serializer} will automatically
     * use the full classname.
     *
     * @param object object to serialize
     * @return the serialized object
     */
    @NotNull
    Map<Object, Object> serialize(@NotNull T object);

    /**
     * Deserializes the given map into instance of this type.
     * <p>
     * The given map is a raw object map; there are no {@link Block} instances, just native Java objects themselves.
     * <p>
     * Use {@link #toStringKeyedMap(Map)} to convert the map.
     *
     * @param map the raw map to deserialize
     * @return the deserialized object
     */
    @NotNull
    T deserialize(@NotNull Map<Object, Object> map);

    /**
     * Converts this map (including all sub-maps) to {@link String}=value {@link HashMap}.
     *
     * @param map the map to convert
     * @return the converted map
     */
    @NotNull
    default Map<String, Object> toStringKeyedMap(@NotNull Map<?, ?> map) {
        // New map
        Map<String, Object> newMap = new HashMap<>();
        // Iterate
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            // If the value is a map
            if (entry.getValue() instanceof Map)
                newMap.put(entry.getKey().toString(), toStringKeyedMap((Map<?, ?>) entry.getValue()));
            else
                newMap.put(entry.getKey().toString(), entry.getValue());
        }
        return newMap;
    }
}