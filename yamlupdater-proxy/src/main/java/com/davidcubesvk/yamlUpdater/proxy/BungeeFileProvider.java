package com.davidcubesvk.yamlUpdater.proxy;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import net.md_5.bungee.config.Configuration;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * File provider for the updater if used on a server powered by BungeeCord.
 */
public class BungeeFileProvider implements FileProvider<Configuration> {

    /**
     * Constructor used to initialize {@link Configuration} objects with defaults.
     */
    private static Constructor<Configuration> DEFAULTS_CONSTRUCTOR;

    static {
        try {
            //Get the constructor
            DEFAULTS_CONSTRUCTOR = Configuration.class.getDeclaredConstructor(Map.class, Configuration.class);
            //Set accessible
            DEFAULTS_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            //Print the exception
            ex.printStackTrace();
        }
    }

    @Override
    public Configuration convert(UpdatedFile<Configuration> file) throws ReflectiveOperationException, ClassCastException {
        //Create a new instance
        return DEFAULTS_CONSTRUCTOR.newInstance(file.getMap(), DEFAULTS_CONSTRUCTOR.newInstance(file.getResourceMap(), null));
    }
}