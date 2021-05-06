package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import com.davidcubesvk.yamlUpdater.core.reactor.Reactor;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;

import java.io.File;

public class YamlUpdaterCore<T> {

    private ClassLoader classLoader;
    private File diskFolder;
    private FileProvider<T> fileProvider;

    protected YamlUpdaterCore(ClassLoader classLoader, File diskFolder, FileProvider<T> fileProvider) {
        if (!diskFolder.isDirectory())
            throw new IllegalArgumentException();
        this.classLoader = classLoader;
        this.diskFolder = diskFolder;
        this.fileProvider = fileProvider;
    }

    public Settings createSettings() {
        return new Settings(this);
    }

    public UpdatedFile<T> update(Settings settings) throws Exception {
        return Reactor.react(settings, fileProvider);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setDiskFolder(File diskFolder) {
        this.diskFolder = diskFolder;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public File getDiskFolder() {
        return diskFolder;
    }
}