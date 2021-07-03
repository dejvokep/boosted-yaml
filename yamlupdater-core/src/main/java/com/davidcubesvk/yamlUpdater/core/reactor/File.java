package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.reader.FileReader;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;
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
    private final String separator, escapedSeparator;

    /**
     * Initializes and loads the file from the given stream, section values and key separator.
     *
     * @param streamReader  stream to read from
     * @param sectionValues section values for the current file version
     * @param settings      settings this file will be loaded with, used to get the key separators
     * @throws ParseException if something failed to parse correctly
     */
    public File(InputStreamReader streamReader, Set<String> sectionValues, Settings settings) throws ParseException {
        super(EMPTY_STRING, EMPTY_KEY, EMPTY_STRING_BUILDER, FileReader.load(new BufferedReader(streamReader).lines().collect(Collectors.toCollection(ArrayList::new)), sectionValues, settings.getSeparator()), -1);
        this.separator = settings.getSeparatorString();
        this.escapedSeparator = settings.getEscapedSeparator();
    }

    /**
     * Returns the upper map of the given path, or <code>null</code> if does not exist.
     *
     * @param path the path to search the upper map for with keys separated by the given key separator
     * @return the upper map, or <code>null</code> if does not exist
     */
    public Map<String, Object> getUpperMap(String path) {
        return getUpperMap(splitKey(path, separator, escapedSeparator));
    }

    /**
     * Returns the upper map of the given path, or <code>null</code> if does not exist.
     *
     * @param path the path to search the upper map for
     * @return the upper map, or <code>null</code> if does not exist
     */
    public Map<String, Object> getUpperMap(String[] path) {
        //If not a direct key
        if (path.length > 1) {
            //Get the section at the super path
            Section section = ((Section) get(getMappings(), path, path.length - 1));
            //Return mappings or null
            return section == null ? null : section.getMappings();
        }

        //Return mappings
        return getMappings();
    }

    /**
     * Returns object at the given path in the given mappings, or <code>null</code> if not found.
     *
     * @param mappings            the mappings to search
     * @param path                the path with keys separated by the given key separator
     * @param keySeparator        the key separator used to differentiate super from sub keys
     * @param escapedKeySeparator the escaped version of the key separator
     * @return the object, or <code>null</code> if not found
     */
    public static Object get(Map<?, ?> mappings, String path, String keySeparator, String escapedKeySeparator) {
        return get(mappings, splitKey(path, keySeparator, escapedKeySeparator), Integer.MAX_VALUE);
    }

    /**
     * Returns object at the given path in the given mappings, or <code>null</code> if not found.
     *
     * @param mappings the mappings to search
     * @param path     the path
     * @param to       exclusive index of the last path element (allows to get super-objects), if it is larger than the
     *                 length of the array, the length of the array is taken
     * @return the object, or <code>null</code> if not found
     */
    public static Object get(Map<?, ?> mappings, String[] path, int to) {
        //Current object
        Object current = mappings;
        //Go to the end of the path
        for (int index = 0; index < Math.min(path.length, to); index++) {
            //If not at the last index and it is not a map
            if (index + 1 < to && !(current instanceof Map))
                return null;

            //Set
            current = ((Map<?, ?>) current).get(path[index]);
        }

        //Return the object
        return current;
    }

    /**
     * Splits and returns the given key as an array.
     *
     * @param key              the key to split
     * @param separator        the key separator
     * @param escapedSeparator the escaped key separator used to split the key
     * @return the split key
     */
    public static String[] splitKey(String key, String separator, String escapedSeparator) {
        return key.contains(separator) ? key.split(escapedSeparator) : new String[]{key};
    }

}