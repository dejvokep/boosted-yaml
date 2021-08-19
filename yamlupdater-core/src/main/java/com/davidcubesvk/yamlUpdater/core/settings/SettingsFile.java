package com.davidcubesvk.yamlUpdater.core.settings;

import com.davidcubesvk.yamlUpdater.core.utils.Constants;
import com.davidcubesvk.yamlUpdater.core.version.Pattern;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.*;

/**
 * Settings file loader.
 */
public class SettingsFile {

    /**
     * Path to the file paths section.
     */
    private static final String PATH_FILE_PATHS = "file-paths";
    /**
     * Path to the disk file path (under {@link #PATH_FILE_PATHS}).
     */
    private static final String PATH_FILE_PATHS_DISK = "disk";
    /**
     * Path to the resource file path (under {@link #PATH_FILE_PATHS}).
     */
    private static final String PATH_FILE_PATHS_RESOURCE = "resource";
    /**
     * Path to the setting if to update the disk file after the update is done.
     */
    private static final String PATH_UPDATE_DISK_FILE = "update-disk-file";
    /**
     * Path to the amount of spaces per indentation level.
     */
    private static final String PATH_INDENT_SPACES = "indent-spaces";
    /**
     * Path to the key separator.
     */
    private static final String PATH_SEPARATOR = "separator";
    /**
     * Path to the setting if to copy header.
     */
    private static final String PATH_COPY_HEADER = "copy-header";
    /**
     * Path to the setting if to keep former directives.
     */
    private static final String PATH_KEEP_FORMER_DIRECTIVES = "keep-former-directives";
    /**
     * Path to the file versioning properties section.
     */
    private static final String PATH_VERSIONING = "versioning";
    /**
     * Path to the versioning pattern (under {@link #PATH_VERSIONING}).
     */
    private static final String PATH_VERSIONING_PATTERN = "pattern";
    /**
     * Path to the file path of each file's version (under {@link #PATH_VERSIONING}).
     */
    private static final String PATH_VERSIONING_FILE_VERSION_ID_PATH = "file-version-id-path";
    /**
     * Path to the file versions section (under {@link #PATH_VERSIONING}).
     */
    private static final String PATH_VERSIONING_FILE_VERSION_IDS = "file-version-ids";
    /**
     * Path to the disk file version (under {@link #PATH_VERSIONING_FILE_VERSION_IDS}).
     */
    private static final String PATH_VERSIONING_FILE_VERSION_IDS_DISK = "disk";
    /**
     * Path to the resource file version (under {@link #PATH_VERSIONING_FILE_VERSION_IDS}).
     */
    private static final String PATH_VERSIONING_FILE_VERSION_IDS_RESOURCE = "resource";
    /**
     * Path to the relocations.
     */
    private static final String PATH_RELOCATIONS = "relocations";
    /**
     * Path to the section values.
     */
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
     * @throws ClassCastException       if the settings are invalidly formatted
     */
    public void load(InputStream stream) throws YAMLException, ClassCastException {
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
     * @throws ClassCastException if anything was specified invalidly
     */
    private void loadFiles(Map<String, Object> baseMap) throws ClassCastException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_FILE_PATHS))
            return;

        //Object
        Object pathsObject = baseMap.get(PATH_FILE_PATHS);
        //If not a map
        if (!(pathsObject instanceof Map))
            throw new ClassCastException("Object at " + PATH_FILE_PATHS + " is not a map!");
        //The map
        Map<?, ?> paths = (Map<?, ?>) baseMap.get(PATH_FILE_PATHS);

        //If contains the disk file path
        if (paths.containsKey(PATH_FILE_PATHS_DISK))
            //Set
            settings.setDiskFile(paths.get(PATH_FILE_PATHS_DISK).toString());
        //If contains the resource file path
        if (paths.containsKey(PATH_FILE_PATHS_RESOURCE))
            //Set
            settings.setDiskFile(paths.get(PATH_FILE_PATHS_RESOURCE).toString());
    }

    /**
     * Loads general data (separator and indent spaces) from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws ClassCastException if anything was specified invalidly
     */
    private void loadGeneral(Map<String, Object> baseMap) throws ClassCastException {
        //If contains the update disk file setting
        if (baseMap.containsKey(PATH_UPDATE_DISK_FILE))
            //Set
            settings.setUpdateDiskFile((Boolean) baseMap.get(PATH_UPDATE_DISK_FILE));
        //If contains the separator
        if (baseMap.containsKey(PATH_SEPARATOR))
            //Set
            settings.setSeparator(baseMap.get(PATH_SEPARATOR).toString().charAt(0));
        //If contains the indentation spaces
        if (baseMap.containsKey(PATH_INDENT_SPACES))
            //Set
            settings.setIndentSpaces((Integer) baseMap.get(PATH_INDENT_SPACES));
        //If contains the copy header setting
        if (baseMap.containsKey(PATH_COPY_HEADER))
            //Set
            settings.setCopyHeader((Boolean) baseMap.get(PATH_COPY_HEADER));
        //If contains the keep former directives setting
        if (baseMap.containsKey(PATH_KEEP_FORMER_DIRECTIVES))
            //Set
            settings.setKeepFormerDirectives((Boolean) baseMap.get(PATH_KEEP_FORMER_DIRECTIVES));
    }

    /**
     * Loads file versions, version path and version pattern from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws ClassCastException if anything was specified invalidly
     */
    private void loadVersion(Map<String, Object> baseMap) throws ClassCastException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_VERSIONING))
            return;

        //Object
        Object versioningObject = baseMap.get(PATH_VERSIONING);
        //If not a map
        if (!(versioningObject instanceof Map))
            throw new ClassCastException("Object at " + PATH_VERSIONING + " is not a map!");
        //The map
        Map<?, ?> versioning = (Map<?, ?>) versioningObject;

        //If contains the pattern
        if (versioning.containsKey(PATH_VERSIONING_PATTERN)) {
            //Object
            Object patternObject = versioning.get(PATH_VERSIONING_PATTERN);
            //If not a list
            if (!(patternObject instanceof List))
                throw new ClassCastException("Version pattern is not a list!");
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

                throw new ClassCastException("Versioning pattern element must be a string (for integer range specification) or an array (for string elements)!");
            }

            //Set
            settings.setVersioningPattern(new Pattern(parts));
        }

        //If contains the file path
        if (versioning.containsKey(PATH_VERSIONING_FILE_VERSION_ID_PATH))
            //Set
            settings.setFileVersionIdPath(versioning.get(PATH_VERSIONING_FILE_VERSION_ID_PATH).toString());

        //If contains the version section
        if (!baseMap.containsKey(PATH_VERSIONING_FILE_VERSION_IDS)) {
            //Object
            Object fileVersionIdsObject = versioning.get(PATH_VERSIONING_FILE_VERSION_IDS);
            //If not a map
            if (!(fileVersionIdsObject instanceof Map))
                throw new ClassCastException("File versions section must be a map!");
            //The map
            Map<?, ?> fileVersionIds = (Map<?, ?>) fileVersionIdsObject;

            //If contains the disk file version
            if (fileVersionIds.containsKey(PATH_VERSIONING_FILE_VERSION_IDS_DISK))
                //Set
                settings.setDiskFileVersionId(fileVersionIds.get(PATH_VERSIONING_FILE_VERSION_IDS_DISK).toString());
            //If contains the resource file version
            if (fileVersionIds.containsKey(PATH_VERSIONING_FILE_VERSION_IDS_RESOURCE))
                //Set
                settings.setResourceFileVersionId(fileVersionIds.get(PATH_VERSIONING_FILE_VERSION_IDS_RESOURCE).toString());
        }
    }

    /**
     * Loads relocations from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws ClassCastException if anything was specified invalidly
     */
    private void loadRelocations(Map<String, Object> baseMap) throws ClassCastException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_RELOCATIONS))
            return;

        //Object
        Object relocationsObject = baseMap.get(PATH_RELOCATIONS);
        //If not a map
        if (!(relocationsObject instanceof Map))
            throw new ClassCastException("Relocations must be specified as a map!");
        //Set
        settings.setRelocationsFromConfig((Map<?, ?>) baseMap.get(PATH_VERSIONING));
    }

    /**
     * Loads section values from the given base (main file) map.
     *
     * @param baseMap the base (main) file map
     * @throws ClassCastException if anything was specified invalidly
     */
    private void loadSectionValues(Map<String, Object> baseMap) throws ClassCastException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_SECTION_VALUES))
            return;

        //Object
        Object sectionValuesObject = baseMap.get(PATH_SECTION_VALUES);
        //If not a map
        if (!(sectionValuesObject instanceof Map))
            throw new ClassCastException("Section values must be specified as a map!");

        //Go through all entries
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) sectionValuesObject).entrySet()) {
            //If not a list
            if (!(entry.getValue() instanceof Collection))
                throw new ClassCastException("Each version must have it's own section values specified in a list (set) of strings!");

            //The set
            Set<String> paths = new HashSet<>();
            //Go through all elements
            for (Object pathObject : (Collection<?>) entry.getValue()) {
                //If not a string
                if (!(pathObject instanceof String))
                    throw new ClassCastException("Each version must have it's own section values specified in a list (set) of strings!");

                //Add
                paths.add(pathObject.toString());
            }

            //Set
            settings.setSectionValues(entry.getKey().toString(), paths);
        }
    }

}