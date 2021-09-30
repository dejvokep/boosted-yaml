package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.block.*;
import com.davidcubesvk.yamlUpdater.core.reader.AccessibleConstructor;
import com.davidcubesvk.yamlUpdater.core.reader.FullRepresenter;
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
    private final UpdaterSettings updaterSettings;
    private final YamlFile defaults;

    /**
     * Initializes the file from the given header, mappings and key separator.
     *
     * @param header   the header of the file, or <code>null</code> if not any
     * @param mappings mappings inside the file
     * @param generalSettings settings this file will be loaded with, used to get the key separators
     */
    public YamlFile(BufferedInputStream inputStream, YamlFile defaults, GeneralSettings generalSettings, LoaderSettings loaderSettings, DumperSettings dumperSettings, UpdaterSettings updaterSettings) {
        //Call superclass
        super(generalSettings.getDefaultMap());
        //Set
        this.generalSettings = generalSettings;
        this.loaderSettings = loaderSettings;
        this.dumperSettings = dumperSettings;
        this.updaterSettings = updaterSettings;
        this.file = null;
        this.defaults = defaults;

        //Load
        load(inputStream);
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

        //Load
        load();
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
        AccessibleConstructor constructor = new AccessibleConstructor(settings);
        //Create the composer
        Composer composer = new Composer(settings, new ParserImpl(settings, new StreamReader(settings, new YamlUnicodeReader(inputStream))));
        //Node
        Node node = composer.next();
        //Construct
        constructor.constructSingleDocument(Optional.of(node));

        //Init
        init(this, constructor, null, (MappingNode) constructor.getConstructed().get(composer.next()));
    }

    public YamlFile getDefaults() {
        return defaults;
    }

    public void update() {
    }

    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public void setDumperSettings(DumperSettings dumperSettings) {
        this.dumperSettings = dumperSettings;
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
        try {
            //Write
            writer.write(dump());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public String dump() {
        //Create the settings
        DumpSettings settings = dumperSettings.getSettings();
        //Output
        SerializedStream stream = new SerializedStream();
        //Create the representer
        BaseRepresenter representer = new FullRepresenter(settings);

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

    private class SerializedStream extends StringWriter implements StreamDataWriter {}

}