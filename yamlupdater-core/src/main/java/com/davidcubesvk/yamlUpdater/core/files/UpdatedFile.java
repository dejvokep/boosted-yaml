package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.settings.Settings;

import java.util.Map;

public class UpdatedFile<T> {

    private Settings settings;
    private Map<?, ?> diskMap, resourceMap;
    private String string;
    private FileProvider<T> fileProvider;

    public UpdatedFile(Settings settings, FileProvider<T> fileProvider, Map<?, ?> diskMap, Map<?, ?> resourceMap, String string) {
        this.settings = settings;
        this.fileProvider = fileProvider;
        this.diskMap = diskMap;
        this.resourceMap = resourceMap;
        this.string = string;
    }

    public Map<?, ?> getDiskMap() {
        return diskMap;
    }

    public Map<?, ?> getResourceMap() {
        return resourceMap;
    }

    public String getString() {
        return string;
    }

    public T toFile() throws ReflectiveOperationException {
        return fileProvider.convert(this);
    }

    public Settings getSettings() {
        return settings;
    }
}