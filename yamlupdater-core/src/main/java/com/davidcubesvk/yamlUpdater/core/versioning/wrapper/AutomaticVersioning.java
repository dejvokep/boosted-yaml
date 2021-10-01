package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

public class AutomaticVersioning implements Versioning {

    private final Pattern pattern;
    private final String path;

    public AutomaticVersioning(Pattern pattern, String path) {
        this.pattern = pattern;
        this.path = path;
    }

    @Override
    public Version getDefaultFileId(YamlFile file) {
        return getId(file);
    }

    @Override
    public Version getUserFileId(YamlFile file) {
        return getId(file);
    }

    private Version getId(YamlFile file) {
        //If not a string
        if (!file.isString(path))
            return null;

        //Version ID
        return pattern.getVersion(file.getString(path));
    }

}