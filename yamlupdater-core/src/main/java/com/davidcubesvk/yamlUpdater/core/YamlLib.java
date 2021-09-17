package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.files.YamlFile;
import com.davidcubesvk.yamlUpdater.core.reactor.Reactor;
import com.davidcubesvk.yamlUpdater.core.reader.FileReader;
import com.davidcubesvk.yamlUpdater.core.settings.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.UpdaterSettings;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;

import java.io.*;

public class YamlLib {

    public static YamlFile load(File file) throws FileNotFoundException, ParseException {
        return load(new FileInputStream(file));
    }

    public static YamlFile load(InputStream fileStream) throws ParseException {
        return new FileReader(new InputStreamReader(fileStream), new LoaderSettings()).load();
    }

    public static YamlFile load(File userFile, InputStream defaultFile) {
        return load(userFile, defaultFile, new LoaderSettings(), new UpdaterSettings());
    }

    public static YamlFile load(File userFile, InputStream defaultFile, LoaderSettings loaderSettings) {
        return load(userFile, defaultFile, loaderSettings, new UpdaterSettings());
    }

    public static YamlFile load(File userFile, InputStream defaultFile, UpdaterSettings updaterSettings) {
        return load(userFile, defaultFile, new LoaderSettings(), updaterSettings);
    }

    public static YamlFile load(File userFile, InputStream defaultFile, LoaderSettings loaderSettings, UpdaterSettings updaterSettings) {
        return Reactor.react(userFile, defaultFile, loaderSettings, updaterSettings);
    }

}