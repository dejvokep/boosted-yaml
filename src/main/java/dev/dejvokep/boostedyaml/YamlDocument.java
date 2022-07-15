/*
 * Copyright 2022 https://dejvokep.dev/
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
import dev.dejvokep.boostedyaml.engine.ExtendedConstructor;
import dev.dejvokep.boostedyaml.engine.ExtendedRepresenter;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.updater.Updater;
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
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a YAML document.
 */
@SuppressWarnings("unused")
public class YamlDocument extends Section {

    // The file
    private final File file;
    // Defaults
    private final YamlDocument defaults;
    // Settings
    private GeneralSettings generalSettings;
    private LoaderSettings loaderSettings;
    private DumperSettings dumperSettings;
    private UpdaterSettings updaterSettings;

    /**
     * Creates and initially loads YAML document from the given stream using the given settings. The defaults will also
     * be loaded using the same settings.
     * <p>
     * The given settings will now be associated with the created document and loaded defaults. As you are not providing
     * a {@link File}, you will need to provide data input/output each time you're reloading/saving.
     *
     * @param document        document
     * @param defaults        defaults
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @throws IOException an IO error
     */
    private YamlDocument(@NotNull InputStream document, @Nullable InputStream defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = null;
        this.defaults = defaults == null ? null : new YamlDocument(defaults, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);

        //Load
        reload(document);
    }

