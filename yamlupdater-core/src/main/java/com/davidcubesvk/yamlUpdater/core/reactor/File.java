package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.block.Key;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.reader.FileReader;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.davidcubesvk.yamlUpdater.core.utils.Constants.*;

/**
 * Represents a loaded YAML file.
 */
public class File extends Section {

    //The key separator
    private final String keySeparator;

    /**
     * Initializes and loads the file from the given stream, section values and key separator.
     *
     * @param streamReader  stream to read from
     * @param sectionValues section values for the current file version
     * @param keySeparator  key separator used to identify keys
     * @throws ParseException if something failed to parse correctly
     */
    public File(InputStreamReader streamReader, Set<String> sectionValues, char keySeparator) throws ParseException {
        super(EMPTY_STRING, new Key(EMPTY_STRING, EMPTY_STRING, 0), EMPTY_STRING_BUILDER, FileReader.load(new BufferedReader(streamReader).lines().collect(Collectors.toCollection(ArrayList::new)), sectionValues, keySeparator), -1);
        this.keySeparator = String.valueOf(keySeparator);
    }

    /**
     * Returns the upper map of the given path, or <code>null</code> if does not exist.
     *
     * @param path the path to search the upper map for
     * @return the upper map, or <code>null</code> if does not exist
     */
    public Map<String, Object> getUpperMap(String path) {
        //If not a direct key
        if (path.contains(keySeparator)) {
            //Get the section at the super path
            Section section = ((Section) get(getMappings(), path.substring(0, path.lastIndexOf(keySeparator)), keySeparator));
            //Return mappings or null
            return section == null ? null : section.getMappings();
        }

        //Return mappings
        return getMappings();
    }

    /**
     * Returns object at the given path in the given file, or <code>null</code> if not found.
     *
     * @param path the path with keys separated by the given key separator on initialization
     * @return the object, or <code>null</code> if not found
     */
    public Object get(String path) {
        return get(getMappings(), path, keySeparator);
    }

    /**
     * Returns object at the given path in the given mappings, or <code>null</code> if not found.
     *
     * @param mappings     the mappings to search
     * @param path         the path with keys separated by the given key separator
     * @param keySeparator the key separator used to differentiate super from sub keys
     * @return the object, or <code>null</code> if not found
     */
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