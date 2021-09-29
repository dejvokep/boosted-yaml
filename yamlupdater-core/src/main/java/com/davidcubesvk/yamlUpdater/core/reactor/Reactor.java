package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.files.YamlFile;
import com.davidcubesvk.yamlUpdater.core.settings.UpdaterSettings;
import com.davidcubesvk.yamlUpdater.core.utils.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.davidcubesvk.yamlUpdater.core.utils.Constants.YAML;

/**
 * Class responsible for reacting everything together.
 */
public class Reactor {

    /**
     * An empty string set used if there are no relocations present.
     */
    private static final Set<String> EMPTY_STRING_SET = new HashSet<>();

    /**
     * Reacts and updates a file per the given settings object. Full reacting consists of:
     * <ol>
     *     <li>loading (or creating if does not exist) the disk file,</li>
     *     <li>parsing both files using SnakeYAML,</li>
     *     <li>getting file versions from the files,</li>
     *     <li>parsing both files,</li>
     *     <li>applying relocations (see {@link Relocator}),</li>
     *     <li>merging together (see {@link Merger}),</li>
     * </ol>
     *
     * @param settings     the settings
     * @param fileProvider the file provider
     * @param <T>          type of the returned file and file provider simultaneously
     * @return the updated file
     * @throws ParseException         if failed to internally parse any of the files (usually compatibility problem)
     * @throws NullPointerException   if disk or resource file path is not set
     * @throws IOException            if any IO operation (reading and saving from/to files)
     * @throws ClassCastException     if an object failed to cast (usually compatibility problem)
     * @throws ClassNotFoundException if class was not found (usually compatibility problem)
     */
    public static YamlFile react(File userFile, InputStream defaultFile, LoaderSettings loaderSettings, UpdaterSettings updaterSettings) throws ParseException, NullPointerException, IOException, ClassCastException, ClassNotFoundException {
        if (userFile == null || defaultFile == null || loaderSettings == null || updaterSettings == null)
            throw new NullPointerException("User file, default file stream, loader or updater settings are null!");

        //If the file does not exist
        if (!userFile.exists()) {
            //If creating new file is enabled
            if (updaterSettings.isUpdatePhysicalFile()) {
                //Create directories
                if (userFile.getParentFile() != null)
                    userFile.getParentFile().mkdirs();
                //Create the file
                userFile.createNewFile();
                //Copy
                Files.copy(defaultFile, userFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            //Read and return
            return new com.davidcubesvk.yamlUpdater.core.reader.FileReader(new FileReader(userFile), loaderSettings).load();
        }

        //Load as YAML
        Map<Object, Object> diskMap = YAML.load(new FileReader(settings.getDiskFile()));

        //If the current version is not set
        if (settings.getDiskFileVersionId() == null && settings.getVersionIdPath() != null) {
            //The object
            Object version = YamlFile.get(diskMap, settings.getVersionIdPath(), settings.getSeparatorString(), settings.getEscapedSeparator());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersionId(version.toString());
        }

        //If the latest version is not set
        if (settings.getResourceFileVersionId() == null && settings.getVersionIdPath() != null) {
            //The object
            Object version = YamlFile.get(resourceMap, settings.getVersionIdPath(), settings.getSeparatorString(), settings.getEscapedSeparator());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersionId(version.toString());
        }


        //Load both the files
        YamlFile diskYamlFile = new com.davidcubesvk.yamlUpdater.core.reader.FileReader(new FileReader(settings.getDiskFile()), settings.getDiskFileVersionId() != null ?
                settings.getSectionValues().getOrDefault(settings.getDiskFileVersionId(), EMPTY_STRING_SET) : EMPTY_STRING_SET, settings).load(),
                resourceYamlFile = new com.davidcubesvk.yamlUpdater.core.reader.FileReader(new FileReader(settings.getResourceFile()), settings.getResourceFileVersionId() != null ?
                        settings.getSectionValues().getOrDefault(settings.getResourceFileVersionId(), EMPTY_STRING_SET) : EMPTY_STRING_SET, settings).load();

        //If both versions are set
        if (settings.getDiskFileVersionId() != null && settings.getResourceFileVersionId() != null) {
            //Initialize relocator
            Relocator relocator = new Relocator(
                    diskYamlFile,
                    settings.getVersioningPattern().getVersion(settings.getDiskFileVersionId()),
                    settings.getVersioningPattern().getVersion(settings.getResourceFileVersionId()),
                    settings.getSeparatorString(), settings.getEscapedSeparator());
            //Apply all
            relocator.apply(settings.getRelocations());
        }

        //Merge
        String merged = Merger.merge(diskYamlFile, resourceYamlFile, resourceMap, settings);
        //If file update is enabled
        if (settings.isUpdateDiskFile()) {
            //Overwrite
            FileWriter writer = new FileWriter(settings.getDiskFile(), false);
            //Write
            writer.write(merged);
            //Close
            writer.close();
        }
        //Return
        return new UpdatedFile<>(settings, fileProvider, diskMap, resourceMap, merged);
    }

}