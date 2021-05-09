package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.settings.Settings;

import java.util.Map;

/**
 * Represents an updated file with all source settings and data.
 *
 * @param <T> the return type of {@link #toFile()} and simultaneously type of the {@link FileProvider}
 */
public class UpdatedFile<T> {

    //Settings
    private final Settings settings;
    //Maps of both disk and resource files
    private final Map<?, ?> diskMap, resourceMap;
    //String representation of the updated file
    private final String string;
    //The file provider
    private final FileProvider<T> fileProvider;

    /**
     * Initializes the updated file with the given data.
     *
     * @param settings     the source settings used to update the file
     * @param fileProvider file provider used to convert to standard configuration object
     * @param diskMap      the YAML map representation of the disk (now updated) file
     * @param resourceMap  the YAML map representation of the resource file, typically used for defaults
     * @param string       the string representation of the updated file
     */
    public UpdatedFile(Settings settings, FileProvider<T> fileProvider, Map<?, ?> diskMap, Map<?, ?> resourceMap, String string) {
        this.settings = settings;
        this.fileProvider = fileProvider;
        this.diskMap = diskMap;
        this.resourceMap = resourceMap;
        this.string = string;
    }

    /**
     * Returns the YAML map representation of the disk (now updated) file.
     *
     * @return the disk file map
     */
    public Map<?, ?> getMap() {
        return diskMap;
    }

    /**
     * Returns the YAML map representation of the resource file.
     *
     * @return the resource file map
     */
    public Map<?, ?> getResourceMap() {
        return resourceMap;
    }

    /**
     * Returns the string representation of the updated (disk) file.
     *
     * @return the string representation
     */
    public String getString() {
        return string;
    }

    /**
     * Converts and returns the updated file as a standard configuration object (per the type).
     *
     * @return the standard configuration object
     * @throws ReflectiveOperationException if anything went wrong during converting operation, which involves
     *                                      reflection
     */
    public T toFile() throws ReflectiveOperationException {
        return fileProvider.convert(this);
    }

    /**
     * Returns the settings used to update the file.
     *
     * @return the settings used to update the file
     */
    public Settings getSettings() {
        return settings;
    }
}