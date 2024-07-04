/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.utils.format;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.nodes.Tag;

/**
 * An interface used to format (style) nodes.
 *
 * @param <S> the style type of the node
 * @param <V> the value type
 */
public interface Formatter<S, V> {

    /**
     * Returns the format to use for the given node.
     * <p>
     * <b>Please note that the returned style might be overridden</b> in order to produce output compliant with the
     * YAML specification. Please learn more at the corresponding setting documentation.
     *
     * @param tag   the actual datatype the node is representing
     * @param value value of the node (for scalars, this is a string-based representation)
     * @param role  role of the node
     * @param def   the default style (as configured by the respective setting)
     * @return the style to use for the node
     */
    @NotNull
    S format(@NotNull Tag tag, @NotNull V value, @NotNull NodeRole role, @NotNull S def);

    /**
     * Returns a formatter which returns the default style, as given to the method (as configured by the respective
     * setting).
     *
     * @param <S> the style type of the node
     * @param <V> the value type
     * @return the identity formatter
     */
    @NotNull
    static <S, V> Formatter<S, V> identity() {
        return (tag, value, role, def) -> def;
    }

}
