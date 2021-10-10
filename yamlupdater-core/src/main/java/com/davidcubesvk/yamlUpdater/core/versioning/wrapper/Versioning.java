package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

public interface Versioning {

    Version getUserFileId(Section section);
    Version getDefaultFileId(Section section);
    Version getOldest();

}