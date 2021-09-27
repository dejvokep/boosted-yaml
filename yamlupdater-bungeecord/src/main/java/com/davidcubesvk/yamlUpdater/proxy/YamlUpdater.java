package com.davidcubesvk.yamlUpdater.proxy;

import com.davidcubesvk.yamlUpdater.core.YamlUpdaterCore;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;

/**
 * Yaml updater class wrapping the main API class {@link YamlUpdaterCore}, used by plugins on servers powered by
 * BungeeCord, or any supported forks.
 */
public class YamlUpdater extends YamlUpdaterCore<Configuration> {

    /**
     * File provider.
     */
    private static final FileProvider<Configuration> FILE_PROVIDER = new BungeeFileProvider();

    /**
     * Creates an instance of the API class for the given plugin. This class is meant to be per-plugin-singleton,
     * therefore it should be created only once. This calls another constructor {@link #YamlUpdater(ClassLoader, File)}
     * with respective parameters {@link Class#getClassLoader()} and {@link Plugin#getDataFolder()}.
     *
     * @param plugin the plugin to create access for
     * @see #YamlUpdater(ClassLoader, File) more customizable constructor
     */
    public YamlUpdater(Plugin plugin) {
        super(plugin.getClass().getClassLoader(), plugin.getDataFolder(), FILE_PROVIDER);
    }

    /**
     * Creates an instance of the API class for the given class loader (usually main class loader, used to retrieve
     * compiled resources from the resource folder) and disk folder (usually the plugin folder, used to get already
     * existing configuration to update from the disk). This class is meant to be per-plugin-singleton,
     * therefore it should be created only once.
     *
     * @param classLoader the class loader of the main plugin class
     * @param diskFolder  the folder to get disk files from
     */
    public YamlUpdater(ClassLoader classLoader, File diskFolder) {
        super(classLoader, diskFolder, FILE_PROVIDER);
    }
}