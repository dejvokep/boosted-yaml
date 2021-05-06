package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.utils.ParseException;
import com.davidcubesvk.yamlUpdater.core.block.Key;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.reader.FileReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class File extends Section {

    private String keySeparator;

    public File(InputStreamReader streamReader, Set<String> sectionValues, char keySeparator) throws ParseException {
        super("", new Key("", "", 0), new StringBuilder(), FileReader.load(new BufferedReader(streamReader).lines().collect(Collectors.toCollection(ArrayList::new)), sectionValues, keySeparator), -1);
        this.keySeparator = String.valueOf(keySeparator);
    }

    public Map<String, Object> getUpperMap(String path) {
        if (path.contains(keySeparator)) {
            Section section = ((Section) get(getMappings(), path.substring(0, path.lastIndexOf(keySeparator)), keySeparator));
            return section == null ? null : section.getMappings();
        } else
            return getMappings();
    }

    public Object get(String path) {
        return get(getMappings(), path, keySeparator);
    }

    public static Object get(Map<?, ?> mappings, String path, String keySeparator) {
        //Split
        String[] pathKeys = path.contains(keySeparator) ? path.split(keySeparator) : new String[]{path};

        //Current object
        Object current = mappings;
        //Go to the end of the path
        for (int index = 0; index < pathKeys.length; index++) {
            //If not at the last index and it is not a map
            if (index + 1 < pathKeys.length && !(current instanceof Map))
                return null;

            //Set
            current = ((Map<?, ?>) current).get(pathKeys[index]);
        }

        //Return the object
        return current;
    }

}