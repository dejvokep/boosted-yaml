package com.davidcubesvk.yamlUpdater.core.serialization;

import java.util.Map;

/**
 * Interface used to (de)serialize custom objects.
 * <p>
 * Please note that classes implementing this interface must also be registered in the appropriate serializer used to
 * load/save files (see the corresponding wiki page or {@link com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings.Builder#setSerializer(YamlSerializer)}.
 */
public interface Serializable {

    /**
     * Deserializes the given map into instance of this class (that's implementing this interface).
     * <p>
     * The given map is a raw object map - there are no {@link com.davidcubesvk.yamlUpdater.core.block.Block} instances, just the values themselves.
     *
     * @param map the raw map to deserialize
     * @return the deserialized object (must be an instance of the class implementing this interface)
     */
    Object deserialize(Map<Object, Object> map);

    /**
     * Serializes the current state of this instance into a map and returns it.
     * <p>
     * The returned map does not need to (but may) contain the class identifier. Class identifier is one entry in the
     * top-level map (the one returned), where the key is defined by the serializer (but <code>==</code> by default) and
     * value identifies the class that map represents (and into which should be deserialized when needed) - either the
     * full classname (e.g. <code>me.name.project.objects.CustomObject</code>) or it's alias (<b>if alias is used, it must also be registered</b>).
     * <p>
     * If the returned map does not contain the identifier, the value will always be the full classname. This altering
     * possibility is generally useful if the alias must be used instead of the classname (to maintain compatibility
     * between versions).
     *
     * @return the map containing the serialized map
     */
    Map<Object, Object> serialize();

}