package com.davidcubesvk.yamlUpdater.core.serialization;

import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard library serializer.
 */
public class BaseSerializer implements YamlSerializer {

    /**
     * Default key for serialized class identifier. Used by the default serializer {@link #DEFAULT}.
     */
    public static final String DEFAULT_SERIALIZED_TYPE_KEY = "==";

    /**
     * The default serializer.
     */
    public static final BaseSerializer DEFAULT = new BaseSerializer(DEFAULT_SERIALIZED_TYPE_KEY);

    //Registered classes
    private final Map<String, Class<? extends Serializable>> classes = new HashMap<>();
    //Serialized class identifier key
    private final Object serializedTypeKey;

    /**
     * Creates a serializer.
     *
     * @param serializedTypeKey the key for serialized class identifier
     */
    public BaseSerializer(Object serializedTypeKey) {
        this.serializedTypeKey = serializedTypeKey;
    }

    /**
     * Registers this class for serialization. The class now will be recognizable by its full classname
     * (e.g. <code>me.name.project.objects.Custom</code>). If you want to set an alias (maybe for compatibility
     * reasons), please use {@link #register(Class, String)} instead.
     * <p>
     * Returns <code>true</code> if successful, <code>false</code> otherwise - if the class does not contain
     * deserialization method as defined by {@link Serializable}:
     * <p>
     * <code>public static deserialize(Map&lt;Object, Object&gt;);</code>
     *
     * @param clazz the class to register with its full classname
     * @return if registration was successful
     */
    public boolean register(Class<? extends Serializable> clazz) {
        return register(clazz, clazz.getCanonicalName());
    }

    /**
     * Registers this class for serialization with the specified alias - the class now will be recognizable by given
     * alias. It is not necessary to call {@link #register(Class)}, just one class identification is enough.
     * <p>
     * Returns <code>true</code> if successful, <code>false</code> otherwise - if the class does not contain
     * deserialization method as defined by {@link Serializable}:
     * <p>
     * <code>public static Object deserialize(Map&lt;Object, Object&gt;);</code>
     *
     * @param clazz the class to register
     * @param alias alias for the class to register
     * @return if registration was successful
     */
    public boolean register(Class<? extends Serializable> clazz, String alias) {
        // Verify
        if (!verifyClass(clazz))
            return false;

        // Register
        classes.put(alias, clazz);
        return true;
    }

    private boolean verifyClass(Class<? extends Serializable> clazz) {
        // Verify
        try {
            // Method
            Method method = clazz.getDeclaredMethod("deserialize", Map.class);
            // Must be public and static
            return Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers());
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    @Override
    public Object deserialize(Map<Object, Object> map) {
        //If not deserializable
        if (!map.containsKey(serializedTypeKey) || !classes.containsKey(map.get(serializedTypeKey).toString()))
            return null;

        //The class
        Class<? extends Serializable> clazz = classes.get(map.get(serializedTypeKey).toString());
        //Deserialize
        try {
            return clazz.getDeclaredMethod("deserialize", Map.class).invoke(null, map);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
        // Cannot happen
        return null;
    }

    @Override
    public Map<Object, Object> serialize(Object object, MapSupplier supplier) {
        //If not serializable
        if (!(object instanceof Serializable))
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