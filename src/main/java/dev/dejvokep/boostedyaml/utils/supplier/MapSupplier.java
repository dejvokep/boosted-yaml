/*
 * Copyright 2021 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.utils.supplier;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Supplier used to supply maps of any type.
 */
public interface MapSupplier {

    /**
     * Supplies map of the given key and value types and (initial) size.
     *
     * @param size the (initial) size of the returned map, if supported by the map implementation returned
     * @param <K>  key type
     * @param <V>  value type
     * @return the map of the given size
     */
    @NotNull
    <K, V> Map<K, V> supply(int size);

}