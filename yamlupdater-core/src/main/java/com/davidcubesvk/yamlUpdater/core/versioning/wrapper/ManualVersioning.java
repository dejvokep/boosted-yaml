package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

public class ManualVersioning implements Versioning {

    private final Version userFileVersion;
    private final Version defaultFileVersion;

    public ManualVersioning(Pattern pattern, String userFileVersionId, String defaultFileVersionId) {
        this.userFileVersion = pattern.getVersion(userFileVersionId);
        this.defaultFileVersion = pattern.getVersion(defaultFileVersionId);
    }

    @Override
    public Version getDefaultFileId(YamlFile file) {
        return defaultFileVersion;
    }

    @Override
    public Version getUserFileId(YamlFile file) {
        return userFileVersion;
    }
}