package com.davidcubesvk.yamlUpdater.backend;

import com.davidcubesvk.yamlUpdater.core.serialization.YamlSerializer;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.HashMap;
import java.util.Map;

public class SpigotSerializer implements YamlSerializer {

    @Override
    public Object deserialize(Map<Object, Object> map) {
        //If does not contain the key
        if (!map.containsKey(getClassIdentifierKey()))
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
    public Map<Object, Object> serialize(Object object, MapSupplier supplier) {
        //Create a map
        Map<Object, Object> serialized = supplier.supply(1);
        //Add
        serialized.putAll(((ConfigurationSerializable) object).serialize());
        serialized.computeIfAbsent(getClassIdentifierKey(), k -> object.getClass().getCanonicalName());
        //Return
        return serialized;
    }

    @Override
    public Class<?> getSerializableClass() {
        return ConfigurationSerializable.class;
    }

    @Override
    public Object getClassIdentifierKey() {
        return ConfigurationSerialization.SERIALIZED_TYPE_KEY;
    }
}
