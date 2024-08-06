/*
 * Copyright 2024 https://dejvokep.dev/
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
import dev.dejvokep.boostedyaml.settings.Settings;
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
import java.util.Collections;
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
     * Creates and loads a YAML document from the given stream and loads the defaults (another YAML document, later
     * accessible via {@link #getDefaults()}) from the <code>defaults</code> stream, if provided.
     * <p>
     * The provided settings will be stored and used by this document and the defaults. You can overwrite them using
     * {@link #setSettings(Settings...)}). If settings of any type are not provided, their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}) are used.
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     * <p>
     * <b>Please note that methods without an I/O parameter will not be usable.</b> Refer to the method documentation
     * for more information.
     *
     * @param document document
     * @param defaults defaults
     * @param settings settings
     * @throws IOException an IO error
     */
    protected YamlDocument(@NotNull InputStream document, @Nullable InputStream defaults, @NotNull Settings... settings) throws IOException {
        //Call superclass
        super(Collections.emptyMap());

        //Set
        setSettingsInternal(settings);
        setValue(generalSettings.getDefaultMap());
        this.file = null;
        this.defaults = defaults == null ? null : new YamlDocument(defaults, null, settings);

        //Load
        reload(document);
    }

    /**
     * Creates and loads a YAML document from the given file and loads the defaults (another YAML document, later
     * accessible via {@link #getDefaults()}) from the <code>defaults</code> stream, if provided.
     * <p>
     * The provided settings will be stored and used by this document and the defaults. You can overwrite them using
     * {@link #setSettings(Settings...)}). If settings of any type are not provided, their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}) are used.
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     * <p>
     * If the given {@link File} does not exist, the document will be loaded from a <b>copy</b> of the defaults. If
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) enabled}, the file will automatically be created.
     *
     * @param document document (does not need to {@link File#exists() exist})
     * @param defaults defaults
     * @param settings settings
     * @throws IOException an IO error
     */
    protected YamlDocument(@NotNull File document, @Nullable InputStream defaults, @NotNull Settings... settings) throws IOException {
        //Call superclass
        super(Collections.emptyMap());

        //Set
        setSettingsInternal(settings);
        setValue(generalSettings.getDefaultMap());
        this.file = document;
        this.defaults = defaults == null ? null : new YamlDocument(defaults, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
        //Load
        reload();
    }

    /**
     * Sets the given settings internally, into their respective fields. If any of the setting fields in this document
     * instance are <code>null</code>, this method automatically uses their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}).
     * <p>
     * <b>If you are changing {@link GeneralSettings}:</b>
     * <ul>
     *     <li>It is required that the {@link GeneralSettings#getKeyFormat()} is the same as the one defined by
     *     {@link #getGeneralSettings()} (settings currently in use by the document). Such attempt will result in an
     *     {@link IllegalArgumentException}; to update the key format, recreate the document.</li>
     *     <li>Changing the default {@link GeneralSettings#getDefaultList() list},
     *     {@link GeneralSettings#getDefaultMap() map} or {@link GeneralSettings#getDefaultSet() set} suppliers will
     *     only affect collections created from now on.</li>
     * </ul>
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} or {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same class, the last one will take effect.
     *
     * @param settings the settings to set
     */
    private void setSettingsInternal(@NotNull Settings... settings) {
        for (Settings obj : settings) {
            if (obj instanceof GeneralSettings) {
                if (generalSettings != null && generalSettings.getKeyFormat() != ((GeneralSettings) obj).getKeyFormat())
                    throw new IllegalArgumentException("Cannot change the key format! Recreate the file if needed to do so.");
                this.generalSettings = (GeneralSettings) obj;
            } else if (obj instanceof LoaderSettings) {
                this.loaderSettings = (LoaderSettings) obj;
            } else if (obj instanceof DumperSettings) {
                this.dumperSettings = (DumperSettings) obj;
            } else if (obj instanceof UpdaterSettings) {
                this.updaterSettings = (UpdaterSettings) obj;
            } else {
                throw new IllegalArgumentException("Unknown settings object!");
            }
        }

        this.generalSettings = generalSettings == null ? GeneralSettings.DEFAULT : generalSettings;
        this.loaderSettings = loaderSettings == null ? LoaderSettings.DEFAULT : loaderSettings;
        this.dumperSettings = dumperSettings == null ? DumperSettings.DEFAULT : dumperSettings;
        this.updaterSettings = updaterSettings == null ? UpdaterSettings.DEFAULT : updaterSettings;
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
     * Reloads the contents from the {@link #getFile() associated file} using the associated
     * {@link #getLoaderSettings() loader} and {@link #getGeneralSettings() general} settings.
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
     * Reloads the contents from the given file using the associated {@link #getLoaderSettings() loader} and
     * {@link #getGeneralSettings() general} settings.
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
     * Reloads the contents from the given stream using the associated {@link #getLoaderSettings() loader} and
     * {@link #getGeneralSettings() general} settings.
     * <p>
     * If there is any {@link #getFile() associated file} and it does not exist, unless
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will automatically be created
     * and saved.
     *
     * @param inputStream file to reload from
     * @throws IOException an IO error
     */
    public void reload(@NotNull InputStream inputStream) throws IOException {
        reload(inputStream, loaderSettings);
    }

    /**
     * Reloads the contents from the given stream using the given loader and associated
     * {@link #getGeneralSettings() general} settings.
     * <p>
     * If there is any {@link #getFile() associated file} and it does not exist, unless
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) disabled}, the file will automatically be created
     * and saved.
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
     * Updates the contents against the {@link #getDefaults() associated defaults} using the associated
     * {@link #getUpdaterSettings() updater} and {@link #getGeneralSettings() general} settings.
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
     * don't want the defaults to be used in any other means,</b> you can disable use of them via
     * {@link GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right to the
     * <code>YamlDocument.create()</code> method.
     *
     * @param defaults defaults to load and update against
     * @throws IOException an IO error
     */
    public void update(@NotNull InputStream defaults) throws IOException {
        update(defaults, updaterSettings);
    }

    /**
     * Updates the contents against the given defaults using the given updater and associated
     * {@link #getGeneralSettings() general} settings.
     * <p>
     * Please note that this involves loading a YAML document from the given stream. <b>If you'd like to update, but
     * don't want the defaults to be used in any other means,</b> you can disable use of them via
     * {@link GeneralSettings.Builder#setUseDefaults(boolean)} and provide the defaults right to the
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
     * Saves the contents to the {@link #getFile() associated file} using the associated
     * {@link #getDumperSettings() dumper} and {@link #getGeneralSettings() general} settings, in
     * {@link StandardCharsets#UTF_8 UTF-8} charset.
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
     * Saves the contents to the given file using the associated {@link #getDumperSettings() dumper} and
     * {@link #getGeneralSettings() general} settings, in {@link StandardCharsets#UTF_8 UTF-8} charset.
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
     * Saves the contents to the given stream using the associated {@link #getDumperSettings() dumper} and
     * {@link #getGeneralSettings() general} settings, in the given charset.
     *
     * @param stream  stream to save to
     * @param charset charset to use
     * @throws IOException an IO error
     */
    public void save(@NotNull OutputStream stream, Charset charset) throws IOException {
        stream.write(dump().getBytes(charset));
    }

    /**
     * Saves the contents to the given writer using the associated {@link #getDumperSettings() dumper} and
     * {@link #getGeneralSettings() general} settings.
     *
     * @param writer writer to save to
     * @throws IOException an IO error
     */
    public void save(@NotNull OutputStreamWriter writer) throws IOException {
        writer.write(dump());
    }

    /**
     * Dumps the contents to a string using the associated {@link #getDumperSettings() dumper} and
     * {@link #getGeneralSettings() general} settings.
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
    public String dump(@NotNull DumperSettings dumperSettings) {
        //Create the settings
        DumpSettings settings = dumperSettings.buildEngineSettings();
        //Output
        SerializedStream stream = new SerializedStream();
        //Create the representer
        BaseRepresenter representer = new ExtendedRepresenter(this.getClass(), generalSettings, dumperSettings, settings);

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
     * Sets new settings to be used by this document, overwriting the previous settings associated with this document.
     * <p>
     * <b>If you are changing {@link GeneralSettings}:</b>
     * <ul>
     *     <li>It is required that the {@link GeneralSettings#getKeyFormat()} is the same as the one defined by
     *     {@link #getGeneralSettings()} (settings currently in use by the document). Such attempt will result in an
     *     {@link IllegalArgumentException}; to update the key format, recreate the document.</li>
     *     <li>Changing the default {@link GeneralSettings#getDefaultList() list},
     *     {@link GeneralSettings#getDefaultMap() map} or {@link GeneralSettings#getDefaultSet() set} suppliers will
     *     only affect collections created from now on.</li>
     * </ul>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     *
     * @param settings the new settings
     */
    public void setSettings(@NotNull Settings... settings) {
        setSettingsInternal(settings);
    }

    /**
     * Associates new loader settings.
     *
     * @param loaderSettings the new loader settings
     * @deprecated replaced by {@link #setSettings(Settings...)} and subject for removal
     */
    @Deprecated
    public void setLoaderSettings(@NotNull LoaderSettings loaderSettings) {
        this.loaderSettings = loaderSettings;
    }

    /**
     * Associates new dumper settings.
     *
     * @param dumperSettings the new dumper settings
     * @deprecated replaced by {@link #setSettings(Settings...)} and subject for removal
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
     * @deprecated replaced by {@link #setSettings(Settings...)} and subject for removal
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
     * @deprecated replaced by {@link #setSettings(Settings...)} and subject for removal
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
     * Creates and loads a YAML document from the given file and loads the defaults (another YAML document, later
     * accessible via {@link #getDefaults()}) from the <code>defaults</code> stream.
     * <p>
     * The provided settings will be stored and used by this document and the defaults. You can overwrite them using
     * {@link #setSettings(Settings...)}). If settings of any type are not provided, their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}) are used.
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     * <p>
     * If the given {@link File} does not exist, the document will be loaded from a <b>copy</b> of the defaults. If
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) enabled}, the file will automatically be created.
     *
     * @param document document (does not need to {@link File#exists() exist})
     * @param defaults defaults
     * @param settings settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull File document, @NotNull InputStream defaults, @NotNull Settings... settings) throws IOException {
        return new YamlDocument(document, defaults, settings);
    }

    /**
     * Creates and loads a YAML document from the given stream and loads the defaults (another YAML document, later
     * accessible via {@link #getDefaults()}) from the <code>defaults</code> stream.
     * <p>
     * The provided settings will be stored and used by this document and the defaults. You can overwrite them using
     * {@link #setSettings(Settings...)}). If settings of any type are not provided, their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}) are used.
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     * <p>
     * <b>Please note that methods without an I/O parameter will not be usable.</b> Refer to the method documentation
     * for more information.
     *
     * @param document document
     * @param defaults defaults
     * @param settings settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull InputStream document, @NotNull InputStream defaults, @NotNull Settings... settings) throws IOException {
        return new YamlDocument(document, defaults, settings);
    }

    /**
     * Creates and loads a YAML document from the given file. The returned document will not have any defaults.
     * <p>
     * The provided settings will be stored and used by this document and the defaults. You can overwrite them using
     * {@link #setSettings(Settings...)}). If settings of any type are not provided, their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}) are used.
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     * <p>
     * If the given {@link File} does not exist, the document will be loaded from a <b>copy</b> of the defaults. If
     * {@link LoaderSettings.Builder#setCreateFileIfAbsent(boolean) enabled}, the file will automatically be created.
     *
     * @param document document (does not need to {@link File#exists() exist})
     * @param settings settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull File document, @NotNull Settings... settings) throws IOException {
        return new YamlDocument(document, null, settings);
    }

    /**
     * Creates and loads a YAML document from the given stream. The returned document will not have any defaults.
     * <p>
     * The provided settings will be stored and used by this document and the defaults. You can overwrite them using
     * {@link #setSettings(Settings...)}). If settings of any type are not provided, their defaults (e.g.
     * {@link GeneralSettings#DEFAULT}) are used.
     * <p>
     * If any of the given objects is not an instance of {@link GeneralSettings}, {@link LoaderSettings},
     * {@link DumperSettings} nor {@link UpdaterSettings}, an {@link IllegalArgumentException} will be thrown. If there
     * are multiple instances of the same settings type, the last one will take effect.
     * <p>
     * <b>Please note that methods without an I/O parameter will not be usable.</b> Refer to the method documentation
     * for more information.
     *
     * @param document document
     * @param settings settings
     * @return the created and loaded document
     * @throws IOException an IO error
     */
    public static YamlDocument create(@NotNull InputStream document, @NotNull Settings... settings) throws IOException {
        return new YamlDocument(document, null, settings);
    }

    /**
     * An implementation of {@link StreamDataWriter} used to write to a string.
     */
    private static class SerializedStream extends StringWriter implements StreamDataWriter {
    }

}