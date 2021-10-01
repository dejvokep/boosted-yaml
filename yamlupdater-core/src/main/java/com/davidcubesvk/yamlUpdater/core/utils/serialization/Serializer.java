package com.davidcubesvk.yamlUpdater.core.utils.serialization;

import java.util.HashMap;
import java.util.Map;

public class Serializer implements YamlSerializer {

    private final Map<String, Class<? extends Serializable>> classes = new HashMap<>();
    private final Object serializedTypeKey;

    public Serializer(Object serializedTypeKey) {
        this.serializedTypeKey = serializedTypeKey;
    }

    @Override
    public Map<Object, Object> serialize(Object object) {
        //If not an instance of serializable
        if (!(object instanceof Serializable))
            return null;

        //Serialize
        Map<Object, Object> serialized = ((Serializable) object).serialize();
        //Compute if absent
        serialized.computeIfAbsent(serializedTypeKey, o -> object.getClass().getCanonicalName());
        //Return
        return serialized;
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

    public boolean isDeserializable(Map<Object, Object> map) {
        return map.containsKey(serializedTypeKey) && classes.containsKey(map.get(serializedTypeKey).toString());
    }

}