package com.davidcubesvk.yamlUpdater.reactor;

import com.davidcubesvk.yamlUpdater.block.Key;
import com.davidcubesvk.yamlUpdater.block.Section;
import com.davidcubesvk.yamlUpdater.version.Version;
import com.davidcubesvk.yamlUpdater.block.Block;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

public class Relocator {

    private final File map;
    private final Version current, latest;

    public Relocator(File map, Version current, Version latest) {
        this.map = map;
        this.current = current;
        this.latest = latest;
    }

    public void apply(Map<Object, Object> relocations) throws IllegalArgumentException {
        //Copy
        Version current = this.current.copy();
        //Move to the next version
        current.next();
        //While not at the latest version
        while (current.compareTo(latest) <= 0) {
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

            //Apply relocations for this version
            applyVersion((Map<?, ?>) relocation);
        }
    }

    private void applyVersion(Map<?, ?> relocations) {
        //The iterator
        Iterator<?> iterator = relocations.keySet().iterator();
        //Go through all entries
        while (iterator.hasNext())
            //Apply
            apply(relocations, iterator, iterator.next().toString());
    }

    private void apply(Map<?, ?> relocations, Iterator<?> keyIterator, String from) {
        if (from == null || !relocations.containsKey(from))
            return;
        //To
        String to = relocations.get(from).toString();
        //Block
        Map<String, Object> upper = map.getUpperMap(from);
        String lastKey = getLastKey(from);
        Object block = upper.get(lastKey);
        System.out.println("APPLY " + to + block);
        //If null
        if (to == null || block == null)
            return;

        //Remove
        keyIterator.remove();
        //Remove the block to free up the space
        upper.remove(lastKey);
        removeIfEmpty(map, from.contains(".") ? from.split("\\.") : new String[]{from}, 0);

        //Relocate to
        apply(relocations, keyIterator, to);
        ((Block) block).setKey(getLastKey(to));
        if (map.getUpperMap(to) == null) {
            create(map, to.contains(".") ? to.split("\\.") : new String[]{to}, 0);
        }
        //Relocate
        System.out.println(map.getUpperMap(to).toString());
        map.getUpperMap(to).put(getLastKey(to), block);
        //Relocate
        System.out.println(map.getUpperMap(to).get("d"));
    }

    private String getLastKey(String fullKey) {
        return fullKey.contains(".") ? fullKey.substring(fullKey.lastIndexOf('.') + 1) : fullKey;
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
        System.out.println("REMOVE " + subSection.getMappings().size());
        if (subSection.getMappings().size() == 0) {
            section.getMappings().remove(path[index]);
        }
    }

}