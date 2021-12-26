package com.davidcubesvk.yamlUpdater.core.serialization;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Interface used by custom serializer implementations.
 */
public interface YamlSerializer {

    /**
     * Attempts to deserialize the given map into a class instance using this serializer.
     * <p>
     * The given map is a raw object map - there are no {@link Block} instances, just the values themselves.
     * <p>
     * If the serializer does not recognize the map, or could not deserialize the map, <b>this method must return
     * <code>null</code></b>.
     *
     * @param map the raw map to deserialize
     * @return the deserialized object
     */
    @Nullable
    Object deserialize(@NotNull Map<Object, Object> map);

    /**
     * Attempts to serialize the given object into a map and returns it.
     * <p>
     * The given object is guaranteed to be:
     * <ol>
     *     <li>of type contained within {@link #getSupportedClasses()},</li>
     *     <li>a sub-class of any of {@link #getSupportedAbstractClasses()}.</li>
     * </ol>
     * Map supplier is provided to supply default map as configured in {@link GeneralSettings#getDefaultMapSupplier()}.
     * Serializers should not use their own map implementations, they should return the map provided by the supplier.
     * <p>
     * If could not serialize into a map, <b>this method must return <code>null</code></b>.
     *
     * @param object   the object to serialize
     * @param supplier the supplier used to supply default maps
     * @return the serialized object
     */
    @Nullable
    <T> Map<Object, Object> serialize(@NotNull T object, @NotNull MapSupplier supplier);

    /**
     * Returns a set of all classes this serializer supports and can (de-)serialize.
     * <p>
     * The returned set cannot be <code>null</code> as indicated by the annotation.
     *
     * @return the set of supported classes
     */
    @NotNull
    Set<Class<?>> getSupportedClasses();

    /**
     * Returns a set of all abstract classes (or interfaces) this serializer supports and can (de-)serialize.
     * <p>
     * The returned set cannot be <code>null</code> as indicated by the annotation.
     *
     * @return the set of supported classes
     */
    @NotNull
    Set<Class<?>> getSupportedAbstractClasses();

}
