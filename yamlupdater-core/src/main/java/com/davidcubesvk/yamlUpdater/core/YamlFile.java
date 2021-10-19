package com.davidcubesvk.yamlUpdater.core;

import com.davidcubesvk.yamlUpdater.core.block.*;
import com.davidcubesvk.yamlUpdater.core.updater.Updater;
import com.davidcubesvk.yamlUpdater.core.engine.LibConstructor;
import com.davidcubesvk.yamlUpdater.core.engine.LibRepresenter;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
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
import java.util.*;

/**
 * Represents a loaded YAML file.
 */
public class YamlFile extends Section {

    private final File file;
    private final GeneralSettings generalSettings;
    private final LoaderSettings loaderSettings;
    private DumperSettings dumperSettings;
    private UpdaterSettings updaterSettings;
    private final YamlFile defaults;

    /**
     * Initializes the file from the given header, mappings and key separator.
     *
     * @param header          the header of the file, or <code>null</code> if not any
     * @param mappings        mappings inside the file
     * @param generalSettings settings this file will be loaded with, used to get the key separators
     */
    public YamlFile(InputStream userFile, YamlFile defaultFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) {
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
        //Update if enabled
        if (defaultFile != null && loaderSettings.isAutoUpdate())
            Updater.update(this, defaultFile, updaterSettings, generalSettings);
    }

    /**
     * Initializes the file from the given header, mappings and key separator.
     *
     * @param header          the header of the file, or <code>null</code> if not any
     * @param mappings        mappings inside the file
     * @param generalSettings settings this file will be loaded with, used to get the key separators
     */
    public YamlFile(InputStream userFile, InputStream defaultFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) {
        this(userFile, new YamlFile(new BufferedInputStream(defaultFile), generalSettings, loaderSettings), generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public YamlFile(File userFile, InputStream defaultFile, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws FileNotFoundException {
        this(userFile, new YamlFile(new BufferedInputStream(defaultFile), generalSettings, loaderSettings), generalSettings, loaderSettings, dumperSettings, updaterSettings);
    }

    public YamlFile(File file, YamlFile defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) throws FileNotFoundException {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = file;
        this.defaults = defaults;

        //If the file exists
        if (file.exists()) {
            //Load
            load();
            //Update if enabled
            if (defaults != null && loaderSettings.isAutoUpdate())
                Updater.update(this, defaults, updaterSettings, generalSettings);
            return;
        }

        //If enabled
        if (loaderSettings.isCreateFileIfAbsent()) {
            //Save
            try (FileWriter writer = new FileWriter(file)) {
                //Write
                writer.write(defaults.dump(dumperSettings));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //Load
            load();
        } else {
            //Load
            load(new BufferedInputStream(new ByteArrayInputStream(defaults.dump(dumperSettings).getBytes())));
        }
    }

    public YamlFile(InputStream inputStream, GeneralSettings generalSettings, LoaderSettings loaderSettings) {
        this(inputStream, (YamlFile) null, generalSettings, loaderSettings, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    public boolean load() throws FileNotFoundException {
        //If not present
        if (file == null)
            return false;
        //Load
        load(file);
        return true;
    }

    public void load(File file) throws FileNotFoundException {
        load(new BufferedInputStream(new FileInputStream(file)));
    }

    public void load(BufferedInputStream inputStream) {
        //Create the settings
        LoadSettings settings = loaderSettings.getSettings(generalSettings);
        //Create the constructor
        LibConstructor constructor = new LibConstructor(settings, generalSettings.getSerializer());
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
    }

    public YamlFile getDefaults() {
        return defaults;
    }

    public boolean update() {
        return update(updaterSettings);
    }

    public boolean update(UpdaterSettings updaterSettings) {
        //Check
        Objects.requireNonNull(updaterSettings);
        //If there are no defaults
        if (defaults == null)
            return false;

        //Update
        Updater.update(this, defaults, updaterSettings);
        return true;
    }

    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public DumperSettings getDumperSettings() {
        return dumperSettings;
    }

    public UpdaterSettings getUpdaterSettings() {
        return updaterSettings;
    }

    public LoaderSettings getLoaderSettings() {
        return loaderSettings;
    }

    public boolean save() {
        //If not present
        if (file == null)
            return false;

        //Save
        return save(file);
    }

    public boolean save(File file) {
        //Check
        Objects.requireNonNull(file);

        try (FileWriter fileWriter = new FileWriter(file)) {
            //Save
            return save(fileWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean save(OutputStreamWriter writer) {
        //Check
        Objects.requireNonNull(file);

        try {
            //Write
            writer.write(dump());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public String dump(DumperSettings dumperSettings) {
        //Create the settings
        DumpSettings settings = dumperSettings.getSettings();
        //Output
        SerializedStream stream = new SerializedStream();
        //Create the representer
        BaseRepresenter representer = new LibRepresenter(generalSettings, settings);

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

    public String dump() {
        return dump(dumperSettings);
    }

    private class SerializedStream extends StringWriter implements StreamDataWriter {
    }

}