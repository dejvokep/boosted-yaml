package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;

import java.io.File;

public interface UpdaterCallback {

    void call(File userFile, File defaultFile, LoaderSettings loaderSettings, UpdaterSettings updaterSettings);

}