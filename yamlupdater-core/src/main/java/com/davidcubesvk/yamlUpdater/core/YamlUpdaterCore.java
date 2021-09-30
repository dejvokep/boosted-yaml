package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.reactor.Reactor;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;

import java.io.File;
import java.io.IOException;

/**
 * The main API class used to access settings and update configuration files. The class should not be accessed
 * (initialized) directly, only from a superclass - please use any appropriate updater classes.
 *
 * @param <T> the type of the file returned by the {@link FileProvider}
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
     *
     * @param classLoader  the class loader used to retrieve compiled resource files
     * @param diskFolder   the folder used to retrieve disk files
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
     *
     * @return the new settings object
     */
    public GeneralSettings createSettings() {
        return new GeneralSettings(this);
    }

    /**
     * Updates a configuration file as per the given settings object. Full updating process consists of:
     * <ol>
     *     <li>loading (or creating if does not exist) the disk file,</li>
     *     <li>parsing both files using SnakeYAML,</li>
     *     <li>getting file versions from the files,</li>
     *     <li>parsing both files,</li>
     *     <li>applying relocations (see {@link com.davidcubesvk.yamlUpdater.core.reactor.Relocator}),</li>
     *     <li>merging together (see {@link com.davidcubesvk.yamlUpdater.core.reactor.Merger}),</li>
     * </ol>
     * Please see the {@link Reactor#react(GeneralSettings, FileProvider)} method for more information.
     *
     * @param generalSettings the settings to use
     * @return the updated file
     * @throws ParseException         if failed to internally parse any of the files (usually compatibility problem)
     * @throws NullPointerException   if disk or resource file path is not set
     * @throws IOException            if any IO operation (reading and saving from/to files)
     * @throws ClassCastException     if an object failed to cast (usually compatibility problem)
     * @throws ClassNotFoundException if class was not found (usually compatibility problem)
     */
    public UpdatedFile<T> update(GeneralSettings generalSettings) throws ParseException, NullPointerException, IOException, ClassCastException, ClassNotFoundException {
        return Reactor.react(generalSettings, fileProvider);
    }

    /**
     * Sets the class loader used for all setting objects created by this class instance. Please note that already
     * existing settings objects will not be affected.
     *
     * @param classLoader the new class loader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the disk file used for all setting objects created by this class instance. Please note that already existing
     * settings objects will not be affected.
     *
     * @param diskFolder the new disk folder
     */
    public void setDiskFolder(File diskFolder) {
        this.diskFolder = diskFolder;
    }

    /**
     * Returns the class loader.
     *
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the disk folder.
     *
     * @return the disk folder
     */
    public File getDiskFolder() {
        return diskFolder;
    }
}