package com.davidcubesvk.yamlUpdater;

import java.util.Map;

public class UpdatedFile {

    private Settings settings;
    private Map<Object, Object> map;
    private String string;

    public UpdatedFile(Settings settings, Map<Object, Object> map, String string) {
        this.settings = settings;
        this.map = map;
        this.string = string;
    }

    public Map<Object, Object> getMap() {
        return map;
    }
    public String getString() {
        return string;
    }
    public <T> T toFile(FileProvider<T> provider) {
        return null;
    }

}