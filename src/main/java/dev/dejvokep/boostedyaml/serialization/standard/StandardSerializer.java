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

import dev.dejvokep.boostedyaml.serialization.YamlSerializer;
import dev.dejvokep.boostedyaml.utils.supplier.MapSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Standard serializer.
 */
public class StandardSerializer implements YamlSerializer {

    /**
     * Default key for serialized class identifier. Used by the default serializer {@link #defaultSerializer}.
     */
    public static final String DEFAULT_SERIALIZED_TYPE_KEY = "==";

    /**
     * The default serializer.
     */
    private static final StandardSerializer defaultSerializer = new StandardSerializer(DEFAULT_SERIALIZED_TYPE_KEY);

    //Registered classes
    private final Map<Class<?>, TypeAdapter<?>> adapters = new HashMap<>();
    private final Map<String, Class<?>> aliases = new HashMap<>();
    //Serialized type key
    private final Object serializedTypeKey;

    /**
     * Creates a serializer.
     *
     * @param serializedTypeKey the key for serialized class identifier
     */
    public StandardSerializer(@NotNull Object serializedTypeKey) {
        this.serializedTypeKey = serializedTypeKey;
    }

    /**
     * Registers the given type for serialization. The type now will be recognizable by its full classname (e.g.
     * <code>me.name.project.objects.Custom</code>).
     * <p>
     * If you also want to set an alias (maybe for compatibility reasons), please use {@link #register(String, Class)}
     * afterwards.
     *
     * @param clazz   the class of the type to register
     * @param adapter adapter for the type
     * @param <T>     type to register
     */
    public <T> void register(@NotNull Class<T> clazz, @NotNull TypeAdapter<T> adapter) {
        adapters.put(clazz, adapter);
        aliases.put(clazz.getCanonicalName(), clazz);
    }

    /**
     * Registers the specified alias for the given type (represented by the class).
     * <p>
     * <b>The type must already be registered.</b>
     *
     * @param clazz the class to register
     * @param alias alias for the class to register
     * @param <T>   type to register
     */
    public <T> void register(@NotNull String alias, @NotNull Class<T> clazz) {
        //If not registered
        if (!adapters.containsKey(clazz))
            throw new IllegalStateException("Cannot register an alias for yet unregistered type!");

        aliases.put(alias, clazz);
    }

    @Nullable
    @Override
    public Object deserialize(@NotNull Map<Object, Object> map) {
        //If not deserializable
        if (!map.containsKey(serializedTypeKey))
            return null;
        //Type
        Class<?> type = aliases.get(map.get(serializedTypeKey).toString());
        //If null
        if (type == null)
            return null;
        //Deserialize
        return adapters.get(type).deserialize(map);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> Map<Object, Object> serialize(@NotNull T object, @NotNull MapSupplier supplier) {
        //No adapter
        if (!adapters.containsKey(object.getClass()))
            return null;

        //Create a map
        Map<Object, Object> serialized = supplier.supply(1);
        //Add (safe to cast)
        serialized.putAll(((TypeAdapter<T>) adapters.get(object.getClass())).serialize(object));
        serialized.computeIfAbsent(serializedTypeKey, k -> object.getClass().getCanonicalName());
        //Return
        return serialized;
    }

    @NotNull
    @Override
    public Set<Class<?>> getSupportedClasses() {
        return adapters.keySet();
    }

    @NotNull
    @Override
    public Set<Class<?>> getSupportedParentClasses() {
        return Collections.emptySet();
    }

    /**
     * Returns the default serializer.
     *
     * @return the default serializer
     */
    public static StandardSerializer getDefault() {
        return defaultSerializer;
    }
}