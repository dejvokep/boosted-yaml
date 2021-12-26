package com.davidcubesvk.yamlUpdater.backend;

import com.davidcubesvk.yamlUpdater.core.serialization.YamlSerializer;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Custom serializer which adds support for {@link ConfigurationSerialization}.
 */
public class SpigotSerializer implements YamlSerializer {

    /**
     * All supported abstract classes.
     */
    private static final Set<Class<?>> SUPPORTED_ABSTRACT_CLASSES = new HashSet<Class<?>>(){{
        add(ConfigurationSerializable.class);
    }};

    @Override
    @Nullable
    public Object deserialize(@NotNull Map<Object, Object> map) {
        //If does not contain the key
        if (!map.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY))
            return null;

        //If is not a valid class
        if (ConfigurationSerialization.getClassByAlias(map.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY).toString()) == null)
            return null;

        //Create a map
        Map<String, Object> converted = new HashMap<>();
        //Go through all entries
        for (Map.Entry<Object, Object> entry : map.entrySet())
            //Add
            converted.put(entry.getKey().toString(), entry.getValue());

        //Deserialize
        return ConfigurationSerialization.deserializeObject(converted);
    }

    @Override
    @NotNull
    public Map<Object, Object> serialize(@NotNull Object object, @NotNull MapSupplier supplier) {
        //Create a map
        Map<Object, Object> serialized = supplier.supply(1);
        //Add
        serialized.putAll(((ConfigurationSerializable) object).serialize());
        serialized.computeIfAbsent(ConfigurationSerialization.SERIALIZED_TYPE_KEY, k -> object.getClass().getCanonicalName());
        //Return
        return serialized;
    }

    @NotNull
    @Override
    public Set<Class<?>> getSupportedClasses() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public Set<Class<?>> getSupportedParentClasses() {
        return SUPPORTED_ABSTRACT_CLASSES;
    }

}
