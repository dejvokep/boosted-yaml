package com.davidcubesvk.yamlUpdater.backend;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * File provider for the updater if used on a server powered by Spigot.
 */
public class SpigotFileProvider implements FileProvider<YamlConfiguration> {

    /**
     * Method used to parse the header of a YAML file.
     */
    private static Method HEADER_METHOD;
    /**
     * Method used to convert Maps to configuration sections.
     */
    private static Method CONVERT_METHOD;

    static {
        try {
            //Get the methods
            HEADER_METHOD = YamlConfiguration.class.getDeclaredMethod("parseHeader", String.class);
            CONVERT_METHOD = YamlConfiguration.class.getDeclaredMethod("convertMapsToSections", Map.class, ConfigurationSection.class);
            //Set accessible
            HEADER_METHOD.setAccessible(true);
            CONVERT_METHOD.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            //Print the exception
            ex.printStackTrace();
        }
    }

    @Override
    public YamlConfiguration convert(UpdatedFile<YamlConfiguration> file) throws ReflectiveOperationException, ClassCastException {
        //Create a configuration
        YamlConfiguration configuration = new YamlConfiguration();
        //Options
        YamlConfigurationOptions options = configuration.options();
        //Configure from settings
        options.pathSeparator(file.getSettings().getSeparator());
        options.indent(file.getSettings().getIndentSpaces());
        options.copyHeader(file.getSettings().isCopyHeader());

        //Parse the header
        String header = (String) HEADER_METHOD.invoke(configuration, file.getString());
        //If not empty
        if (header.length() > 0)
            //Set
            configuration.options().header(header);

        //Convert
        CONVERT_METHOD.invoke(configuration, file.getMap(), configuration);
        //Return
        return configuration;
    }
}