package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.updater.Updater;
import com.davidcubesvk.yamlUpdater.core.engine.ExtendedConstructor;
import com.davidcubesvk.yamlUpdater.core.engine.ExtendedRepresenter;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
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
    private YamlFile defaults;
    // Settings
    private GeneralSettings generalSettings;
    private LoaderSettings loaderSettings;
    private DumperSettings dumperSettings;
    private UpdaterSettings updaterSettings;

    private YamlFile(@NotNull InputStream userFile, @Nullable YamlFile defaultFile, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = null;
        this.defaults = defaultFile;

        //Load
        load(new BufferedInputStream(userFile));
    }

    private YamlFile(@NotNull File userFile, @Nullable YamlFile defaults, @NotNull GeneralSettings generalSettings, @NotNull LoaderSettings loaderSettings, @NotNull DumperSettings dumperSettings, @NotNull UpdaterSettings updaterSettings) throws IOException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = userFile;
        this.defaults = defaults;
        //Load
        load();
    }

    private YamlFile(File userFile, InputStream defaultFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        this(userFile, new YamlFile(new BufferedInputStream(defaultFile), generalSettings, loaderSettings), generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public YamlFile(InputStream inputStream, GeneralSettings generalSettings, LoaderSettings loaderSettings) throws IOException {
        this(inputStream, null, generalSettings, loaderSettings, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    /**
     * Loads the contents from the associated user file ({@link #getFile}) using the associated settings ({@link
     * #getLoaderSettings()} and {@link #getGeneralSettings()}).
     * <p>
     * If there is no associated user file, returns <code>false</code> (use other loading methods). Returns
     * <code>true</code> otherwise.
     *
     * @return if there is any associated (user) file
     * @throws IOException if an IO error occurred
     */
    public boolean load() throws IOException {
        //If not present
        if (file == null)
            return false;
        //Load
        load(file);
        return true;
    }

    /**
     * Loads the contents from the given file using the associated settings ({@link #getLoaderSettings()} and {@link
     * #getGeneralSettings()}).
     * <p>
     * If the given file does not <b>physically</b> exist, loads the defaults instead.
     *
     * @param file file to load the contents from
     * @throws IOException if an IO error occurred
     */
    public void load(@NotNull File file) throws IOException {
        //Clear
        clear();
        //If exists
        if (file.exists()) {
            //Load from the file
            load(new BufferedInputStream(new FileInputStream(file)));
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
        load(new BufferedInputStream(new ByteArrayInputStream(defaults.dump().getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Loads the contents from the given stream using the associated settings ({@link #getLoaderSettings()} and {@link
     * #getGeneralSettings()}).
     *
     * @param inputStream stream to load the contents from
     * @throws IOException if an IO error occurred
     */
    public void load(@NotNull BufferedInputStream inputStream) throws IOException {
        load(inputStream, loaderSettings, generalSettings);
    }

    /**
     * Loads the contents from the given stream and using the given settings.
     *
     * @param inputStream     stream to load the contents from
     * @param loaderSettings  the loader settings to use for this load
     * @param generalSettings the general settings to use for this load
     * @throws IOException if an IO error occurred
     */
    public void load(@NotNull BufferedInputStream inputStream, @NotNull LoaderSettings loaderSettings, @NotNull GeneralSettings generalSettings) throws IOException {
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

    /**
     * Returns the defaults associated with the file.
     *
     * @return the defaults associated
     */
    public YamlFile getDefaults() {
        return defaults;
    }

    public boolean update() throws IOException {
        return update(updaterSettings);
    }

    public boolean update(UpdaterSettings updaterSettings) throws IOException {
        //Check
        Objects.requireNonNull(updaterSettings);
        //If there are no defaults
        if (defaults == null)
            return false;

        //Update
        Updater.update(this, defaults, updaterSettings, generalSettings);
        return true;
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

    /**
     * Saves the contents into the associated user file {@link #getFile()} using the associated settings ({@link
     * #getDumperSettings()} and {@link #getGeneralSettings()}).
     * <p>
     * If there is no associated user file, returns <code>false</code> (use other saving methods). Returns
     * <code>true</code> otherwise.
     *
     * @return if there is any associated (user) file
     * @throws IOException if an IO error occurred
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
     *
     * @throws IOException if an IO error occurred
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
     *
     * @throws IOException if an IO error occurred
     */
    public void save(@NotNull OutputStreamWriter writer) throws IOException {
        //Write
        writer.write(dump());
    }

    /**
     * Dumps the contents to a string using the associated settings ({@link #getDumperSettings()} and {@link
     * #getGeneralSettings()}).
     *
     * @return the dumped contents
     */
    public String dump() {
        return dump(dumperSettings, generalSettings);
    }

    /**
     * Dumps the contents to a string using the given settings.
     *
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

    /**
     * Associates the new defaults. To clear defaults, pass <code>null</code>.
     * <p>
     * <b>WARNING!</b> The given defaults should:
     * <ul>
     *     <li>never have their own defaults specified,</li>
     *     <li>have the same key mode, separator and serializer ({@link GeneralSettings}).</li>
     * </ul>
     * Alternatively, to deal with all of this, use {@link #setDefaults(InputStream)} instead.
     *
     * @param defaults the new defaults
     * @see #setDefaults(InputStream)
     */
    public void setDefaults(@Nullable YamlFile defaults) {
        this.defaults = defaults;
    }

    /**
     * Loads (using the associated {@link #getGeneralSettings()} and {@link #getLoaderSettings()} settings) and
     * associates the new defaults.
     * <p>
     * To clear defaults, pass <code>null</code>.
     *
     * @param defaults the new defaults
     */
    public void setDefaults(@Nullable InputStream defaults) throws IOException {
        this.defaults = defaults == null ? null : new YamlFile(defaults, generalSettings, loaderSettings);
    }

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
     *     <li>If key mode was changed, make sure to reload immediately.</li>
     *     <li>If the default {@link GeneralSettings#getDefaultList() list}, {@link GeneralSettings#getDefaultMap() map}
     *     or {@link GeneralSettings#getDefaultSet() set} was changed, already existing instances will not be changed.
     *     Reload to take effect.</li>
     * </ul>
     *
     * @param generalSettings the new general settings
     */
    public void setGeneralSettings(@NotNull GeneralSettings generalSettings) {
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

    public static YamlFile create(File userFile, InputStream defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public static YamlFile create(File userFile, InputStream defaults) throws IOException {
        return create(userFile, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    public static YamlFile create(InputStream userFile, InputStream defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, create(new BufferedInputStream(defaults), generalSettings, loaderSettings, dumperSettings, updaterSettings), generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public static YamlFile create(InputStream userFile, InputStream defaults) throws IOException {
        return create(userFile, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    public static YamlFile create(File userFile, YamlFile defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public static YamlFile create(File userFile, YamlFile defaults) throws IOException {
        return create(userFile, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    public static YamlFile create(File userFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return create(userFile, (YamlFile) null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public static YamlFile create(File userFile) throws IOException {
        return create(userFile, (YamlFile) null, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    public static YamlFile create(InputStream userFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws IOException {
        return new YamlFile(userFile, null, generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public static YamlFile create(InputStream userFile) throws IOException {
        return create(userFile, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    /**
     * Class used to write to a string.
     */
    private static class SerializedStream extends StringWriter implements StreamDataWriter {
    }

}