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

import java.util.List;

/**
 * Supplier used to supply lists of any type.
 */
public interface ListSupplier {

    /**
     * Supplies list of the given type and (initial) size.
     *
     * @param size the (initial) size of the returned list, if supported by the list implementation returned
     * @param <T>  the type of the list
     * @return the list of the given size
     */
    @NotNull
    <T> List<T> supply(int size);

}