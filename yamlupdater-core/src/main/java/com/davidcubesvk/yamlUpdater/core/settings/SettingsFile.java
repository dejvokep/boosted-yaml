package com.davidcubesvk.yamlUpdater.core.settings;

import com.davidcubesvk.yamlUpdater.core.utils.Constants;
import com.davidcubesvk.yamlUpdater.core.version.Pattern;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.*;

public class SettingsFile {

    private static final String PATH_FILES = "files";
    private static final String PATH_FILES_DISK = "disk";
    private static final String PATH_FILES_RESOURCE = "resource";
    private static final String PATH_SEPARATOR = "separator";
    private static final String PATH_INDENT_SPACES = "indent-spaces";
    private static final String PATH_VERSION = "version";
    private static final String PATH_VERSION_PATTERN = "pattern";
    private static final String PATH_VERSION_PATH = "file-path";
    private static final String PATH_VERSION_FILE_VERSIONS = "file-versions";
    private static final String PATH_VERSION_FILE_VERSIONS_DISK = "disk";
    private static final String PATH_VERSION_FILE_VERSIONS_RESOURCE = "resource";
    private static final String PATH_RELOCATIONS = "relocations";
    private static final String PATH_SECTION_VALUES = "section-values";

    //Settings to load
    private final Settings settings;

    /**
     * Initializes the file loader with the given super settings object.
     *
     * @param settings the super-object
     */
    public SettingsFile(Settings settings) {
        this.settings = settings;
    }

    /**
     * Loads all settings from the given stream. It must be a valid YAML.
     *
     * @param stream the stream to load from
     * @throws YAMLException            if the YAML is invalidly formatted
     * @throws IllegalArgumentException if the settings are invalidly formatted
     */
    public void load(InputStream stream) throws YAMLException, IllegalArgumentException {
        //Load
        Map<String, Object> baseMap = Constants.YAML.load(stream);
        //Load all
        loadFiles(baseMap);
        loadGeneral(baseMap);
        loadVersion(baseMap);
        loadRelocations(baseMap);
        loadSectionValues(baseMap);
    }

