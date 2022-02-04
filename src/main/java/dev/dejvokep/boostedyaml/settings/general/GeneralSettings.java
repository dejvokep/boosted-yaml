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
package dev.dejvokep.boostedyaml.settings.general;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.serialization.YamlSerializer;
import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.utils.supplier.ListSupplier;
import dev.dejvokep.boostedyaml.utils.supplier.MapSupplier;
import dev.dejvokep.boostedyaml.utils.supplier.SetSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * General settings cover all options related to documents in general.
 * <p>
 * Settings introduced by BoostedYAML are follow builder design pattern, e.g. you may build your own settings using
 * <code>GeneralSettings.builder() //configure// .build()</code>
 */
@SuppressWarnings("unused")
public class GeneralSettings {

    /**
     * Key format for sections to use; specifies how the loaded/supplied keys should be formatted.
     */
    public enum KeyFormat {

        /**
         * Allows only strings as keys.
         * <ul>
         *     <li>All keys loaded are converted to strings via {@link Object#toString()} (e.g. <code>5</code> -> <code>"5"</code>), except <code>null</code> keys, which are considered illegal and will throw a {@link NullPointerException}. Please note that such conversion is irrevocable upon saving.</li>
         *     <li>String routes should only be used, as all keys are guaranteed to be strings. {@link Route Routes} can still be used, however, due to their capabilities, it is considered to be an overkill; all non-string keys supplied via those will internally be converted to strings (without modifying the route itself, they are immutable).</li>
         * </ul>
         * <p>
         * <b>This key format ensures compatibility with Spigot/BungeeCord APIs.</b>
         */
        STRING,

        /**
         * Allows anything as the key per the YAML specification (that is, integers, strings, doubles...).
         * <ul>
         *     <li>Preserves keys as they were loaded by SnakeYAML Engine (YAML processor), or supplied.</li>
         *     <li>Note that the only way to refer to data at routes that contain non-string keys is using {@link Route}. String routes can still be used, however, only to the extent of their limitations.</li>
         * </ul>
         */
        OBJECT
    }

    /**
     * The default string route separator.
     */
    public static final char DEFAULT_ROUTE_SEPARATOR = '.';
    /**
     * Escaped version of the default separator.
     */
    public static final String DEFAULT_ESCAPED_SEPARATOR = Pattern.quote(String.valueOf(DEFAULT_ROUTE_SEPARATOR));
    /**
     * Default key format.
     */
    public static final KeyFormat DEFAULT_KEY_FORMATTING = KeyFormat.STRING;
    /**
     * Default serializer.
     */
    public static final YamlSerializer DEFAULT_SERIALIZER = StandardSerializer.getDefault();
    /**
     * If to use defaults by default.
     */
    public static final boolean DEFAULT_USE_DEFAULTS = true;
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

