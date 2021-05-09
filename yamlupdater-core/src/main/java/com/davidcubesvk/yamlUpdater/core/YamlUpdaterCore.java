package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import com.davidcubesvk.yamlUpdater.core.reactor.Reactor;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;

import java.io.File;

/**
 * The main API class used to access settings and update configuration files. The class should not be accessed
 * (initialized) directly, only from a superclass - please use any appropriate updater classes.
 * @param <T> the type of the file provider
 */
public class YamlUpdaterCore<T> {

    //Class loader
    private ClassLoader classLoader;
    //Disk folder
    private File diskFolder;
    //File provider
    private FileProvider<T> fileProvider;

    /**
     * Creates an instance of the main API class for the given class loader and disk folder. This class is meant to be
     * per-plugin-singleton, therefore it should be created only once.
     * @param classLoader the class loader used to retrieve compiled resource files
     * @param diskFolder the folder used to retrieve disk files
     * @param fileProvider the file provider
     * @throws IllegalArgumentException if the given disk folder is not a folder
     */
    protected YamlUpdaterCore(ClassLoader classLoader, File diskFolder, FileProvider<T> fileProvider) throws IllegalArgumentException {
        //If not a folder
        if (!diskFolder.isDirectory())
            throw new IllegalArgumentException("The given disk folder is not a folder!");
        //Set
        this.classLoader = classLoader;
        this.diskFolder = diskFolder;
        this.fileProvider = fileProvider;
    }

    /**
     * Creates settings for the current updater API class - e.g. with already set class loader and disk folder.
     * @return the new settings object
     */
    public Settings createSettings() {
        return new Settings(this);
    }

    /**
     * Updates a configuration file as per the given settings object.
     * @param settings the settings to use
     * @return the updated file
     * @throws Exception if anything goes wrong
     */
    public UpdatedFile<T> update(Settings settings) throws Exception {
        return Reactor.react(settings, fileProvider);
    }

    /**
     * Sets the class loader used for all setting objects created by this class instance. Please note that already
     * existing settings objects will not be affected.
     * @param classLoader the new class loader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the disk file used for all setting objects created by this class instance. Please note that already existing
     * settings objects will not be affected.
     * @param diskFolder the new disk folder
     */
    public void setDiskFolder(File diskFolder) {
        this.diskFolder = diskFolder;
    }

    /**
     * Returns the class loader.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the disk folder.
     * @return the disk folder
     */
    public File getDiskFolder() {
        return diskFolder;
    }
}