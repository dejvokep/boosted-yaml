package com.davidcubesvk.yamlUpdater.core.reactor;

import com.davidcubesvk.yamlUpdater.core.files.FileProvider;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;
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
    public static <T> UpdatedFile<T> react(Settings settings, FileProvider<T> fileProvider) throws ParseException, NullPointerException, IOException, ClassCastException, ClassNotFoundException {
        if (settings.getDiskFile() == null || settings.getResourceFile() == null)
            throw new NullPointerException();

        //Load as YAML
        Map<Object, Object> resourceMap = YAML.load(new FileReader(settings.getResourceFile()));

        //If the file does not exist
        if (!settings.getDiskFile().exists()) {
            //If updating the disk file is enabled
            if (settings.isUpdateDiskFile()) {
                //Create directories
                if (settings.getDiskFile().getParentFile() != null)
                    settings.getDiskFile().getParentFile().mkdirs();
                //Create the file
                settings.getDiskFile().createNewFile();
                //Copy
                Files.copy(settings.getResourceFile().toPath(), settings.getDiskFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            //Create the output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //Write the object
            new ObjectOutputStream(outputStream).writeObject(outputStream);
            //Read and return
            return new UpdatedFile<>(settings, fileProvider, resourceMap,
                    (Map<?, ?>) new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray())).readObject(), new String(Files.readAllBytes(settings.getResourceFile().toPath())));
        }

        //Load as YAML
        Map<Object, Object> diskMap = YAML.load(new FileReader(settings.getDiskFile()));

        //If the current version is not set
        if (settings.getDiskFileVersionId() == null && settings.getVersionIdPath() != null) {
            //The object
            Object version = File.get(diskMap, settings.getVersionIdPath(), settings.getSeparatorString(), settings.getEscapedSeparator());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersionId(version.toString());
        }

        //If the latest version is not set
        if (settings.getResourceFileVersionId() == null && settings.getVersionIdPath() != null) {
            //The object
            Object version = File.get(resourceMap, settings.getVersionIdPath(), settings.getSeparatorString(), settings.getEscapedSeparator());
            //If not null
            if (version != null)
                //Set
                settings.setResourceFileVersionId(version.toString());
        }


        //Load both the files
        File currentFile = new File(new FileReader(settings.getDiskFile()), settings.getDiskFileVersionId() != null ?
                settings.getSectionValues().getOrDefault(settings.getDiskFileVersionId(), EMPTY_STRING_SET) : EMPTY_STRING_SET, settings),
                latestFile = new File(new FileReader(settings.getResourceFile()), settings.getResourceFileVersionId() != null ?
                        settings.getSectionValues().getOrDefault(settings.getResourceFileVersionId(), EMPTY_STRING_SET) : EMPTY_STRING_SET, settings);

        //If both versions are set
        if (settings.getDiskFileVersionId() != null && settings.getResourceFileVersionId() != null) {
            //Initialize relocator
            Relocator relocator = new Relocator(
                    currentFile,
                    settings.getVersioningPattern().getVersion(settings.getDiskFileVersionId()),
                    settings.getVersioningPattern().getVersion(settings.getResourceFileVersionId()),
                    settings.getSeparatorString(), settings.getEscapedSeparator());
            //Apply all
            relocator.apply(settings.getRelocations());
        }

        //Merge
        String merged = Merger.merge(currentFile, latestFile, resourceMap, settings.getIndentSpaces());
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