    //Key format
    private final KeyFormat keyFormat;
    //Route separator
    private final char separator;
    //Escaped route separator
    private final String escapedSeparator;
    //Serializer
    private final YamlSerializer serializer;
    //Use defaults
    private final boolean useDefaults;
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
        this.keyFormat = builder.keyFormat;
        this.separator = builder.routeSeparator;
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
        this.useDefaults = builder.useDefaults;
    }

    /**
     * Returns the key format to use; please read more at your selected {@link KeyFormat}.
     *
     * @return the key format to use
     * @see #getRouteSeparator()
     */
    public KeyFormat getKeyFormat() {
        return keyFormat;
    }

    /**
     * Sets route separator used to separate individual keys inside a string route and vice-versa.
     *
     * @return the separator to use
     */
    public char getRouteSeparator() {
        return separator;
    }

    /**
     * Returns escaped route separator.
     *
     * @return the escaped route separator
     * @see #getRouteSeparator()
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
     * Returns if to use defaults in {@link Section} methods.
     *
     * @return if to use defaults
     */
    public boolean isUseDefaults() {
        return useDefaults;
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
     * @param size initial size (if supported)
     * @param <T>  content type
     * @return the default list of the given size
     */
    public <T> List<T> getDefaultList(int size) {
        return defaultList.supply(size);
    }

    /**
     * Returns default empty list using the default list supplier.
     *
     * @param <T> content type
     * @return the default empty list
     */
    public <T> List<T> getDefaultList() {
        return getDefaultList(0);
    }

    /**
     * Returns default set of the given size, using the default set supplier.
     *
     * @param size initial size (if supported)
     * @param <T>  content type
     * @return the default set of the given size
     */
    public <T> Set<T> getDefaultSet(int size) {
        return defaultSet.supply(size);
    }

    /**
     * Returns default empty set using the default set supplier.
     *
     * @param <T> content type
     * @return the default empty set
     */
    public <T> Set<T> getDefaultSet() {
        return getDefaultSet(0);
    }

    /**
     * Returns default map of the given size, using the default map supplier.
     *
     * @param size initial size (if supported)
     * @param <K>  key type
     * @param <V>  value type
     * @return the default empty size
     */
    public <K, V> Map<K, V> getDefaultMap(int size) {
        return defaultMap.supply(size);
    }

    /**
     * Returns default empty map using the default map supplier.
     *
     * @param <K> key type
     * @param <V> value type
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
     * Returns a new builder with the same configuration as the given settings.
     *
     * @param settings preset settings
     * @return the new builder
     */
    public static Builder builder(GeneralSettings settings) {
        return builder()
                .setKeyFormat(settings.keyFormat)
                .setRouteSeparator(settings.separator)
                .setSerializer(settings.serializer)
                .setUseDefaults(settings.useDefaults)
                .setDefaultObject(settings.defaultObject)
                .setDefaultNumber(settings.defaultNumber)
                .setDefaultString(settings.defaultString)
                .setDefaultChar(settings.defaultChar)
                .setDefaultBoolean(settings.defaultBoolean)
                .setDefaultList(settings.defaultList)
                .setDefaultSet(settings.defaultSet)
                .setDefaultMap(settings.defaultMap);
    }

    /**
     * Builder for general settings.
     */
    public static class Builder {
        //Key format
        private KeyFormat keyFormat = DEFAULT_KEY_FORMATTING;
        //Route separator
        private char routeSeparator = DEFAULT_ROUTE_SEPARATOR;
        //Serializer
        private YamlSerializer serializer = DEFAULT_SERIALIZER;
        //Use defaults
        private boolean useDefaults = DEFAULT_USE_DEFAULTS;
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
         * Sets the key format to use.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_KEY_FORMATTING}
         *
         * @param keyFormat the key format to use
         * @return the builder
         * @see #setRouteSeparator(char)
         */
        public Builder setKeyFormat(@NotNull KeyFormat keyFormat) {
            this.keyFormat = keyFormat;
            return this;
        }

        /**
         * Sets route separator used to separate individual keys inside a string route and vice-versa.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_ROUTE_SEPARATOR}
         *
         * @param separator the separator to use
         * @return the builder
         */
        public Builder setRouteSeparator(char separator) {
            this.routeSeparator = separator;
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
         * Sets if to enable usage of the defaults by {@link Section} methods (if any are present).
         * <p>
         * <b>If enabled (<code>true</code>):</b>
         * <ul>
         *     <li>
         *         Bulk getter methods (return a set/map of all keys, routes, values, blocks) will not only include
         *         content from the file, but also from the equivalent section in the defaults.
         *     </li>
         *     <li>
         *         Value getters with signature <code>getX(route)</code> will search the defaults as documented. You can also view the behaviour in the
         *         call stack below:
         *         <ol>
         *             <li>
         *                 Is there any value at the specified route?
         *                 <ul>
         *                     <li>
         *                         <b>1A.</b> Yes: Is it compatible with the return type (see method documentation)?
         *                         <ul>
         *                             <li><b>2A.</b> Yes: Return it.</li>
         *                             <li>
         *                                 <b>2B.</b> No: Is there an equivalent of this section in the defaults ({@link Section#hasDefaults()})?
         *                                 <ul>
         *                                     <li>
         *                                         <b>3A.</b> Yes: Return the value returned by calling the same method on the default section equivalent ({@link Section#getDefaults()}).
         *                                     </li>
         *                                     <li>
         *                                         <b>3B.</b> No: Return the default value defined by the settings (see method documentation).
         *                                     </li>
         *                                 </ul>
         *                             </li>
         *                         </ul>
         *                     </li>
         *                     <li>
         *                         <b>1B.</b> No: Continue with 2B.
         *                     </li>
         *                 </ul>
         *             </li>
         *         </ol>
         *     </li>
         * </ul>
         * <b>If disabled (<code>false</code>):</b>
         * <ul>
         *     <li>
         *         None of the {@link Section} methods will interact with the defaults.
         *     </li>
         *     <li>
         *         This is recommended if you would like to handle all value absences (present in the defaults, but not
         *         in the file) manually - e.g. notifying the user and then using the default value defined within
         *         <code>final</code> fields, or obtained via {@link Section#getDefaults()}.
         *     </li>
         * </ul>
         * <p>
         * <b>Default: </b>{@link #DEFAULT_USE_DEFAULTS}
         *
         * @param useDefaults if to use defaults
         * @return the builder
         */
        public Builder setUseDefaults(boolean useDefaults) {
            this.useDefaults = useDefaults;
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
         * <i>The given default can not be <code>null</code> as multiple section getters derive their defaults from
         * this default (using {@link Number#intValue()}...).</i>
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
         * <i>The parameter is not of a primitive type, to allow for <code>null</code> values. Setting the default to
         * such value might produce unexpected issues, unless your program is adapted for it. On the other hand, there
         * are methods returning optionals, so having default value like this is rather pointless.</i>
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
         * <i>The parameter is not of a primitive type, to allow for <code>null</code> values. Setting the default to
         * such value might produce unexpected issues, unless your program is adapted for it. On the other hand, there
         * are methods returning optionals, so having default value like this is rather pointless.</i>
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