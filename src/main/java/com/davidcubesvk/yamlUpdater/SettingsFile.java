package com.davidcubesvk.yamlUpdater;

import com.davidcubesvk.yamlUpdater.version.Pattern;
import org.yaml.snakeyaml.Yaml;

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

    private static final Yaml YAML = new Yaml();
    private final Settings settings;

    public SettingsFile(Settings settings) {
        this.settings = settings;
    }

    public void load(InputStream stream) {
        //Load
        Map<String, Object> baseMap = YAML.load(stream);
        //Load all
        loadFiles(baseMap);
        loadGeneral(baseMap);
        loadVersion(baseMap);
        loadRelocations(baseMap);
        loadSectionValues(baseMap);
    }

    private void loadFiles(Map<String, Object> baseMap) throws IllegalArgumentException {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_FILES))
            return;

        //Object
        Object pathsObject = baseMap.get(PATH_FILES);
        //If not a map
        if (!(pathsObject instanceof Map))
            throw new IllegalArgumentException();
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

    private void loadVersion(Map<String, Object> baseMap) {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_VERSION))
            return;

        //Object
        Object versionObject = baseMap.get(PATH_VERSION);
        //If not a map
        if (!(versionObject instanceof Map))
            throw new IllegalArgumentException();
        //The map
        Map<?, ?> version = (Map<?, ?>) baseMap.get(PATH_VERSION);

        //If contains the pattern
        if (version.containsKey(PATH_VERSION_PATTERN)) {
            //Object
            Object patternObject = version.get(PATH_VERSION_PATTERN);
            //If not a list
            if (!(patternObject instanceof List))
                throw new IllegalArgumentException();
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

                throw new IllegalArgumentException();
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
                throw new IllegalArgumentException();
            //The map
            Map<?, ?> fileVersions = (Map<?, ?>) version.get(PATH_VERSION_FILE_VERSIONS);

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

    private void loadRelocations(Map<String, Object> baseMap) {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_RELOCATIONS))
            return;

        //Object
        Object versionObject = baseMap.get(PATH_RELOCATIONS);
        //If not a map
        if (!(versionObject instanceof Map))
            throw new IllegalArgumentException();
        //Set
        settings.setRelocationsFromConfig((Map<?, ?>) baseMap.get(PATH_VERSION));
    }

    private void loadSectionValues(Map<String, Object> baseMap) {
        //If does not contain the section
        if (!baseMap.containsKey(PATH_SECTION_VALUES))
            return;

        //Object
        Object versionObject = baseMap.get(PATH_SECTION_VALUES);
        //If not a map
        if (!(versionObject instanceof Map))
            throw new IllegalArgumentException();

        //Go through all entries
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) versionObject).entrySet()) {
            //If not a list
            if (!(entry.getValue() instanceof List))
                throw new IllegalArgumentException();
            //Set
            settings.setSectionValues(entry.getKey().toString(), new HashSet<>((List<String>) entry.getValue()));
        }
    }

}