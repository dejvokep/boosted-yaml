package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Reactor {

    private static final Set<String> EMPTY_STRING_SET = new HashSet<>();
    private static final Yaml YAML = new Yaml();

    public static <T> UpdatedFile<T> react(Settings settings, FileProvider<T> fileProvider) throws ParseException, UnsupportedOperationException, NullPointerException, IOException, ClassCastException, ClassNotFoundException {
        if (settings.getDiskFile() == null || settings.getResourceFile() == null)
            throw new NullPointerException();

        //Load as YAML
        Map<Object, Object> resourceMap = YAML.load(new FileReader(settings.getResourceFile()));

        //If the file does not exist
        if (!settings.getDiskFile().exists() && settings.isUpdateDiskFile()) {
            //Create directories
            if (settings.getDiskFile().getParentFile() != null)
                settings.getDiskFile().getParentFile().mkdirs();
            settings.getDiskFile().createNewFile();
            //Copy
            Files.copy(settings.getResourceFile().toPath(), settings.getDiskFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(outputStream).writeObject(outputStream);
            //Read and return
            return new UpdatedFile<>(settings, fileProvider, resourceMap,
                    (Map<?, ?>) new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray())).readObject(), new String(Files.readAllBytes(settings.getResourceFile().toPath())));
        }

        //Load as YAML
        Map<Object, Object> diskMap = YAML.load(new FileReader(settings.getDiskFile()));

        //If the current version is not set
        if (settings.getDiskFileVersion() == null && settings.getVersionPath() != null) {
            //The object
            Object version = File.get(diskMap, settings.getVersionPath(), settings.getSeparatorString());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersion(version.toString());
        }

        //If the latest version is not set
        if (settings.getResourceFileVersion() == null && settings.getVersionPath() != null) {
            //The object
            Object version = File.get(resourceMap, settings.getVersionPath(), settings.getSeparatorString());
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
        String merged = Merger.merge(currentFile, latestFile, resourceMap, settings.getIndentSpaces());
        //If file update is enabled
        if (settings.isUpdateDiskFile()) {
            FileWriter writer = new FileWriter(settings.getDiskFile(), false);
            writer.write(merged);
            writer.close();
        }
        //Return
        return new UpdatedFile<>(settings, fileProvider, diskMap, resourceMap, merged);
    }

}