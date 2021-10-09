package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class Relocator {

    //The file
    private final YamlFile file;
    //Versions
    private final Version userVersion, defVersion;

    /**
     * Initializes the relocator with the given file (user; to relocate contents in) and file versions.
     *
     * @param file        the (user) file
     * @param userVersion version of the user file
     * @param defVersion  version of the default file
     */
    public Relocator(YamlFile file, Version userVersion, Version defVersion) {
        this.file = file;
        this.userVersion = userVersion;
        this.defVersion = defVersion;
    }

    /**
     * Applies all the given relocations to the given file (in constructor).
     *
     * @param relocations the relocations to apply
     */
    public void apply(Map<String, Map<Path, Path>> relocations) {
        //Copy
        Version current = this.userVersion.copy();
        //Move to the next version
        current.next();
        //While not at the latest version
        while (current.compareTo(defVersion) <= 0) {
            //Relocation
            Map<Path, Path> relocation = relocations.get(current.asID());
            //Move to the next version
            current.next();
            //If there is not any
            if (relocation == null || relocation.isEmpty())
                continue;

            //The iterator
            Iterator<Path> iterator = relocation.keySet().iterator();
            //Go through all entries
            while (iterator.hasNext())
                //Apply
                apply(relocation, iterator, iterator.next());
        }
    }

    /**
     * Applies a relocation from the given map, whose key is defined by <code>from</code> parameter.
     * <p>
     * This method also checks if there are any relocations for the to (target) path and if yes, relocates that first.
     * Cyclic relocations are also supported (<code>a > b</code> and <code>b > a</code> for example). If there is no
     * element to relocate, nothing is changed.
     *
     * @param relocations all the relocations
     * @param keyIterator iterator used to remove applied relocation(s) - key set iterator of the given map
     * @param from        from where to relocate
     */
    private void apply(Map<Path, Path> relocations, Iterator<Path> keyIterator, @Nullable Path from) {
        //If there is no relocation
        if (from == null || !relocations.containsKey(from))
            return;
        //To
        Path to = relocations.get(from);
        //The parent section
        Optional<Section> parent = file.getParent(to);
        //If absent
        if (!parent.isPresent())
            return;
        //The block
        Optional<Block<?>> block = parent.get().getBlockSafe(to.get(to.getLength() - 1));
        //If absent
        if (!block.isPresent())
            return;

        //Remove
        keyIterator.remove();
        removeParents(parent.get());

        //Relocate to
        apply(relocations, keyIterator, to);

        //Relocate
        file.set(to, block.get());
    }

    private void removeParents(Section section) {
        //If empty
        if (section.isEmpty(false) && !section.isRoot()) {
            //Remove
            section.getParent().remove(section.getName());
            //Parents
            removeParents(section.getParent());
        }
    }

}