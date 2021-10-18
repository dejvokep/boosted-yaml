package com.davidcubesvk.yamlUpdater.core.settings.loader;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v2.env.EnvConfig;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

import java.util.Map;
import java.util.Optional;

/**
 * Loader settings; wrapper for SnakeYAML Engine's {@link LoadSettings} class which is more
 * detailed, provides more options and possibilities, hides options which should not be configured.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class LoaderSettings {

    /**
     * Default loader settings.
     */
    public static final LoaderSettings DEFAULT = builder().build();

    //SnakeYAML Engine load settings builder
    private final LoadSettingsBuilder builder;
    //If to automatically update and create file if absent
    private final boolean createFileIfAbsent, autoUpdate;

    /**
     * Creates final, immutable loader settings from the given builder.
     *
     * @param builder the builder
     */
    private LoaderSettings(Builder builder) {
        this.builder = builder.builder;
        this.autoUpdate = builder.autoUpdate;
        this.createFileIfAbsent = builder.createFileIfAbsent;
    }

    /**
     * Returns if to automatically attempt to update the file, after finished loading.
     * <p>
     * Per the {@link YamlFile#update(UpdaterSettings)} specification, update is not possible, therefore this option
     * has no effect, if no defaults (stream or file) have been given to the file instance, for which these settings
     * will be used.
     *
     * @return if to automatically update after loading
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Returns if to create a new file with default content if it does not exist (defaults will be dumped and saved using
     * {@link DumperSettings} given to the file instance, for which these settings will be used). If disabled, only
     * loads the defaults, without modifying the disk contents - manual save is needed.
     * <p>
     * This setting is not effective if no defaults (stream or file) have been given to the file instance, for which
     * these settings will be used.
     *
     * @return if to create a new file if absent
     */
    public boolean isCreateFileIfAbsent() {
        return createFileIfAbsent;
    }

    /**
     * Builds new settings and returns them. If the underlying builder was changed somehow, just in case, resets all
     * settings (those which should not be modified).
     *
     * @param generalSettings settings used to get defaults from
     * @return the new settings
     */
    public LoadSettings getSettings(GeneralSettings generalSettings) {
        return this.builder.setParseComments(true).setDefaultList(generalSettings::getDefaultList).setDefaultSet(generalSettings::getDefaultSet).setDefaultMap(generalSettings::getDefaultMap).build();
    }

    /**
     * Returns a new builder.
     *
     * @return the new builder
     */
    public static Builder builder() {
        return new Builder(LoadSettings.builder());
    }

    /**
     * Creates and returns a new builder from the given, already created SnakeYAML Engine settings builder.
     * <p>
     * <b>Note that the given builder is not cloned, so it is in the caller's best interest to never change it's
     * settings from now on.</b>
     * <p>
     * Please note that {@link Builder#setCreateFileIfAbsent(boolean)} and {@link Builder#setAutoUpdate(boolean)} still have to be
     * called (if you want to alter the default), as they are not part of the Engine's settings.
     *
     * @param builder the underlying builder
     * @return the new builder
     */
    public static Builder builder(LoadSettingsBuilder builder) {
        return new Builder(builder);
    }

    /**
     * Builder for loader settings; wrapper for SnakeYAML Engine's {@link LoadSettingsBuilder} class which is more
     * detailed, provides more options and possibilities, hides options which should not be configured.
     */
    public static class Builder {

        /**
         * If to automatically create a new file if absent by default.
         */
        public static final boolean DEFAULT_CREATE_FILE_IF_ABSENT = true;
        /**
         * If to automatically update the file after load by default.
         */
        public static final boolean DEFAULT_AUTO_UPDATE = true;
        /**
         * If to print detailed error messages by default.
         */
        public static final boolean DEFAULT_DETAILED_ERRORS = true;
        /**
         * If to allow duplicate keys by default.
         */
        public static final boolean DEFAULT_ALLOW_DUPLICATE_KEYS = true;
        /**
         * If to allow recursive map/set keys by default.
         */
        public static final boolean DEFAULT_ALLOW_RECURSIVE_KEYS = false;

        //Underlying SnakeYAML Engine settings builder
        private final LoadSettingsBuilder builder;
        //If to automatically update and create file if absent
        private boolean autoUpdate = DEFAULT_AUTO_UPDATE, createFileIfAbsent = DEFAULT_CREATE_FILE_IF_ABSENT;

        /**
         * Creates a new builder from the given, already created SnakeYAML Engine settings builder.
         * <p>
         * Please note that {@link #setCreateFileIfAbsent(boolean)} and {@link #setAutoUpdate(boolean)} still have to be
         * called (if you want to alter the default), as they are not part of the Engine's settings.
         *
         * @param builder the underlying builder
         */
        private Builder(LoadSettingsBuilder builder) {
            this.builder = builder;
        }

        /**
         * Creates a new builder. Automatically applies the defaults, compatible with Spigot/BungeeCord API.
         */
        private Builder() {
            //Create
            this.builder = LoadSettings.builder();
            //Set defaults
            setDetailedErrors(DEFAULT_DETAILED_ERRORS);
            setAllowDuplicateKeys(DEFAULT_ALLOW_DUPLICATE_KEYS);
            setAllowRecursiveKeys(DEFAULT_ALLOW_RECURSIVE_KEYS);
        }

        /**
         * Sets if to create a new file with default content if it does not exist (defaults will be dumped and saved using
         * {@link DumperSettings} given to the file instance, for which these settings will be used). If disabled, only
         * loads the defaults, without modifying the disk contents - manual save is needed.
         * <p>
         * This setting is not effective if no defaults (stream or file) have been given to the file instance, for which
         * these settings will be used.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_CREATE_FILE_IF_ABSENT}
         *
         * @param createFileIfAbsent if to create a new file if absent
         * @return the builder
         */
        public Builder setCreateFileIfAbsent(boolean createFileIfAbsent) {
            this.createFileIfAbsent = createFileIfAbsent;
            return this;
        }

        /**
         * Sets if to automatically attempt to update the file, after finished loading.
         * <p>
         * Per the {@link YamlFile#update(UpdaterSettings)} specification, update is not possible, therefore this option
         * has no effect, if no defaults (stream or file) have been given to the file instance, for which these settings
         * will be used.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_AUTO_UPDATE}
         *
         * @param autoUpdate if to automatically update after loading
         * @return the builder
         */
        public Builder setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            return this;
        }

        /**
         * Sets custom label for error messages.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setLabel(String)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setAnchorGenerator(java.lang.String)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param label the label
         * @return the builder
         */
        public Builder setErrorLabel(String label) {
            builder.setLabel(label);
            return this;
        }

        /**
         * Sets if to print detailed error messages.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_DETAILED_ERRORS}<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setUseMarks(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setUseMarks(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param detailedErrors if to print detailed errors
         * @return the builder
         */
        public Builder setDetailedErrors(boolean detailedErrors) {
            builder.setUseMarks(detailedErrors);
            return this;
        }

        /**
         * Sets if to allow duplicate keys in sections (last key wins when loading).
         * <p>
         * YAML 1.1 (used by Spigot/BungeeCord API), supported those as if this option was enabled, YAML 1.2 however,
         * requires unique keys. This is just a compatibility method.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ALLOW_DUPLICATE_KEYS}<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setAllowDuplicateKeys(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setAllowDuplicateKeys(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param allowDuplicateKeys if to allow duplicate keys
         * @return the builder
         */
        public Builder setAllowDuplicateKeys(boolean allowDuplicateKeys) {
            builder.setAllowDuplicateKeys(allowDuplicateKeys);
            return this;
        }

        /**
         * Sets if to allow recursive keys for maps and sets. Manipulate with caution, as it loading such structures
         * might cause unexpected issues.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ALLOW_RECURSIVE_KEYS}<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setAllowRecursiveKeys(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setAllowRecursiveKeys(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param allowRecursiveKeys if to allow recursive keys
         * @return the builder
         */
        public Builder setAllowRecursiveKeys(boolean allowRecursiveKeys) {
            builder.setAllowRecursiveKeys(allowRecursiveKeys);
            return this;
        }

        /**
         * Sets maximum aliases for collections to prevent memory leaks
         * (<a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">Billion laughs attack</a> to be more specific).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setMaxAliasesForCollections(int)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setMaxAliasesForCollections(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param maxCollectionAliases maximum aliases for collections
         * @return the builder
         */
        public Builder setMaxCollectionAliases(int maxCollectionAliases) {
            builder.setMaxAliasesForCollections(maxCollectionAliases);
            return this;
        }

        /**
         * Sets constructors used to construct Java objects from nodes (by their corresponding tag types). If there was
         * anything set previously, it is overwritten.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setTagConstructors(Map)} (int)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setTagConstructors(java.util.Map)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param constructors constructor map
         * @return the builder
         */
        public Builder setObjectConstructors(Map<Tag, ConstructNode> constructors) {
            builder.setTagConstructors(constructors);
            return this;
        }

        /**
         * Sets custom scalar resolver, used to resolve tags for objects in string format (<code>!str "x"</code>).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setScalarResolver(ScalarResolver)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setScalarResolver(org.snakeyaml.engine.v2.resolver.ScalarResolver)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param resolver the resolver to set
         * @return the builder
         * @see DumpSettingsBuilder#setScalarResolver(ScalarResolver)
         */
        public Builder setScalarResolver(ScalarResolver resolver) {
            builder.setScalarResolver(resolver);
            return this;
        }

        /**
         * Sets custom environment variable config.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setEnvConfig(Optional)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setEnvConfig(java.util.Optional)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param envConfig the config to set
         * @return the builder
         * @see DumpSettingsBuilder#setScalarResolver(ScalarResolver)
         */
        public Builder setEnvironmentConfig(EnvConfig envConfig) {
            builder.setEnvConfig(Optional.ofNullable(envConfig));
            return this;
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public LoaderSettings build() {
            return new LoaderSettings(this);
        }
    }

}