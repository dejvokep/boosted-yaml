package com.davidcubesvk.yamlUpdater.core.serialization;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;

import java.util.Map;

public interface YamlSerializer {

    /**
     * Attempts to deserialize the given map into a class instance using this serializer.
     * <p>
     * The given map is a raw object map - there are no {@link com.davidcubesvk.yamlUpdater.core.block.Block} instances,
     * just the values themselves. The map is also guaranteed and this method will be called only if it contains a key
     * defined by {@link #getClassIdentifierKey()}.
     * <p>
     * If no class in this serializer is registered (no class was found to deserialize the given map), or could not
     * deserialize the map, <b>this method must return <code>null</code></b>.
     *
     * @param map the raw map to deserialize
     * @return the deserialized object (must be an instance of {@link Serializable})
     */
    Object deserialize(Map<Object, Object> map);

    /**
     * Attempts to serialize the given object into a map and returns it.
     * <p>
     * The given object is guaranteed to be instance of class implementing or extending class defined by {@link #getSerializableClass()} (as this method will only be called if it is).
     * Map supplier is provided to supply default map as configured in {@link GeneralSettings#getDefaultMapSupplier()}.
     * Plugins should not (but may) use their own map implementations, they should use the map provided by the supplier.
     * <p>
     * If no class in this serializer is registered (no class was found to serialize the object), or could not
     * serialize into a map, <b>this method must return <code>null</code></b> (with an exception following).
     *
     * @param object   the object to serialize
     * @param supplier the supplier used to supply default maps
     * @return the serialized object
     */
    Map<Object, Object> serialize(Object object, MapSupplier supplier);

    /**
     * Returns the class which is guaranteed to be implemented/extended by all classes (simply, be a <b>superclass</b>
     * to all of those classes) and it must be guaranteed that this serializer can serialize/deserialize all instances of those.
     * <p>
     * For example, {@link Serializer} uses {@link Serializable} as this class. It can serialize/deserialize all objects
     * of this type, because the interface has method for serialization built-in.
     *
     * @return the superclass of all classes that are guaranteed to be able to be serialized/deserialized using this serializer
     */
    Class<?> getSerializableClass();

    /**
     * Returns the direct key for the class identifier. Class identifier is a map entry in each serialized object's map
     * (top-level map) and it contains information about the class that is represented by this map (processed by the
     * serializer).
     *
     * @return the direct key for the class identifier entry in the top-level map of any serialized object
     */
    Object getClassIdentifierKey();

}
