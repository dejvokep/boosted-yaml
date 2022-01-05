/*
 * Copyright 2021 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.updater.Updater;
import dev.dejvokep.boostedyaml.engine.ExtendedConstructor;
import dev.dejvokep.boostedyaml.engine.ExtendedRepresenter;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.representer.BaseRepresenter;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.serializer.Serializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Represents a YAML file.
 */
@SuppressWarnings("unused")
public class YamlFile extends Section {

    // The file
    private final File file;
    // Defaults
    private final YamlFile defaults;
    // Settings
    private GeneralSettings generalSettings;
    private LoaderSettings loaderSettings;
    private DumperSettings dumperSettings;
    private UpdaterSettings updaterSettings;

    /**
     * Loads the given user file and defaults, which are given (associated to) the same settings.
     * <p>
     * Associates loaded defaults and settings.
     *
     * @param userFile        stream to load from
     * @param defaults        defaults to load
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @throws IOException an IO error
     */
    private YamlFile(@NotNull InputStream userFile, @Nullable InputStream defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = null;
        this.defaults = defaults == null ? null : new YamlFile(new BufferedInputStream(defaults), null, generalSettings, loaderSettings, dumperSettings, updaterSettings);

        //Load
        reload(userFile instanceof BufferedInputStream ? (BufferedInputStream) userFile : new BufferedInputStream(userFile));
    }