    /**
     * Loads file paths from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws IllegalArgumentException if anything was specified invalidly
     */
    private void loadFiles(Map<String, Object> baseMap) throws IllegalArgumentException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_FILES))
            return;

        //Object
        Object pathsObject = baseMap.get(PATH_FILES);
        //If not a map
        if (!(pathsObject instanceof Map))
            throw new IllegalArgumentException("Object at " + PATH_FILES + " is not a map!");
        //The map
        Map<?, ?> paths = (Map<?, ?>) baseMap.get(PATH_FILES);

        //If contains the disk file path
        if (paths.containsKey(PATH_FILES_DISK))
            //Set
            settings.setDiskFile(paths.get(PATH_FILES_DISK).toString());
        //If contains the resource file path
        if (paths.containsKey(PATH_FILES_RESOURCE))
            //Set
            settings.setDiskFile(paths.get(PATH_FILES_RESOURCE).toString());
    }

    /**
     * Loads general data (separator and indent spaces) from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     */
    private void loadGeneral(Map<String, Object> baseMap) {
        //If contains the separator
        if (baseMap.containsKey(PATH_SEPARATOR))
            //Set
            settings.setSeparator(baseMap.get(PATH_SEPARATOR).toString().charAt(0));
        //If contains the indentation spaces
        if (baseMap.containsKey(PATH_INDENT_SPACES))
            //Set
            settings.setIndentSpaces((Integer) baseMap.get(PATH_INDENT_SPACES));
    }

    /**
     * Loads file versions, version path and version pattern from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws IllegalArgumentException if anything was specified invalidly
     */
    private void loadVersion(Map<String, Object> baseMap) throws IllegalArgumentException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_VERSION))
            return;

        //Object
        Object versionObject = baseMap.get(PATH_VERSION);
        //If not a map
        if (!(versionObject instanceof Map))
            throw new IllegalArgumentException("Object at " + PATH_VERSION + " is not a map!");
        //The map
        Map<?, ?> version = (Map<?, ?>) versionObject;

        //If contains the pattern
        if (version.containsKey(PATH_VERSION_PATTERN)) {
            //Object
            Object patternObject = version.get(PATH_VERSION_PATTERN);
            //If not a list
            if (!(patternObject instanceof List))
                throw new IllegalArgumentException("Version pattern is not a list!");
            //The list
            List<?> patternList = (List<?>) patternObject;
            //The pattern parts
            Pattern.Part[] parts = new Pattern.Part[patternList.size()];

            //Go through all parts
            for (int index = 0; index < patternList.size(); index++) {
                //The object
                Object partObject = patternList.get(index);
                //If an array
                if (partObject instanceof String[]) {
                    //Set
                    parts[index] = new Pattern.Part((String[]) partObject);
                    continue;
                }

                //If a string
                if (partObject instanceof String) {
                    //String
                    String part = (String) partObject;
                    //The index of the dash
                    int dashIndex = part.indexOf('-');
                    //If not -1
                    if (dashIndex != -1) {
                        try {
                            //Set
                            parts[index] = new Pattern.Part(Integer.parseInt(part.substring(0, dashIndex)), Integer.parseInt(part.substring(dashIndex + 1)), dashIndex == part.length() - dashIndex ? dashIndex : 0);
                            //Continue
                            continue;
                        } catch (NumberFormatException ex) {
                            //Ignored
                        }
                    }

                    //A fixed part
                    parts[index] = new Pattern.Part(part);
                    continue;
                }

                throw new IllegalArgumentException("Version pattern element must be a string or an array!");
            }

            //Set
            settings.setVersionPattern(new Pattern(parts));
        }

        //If contains the file path
        if (version.containsKey(PATH_VERSION_PATH))
            //Set
            settings.setVersionPath(version.get(PATH_VERSION_PATH).toString());

        //If contains the version section
        if (!baseMap.containsKey(PATH_VERSION_FILE_VERSIONS)) {
            //Object
            Object fileVersionsObject = version.get(PATH_VERSION_FILE_VERSIONS);
            //If not a map
            if (!(fileVersionsObject instanceof Map))
                throw new IllegalArgumentException("File versions section must be a map!");
            //The map
            Map<?, ?> fileVersions = (Map<?, ?>) fileVersionsObject;

            //If contains the disk file version
            if (fileVersions.containsKey(PATH_VERSION_FILE_VERSIONS_DISK))
                //Set
                settings.setDiskFileVersion(fileVersions.get(PATH_VERSION_FILE_VERSIONS_DISK).toString());
            //If contains the resource file version
            if (fileVersions.containsKey(PATH_VERSION_FILE_VERSIONS_RESOURCE))
                //Set
                settings.setResourceFileVersion(fileVersions.get(PATH_VERSION_FILE_VERSIONS_RESOURCE).toString());
        }
    }

    /**
     * Loads relocations from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws IllegalArgumentException if anything was specified invalidly
     */
    private void loadRelocations(Map<String, Object> baseMap) throws IllegalArgumentException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_RELOCATIONS))
            return;

        //Object
        Object versionObject = baseMap.get(PATH_RELOCATIONS);
        //If not a map
        if (!(versionObject instanceof Map))
            throw new IllegalArgumentException("Relocations must be specified as a map!");
        //Set
        settings.setRelocationsFromConfig((Map<?, ?>) baseMap.get(PATH_VERSION));
    }

    /**
     * Loads section values from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws IllegalArgumentException if anything was specified invalidly
     */
    private void loadSectionValues(Map<String, Object> baseMap) throws IllegalArgumentException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_SECTION_VALUES))
            return;

        //Object
        Object versionObject = baseMap.get(PATH_SECTION_VALUES);
        //If not a map
        if (!(versionObject instanceof Map))
            throw new IllegalArgumentException("Section values must be specified as a map!");

        //Go through all entries
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) versionObject).entrySet()) {
            //If not a list
            if (!(entry.getValue() instanceof Collection))
                throw new IllegalArgumentException("Each version must have it's own section values specified in a list (set)!");
            //Set
            settings.setSectionValues(entry.getKey().toString(), new HashSet<>((List<String>) entry.getValue()));
        }
    }

}