    /**
     * Creates and initially loads YAML document from the given file using the given settings. The defaults will also be
     * loaded using the same settings.
     * <p>
     * The given settings will now be associated with the created document and loaded defaults. You will be able to use
     * {@link #save()} and {@link #reload()}.
     * <p>
     * If the given {@link File} does not exist, the document will be loaded from a <b>copy</b> of the defaults. If
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) enabled}, the file will automatically be created and
     * saved.
     *
     * @param document        document (does not need to {@link File#exists() exist})
     * @param defaults        defaults
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @throws IOException an IO error
     */
    private YamlDocument(@NotNull File document, @Nullable InputStream defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = document;
        this.defaults = defaults == null ? null : new YamlDocument(defaults, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
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
     * Reloads the contents from the {@link #getFile() associated file} using the associated {@link #getLoaderSettings()
     * loader} and {@link #getGeneralSettings() general} settings.
     * <p>
     * Returns if the operation was successful - <code>false</code> if there is no associated file, <code>true</code>
     * otherwise.
     * <p>
     * If the file does not exist, the document will be reloaded from a <b>copy</b> of the defaults (or empty if there
     * are not any). Unless {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will
     * automatically be created and saved.
     *
     * @return if the operation was successful (see {@link #getFile()})
     * @throws IOException an IO error
     */
    public boolean reload() throws IOException {
        //If not present
        if (file == null)
            return false;
        //Reload
        reload(file);
        return true;
    }

    /**
     * Reloads the contents from the given file using the associated {@link #getLoaderSettings() loader} and {@link
     * #getGeneralSettings() general} settings.
     * <p>
     * If the file does not exist, the document will be reloaded from a <b>copy</b> of the defaults (or empty if there
     * are not any). Unless {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will
     * automatically be created and saved.
     *
     * @param file file to reload from
     * @throws IOException an IO error
     */
    private void reload(@NotNull File file) throws IOException {
        //Clear
        clear();
        //If exists
        if (Objects.requireNonNull(file, "File cannot be null!").exists()) {
            //Load from the file
            reload(new BufferedInputStream(new FileInputStream(file)));
            return;
        }

        //Create if enabled
        if (loaderSettings.isCreateFileIfAbsent()) {
            //Create new file
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();
            file.createNewFile();
        }

        //If there are no defaults
        if (defaults == null) {
            //Initialize empty
            initEmpty(this);
            return;
        }

        //Dump
        String dump = defaults.dump();
        //Save
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            //Save
            writer.write(dump);
        }

        //Load the defaults
        reload(new BufferedInputStream(new ByteArrayInputStream(dump.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Reloads the contents from the given stream using the associated {@link #getLoaderSettings() loader} and {@link
     * #getGeneralSettings() general} settings.
     * <p>
     * If there is any {@link #getFile() associated file} and it does not exist, unless {@link
     * LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will automatically be created and
     * saved.
     *
     * @param inputStream file to reload from
     * @throws IOException an IO error
     */
    public void reload(@NotNull InputStream inputStream) throws IOException {
        reload(inputStream, loaderSettings);
    }

    /**
     * Reloads the contents from the given stream using the given loader and associated {@link #getGeneralSettings()
     * general} settings.
     * <p>
     * If there is any {@link #getFile() associated file} and it does not exist, unless {@link
     * LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will automatically be created and
     * saved.
     *
     * @param inputStream    file to reload from
     * @param loaderSettings loader settings to use
     * @throws IOException an IO error
     */
    public void reload(@NotNull InputStream inputStream, @NotNull LoaderSettings loaderSettings) throws IOException {
        //Clear
        clear();

        //Create the settings
        LoadSettings settings = Objects.requireNonNull(loaderSettings, "Loader settings cannot be null!").buildEngineSettings(generalSettings);
        //Create the constructor
        ExtendedConstructor constructor = new ExtendedConstructor(settings, generalSettings.getSerializer());
        //Create the parser and composer
        Parser parser = new ParserImpl(settings, new StreamReader(settings, new YamlUnicodeReader(Objects.requireNonNull(inputStream, "Input stream cannot be null!"))));
        Composer composer = new Composer(settings, parser);

        //If there's no next document (also drops stream start)
        if (composer.hasNext()) {
            //Node
            Node node = composer.next();
            //Handle
            if (composer.hasNext())
                throw new InvalidObjectException("Multiple documents are not supported!");
            if (!(node instanceof MappingNode))
                throw new IllegalArgumentException(String.format("Top level object is not a map! Parsed node: %s", node.toString()));
            //Construct
            constructor.constructSingleDocument(Optional.of(node));

            //Init
            init(this, null, (MappingNode) node, constructor);
            //Clear
            constructor.clear();
        } else {
            //Init
            initEmpty(this);
        }

        //If enabled
        if (file != null && loaderSettings.isCreateFileIfAbsent() && !file.exists()) {
            //Create new file
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();
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
     * Updates the contents against the {@link #getDefaults() associated defaults} using the associated {@link
     * #getUpdaterSettings() updater} and {@link #getGeneralSettings() general} settings.
     * <p>
     * Returns if the operation was successful - <code>false</code> if there are no defaults, <code>true</code>
     * otherwise.
     *
     * @return if the operation was successful (see {@link #getDefaults()})
     * @throws IOException an IO error
     */
    public boolean update() throws IOException {
        return update(updaterSettings);
    }

    /**
     * Updates the contents against the {@link #getDefaults() associated defaults} using the given updater and
     * associated {@link #getGeneralSettings() general} settings.
     * <p>
     * Returns if the operation was successful - <code>false</code> if there are no defaults, <code>true</code>
     * otherwise.
     *
     * @param updaterSettings updater settings to use
     * @return if the operation was successful (see {@link #getDefaults()})
     * @throws IOException an IO error
     */
    public boolean update(@NotNull UpdaterSettings updaterSettings) throws IOException {
        //If there are no defaults
        if (defaults == null)
            return false;
        //Update
        Updater.update(this, defaults, Objects.requireNonNull(updaterSettings, "Updater settings cannot be null!"), generalSettings);
        return true;
    }

    /**
     * Updates the contents against the given defaults using the associated {@link #getUpdaterSettings() updater} and
     * {@link #getGeneralSettings() general} settings.
     * <p>
     * Please note that this involves loading a YAML document from the given stream. <b>If you'd like to update, but
     * don't want the defaults to be used in any other means,</b> you can disable use of them via {@link
     * GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right to the
     * <code>YamlDocument.create()</code> method.
     *
     * @param defaults defaults to load and update against
     * @throws IOException an IO error
     */
    public void update(@NotNull InputStream defaults) throws IOException {
        update(defaults, updaterSettings);
    }

    /**
     * Updates the contents against the given defaults using the given updater and associated {@link
     * #getGeneralSettings() general} settings.
     * <p>
     * Please note that this involves loading a YAML document from the given stream. <b>If you'd like to update, but
     * don't want the defaults to be used in any other means,</b> you can disable use of them via {@link
     * GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right to the
     * <code>YamlDocument.create()</code> method.
     *
     * @param defaults        defaults to load and update against
     * @param updaterSettings updater settings to use
     * @throws IOException an IO error
     */
    public void update(@NotNull InputStream defaults, @NotNull UpdaterSettings updaterSettings) throws IOException {
        Updater.update(this, YamlDocument.create(Objects.requireNonNull(defaults, "Defaults cannot be null!"), generalSettings, loaderSettings, dumperSettings, UpdaterSettings.DEFAULT), Objects.requireNonNull(updaterSettings, "Updater settings cannot be null!"), generalSettings);
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
     * Saves the contents to the {@link #getFile() associated file} using the associated {@link #getDumperSettings()
     * dumper} and {@link #getGeneralSettings() general} settings, in {@link StandardCharsets#UTF_8 UTF-8} charset.
     * <p>
     * Returns if the operation was successful - <code>false</code> if there is no associated file, <code>true</code>
     * otherwise.
     *
     * @return if the operation was successful (see {@link #getFile()})
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
     * Saves the contents to the given file using the associated {@link #getDumperSettings() dumper} and {@link
     * #getGeneralSettings() general} settings, in {@link StandardCharsets#UTF_8 UTF-8} charset.
     *
     * @param file file to save to
     * @throws IOException an IO error
     */
    public void save(@NotNull File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            //Save
            writer.write(dump());
        }
    }

    /**
     * Saves the contents to the given stream using the associated {@link #getDumperSettings() dumper} and {@link
     * #getGeneralSettings() general} settings, in the given charset.
     *
     * @param stream  stream to save to
     * @param charset charset to use
     * @throws IOException an IO error
     */
    public void save(@NotNull OutputStream stream, Charset charset) throws IOException {
        stream.write(dump().getBytes(charset));
    }

    /**
     * Saves the contents to the given writer using the associated {@link #getDumperSettings() dumper} and {@link
     * #getGeneralSettings() general} settings.
     *
     * @param writer writer to save to
     * @throws IOException an IO error
     */
    public void save(@NotNull OutputStreamWriter writer) throws IOException {
        writer.write(dump());
    }

    /**
     * Dumps the contents to a string using the associated {@link #getDumperSettings() dumper} and {@link
     * #getGeneralSettings() general} settings.
     *
     * @return the dumped contents
     */
    public String dump() {
        return dump(dumperSettings);
    }

    /**
     * Dumps the contents to a string using the given dumper and associated {@link #getGeneralSettings() general}
     * settings.
     *
     * @param dumperSettings dumper settings to use
     * @return the dumped contents
     */
    public String dump(DumperSettings dumperSettings) {
        //Create the settings
        DumpSettings settings = dumperSettings.buildEngineSettings();
        //Output
        SerializedStream stream = new SerializedStream();
        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(generalSettings, settings);

        //Serializer
        Serializer serializer = new Serializer(settings, new Emitter(settings, stream));
        serializer.emitStreamStart();
        //Serialize
        serializer.serializeDocument(representer.represent(this));
        //Close
        serializer.emitStreamEnd();

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
     *     <li>Never change the key format! Such attempts will result in an {@link IllegalArgumentException}.</li>
     *     <li>If the default {@link GeneralSettings#getDefaultList() list}, {@link GeneralSettings#getDefaultMap() map}
     *     or {@link GeneralSettings#getDefaultSet() set} was changed, already existing instances will not be changed.
     *     Reload to take effect.</li>
     * </ul>
     *
     * @param generalSettings the new general settings
     */
    public void setGeneralSettings(@NotNull GeneralSettings generalSettings) {
        //Validate
        if (generalSettings.getKeyFormat() != this.generalSettings.getKeyFormat())
            throw new IllegalArgumentException("Cannot change key format! Recreate the file if needed to do so.");
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
     * Returns the defaults associated with the document, if any were given to <code>YamlDocument.create()</code>
     * method.
     * <p>
     * If there are no defaults, returns <code>null</code>.
     *
     * @return the associated defaults
     */
    @Nullable
    public YamlDocument getDefaults() {
        return defaults;
    }

    /**
     * Returns the general settings associated with the file.
     *
     * @return the associated general settings
     */
    @NotNull
    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    /**
     * Returns the dumper settings associated with the file.
     *
     * @return the associated dumper settings
     */
    @NotNull
    public DumperSettings getDumperSettings() {
        return dumperSettings;
    }

    /**
     * Returns the updater settings associated with the file.
     *
     * @return the associated updater settings
     */
    @NotNull
    public UpdaterSettings getUpdaterSettings() {
        return updaterSettings;
    }

    /**
     * Returns the loader settings associated with the file.
     *
     * @return the associated loader settings
     */
    @NotNull
    public LoaderSettings getLoaderSettings() {
        return loaderSettings;
    }

    /**
     * Returns the file associated with this document, if any were given to <code>YamlDocument.create()</code> method.
     * <p>
     * If there are no defaults, returns <code>null</code>.
     *
     * @return the associated file
     */
    @Nullable
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
     * Creates and initially loads YAML document from the given file using the given settings. The defaults will also be
     * loaded using the same settings.
     * <p>
     * The given settings will now be associated with the created document and loaded defaults. You will be able to use
     * {@link #save()} and {@link #reload()}.
     * <p>
     * If the given {@link File} does not exist, the document will be loaded from a <b>copy</b> of the defaults. If
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) enabled}, the file will automatically be created and
     * saved.
     *
     * @param document        document (does not need to {@link File#exists() exist})
     * @param defaults        defaults
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull File document, @NotNull InputStream defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        return new YamlDocument(document, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Creates and initially loads YAML document from the given file using default settings ({@link
     * GeneralSettings#DEFAULT}...). The defaults will also be loaded using the default settings.
     * <p>
     * The default settings will now be associated with the created document and loaded defaults. You will be able to
     * use {@link #save()} and {@link #reload()}.
     * <p>
     * If the given {@link File} does not exist, the document will be loaded from a <b>copy</b> of the defaults. If
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) enabled}, the file will automatically be created and
     * saved.
     *
     * @param document document (does not need to {@link File#exists() exist})
     * @param defaults defaults
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull File document, @NotNull InputStream defaults) throws IOException {
        return create(document, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Creates and initially loads YAML document from the given stream using the given settings. The defaults will also
     * be loaded using the same settings.
     * <p>
     * The given settings will now be associated with the created document and loaded defaults. As you are not providing
     * a {@link File}, you will need to provide data input/output each time you're reloading/saving.
     *
     * @param document        document
     * @param defaults        defaults
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull InputStream document, @NotNull InputStream defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        return new YamlDocument(document, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Creates and initially loads YAML document from the given file using default settings ({@link
     * GeneralSettings#DEFAULT}...). The defaults will also be loaded using the default settings.
     * <p>
     * The default settings will now be associated with the created document and loaded defaults. As you are not
     * providing a {@link File}, you will need to provide data input/output each time you're reloading/saving.
     *
     * @param document document
     * @param defaults defaults
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull InputStream document, @NotNull InputStream defaults) throws IOException {
        return create(document, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Creates and initially loads YAML document from the given file using the given settings.
     * <p>
     * The given settings will now be associated with the created document. You will be able to use {@link #save()} and
     * {@link #reload()}. As you are not providing any defaults, you will need to provide them each time you're
     * updating.
     * <p>
     * If the given {@link File} does not exist, the document will be empty. Unless {@link
     * LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will automatically be created and
     * saved.
     * <p>
     * <b>If you'd like to update, but don't want the defaults to be used in any other means,</b> you can disable use of
     * them via {@link GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right here.
     *
     * @param document        document (does not need to {@link File#exists() exist})
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull File document, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        return new YamlDocument(document, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Creates and initially loads YAML document from the given file using default settings ({@link
     * GeneralSettings#DEFAULT}...).
     * <p>
     * The default settings will now be associated with the created document. You will be able to use {@link #save()}
     * and {@link #reload()}. As you are not providing any defaults, you will need to provide them each time you're
     * updating.
     * <p>
     * If the given {@link File} does not exist, the document will be empty. Unless {@link
     * LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will automatically be created and
     * saved.
     * <p>
     * <b>If you'd like to update, but don't want the defaults to be used in any other means,</b> you can disable use of
     * them via {@link GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right here.
     *
     * @param document document (does not need to {@link File#exists() exist})
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull File document) throws IOException {
        return create(document, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Creates and initially loads YAML document from the given stream using the given settings.
     * <p>
     * The given settings will now be associated with the created document. As you are not providing a {@link File}, you
     * will need to provide data input/output each time you're reloading/saving. As you are not providing any defaults,
     * you will need to provide them each time you're updating.
     * <p>
     * <b>If you'd like to update, but don't want the defaults to be used in any other means,</b> you can disable use of
     * them via {@link GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right here.
     *
     * @param document        document
     * @param generalSettings general settings
     * @param loaderSettings  loader settings
     * @param dumperSettings  dumper settings
     * @param updaterSettings updater settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull InputStream document, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        return new YamlDocument(document, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    /**
     * Creates and initially loads YAML document from the given stream using default settings ({@link
     * GeneralSettings#DEFAULT}...).
     * <p>
     * The default settings will now be associated with the created document. As you are not providing a {@link File},
     * you will need to provide data input/output each time you're reloading/saving. As you are not providing any
     * defaults, you will need to provide them each time you're updating.
     * <p>
     * <b>If you'd like to update, but don't want the defaults to be used in any other means,</b> you can disable use of
     * them via {@link GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right here.
     *
     * @param document document
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull InputStream document) throws IOException {
        return create(document, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * An implementation of {@link StreamDataWriter} used to write to a string.
     */
    private static class SerializedStream extends StringWriter implements StreamDataWriter {
    }

}