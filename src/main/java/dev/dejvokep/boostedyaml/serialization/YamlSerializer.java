/*
 * Copyright 2022 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.serialization;

import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.utils.supplier.MapSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Interface used by custom serializer implementations. If you are looking into building your own implementation, it is
 * recommended that you read the <a href="https://dejvokep.gitbook.io/boostedyaml/">wiki</a>.
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
     *     <li>a sub-class of any of {@link #getSupportedParentClasses()}.</li>
     * </ol>
     * Map supplier is provided to supply default map as configured in {@link GeneralSettings#getDefaultMapSupplier()}.
     * Serializers should not use their own map implementations, they should return the map provided by the supplier.
     * <p>
     * If could not serialize into a map, <b>this method must return <code>null</code></b>.
     *
     * @param object   the object to serialize
     * @param supplier the supplier used to supply default maps
     * @param <T> type of the object to serialize
     * @return the serialized object
     */
    @Nullable
    <T> Map<Object, Object> serialize(@NotNull T object, @NotNull MapSupplier supplier);

    /**
     * Returns a set of all explicitly defined classes this serializer supports and can (de-)serialize.
     * <p>
     * The returned set cannot be <code>null</code> as indicated by the annotation.
     *
     * @return the set of supported classes
     */
    @NotNull
    Set<Class<?>> getSupportedClasses();

    /**
     * Returns a set of all parent classes (classes, interfaces...) instances of which this serializer supports and can
     * (de-)serialize.
     * <p>
     * The returned set cannot be <code>null</code> as indicated by the annotation.
     *
     * @return the set of supported parent classes
     */
    @NotNull
    Set<Class<?>> getSupportedParentClasses();

}
