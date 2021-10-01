package com.davidcubesvk.yamlUpdater.core.settings.general;

import com.davidcubesvk.yamlUpdater.core.block.Section;
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
    private final char defaultChar;
    private final boolean defaultBoolean;
    private final ListSupplier defaultList;
    private final SetSupplier defaultSet;
    private final MapSupplier defaultMap;

    /**
     * Initializes the settings with data (to be more specific, disk folder and class loader) from the given main class.
     *
     * @param updater the main updater class
     */
    public GeneralSettings(PathMode pathMode, char separator, YamlSerializer serializer, Object defaultObject, Section defaultSection, Number defaultNumber, String defaultString, char defaultChar, boolean defaultBoolean, ListSupplier defaultList, SetSupplier defaultSet, MapSupplier defaultMap) {
        this.pathMode = pathMode;
        this.separator = separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
        this.serializer = serializer;
        this.defaultObject = defaultObject;
        this.defaultSection = defaultSection;
        this.defaultNumber = defaultNumber;
        this.defaultString = defaultString;
        this.defaultChar = defaultChar;
        this.defaultBoolean = defaultBoolean;
        this.defaultList = defaultList;
        this.defaultSet = defaultSet;
        this.defaultMap = defaultMap;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PathMode pathMode = DEFAULT_PATH_MODE;
        private char separator = DEFAULT_SEPARATOR;
        private YamlSerializer serializer = null;
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
            return new GeneralSettings(pathMode, separator, serializer, defaultObject, defaultSection, defaultNumber, defaultString, defaultChar, defaultBoolean, defaultList, defaultSet, defaultMap);
        }

        public void setPathMode(PathMode pathMode) {
            this.pathMode = pathMode;
        }

        public void setSeparator(char separator) {
            this.separator = separator;
        }

        public void setSerializer(YamlSerializer serializer) {
            this.serializer = serializer;
        }

        public void setDefaultObject(Object defaultObject) {
            this.defaultObject = defaultObject;
        }

        public void setDefaultSection(Section defaultSection) {
            this.defaultSection = defaultSection;
        }

        public void setDefaultNumber(Number defaultNumber) {
            this.defaultNumber = defaultNumber;
        }

        public void setDefaultString(String defaultString) {
            this.defaultString = defaultString;
        }

        public void setDefaultChar(char defaultChar) {
            this.defaultChar = defaultChar;
        }

        public void setDefaultBoolean(boolean defaultBoolean) {
            this.defaultBoolean = defaultBoolean;
        }

        public void setDefaultList(ListSupplier defaultList) {
            this.defaultList = defaultList;
        }

        public void setDefaultSet(SetSupplier defaultSet) {
            this.defaultSet = defaultSet;
        }

        public void setDefaultMap(MapSupplier defaultMap) {
            this.defaultMap = defaultMap;
        }
    }


}