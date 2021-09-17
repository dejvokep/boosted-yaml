package com.davidcubesvk.yamlUpdater.core.settings;

import com.davidcubesvk.yamlUpdater.core.files.YamlFile;
import com.davidcubesvk.yamlUpdater.core.version.Version;

public interface Versioning {

    Version getUserFileId(YamlFile file);
    Version getDefaultFileId(YamlFile file);

}