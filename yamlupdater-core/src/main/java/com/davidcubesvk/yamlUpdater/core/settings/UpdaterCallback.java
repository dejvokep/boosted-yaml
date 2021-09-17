package com.davidcubesvk.yamlUpdater.core.settings;

import java.io.File;

public interface UpdaterCallback {

    void call(File userFile, File defaultFile, LoaderSettings loaderSettings, UpdaterSettings updaterSettings);

}