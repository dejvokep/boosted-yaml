package com.davidcubesvk.yamlUpdater.reactor;

import com.davidcubesvk.yamlUpdater.ParseException;
import com.davidcubesvk.yamlUpdater.Settings;
import com.davidcubesvk.yamlUpdater.UpdatedFile;
import com.davidcubesvk.yamlUpdater.block.Block;
import com.davidcubesvk.yamlUpdater.block.Section;
import com.davidcubesvk.yamlUpdater.version.Version;
import com.davidcubesvk.yamlUpdater.YamlUpdater;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Reactor {

    private static final Set<String> EMPTY_STRING_SET = new HashSet<>();
    private static final Yaml YAML = new Yaml();

    public static UpdatedFile react(Settings settings) throws ParseException, UnsupportedOperationException, NullPointerException, IOException {
        if (settings.getDiskFile() == null || settings.getResourceFile() == null)
            throw new NullPointerException();

        //If the file does not exist
        if (!settings.getDiskFile().exists() && settings.isUpdateDiskFile()) {
            //Create directories
            if (settings.getDiskFile().getParentFile() != null)
                settings.getDiskFile().getParentFile().mkdirs();
            settings.getDiskFile().createNewFile();
            //Copy
            Files.copy(settings.getResourceFile().toPath(), settings.getDiskFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            //Read and return
            return new UpdatedFile(settings, YAML.load(new FileReader(settings.getResourceFile())), new String(Files.readAllBytes(settings.getResourceFile().toPath())));
        }

        //Load as YAML
        Map<Object, Object> resourceFile = YAML.load(new FileReader(settings.getResourceFile()));

        //If the current version is not set
        if (settings.getDiskFileVersion() == null && settings.getVersionPath() != null) {
            //The object
            Object version = File.get(YAML.load(new FileReader(settings.getDiskFile())), settings.getVersionPath(), settings.getSeparatorString());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersion(version.toString());
        }

        //If the latest version is not set
        if (settings.getResourceFileVersion() == null && settings.getVersionPath() != null) {
            //The object
            Object version = File.get(resourceFile, settings.getVersionPath(), settings.getSeparatorString());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersion(version.toString());
        }


        //Load both the files
        File currentFile = new File(new FileReader(settings.getDiskFile()), settings.getDiskFileVersion() != null ?
                settings.getSectionValues().getOrDefault(settings.getDiskFileVersion(), EMPTY_STRING_SET) : EMPTY_STRING_SET, settings.getSeparator()),
                latestFile = new File(new FileReader(settings.getResourceFile()), settings.getResourceFileVersion() != null ?
                        settings.getSectionValues().getOrDefault(settings.getResourceFileVersion(), EMPTY_STRING_SET) : EMPTY_STRING_SET, settings.getSeparator());

        //If both versions are set
        if (settings.getDiskFileVersion() != null && settings.getResourceFileVersion() != null) {
            //Initialize relocator
            Relocator relocator = new Relocator(
                    currentFile,
                    settings.getVersionPattern().getVersion(settings.getDiskFileVersion()),
                    settings.getVersionPattern().getVersion(settings.getResourceFileVersion()));
            //Apply all
            relocator.apply(settings.getRelocations());
        }

        //Merge
        String merged = Merger.merge(currentFile, latestFile, resourceFile, settings.getIndentSpaces());
        //If file update is enabled
        if (settings.isUpdateDiskFile()) {
            FileWriter writer = new FileWriter(settings.getDiskFile(), false);
            writer.write(merged);
            writer.close();
        }
        //Return
        return new UpdatedFile(settings, resourceFile, merged);
    }

}