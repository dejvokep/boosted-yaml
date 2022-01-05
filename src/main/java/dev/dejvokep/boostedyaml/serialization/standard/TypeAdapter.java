package dev.dejvokep.boostedyaml.serialization.standard;

import dev.dejvokep.boostedyaml.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Interface used to (de-)serialize custom types with {@link StandardSerializer}.
 * <p>
 * Please note that classes implementing this interface must also be registered at the serializer instance used. See
 * {@link StandardSerializer#register(Class, TypeAdapter)}.
 */
public interface TypeAdapter<T> {

    /**
     * Serializes the given instance into a map.
     * <p>
     * The returned map does not need to (but may) contain the type identifier {wiki}. Type identifier is one entry in
     * the top-level map (the one returned), where the key is defined by the serializer (<code>==</code> for {@link
     * StandardSerializer#DEFAULT}) and the value identifies the serialized type - either by the full canonical
     * classname (e.g. <code>me.name.project.objects.CustomObject</code>) or it's alias. <b>Both must also be
     * registered</b>.
     * <p>
     * If the returned map does not contain the identifier, the {@link StandardSerializer serializer} will automatically
     * use the full classname.
     *
     * @return the serialized object
     */
    @NotNull
    Map<Object, Object> serialize(@NotNull T object);

    /**
     * Deserializes the given map into instance of this type.
     * <p>
     * The given map is a raw object map; there are no {@link Block} instances, just native Java objects themselves.
     *
     * @param map the raw map to deserialize
     * @return the deserialized object
     */
    @NotNull
    T deserialize(@NotNull Map<Object, Object> map);

}