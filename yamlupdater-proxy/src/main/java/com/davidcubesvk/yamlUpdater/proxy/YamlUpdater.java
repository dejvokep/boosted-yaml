package com.davidcubesvk.yamlUpdater.proxy;

import com.davidcubesvk.yamlUpdater.core.YamlUpdaterCore;
import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;

public class YamlUpdater extends YamlUpdaterCore<Configuration> {

    private static final FileProvider<Configuration> FILE_PROVIDER = new BungeeFileProvider();

    public YamlUpdater(Plugin plugin) {
        super(plugin.getClass().getClassLoader(), plugin.getDataFolder(), FILE_PROVIDER);
    }

    public YamlUpdater(ClassLoader classLoader, File diskFolder) {
        super(classLoader, diskFolder, FILE_PROVIDER);
    }
}