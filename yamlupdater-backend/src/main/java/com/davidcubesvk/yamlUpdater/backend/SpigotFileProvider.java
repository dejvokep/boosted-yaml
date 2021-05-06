package com.davidcubesvk.yamlUpdater.backend;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import java.lang.reflect.Method;
import java.util.Map;

public class SpigotFileProvider implements FileProvider<YamlConfiguration> {

    private static Method HEADER_METHOD;
    private static Method CONVERT_METHOD;

    static {
        try {
            HEADER_METHOD = YamlConfiguration.class.getDeclaredMethod("parseHeader", String.class);
            CONVERT_METHOD = YamlConfiguration.class.getDeclaredMethod("convertMapsToSections", Map.class, ConfigurationSection.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public YamlConfiguration convert(UpdatedFile<YamlConfiguration> file) throws ReflectiveOperationException, ClassCastException {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = configuration.options();
        options.pathSeparator(file.getSettings().getSeparator());
        options.indent(file.getSettings().getIndentSpaces());
        options.copyHeader(file.getSettings().isCopyHeader());

        String header = (String) HEADER_METHOD.invoke(configuration, file.getString());
        if (header.length() > 0)
            configuration.options().header(header);

        CONVERT_METHOD.invoke(configuration, file.getDiskMap(), configuration);
        return configuration;
    }
}