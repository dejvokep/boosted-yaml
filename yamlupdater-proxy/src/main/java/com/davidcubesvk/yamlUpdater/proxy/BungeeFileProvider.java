package com.davidcubesvk.yamlUpdater.proxy;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import net.md_5.bungee.config.Configuration;

import java.lang.reflect.Constructor;
import java.util.Map;

public class BungeeFileProvider implements FileProvider<Configuration> {

    private static Constructor DEFAULTS_CONSTRUCTOR;

    static {
        try {
            DEFAULTS_CONSTRUCTOR = Configuration.class.getDeclaredConstructor(Map.class, Configuration.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Configuration convert(UpdatedFile<Configuration> file) throws ReflectiveOperationException, ClassCastException {
        return (Configuration) DEFAULTS_CONSTRUCTOR.newInstance(file.getDiskMap(), DEFAULTS_CONSTRUCTOR.newInstance(file.getResourceMap(), null));
    }
}