package com.davidcubesvk.yamlUpdater.core.files;

import com.davidcubesvk.yamlUpdater.core.block.*;
import com.davidcubesvk.yamlUpdater.core.reader.Constructor;
import com.davidcubesvk.yamlUpdater.core.settings.Settings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Represents a loaded YAML file.
 */
public class YamlFile extends Section {

    private final File file;
    private final Settings settings;
    private final YamlFile defaults;

    /**
     * Initializes the file from the given header, mappings and key separator.
     *
     * @param header   the header of the file, or <code>null</code> if not any
     * @param mappings mappings inside the file
     * @param settings settings this file will be loaded with, used to get the key separators
     */
    public YamlFile(InputStream inputStream, Settings settings, YamlFile defaults) {
        //Call superclass
        super();
        //Set
        this.settings = settings;
        this.file = null;
        this.defaults = defaults;

        //Load
        load(inputStream);
    }

    public YamlFile(File file, Settings settings, YamlFile defaults) throws FileNotFoundException {
        //Call superclass
        super();
        //Set
        this.settings = settings;
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
        load(new FileInputStream(file));
    }
    public void load(InputStream inputStream) {
        //Create the constructor
        Constructor constructor = new Constructor(settings.getLoadSettings());
        //Create the composer
        Composer composer = new Composer(settings.getLoadSettings(), new ParserImpl(settings.getLoadSettings(), new StreamReader(settings.getLoadSettings(), new YamlUnicodeReader(inputStream))));
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
        //If more documents
        if (documents.si)
    }

    public Settings getSettings() {
        return settings;
    }

    public void save() {}

}