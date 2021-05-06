package com.davidcubesvk.yamlUpdater.backend;

import com.davidcubesvk.yamlUpdater.core.YamlUpdaterCore;
import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class YamlUpdater extends YamlUpdaterCore<YamlConfiguration> {

    private static final FileProvider<YamlConfiguration> FILE_PROVIDER = new SpigotFileProvider();

    public YamlUpdater(JavaPlugin plugin) {
        super(plugin.getClass().getClassLoader(), plugin.getDataFolder(), FILE_PROVIDER);
    }

    public YamlUpdater(ClassLoader classLoader, File diskFolder) {
        super(classLoader, diskFolder, FILE_PROVIDER);
    }
}