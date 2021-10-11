package com.davidcubesvk.yamlUpdater.core.versioning.wrapper;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;

import java.util.Optional;

public class AutomaticVersioning implements Versioning {

    private final Pattern pattern;
    private final Path path;

    public AutomaticVersioning(Pattern pattern, Path path) {
        this.pattern = pattern;
        this.path = path;
    }

    @Override
    public Version getDefaultFileId(Section section) {
        return getId(file);
    }

    @Override
    public Version getUserFileId(Section section) {
        return getId(file);
    }

    private Version getId(Section section) {
        return section.getStringSafe(path).map(pattern::getVersion).orElse(null);
    }

}