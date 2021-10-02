package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class Relocator {

    //The disk file
    private final YamlFile file;
    //Versions
    private final Version userVersion, defVersion;

    /**
     * Initializes the relocator with the given disk file and file versions.
     *
     * @param file            the disk file
     * @param userVersion         version of the disk file
     * @param defVersion     version of the resource (latest) file
     */
    public Relocator(YamlFile file, Version userVersion, Version defVersion) {
        this.file = file;
        this.userVersion = userVersion;
        this.defVersion = defVersion;
    }

    /**
     * Applies all the given relocations to the given disk file on initialization. The value of the given map should be
     * of type {@link Map}, otherwise, an {@link IllegalArgumentException} is thrown. The key type is not restricted,
     * but should be a {@link String}. Key and value specifications are objects only for convenience, when loading from
     * an YAML settings file.
     *
     * @param relocations the relocations to apply (immediately)
     * @throws IllegalArgumentException if the given map's value type is not an instance of {@link Map}
     */
    public void apply(Map<String, Map<Path, Path>> relocations) throws IllegalArgumentException {
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
     * Applies a relocation (specified by the <code>from</code> parameter). This method also checks if there are any
     * relocations for the to (target) path and if yes, relocates that first. Cyclic relocations are also patched. If
     * there is no element at the from path, no relocation is executed.
     *
     * @param relocations all the relocations
     * @param keyIterator iterator used to remove applied relocation(s)
     * @param from        from where to relocate
     */
    private void apply(Map<Path, Path> relocations, Iterator<Path> keyIterator, Path from) {
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
        Optional<Block<?>> block = parent.get().getBlockSafe(to.getKey(to.getLength() - 1));
        //If absent
        if (!block.isPresent())
            return;

        //Remove
        keyIterator.remove();
        removeParents(parent.get());

        //Relocate to
        apply(relocations, keyIterator, to);

        //Relocate
        relocate(from, block.get());
    }

    private void relocate(Path path, Block<?> block) {
        //Create
        Section section = file.createSection(path.parent());
        //Set
        section.set(path.getKey(path.getLength() - 1), block);
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