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
package dev.dejvokep.boostedyaml.updater;

import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.fvs.Version;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class Relocator {

    //The file
    private final Section file;
    //Versions
    private final Version userVersion, defVersion;

    /**
     * Initializes the relocator with the given file (user; to relocate contents in) and file versions.
     *
     * @param section     the (user) section
     * @param userVersion version of the user file (parent of the given section)
     * @param defVersion  version of the default file
     */
    public Relocator(@NotNull Section section, @NotNull Version userVersion, @NotNull Version defVersion) {
        this.file = section;
        this.userVersion = userVersion;
        this.defVersion = defVersion;
    }

    /**
     * Applies all appropriate relocations to the given section (in constructor), one by one using {@link #apply(Map,
     * Route)}.
     * <p>
     * More formally, iterates through all version IDs, starting from the just next version ID of the user file version
     * ID, ending (inclusive) when the currently iterated version ID is equal to the version ID of the default file.
     *
     * @param settings  settings used to get relocations
     * @param separator separator used to split string routes
     * @see #apply(Map, Route)
     */
    public void apply(@NotNull UpdaterSettings settings, char separator) {
        //Copy
        Version current = this.userVersion.copy();
        //While not at the latest version
        while (current.compareTo(defVersion) <= 0) {
            //Move to the next version
            current.next();
            //Relocations
            Map<Route, Route> relocations = settings.getRelocations(current.asID(), separator);
            //If there is not any
            if (relocations.isEmpty())
                continue;

            //Go through all entries
            while (relocations.size() > 0)
                //Apply
                apply(relocations, relocations.keySet().iterator().next());
        }
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
    private void apply(@NotNull Map<Route, Route> relocations, @Nullable Route from) {
        //If there is no relocation
        if (from == null || !relocations.containsKey(from))
            return;
        //The parent section
        Optional<Section> parent = file.getParent(from);
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
        apply(relocations, to);

        //Relocate
        file.set(to, block);
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