package com.davidcubesvk.yamlUpdater.core.settings.general;

import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.serialization.standard.StandardSerializer;
import com.davidcubesvk.yamlUpdater.core.serialization.YamlSerializer;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.ListSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.SetSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * General settings cover all options related to files.
 * <p>
 * To start using this library, it is recommended to take a look at the following methods:
 * <ul>
 *     <li>{@link Builder#setKeyMode(KeyMode)}</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class GeneralSettings {

    /**
     * Key mode for sections to use, specifies how the loaded/supplied keys should be treated and how to convert them.
     * <p>
     * It highly recommended to read {@link Section#getBlockSafe(Route)} and {@link Section#getBlockSafe(String)} documentation.
     */
    public enum KeyMode {

        /**
         * Converts all section keys to strings when loading. If any of the keys in {@link Route} objects used in
         * get/set/remove... methods is not a string, convert it to one (internally, will not modify the routes). Routes
         * with non-string keys will be rendered useless, as the sections contain string keys only.
         * <p>
         * <b>This key mode ensures compatibility with Spigot/BungeeCord APIs.</b>
         */
        STRING,

        /**
         * Does not convert any keys - leaves them as loaded/supplied using methods. If string routes are used, they will
         * only be able to refer to values whose route is constructed only from {@link String} keys.
         */
        OBJECT
    }

    /**
     * The default string route separator.
     */
    public static final char DEFAULT_SEPARATOR = '.';
    /**
     * Escaped version of the default separator.
     */
    public static final String DEFAULT_ESCAPED_SEPARATOR = Pattern.quote(String.valueOf(DEFAULT_SEPARATOR));
    /**
     * Default key mode.
     */
    public static final KeyMode DEFAULT_KEY_MODE = KeyMode.STRING;
    /**
     * Default serializer.
     */
    public static final YamlSerializer DEFAULT_SERIALIZER = StandardSerializer.DEFAULT;
    /**
     * Default object.
     */
    public static final Object DEFAULT_OBJECT = null;
    /**
     * Default number.
     */
    public static final Number DEFAULT_NUMBER = 0;
    /**
     * Default string.
     */
    public static final String DEFAULT_STRING = null;
    /**
     * Default char.
     */
    public static final Character DEFAULT_CHAR = ' ';
    /**
     * Default boolean.
     */
    public static final Boolean DEFAULT_BOOLEAN = false;
    /**
     * Default list supplier.
     */
    public static final ListSupplier DEFAULT_LIST = ArrayList::new;
    /**
     * Default set supplier.
     */
    public static final SetSupplier DEFAULT_SET = LinkedHashSet::new;
    /**
     * Default map supplier.
     */
    public static final MapSupplier DEFAULT_MAP = LinkedHashMap::new;

    /**
     * Default general settings.
     */
    public static final GeneralSettings DEFAULT = builder().build();

    //key mode
    private final KeyMode keyMode;
    //Route separator
    private final char separator;
    //Escaped route separator
    private final String escapedSeparator;
    //Serializer
    private final YamlSerializer serializer;
    //Default object
    private final Object defaultObject;
    //Default number
    private final Number defaultNumber;
    //Default string
    private final String defaultString;
    //Default char
    private final Character defaultChar;
    //Default boolean
    private final Boolean defaultBoolean;
    //Default list supplier
    private final ListSupplier defaultList;
    //Default set supplier
    private final SetSupplier defaultSet;
    //Default map supplier
    private final MapSupplier defaultMap;

    /**
     * Creates final, immutable general settings from the given builder.
     *
     * @param builder the builder
     */
    private GeneralSettings(Builder builder) {
        this.keyMode = builder.keyMode;
        this.separator = builder.separator;
        this.escapedSeparator = Pattern.quote(String.valueOf(separator));
        this.serializer = builder.serializer;
        this.defaultObject = builder.defaultObject;
        this.defaultNumber = builder.defaultNumber;
        this.defaultString = builder.defaultString;
        this.defaultChar = builder.defaultChar;
        this.defaultBoolean = builder.defaultBoolean;
        this.defaultList = builder.defaultList;
        this.defaultSet = builder.defaultSet;
        this.defaultMap = builder.defaultMap;
    }

    /**
     * Returns the key mode to use; please read more at your selected {@link KeyMode}.
     *
     * @return the key mode to use
     * @see #getSeparator()
     */
    public KeyMode getKeyMode() {
        return keyMode;
    }

    /**
     * Returns route separator to use to separate individual keys inside a string route. Functionality compatible with
     * Spigot/BungeeCord API. Unless requested explicitly, used only if key mode is set to {@link KeyMode#STRING}.
     * <p>
     * Assuming separator <code>'.'</code>, route <code>a.b</code> represents object at key <code>b</code> in section
     * at key <code>a</code> in the root file (section).
     *
     * @return the separator to use
     * @see #getKeyMode()
     * @see KeyMode#STRING
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Returns escaped route separator.
     *
     * @return the escaped route separator
     * @see #getSeparator()
     */
    public String getEscapedSeparator() {
        return escapedSeparator;
    }

    /**
     * Returns serializer to use for custom object serialization/deserialization.
     *
     * @return the serializer to use
     */
    public YamlSerializer getSerializer() {
        return serializer;
    }

    /**
     * Returns default object to use by section getters if the return type is object.
     *
     * @return the default object
     */
    public Object getDefaultObject() {
        return defaultObject;
    }

    /**
     * Returns default string to use by section getters if the return type is string.
     *
     * @return the default string
     */
    public String getDefaultString() {
        return defaultString;
    }

    /**
     * Returns default char to use by section getters if the return type is char.
     *
     * @return the default char
     */
    public Character getDefaultChar() {
        return defaultChar;
    }

    /**
     * Returns default number to use by section getters if the return type is a number - integer, float, byte,
     * biginteger... (per the getter documentation).
     *
     * @return the default number
     */
    public Number getDefaultNumber() {
        return defaultNumber;
    }

    /**
     * Returns default boolean to use by section getters if the return type is boolean.
     *
     * @return the default boolean
     */
    public Boolean getDefaultBoolean() {
        return defaultBoolean;
    }

    /**
     * Returns default list of the given size, using the default list supplier.
     *
     * @return the default list of the given size
     */
    public <T> List<T> getDefaultList(int size) {
        return defaultList.supply(size);
    }

    /**
     * Returns default empty list using the default list supplier.
     *
     * @return the default empty list
     */
    public <T> List<T> getDefaultList() {
        return getDefaultList(0);
    }

    /**
     * Returns default set of the given size, using the default set supplier.
     *
     * @return the default set of the given size
     */
    public <T> Set<T> getDefaultSet(int size) {
        return defaultSet.supply(size);
    }

    /**
     * Returns default empty set using the default set supplier.
     *
     * @return the default empty set
     */
    public <T> Set<T> getDefaultSet() {
        return getDefaultSet(0);
    }

    /**
     * Returns default map of the given size, using the default map supplier.
     *
     * @return the default empty size
     */
    public <K, V> Map<K, V> getDefaultMap(int size) {
        return defaultMap.supply(size);
    }

    /**
     * Returns default empty map using the default map supplier.
     *
     * @return the default map of the given size
     */
    public <K, V> Map<K, V> getDefaultMap() {
        return getDefaultMap(0);
    }

    /**
     * Returns default map supplier to use to supply map instances during loading/creating new sections/when needed.
     *
     * @return the supplier
     */
    public MapSupplier getDefaultMapSupplier() {
        return defaultMap;
    }

    /**
     * Returns a new builder.
     *
     * @return the new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for general settings.
     */
    public static class Builder {
        //key mode
        private KeyMode keyMode = DEFAULT_KEY_MODE;
        //Route separator
        private char separator = DEFAULT_SEPARATOR;
        //Serializer
        private YamlSerializer serializer = DEFAULT_SERIALIZER;
        //Default object
        private Object defaultObject = DEFAULT_OBJECT;
        //Default number
        private Number defaultNumber = DEFAULT_NUMBER;
        //Default string
        private String defaultString = DEFAULT_STRING;
        //Default char
        private Character defaultChar = DEFAULT_CHAR;
        //Default boolean
        private Boolean defaultBoolean = DEFAULT_BOOLEAN;
        //Default list supplier
        private ListSupplier defaultList = DEFAULT_LIST;
        //Default set supplier
        private SetSupplier defaultSet = DEFAULT_SET;
        //Default map supplier
        private MapSupplier defaultMap = DEFAULT_MAP;

        /**
         * Creates a new builder will all the default settings applied.
         */
        private Builder() {
        }

        /**
         * Sets the key mode to use. Please read more at your selected {@link KeyMode}.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_KEY_MODE}
         *
         * @param keyMode the key mode to use
         * @return the builder
         * @see #setSeparator(char)
         */
        public Builder setKeyMode(@NotNull KeyMode keyMode) {
            this.keyMode = keyMode;
            return this;
        }

        /**
         * Sets route separator used to separate individual keys inside a string route. Functionality compatible with
         * Spigot/BungeeCord API. Unless requested explicitly, used only if key mode is set to {@link KeyMode#STRING}.
         * <p>
         * Assuming separator <code>'.'</code>, route <code>"a.b"</code> represents object at key <code>"b"</code> in section
         * at key <code>"a"</code> in the root file (section).
         * <p>
         * <b>Default: </b>{@link #DEFAULT_SEPARATOR}
         *
         * @param separator the separator to use
         * @return the builder
         * @see #setKeyMode(KeyMode)
         * @see KeyMode#STRING
         */
        public Builder setSeparator(char separator) {
            this.separator = separator;
            return this;
        }

        /**
         * Sets serializer used for custom object serialization/deserialization.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_SERIALIZER}
         *
         * @param serializer the serializer to use
         * @return the builder
         */
        public Builder setSerializer(@NotNull YamlSerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Sets default object used by section getters if the return type is object.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_OBJECT}
         *
         * @param defaultObject default object
         * @return the builder
         */
        public Builder setDefaultObject(@Nullable Object defaultObject) {
            this.defaultObject = defaultObject;
            return this;
        }

        /**
         * Sets default number used by section getters if the return type is a number - integer, float, byte,
         * biginteger... (per the getter documentation).
         * <p>
         * <b>Default: </b>{@link #DEFAULT_NUMBER}
         * <p>
         * <i>The given default can not be <code>null</code> as multiple section getters derive their defaults from this
         * default (using {@link Number#intValue()}...).</i>
         *
         * @param defaultNumber default number
         * @return the builder
         */
        public Builder setDefaultNumber(@NotNull Number defaultNumber) {
            this.defaultNumber = defaultNumber;
            return this;
        }

        /**
         * Sets default string used by section getters if the return type is string.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_STRING}
         *
         * @param defaultString default string
         * @return the builder
         */
        public Builder setDefaultString(@Nullable String defaultString) {
            this.defaultString = defaultString;
            return this;
        }

        /**
         * Sets default char used by section getters if the return type is char.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_CHAR}
         * <p>
         * <i>The parameter is not of a primitive type, to allow for <code>null</code> values. Setting the default to such
         * value might produce unexpected issues, unless your program is adapted for it. On the other hand, there are
         * methods returning optionals, so having default value like this is rather pointless.</i>
         *
         * @param defaultChar default char
         * @return the builder
         */
        public Builder setDefaultChar(@Nullable Character defaultChar) {
            this.defaultChar = defaultChar;
            return this;
        }

        /**
         * Sets default boolean used by section getters if the return type is boolean.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_BOOLEAN}
         * <p>
         * <i>The parameter is not of a primitive type, to allow for <code>null</code> values. Setting the default to such
         * value might produce unexpected issues, unless your program is adapted for it. On the other hand, there are
         * methods returning optionals, so having default value like this is rather pointless.</i>
         *
         * @param defaultBoolean default boolean
         * @return the builder
         */
        public Builder setDefaultBoolean(@Nullable Boolean defaultBoolean) {
            this.defaultBoolean = defaultBoolean;
            return this;
        }

        /**
         * Sets default list supplier used to supply list instances during loading/when needed.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_LIST}
         *
         * @param defaultList the supplier
         * @return the builder
         */
        public Builder setDefaultList(@NotNull ListSupplier defaultList) {
            this.defaultList = defaultList;
            return this;
        }

        /**
         * Sets default set supplier used to supply set instances during loading/when needed.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_SET}
         *
         * @param defaultSet the supplier
         * @return the builder
         */
        public Builder setDefaultSet(@NotNull SetSupplier defaultSet) {
            this.defaultSet = defaultSet;
            return this;
        }

        /**
         * Sets default map supplier used to supply map instances during loading/creating new sections/when needed.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_MAP}
         *
         * @param defaultMap the supplier
         * @return the builder
         */
        public Builder setDefaultMap(@NotNull MapSupplier defaultMap) {
            this.defaultMap = defaultMap;
            return this;
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public GeneralSettings build() {
            return new GeneralSettings(this);
        }

    }


}