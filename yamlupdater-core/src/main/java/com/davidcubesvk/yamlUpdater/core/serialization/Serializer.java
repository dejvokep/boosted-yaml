package com.davidcubesvk.yamlUpdater.core.serialization;

import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard library serializer.
 */
public class Serializer implements YamlSerializer {

    /**
     * Default key for serialized class identifier. Used by the default serializer {@link #DEFAULT}.
     */
    public static final String DEFAULT_SERIALIZED_TYPE_KEY = "==";

    /**
     * The default serializer.
     */
    public static final Serializer DEFAULT = new Serializer(DEFAULT_SERIALIZED_TYPE_KEY);

    //Registered classes
    private final Map<String, Class<? extends Serializable>> classes = new HashMap<>();
    //Serialized class identifier key
    private final Object serializedTypeKey;

    /**
     * Creates a serializer.
     *
     * @param serializedTypeKey the key for serialized class identifier
     */
    public Serializer(Object serializedTypeKey) {
        this.serializedTypeKey = serializedTypeKey;
    }

    /**
     * Registers this class for serialization. The class now will be recognizable by it's full classname
     * (e.g. <code>me.name.project.objects.CustomObject</code>). If you want to set an alias (maybe for compatibility
     * reasons), please use {@link #register(Class, String)} instead.
     *
     * @param clazz the class to register with it's full classname
     */
    public void register(Class<? extends Serializable> clazz) {
        register(clazz, clazz.getCanonicalName());
    }

    /**
     * Registers this class for serialization with the specified alias - the class now will be recognizable by given
     * alias. It is not necessary to call {@link #register(Class)}, just one class identification is enough.
     *
     * @param clazz the class to register
     * @param alias alias for the class to register
     */
    public void register(Class<? extends Serializable> clazz, String alias) {
        classes.put(alias, clazz);
    }

    @Override
    public Object deserialize(Map<Object, Object> map) {
        //If not deserializable
        if (!map.containsKey(serializedTypeKey) || !classes.containsKey(map.get(serializedTypeKey).toString()))
            return null;

        //The class
        Class<? extends Serializable> clazz = classes.get(map.get(serializedTypeKey).toString());
        //Cast
        return Serializable.class.cast(clazz).deserialize(map);
    }

    @Override
    public Map<Object, Object> serialize(Object object, MapSupplier supplier) {
        //If not serializable
        if (!(object instanceof Serializable) || !classes.containsKey(object.getClass()))
            return null;

        //Create a map
        Map<Object, Object> serialized = supplier.supply(1);
        //Add
        serialized.putAll(((Serializable) object).serialize());
        serialized.computeIfAbsent(getClassIdentifierKey(), k -> object.getClass().getCanonicalName());
        //Return
        return serialized;
    }

    @Override
    public Class<?> getSerializableClass() {
        return Serializable.class;
    }

    @Override
    public Object getClassIdentifierKey() {
        return serializedTypeKey;
    }

}