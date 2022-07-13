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
package dev.dejvokep.boostedyaml.updater.operators;

import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Relocator is a utility class and one of the updating process operators, responsible for applying relocations while
 * updating.
 */
public class Relocator {

    /**
     * Instance for calling non-static methods.
     */
    private static final Relocator INSTANCE = new Relocator();

    /**
     * Applies the provided relocations to the given section.
     *
     * @param section     the section
     * @param relocations the relocations to apply
     */
    public static void apply(@NotNull Section section, @NotNull Map<Route, Route> relocations) {
        //Go through all entries
        while (relocations.size() > 0)
            //Apply
            INSTANCE.apply(section, relocations, relocations.keySet().iterator().next());
    }

    /**
     * Applies a relocation from the given map, whose key is defined by <code>from</code> parameter; removes the
     * relocation from the map.
     * <p>
     * This method also checks if there are any relocations for the <code>to</code> (target) route and if yes, relocates
     * that first. Cyclic relocations are also supported (<code>a > b</code> and <code>b > a</code> for example). If
     * there is no block to relocate, nothing is changed.
     *
     * @param relocations all the relocations
     * @param from        from where to relocate
     */
    private void apply(@NotNull Section section, @NotNull Map<Route, Route> relocations, @Nullable Route from) {
        //If there is no relocation
        if (from == null || !relocations.containsKey(from))
            return;
        //The parent section
        Optional<Section> parent = section.getParent(from);
        //If absent
        if (!parent.isPresent()) {
            relocations.remove(from);
            return;
        }

        // Last key
        Object lastKey = from.get(from.length() - 1);
        //The block
        Block<?> block = parent.get().getStoredValue().get(lastKey);
        //If absent
        if (block == null) {
            relocations.remove(from);
            return;
        }

        //To
        Route to = relocations.get(from);

        //Remove
        relocations.remove(from);
        parent.get().getStoredValue().remove(lastKey);
        removeParents(parent.get());

        //Relocate to
        apply(section, relocations, to);

        //Relocate
        section.set(to, block);
    }

    /**
     * If the given section is empty, removes it from the parent. Then calls this method for the parent section (unless
     * it's not the root section).
     *
     * @param section the section to check
     */
    private void removeParents(@NotNull Section section) {
        //If empty
        if (section.isEmpty(false) && !section.isRoot()) {
            //Remove
            section.getParent().getStoredValue().remove(section.getName());
            //Parents
            removeParents(section.getParent());
        }
    }

}