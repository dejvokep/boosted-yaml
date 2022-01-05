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
package dev.dejvokep.boostedyaml.block.implementation;

import dev.dejvokep.boostedyaml.block.Block;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.nodes.Node;

/**
 * Represents one YAML terminal {@link Block} (key=value pair).
 */
public class TerminalBlock extends Block<Object> {

    /**
     * Creates a mapping using the given parameters; while storing references to comments from the given nodes.
     *
     * @param keyNode   key node of the mapping
     * @param valueNode value node of the mapping
     * @param value     the value to store
     */
    public TerminalBlock(@Nullable Node keyNode, @Nullable Node valueNode, @Nullable Object value) {
        super(keyNode, valueNode, value);
    }

    /**
     * Creates a mapping with the same comments as the provided previous block, with the given value. If given block is
     * <code>null</code>, creates a mapping with no comments.
     *
     * @param previous the previous block to reference comments from
     * @param value    the value to store
     */
    public TerminalBlock(@Nullable Block<?> previous, @Nullable Object value) {
        super(previous, value);
    }

    @Override
    public boolean isSection() {
        return false;
    }
}