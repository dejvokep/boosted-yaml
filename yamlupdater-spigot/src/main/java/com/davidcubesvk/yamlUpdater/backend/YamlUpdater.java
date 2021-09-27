package com.davidcubesvk.yamlUpdater.backend;

import com.davidcubesvk.yamlUpdater.core.YamlUpdaterCore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Yaml updater class wrapping the main API class {@link YamlUpdaterCore}, used by plugins on servers powered by Spigot,
 * or any supported forks.
 */
public class YamlUpdater extends YamlUpdaterCore<YamlConfiguration> {

    /**
     * File provider.
     */
    private static final FileProvider<YamlConfiguration> FILE_PROVIDER = new SpigotFileProvider();

    /**
     * Creates an instance of the API class for the given plugin. This class is meant to be per-plugin-singleton,
     * therefore it should be created only once. This calls another constructor {@link #YamlUpdater(ClassLoader, File)}
     * with respective parameters {@link Class#getClassLoader()} and {@link JavaPlugin#getDataFolder()}.
     *
     * @param plugin the plugin to create access for
     * @see #YamlUpdater(ClassLoader, File) more customizable constructor
     */
    public YamlUpdater(JavaPlugin plugin) {
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