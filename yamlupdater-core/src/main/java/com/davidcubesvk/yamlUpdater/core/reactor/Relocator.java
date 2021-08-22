package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.block.DocumentBlock;
import com.davidcubesvk.yamlUpdater.core.block.Key;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.files.File;
import com.davidcubesvk.yamlUpdater.core.utils.Constants;
import com.davidcubesvk.yamlUpdater.core.version.Version;
import org.yaml.snakeyaml.Yaml;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Relocator {

    //The disk file
    private final File diskFile;
    //Versions
    private final Version diskVersion, resourceVersion;
    //The separator
    private final String separator, escapedSeparator;

    /**
     * Initializes the relocator with the given disk file and file versions.
     *
     * @param diskFile            the disk file
     * @param diskVersion         version of the disk file
     * @param resourceVersion     version of the resource (latest) file
     * @param keySeparator        the key separator
     * @param escapedKeySeparator the escaped key separator
     */
    public Relocator(File diskFile, Version diskVersion, Version resourceVersion, String keySeparator, String escapedKeySeparator) {
        this.diskFile = diskFile;
        this.diskVersion = diskVersion;
        this.resourceVersion = resourceVersion;
        this.separator = keySeparator;
        this.escapedSeparator = escapedKeySeparator;
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
    public void apply(Map<Object, Object> relocations) throws IllegalArgumentException {
        //Copy
        Version current = this.diskVersion.copy();
        //Move to the next version
        current.next();
        //While not at the latest version
        while (current.compareTo(resourceVersion) <= 0) {
            //Relocation
            Object relocation = relocations.get(current.asID());
            //Move to the next version
            current.next();
            //If there is not any
            if (relocation == null)
                continue;
            //If it is not a map
            if (!(relocation instanceof Map))
                throw new IllegalArgumentException();

            //The iterator
            Iterator<?> iterator = relocations.keySet().iterator();
            //Go through all entries
            while (iterator.hasNext())
                //Apply
                apply(relocations, iterator, iterator.next().toString());
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
    private void apply(Map<?, ?> relocations, Iterator<?> keyIterator, String from) {
        //If there is no relocation
        if (from == null || !relocations.containsKey(from))
            return;
        //To
        String to = relocations.get(from).toString();
        //The upper map
        Map<String, DocumentBlock> upper = diskFile.getUpperMap(from);
        //If null
        if (upper == null)
            //Nothing to relocate
            return;
        //The from key
        String[] fromKey = File.splitKey(from, separator, escapedSeparator);
        //The block
        DocumentBlock block = upper.get(fromKey[fromKey.length - 1]);
        //If null
        if (block == null)
            //Nothing to relocate
            return;

        //Remove
        keyIterator.remove();
        //Remove the block to free up the space for another possible relocation
        upper.remove(fromKey[fromKey.length - 1]);
        //Remove sections if empty
        removeIfEmpty(diskFile, fromKey, 0);

        //Relocate to
        apply(relocations, keyIterator, to);

        //The to key
        String[] toKey = File.splitKey(to, separator, escapedSeparator);
        //Reset keys
        block.setRawKey(toKey[toKey.length - 1]);
        //Format the key
        block.setFormattedKey(Constants.YAML.load(block.getRawKey()).toString());

        //If there is no section created
        if (diskFile.getUpperMap(toKey) == null)
            //Create
            createSection(diskFile, toKey, 0);

        //Relocate
        diskFile.getUpperMap(toKey).put(toKey[toKey.length - 1], block);
    }

    /**
     * Creates a new section in the given section with key specified by the array, at the given index. Skips directly to
     * the subsection if an object at the key exists and it is an instance of {@link Section} (in both situations where
     * it does not exist, or it is a block, it is overwritten).
     *
     * @param section the section to create in
     * @param path    the full path
     * @param index   the index at which we are in the path array
     */
    private void createSection(Section section, String[] path, int index) {
        //If at the last key
        if (index + 1 == path.length)
            return;

        //The object at the path
        Object object = section.getMappings().get(path[index]);
        //If null or not a section
        if (!(object instanceof Section)) {
            //Create new section
            Section newSection = new Section("", new Key(path[index], new Yaml().load(path[index]).toString(), -1), new StringBuilder(":\n"), new LinkedHashMap<>(), 0);
            //Put
            section.getMappings().put(path[index], newSection);
            //Create subsection
            createSection(newSection, path, index + 1);
            return;
        }

        //Create subsection
        createSection((Section) section.getMappings().get(path[index]), path, index + 1);
    }

    /**
     * Traces all the keys in the given path (starting from the given index) and from the end removes sections that are
     * empty. Traces all the keys in the array except the last one, which is considered to be the key of the relocated
     * component.
     *
     * @param section the section removing from
     * @param path    the full path to the element that was relocated
     * @param index   the index at which we are in the path array
     */
    private void removeIfEmpty(Section section, String[] path, int index) {
        //If at the last key
        if (index + 1 == path.length)
            return;

        //The subsection
        Section subSection = (Section) section.getMappings().get(path[index]);
        //Remove if empty
        removeIfEmpty(subSection, path, index + 1);
        //If empty now
        if (subSection.getMappings().size() == 0)
            //Remove
            section.getMappings().remove(path[index]);
    }

}