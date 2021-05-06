package com.davidcubesvk.yamlUpdater;

import com.davidcubesvk.yamlUpdater.reactor.Reactor;

import java.io.File;

public class YamlUpdater {

    private ClassLoader classLoader;
    private File diskFolder;

    public YamlUpdater() {}
    public YamlUpdater(Class<?> clazz, File diskFolder) {
        this(clazz.getClassLoader(), diskFolder);
    }
    public YamlUpdater(ClassLoader classLoader, File diskFolder) {
        if (!diskFolder.isDirectory())
            throw new IllegalArgumentException();
        this.classLoader = classLoader;
        this.diskFolder = diskFolder;
    }

    public Settings createSettings() {
        return new Settings(this);
    }

    public UpdatedFile update(Settings settings) throws Exception {
        return Reactor.react(settings);
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