    /**
     * Loads the given user file and defaults, which are given (associated to) the same settings.
     * <p>
     * Associates the given user file, loaded defaults and settings.
     *
     * @param userFile        file to load from
     * @param defaults        defaults to load
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @throws IOException an IO error
     */
    private YamlFile(@NotNull File userFile, @Nullable InputStream defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = userFile;
        this.defaults = defaults == null ? null : new YamlFile(new BufferedInputStream(defaults), null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
        //Load
        reload();
    }

    //
    //
    //      -----------------------
    //
    //
    //             Reload
    //
    //
    //      -----------------------
    //
    //

    /**
     * Reloads the contents from the associated user file ({@link #getFile}) using the associated settings ({@link
     * #getLoaderSettings()} and {@link #getGeneralSettings()}).
     * <p>
     * If there is no associated user file, returns <code>false</code> (use other loading methods). Returns
     * <code>true</code> otherwise.
     *
     * @return if there is any associated (user) file
     * @throws IOException an IO error
     */
    public boolean reload() throws IOException {
        //If not present
        if (file == null)
            return false;
        //Load
        reload(file);
        return true;
    }

    /**
     * Reloads the contents from the given file using the associated settings ({@link #getLoaderSettings()} and {@link
     * #getGeneralSettings()}).
     * <p>
     * If the given file does not <b>physically</b> exist, loads the defaults instead.
     *
     * @param file file to load the contents from
     * @throws IOException an IO error
     */
    public void reload(@NotNull File file) throws IOException {
        //Validate
        Objects.requireNonNull(file, "File cannot be null!");
        //Clear
        clear();
        //If exists
        if (file.exists()) {
            //Load from the file
            reload(new BufferedInputStream(new FileInputStream(file)));
            return;
        }

        //Create if enabled
        if (loaderSettings.isCreateFileIfAbsent())
            file.createNewFile();

        //If there are no defaults
        if (defaults == null) {
            //Initialize empty
            initEmpty(this);
            return;
        }

        //Load the defaults
        reload(new BufferedInputStream(new ByteArrayInputStream(defaults.dump().getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Reloads the contents from the given stream using the associated settings ({@link #getLoaderSettings()} and {@link
     * #getGeneralSettings()}).
     *
     * @param inputStream stream to load the contents from
     * @throws IOException an IO error
     */
    public void reload(@NotNull BufferedInputStream inputStream) throws IOException {
        reload(inputStream, loaderSettings, generalSettings);
    }

    /**
     * Reloads the contents from the given stream and using the given settings.
     *
     * @param inputStream     stream to load the contents from
     * @param loaderSettings  the loader settings to use for this reload
     * @param generalSettings the general settings to use for this reload
     * @throws IOException an IO error
     */
    public void reload(@NotNull BufferedInputStream inputStream, @NotNull LoaderSettings loaderSettings, @NotNull GeneralSettings generalSettings) throws IOException {
        //Validate
        Objects.requireNonNull(inputStream, "Input stream cannot be null!");
        Objects.requireNonNull(loaderSettings, "Loader settings cannot be null!");
        Objects.requireNonNull(generalSettings, "General settings cannot be null!");
        //Clear
        clear();

        //Create the settings
        LoadSettings settings = loaderSettings.buildEngineSettings(generalSettings);
        //Create the constructor
        ExtendedConstructor constructor = new ExtendedConstructor(settings, generalSettings.getSerializer());
        //Create the parser and composer
        Parser parser = new ParserImpl(settings, new StreamReader(settings, new YamlUnicodeReader(inputStream)));
        Composer composer = new Composer(settings, parser);
        //Drop the first event
        parser.next();
        //Node
        Node node = composer.next();
        //Construct
        constructor.constructSingleDocument(Optional.of(node));

        //Init
        init(this, null, (MappingNode) node, constructor);
        //Clear
        constructor.clear();

        //If enabled
        if (file != null && loaderSettings.isCreateFileIfAbsent() && !file.exists()) {
            //Create new file
            file.createNewFile();
            //Save
            save();
        }

        //Update if enabled
        if (defaults != null && loaderSettings.isAutoUpdate())
            Updater.update(this, defaults, updaterSettings, generalSettings);
    }

    //
    //
    //      -----------------------
    //
    //
    //             Update
    //
    //
    //      -----------------------
    //
    //

    /**
     * Updates the contents against the associated defaults ({@link #getDefaults()}) using the associated settings
     * ({@link #getUpdaterSettings()} and {@link #getGeneralSettings()}).
     * <p>
     * If there are no associated defaults, returns <code>false</code> (use other updating methods). Returns
     * <code>true</code> otherwise.
     *
     * @return if there are any associated defaults
     * @throws IOException an IO error
     */
    public boolean update() throws IOException {
        return update(updaterSettings);
    }

    /**
     * Updates the contents against the associated defaults ({@link #getDefaults()}) using the given settings.
     * <p>
     * If there are no associated defaults, returns <code>false</code> (use other updating methods). Returns
     * <code>true</code> otherwise.
     *
     * @param updaterSettings updater settings to use for this update
     * @return if there are any associated defaults
     * @throws IOException an IO error
     */
    public boolean update(@NotNull UpdaterSettings updaterSettings) throws IOException {
        //If there are no defaults
        if (defaults == null)
            return false;
        //Validate
        Objects.requireNonNull(updaterSettings, "Updater settings cannot be null!");
        //Update
        Updater.update(this, defaults, updaterSettings, generalSettings);
        return true;
    }

    /**
     * Updates the contents against the given defaults using the associated settings ({@link #getUpdaterSettings()} and
     * {@link #getGeneralSettings()}).
     *
     * @param defaults defaults to update against (will be loaded using the associated settings)
     * @throws IOException an IO error
     */
    public void update(@NotNull InputStream defaults) throws IOException {
        update(defaults, updaterSettings);
    }

    /**
     * Updates the contents against the given defaults using the given settings.
     *
     * @param defaults        defaults to update against (will be loaded using the associated settings)
     * @param updaterSettings updater settings to use for this update
     * @throws IOException an IO error
     */
    public void update(@NotNull InputStream defaults, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Validate
        Objects.requireNonNull(defaults, "Defaults cannot be null!");
        Objects.requireNonNull(updaterSettings, "Updater settings cannot be null!");
        //Update
        Updater.update(this, YamlFile.create(defaults, generalSettings, loaderSettings, dumperSettings, UpdaterSettings.DEFAULT), updaterSettings, generalSettings);
    }

    //
    //
    //      -----------------------
    //
    //
    //              Save
    //
    //
    //      -----------------------
    //
    //

    /**
     * Saves the contents into the associated user file ({@link #getFile()}) using the associated settings ({@link
     * #getDumperSettings()} and {@link #getGeneralSettings()}).
     * <p>
     * If there is no associated user file, returns <code>false</code> (use other saving methods). Returns
     * <code>true</code> otherwise.
     * <p>
     * <b>Does not include the defaults</b>.
     *
     * @return if there is any associated (user) file
     * @throws IOException an IO error
     */
    public boolean save() throws IOException {
        //If not present
        if (file == null)
            return false;

        //Save
        save(file);
        return true;
    }

    /**
     * Saves the contents into the given file using the associated settings ({@link #getDumperSettings()} and {@link
     * #getGeneralSettings()}).
     * <p>
     * <b>Does not include the defaults</b>.
     *
     * @param file file to save to
     * @throws IOException an IO error
     */
    public void save(@NotNull File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            //Save
            save(fileWriter);
        }
    }

    /**
     * Saves the contents to the given stream using the associated settings ({@link #getDumperSettings()} and {@link
     * #getGeneralSettings()}).
     * <p>
     * <b>Does not include the defaults</b>.
     *
     * @param stream  stream to save to
     * @param charset charset to use
     * @throws IOException an IO error
     */
    public void save(@NotNull OutputStream stream, Charset charset) throws IOException {
        stream.write(dump().getBytes(charset));
    }

    /**
     * Saves the contents to the given stream using the associated settings ({@link #getDumperSettings()} and {@link
     * #getGeneralSettings()}).
     * <p>
     * <b>Does not include the defaults</b>.
     *
     * @param writer writer to save to
     * @throws IOException an IO error
     */
    public void save(@NotNull OutputStreamWriter writer) throws IOException {
        writer.write(dump());
    }

    /**
     * Dumps the contents to a string using the associated settings ({@link #getDumperSettings()} and {@link
     * #getGeneralSettings()}).
     * <p>
     * <b>Does not include the defaults</b>.
     *
     * @return the dumped contents
     */
    public String dump() {
        return dump(dumperSettings, generalSettings);
    }

    /**
     * Dumps the contents to a string using the given settings.
     * <p>
     * <b>Does not include the defaults</b>.
     *
     * @param dumperSettings  dumper settings to use for this dump
     * @param generalSettings general settings to use for this dump
     * @return the dumped contents
     */
    public String dump(DumperSettings dumperSettings, GeneralSettings generalSettings) {
        //Create the settings
        DumpSettings settings = dumperSettings.buildEngineSettings();
        //Output
        SerializedStream stream = new SerializedStream();
        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(generalSettings, settings);

        //Serializer
        Serializer serializer = new Serializer(settings, new Emitter(settings, stream));
        serializer.open();
        //Serialize
        serializer.serialize(representer.represent(this));
        //Close
        serializer.close();

        //Return
        return stream.toString();
    }

    //
    //
    //      -----------------------
    //
    //
    //        Association methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Associates new loader settings.
     *
     * @param loaderSettings the new loader settings
     */
    public void setLoaderSettings(@NotNull LoaderSettings loaderSettings) {
        this.loaderSettings = loaderSettings;
    }

    /**
     * Associates new dumper settings.
     *
     * @param dumperSettings the new dumper settings
     */
    public void setDumperSettings(@NotNull DumperSettings dumperSettings) {
        this.dumperSettings = dumperSettings;
    }

    /**
     * Associates new general settings.
     * <p>
     * <b>WARNING!</b>
     * <ul>
     *     <li>Never change the key mode! Such attempts will result in a {@link IllegalArgumentException}.</li>
     *     <li>If the default {@link GeneralSettings#getDefaultList() list}, {@link GeneralSettings#getDefaultMap() map}
     *     or {@link GeneralSettings#getDefaultSet() set} was changed, already existing instances will not be changed.
     *     Reload to take effect.</li>
     * </ul>
     *
     * @param generalSettings the new general settings
     */
    public void setGeneralSettings(@NotNull GeneralSettings generalSettings) {
        //Validate
        if (generalSettings.getKeyMode() != this.generalSettings.getKeyMode())
            throw new IllegalArgumentException("Cannot change key mode! Recreate the file if needed to do so.");
        //Set
        this.generalSettings = generalSettings;
    }

    /**
     * Associates new updater settings.
     *
     * @param updaterSettings the new updater settings
     */
    public void setUpdaterSettings(@NotNull UpdaterSettings updaterSettings) {
        this.updaterSettings = updaterSettings;
    }

    //
    //
    //      -----------------------
    //
    //
    //             Getters
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns the defaults associated with the file.
     *
     * @return the defaults associated
     */
    public YamlFile getDefaults() {
        return defaults;
    }

    /**
     * Returns the general settings associated with the file.
     *
     * @return the general settings associated
     */
    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    /**
     * Returns the dumper settings associated with the file.
     *
     * @return the dumper settings associated
     */
    public DumperSettings getDumperSettings() {
        return dumperSettings;
    }

    /**
     * Returns the updater settings associated with the file.
     *
     * @return the updater settings associated
     */
    public UpdaterSettings getUpdaterSettings() {
        return updaterSettings;
    }

    /**
     * Returns the loader settings associated with the file.
     *
     * @return the loader settings associated
     */
    public LoaderSettings getLoaderSettings() {
        return loaderSettings;
    }

    /**
     * Returns the user file associated with this file.
     *
     * @return the user file associated
     */
    public File getFile() {
        return file;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    //
    //
    //      -----------------------
    //
    //
    //             Creators
    //
    //
    //      -----------------------
    //
    //

    /**
     * Loads the given user file and defaults, which are given (associated to) the same settings.
     * <p>
     * Associates the given user file, loaded defaults and settings.
     *
     * @param userFile        file to load from
     * @param defaults        defaults to load
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(File userFile, InputStream defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Loads the given user file and defaults, which are given (associated to) the same settings.
     * <p>
     * Associates the given user file, loaded defaults and all default settings ({@link GeneralSettings#DEFAULT}, {@link
     * LoaderSettings#DEFAULT}...).
     *
     * @param userFile file to load from
     * @param defaults defaults to load
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(File userFile, InputStream defaults) throws IOException {
        return create(userFile, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Loads the given user file and defaults, which are given (associated to) the same settings.
     * <p>
     * Associates the loaded defaults and settings.
     *
     * @param userFile        stream to load from
     * @param defaults        defaults to load
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(InputStream userFile, InputStream defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Loads the given user file and defaults, which are given (associated to) the same settings.
     * <p>
     * Associates the loaded defaults and all default settings ({@link GeneralSettings#DEFAULT}, {@link
     * LoaderSettings#DEFAULT}...).
     *
     * @param userFile stream to load from
     * @param defaults defaults to load
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(InputStream userFile, InputStream defaults) throws IOException {
        return create(userFile, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Loads the given user file.
     * <p>
     * Associates the given user file and settings. The returned file will not have any defaults.
     *
     * @param userFile        file to load from
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(File userFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Loads the given user file.
     * <p>
     * Associates the given user file and all default settings ({@link GeneralSettings#DEFAULT}, {@link
     * LoaderSettings#DEFAULT}...).
     *
     * @param userFile file to load from
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(File userFile) throws IOException {
        return create(userFile, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Loads the given user file.
     * <p>
     * Associates the given settings. The returned file will not have any defaults.
     *
     * @param userFile        stream to load from
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(InputStream userFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Loads the given user file.
     * <p>
     * Associates all default settings ({@link GeneralSettings#DEFAULT}, {@link LoaderSettings#DEFAULT}...).
     *
     * @param userFile stream to load from
     * @return the created file
     * @throws IOException an IO error
     */
    public static YamlFile create(InputStream userFile) throws IOException {
        return create(userFile, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Class used to write to a string.
     */
    private static class SerializedStream extends StringWriter implements StreamDataWriter {
    }

}