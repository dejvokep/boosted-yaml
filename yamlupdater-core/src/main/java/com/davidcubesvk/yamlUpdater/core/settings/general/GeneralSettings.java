package com.davidcubesvk.yamlUpdater.core.settings.general;

import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.utils.serialization.Serializer;
import com.davidcubesvk.yamlUpdater.core.utils.serialization.YamlSerializer;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.ListSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.SetSupplier;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A class covering all the update settings. Looking at the API wiki is strongly advised. Mainly, if you are working
 * with relocations and section values.
 */
public class GeneralSettings {

    public enum PathMode {
        STRING_BASED, OBJECT_BASED
    }

    /**
     * The default path (key) separator.
     */
    public static final char DEFAULT_SEPARATOR = '.';
    public static final String DEFAULT_ESCAPED_SEPARATOR = Pattern.quote(String.valueOf(DEFAULT_SEPARATOR));
    public static final PathMode DEFAULT_PATH_MODE = PathMode.STRING_BASED;
    public static final YamlSerializer DEFAULT_SERIALIZER = new Serializer("==");
    public static final Object DEFAULT_OBJECT = null;
    public static final Section DEFAULT_SECTION = null;
    public static final Number DEFAULT_NUMBER = 0;
    public static final String DEFAULT_STRING = null;
    public static final Character DEFAULT_CHAR = null;
    public static final Boolean DEFAULT_BOOLEAN = null;
    public static final ListSupplier DEFAULT_LIST = ArrayList::new;
    public static final SetSupplier DEFAULT_SET = LinkedHashSet::new;
    public static final MapSupplier DEFAULT_MAP = LinkedHashMap::new;

    private final PathMode pathMode;
    private final char separator;
    private final String escapedSeparator;
    private final YamlSerializer serializer;
    private final Object defaultObject;
    private final Section defaultSection;
    private final Number defaultNumber;
    private final String defaultString;
    private final Character defaultChar;
    private final Boolean defaultBoolean;
    private final ListSupplier defaultList;
    private final SetSupplier defaultSet;
    private final MapSupplier defaultMap;

    /**
     * Initializes the settings with data (to be more specific, disk folder and class loader) from the given main class.
     *
     */
    public GeneralSettings(Builder builder) {
        this.pathMode = builder.pathMode;
        this.separator = builder.separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
        this.serializer = builder.serializer;
        this.defaultObject = builder.defaultObject;
        this.defaultSection = builder.defaultSection;
        this.defaultNumber = builder.defaultNumber;
        this.defaultString = builder.defaultString;
        this.defaultChar = builder.defaultChar;
        this.defaultBoolean = builder.defaultBoolean;
        this.defaultList = builder.defaultList;
        this.defaultSet = builder.defaultSet;
        this.defaultMap = builder.defaultMap;
    }

    public PathMode getPathMode() {
        return pathMode;
    }

    public char getSeparator() {
        return separator;
    }

    public String getEscapedSeparator() {
        return escapedSeparator;
    }

    public YamlSerializer getSerializer() {
        return serializer;
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

    public <T> List<T> getDefaultList(int size) {
        return defaultList.supply(size);
    }
    public <T> List<T> getDefaultList() {
        return getDefaultList(0);
    }

    public <T> Set<T> getDefaultSet(int size) {
        return defaultSet.supply(size);
    }
    public <T> Set<T> getDefaultSet() {
        return getDefaultSet(0);
    }

    public <K, V> Map<K, V> getDefaultMap(int size) {
        return defaultMap.supply(size);
    }
    public <K, V> Map<K, V> getDefaultMap() {
        return getDefaultMap(0);
    }

    public MapSupplier getDefaultMapSupplier() {
        return defaultMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PathMode pathMode = DEFAULT_PATH_MODE;
        private char separator = DEFAULT_SEPARATOR;
        private YamlSerializer serializer = DEFAULT_SERIALIZER;
        private Object defaultObject = DEFAULT_OBJECT;
        private Section defaultSection = DEFAULT_SECTION;
        private Number defaultNumber = DEFAULT_NUMBER;
        private String defaultString = DEFAULT_STRING;
        private Character defaultChar = DEFAULT_CHAR;
        private Boolean defaultBoolean = DEFAULT_BOOLEAN;
        private ListSupplier defaultList = DEFAULT_LIST;
        private SetSupplier defaultSet = DEFAULT_SET;
        private MapSupplier defaultMap = DEFAULT_MAP;

        private Builder() {}

        public GeneralSettings build() {
            return new GeneralSettings(this);
        }

        public Builder setPathMode(PathMode pathMode) {
            this.pathMode = pathMode;
            return this;
        }

        public Builder setSeparator(char separator) {
            this.separator = separator;
            return this;
        }

        public Builder setSerializer(YamlSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder setDefaultObject(Object defaultObject) {
            this.defaultObject = defaultObject;
            return this;
        }

        public Builder setDefaultSection(Section defaultSection) {
            this.defaultSection = defaultSection;
            return this;
        }

        public Builder setDefaultNumber(Number defaultNumber) {
            this.defaultNumber = defaultNumber;
            return this;
        }

        public Builder setDefaultString(String defaultString) {
            this.defaultString = defaultString;
            return this;
        }

        public Builder setDefaultChar(char defaultChar) {
            this.defaultChar = defaultChar;
            return this;
        }

        public Builder setDefaultBoolean(boolean defaultBoolean) {
            this.defaultBoolean = defaultBoolean;
            return this;
        }

        public Builder setDefaultList(ListSupplier defaultList) {
            this.defaultList = defaultList;
            return this;
        }

        public Builder setDefaultSet(SetSupplier defaultSet) {
            this.defaultSet = defaultSet;
            return this;
        }

        public Builder setDefaultMap(MapSupplier defaultMap) {
            this.defaultMap = defaultMap;
            return this;
        }
    }


}