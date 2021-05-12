package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.block.Key;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.utils.Constants;
import com.davidcubesvk.yamlUpdater.core.version.Version;
import com.davidcubesvk.yamlUpdater.core.block.Block;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.regex.Pattern;

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
     * @param diskFile        the disk file
     * @param diskVersion     version of the disk file
     * @param resourceVersion version of the resource (latest) file
     * @param separator       the key separator
     */
    public Relocator(File diskFile, Version diskVersion, Version resourceVersion, String separator) {
        this.diskFile = diskFile;
        this.diskVersion = diskVersion;
        this.resourceVersion = resourceVersion;
        this.separator = separator;
        this.escapedSeparator = Pattern.quote(separator);
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
            Object relocation = relocations.get(current.asString());
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
        //The upper map (never null)
        Map<String, Object> upper = diskFile.getUpperMap(from);
        //The from key
        String[] fromKey = splitKey(from);
        //The block
        Object block = upper.get(fromKey[fromKey.length - 1]);
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
        String[] toKey = splitKey(to);
        //The block
        Block blockObj = (Block) block;
        //Reset keys
        blockObj.setRawKey(toKey[toKey.length - 1]);
        //Format the key
        blockObj.setFormattedKey(Constants.YAML.load(blockObj.getRawKey()).toString());

        //If there is no section created
        if (diskFile.getUpperMap(to) == null)
            //Create
            create(diskFile, toKey, 0);

        //Relocate
        diskFile.getUpperMap(to).put(toKey[toKey.length - 1], block);
    }

    private void create(Section section, String[] path, int index) {
        if (index + 1 == path.length)
            return;
        if (!section.getMappings().containsKey(path[index])) {
            Section newSection = new Section("", new Key(path[index], new Yaml().load(path[index]).toString(), -1), new StringBuilder(":\n"), new LinkedHashMap<>(), 0);
            section.getMappings().put(path[index], newSection);
            create(section, path, index + 1);
        } else {
            create((Section) section.getMappings().get(path[index]), path, index + 1);
        }
    }

    private void removeIfEmpty(Section section, String[] path, int index) {
        if (index + 1 == path.length)
            return;

        Section subSection = (Section) section.getMappings().get(path[index]);
        removeIfEmpty(subSection, path, index + 1);
        if (subSection.getMappings().size() == 0) {
            section.getMappings().remove(path[index]);
        }
    }

    private String[] splitKey(String key) {
        return key.contains(separator) ? key.split(escapedSeparator) : new String[]{key};
    }

}