package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

public interface Versioning {

    Version getUserFileId(YamlFile file);
    Version getDefaultFileId(YamlFile file);

}