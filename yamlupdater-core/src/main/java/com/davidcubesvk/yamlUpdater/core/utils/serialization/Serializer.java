package com.davidcubesvk.yamlUpdater.core.utils.serialization;

import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;

import java.util.HashMap;
import java.util.Map;

public class Serializer implements YamlSerializer {

    public static final Object DEFAULT_SERIALIZED_TYPE_KEY = "==";
    public static final Serializer DEFAULT = new Serializer(DEFAULT_SERIALIZED_TYPE_KEY);

    private final Map<String, Class<? extends Serializable>> classes = new HashMap<>();
    private final Object serializedTypeKey;

    public Serializer(Object serializedTypeKey) {
        this.serializedTypeKey = serializedTypeKey;
    }

    public void register(Class<? extends Serializable> clazz) {
        register(clazz, clazz.getCanonicalName());
    }
    public void register(Class<? extends Serializable> clazz, String alias) {
        classes.put(alias, clazz);
    }

    @Override
    public Object deserialize(Map<Object, Object> map) {
        //If not deserializable
        if (!isDeserializable(map))
            return null;

        //The class
        Class<? extends Serializable> clazz = classes.get(map.get(serializedTypeKey).toString());
        //Cast
        return Serializable.class.cast(clazz).deserialize(map);
    }

    @Override
    public Map<Object, Object> serialize(Object object, MapSupplier supplier) {
        //Create a map
        Map<Object, Object> serialized = supplier.supply(1);
        //Add
        serialized.putAll(((Serializable) object).serialize());
        serialized.computeIfAbsent(getClassKey(), k -> object.getClass().getCanonicalName());
        //Return
        return serialized;
    }

    @Override
    public Class<?> getSerializableClass() {
        return Serializable.class;
    }

    @Override
    public Object getClassKey() {
        return serializedTypeKey;
    }

    public boolean isDeserializable(Map<Object, Object> map) {
        return map.containsKey(serializedTypeKey) && classes.containsKey(map.get(serializedTypeKey).toString());
    }

}