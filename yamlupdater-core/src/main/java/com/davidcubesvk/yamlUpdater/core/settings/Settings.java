package com.davidcubesvk.yamlUpdater.core.settings;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class covering all the update settings. Looking at the API wiki is strongly advised. Mainly, if you are working
 * with relocations and section values.
 */
public class Settings {

    public enum Preset {
        YAML, JSON
    }

    public enum PathMode {
        STRING_BASED, OBJECT_BASED
    }

    public enum MappingAbsentReaction {
        RETURN_DEFAULT, THROW_EXCEPTION
    }

    /**
     * The default path (key) separator.
     */
    public static final char DEFAULT_SEPARATOR = '.';
    public static final PathMode DEFAULT_PATH_MODE = PathMode.STRING_BASED;
    /**
     * The string form of the default separator.
     */
    public static final String DEFAULT_STRING_SEPARATOR = String.valueOf(DEFAULT_SEPARATOR);
    /**
     * The escaped form (for regex compatibility at all time) of the default separator.
     */
    public static final String DEFAULT_ESCAPED_SEPARATOR = java.util.regex.Pattern.quote(DEFAULT_STRING_SEPARATOR);

    private final Object defaultObject;
    private final Section defaultSection;
    private final Number defaultNumber;
    private final String defaultString;
    private final char defaultChar;
    private final boolean defaultBoolean;
    private final LoadSettings loadSettings;
    private final DumpSettings dumpSettings;
    private final PathMode pathMode;
    private final char separator;
    private final MappingAbsentReaction mappingAbsentReaction;
    private final Serializator serializator;

    /**
     * Initializes the settings with data (to be more specific, disk folder and class loader) from the given main class.
     *
     * @param updater the main updater class
     */
    public Settings(LoadSettings loadSettings, DumpSettings dumpSettings, Serializator serializator, PathMode pathMode, char separator, MappingAbsentReaction mappingAbsentReaction, Map<MappingType, Object> absentDefaults) {
        this.loadSettings = loadSettings;
        this.dumpSettings = dumpSettings;
        this.pathMode = pathMode;
        this.separator = separator;
        this.serializator = serializator;
        this.mappingAbsentReaction = mappingAbsentReaction;
        this.absentDefaults = absentDefaults;
    }

    public LoadSettings getLoadSettings() {
        return loadSettings;
    }

    public DumpSettings getDumpSettings() {
        return dumpSettings;
    }

    public PathMode getPathMode() {
        return pathMode;
    }

    public char getSeparator() {
        return separator;
    }

    public Serializator getSerializator() {
        return serializator;
    }

    public Object getDefaultObject() {
        return defaultObject;
    }

    public Section getDefaultSection() {
        return defaultSection;
    }

    public String getDefaultString() {
        return defaultString;
    }

    public char getDefaultChar() {
        return defaultChar;
    }

    public Number getDefaultNumber() {
        return defaultNumber;
    }

    public boolean getDefaultBoolean() {
        return defaultBoolean;
    }

    public <T> List<T> getDefaultList() {
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LoadSettingsBuilder loadSettings = LoadSettings.builder();
        private final DumpSettingsBuilder dumpSettings = DumpSettings.builder();
        private PathMode pathMode = DEFAULT_PATH_MODE;
        private char separator = DEFAULT_SEPARATOR;
        private Serializator serializator = null;

        private Builder() {}

        public Settings build() {
            return new Settings(loadSettings.build(), dumpSettings.build(), serializator, pathMode, separator);
        }

        public void setPathMode(PathMode pathMode) {
            this.pathMode = pathMode;
        }

        public void setSeparator(char separator) {
            this.separator = separator;
        }

        public void setSerializator(Serializator serializator) {
            this.serializator = serializator;
        }

        public void applyPreset(Preset preset) {
            this.dumpSettings.setDefaultFlowStyle(preset == Preset.YAML ? FlowStyle.BLOCK : FlowStyle.FLOW);
            this.dumpSettings.setDefaultScalarStyle(preset == Preset.YAML ? ScalarStyle.PLAIN : ScalarStyle.DOUBLE_QUOTED);
            this.dumpSettings.setMultiLineFlow(true);
        }

        public void setIndents(int indents) {
            this.dumpSettings.setIndent(indents);
        }

        public LoadSettingsBuilder getLoadSettings() {
            return loadSettings;
        }

        public DumpSettingsBuilder getDumpSettings() {
            return dumpSettings;
        }
    }


}