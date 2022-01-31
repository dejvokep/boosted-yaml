/*
 * Copyright 2021 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.settings.loader;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
 * Loader settings cover all options related explicitly (only) to file loading.
 * <p>
 * To start using this library, it is recommended to take a look at the following methods:
 * <ul>
 *     <li>{@link Builder#setCreateFileIfAbsent(boolean)}</li>
 *     <li>{@link Builder#setAutoUpdate(boolean)}</li>
 * </ul>
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
     * Per the {@link YamlDocument#update(UpdaterSettings)} specification, update is not possible, therefore this option
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
     * Builds new SnakeYAML engine settings. If the underlying builder was changed somehow, just in case, resets all
     * settings (those which should not be modified).
     *
     * @param generalSettings settings used to get defaults (list, set, map) from
     * @return the new settings
     */
    public LoadSettings buildEngineSettings(GeneralSettings generalSettings) {
        return this.builder.setParseComments(true).setDefaultList(generalSettings::getDefaultList).setDefaultSet(generalSettings::getDefaultSet).setDefaultMap(generalSettings::getDefaultMap).build();
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
     * Returns a new builder with the same configuration as the given settings.
     *
     * @param settings preset settings
     * @return the new builder
     */
    public static Builder builder(LoaderSettings settings) {
        return builder(settings.builder)
                .setAutoUpdate(settings.autoUpdate)
                .setCreateFileIfAbsent(settings.createFileIfAbsent);
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
        public static final boolean DEFAULT_AUTO_UPDATE = false;
        /**
         * If to print detailed error messages by default.
         */
        public static final boolean DEFAULT_DETAILED_ERRORS = true;
        /**
         * If to allow duplicate keys by default.
         */
        public static final boolean DEFAULT_ALLOW_DUPLICATE_KEYS = true;

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
        }

        /**
         * Sets if to create a new file and save it if it does not exist automatically.
         * <p>
         * Not effective if there is no {@link YamlDocument#getFile() file associated} with the document.
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
         * If enabled, automatically calls {@link YamlDocument#update()} after it has been loaded.
         * <p>
         * Not effective if there are no {@link YamlDocument#getDefaults() defaults associated} with the document.
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
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setLabel(java.lang.String)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param label the label
         * @return the builder
         */
        public Builder setErrorLabel(@NotNull String label) {
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
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setUseMarks(boolean)">click</a><br>
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
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ALLOW_DUPLICATE_KEYS}<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setAllowDuplicateKeys(boolean)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setAllowDuplicateKeys(boolean)">click</a><br>
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
         * Sets maximum aliases a collection can have to prevent memory leaks (see
         * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">Billion laughs attack</a>).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setMaxAliasesForCollections(int)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setMaxAliasesForCollections(int)">click</a><br>
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
         * Sets custom node to Java object constructors, per YAML tag.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setTagConstructors(Map)} (int)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setTagConstructors(java.util.Map)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param constructors constructor map
         * @return the builder
         */
        public Builder setTagConstructors(@NotNull Map<Tag, ConstructNode> constructors) {
            builder.setTagConstructors(constructors);
            return this;
        }

        /**
         * Sets custom scalar resolver, used to resolve tags for objects.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setScalarResolver(ScalarResolver)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setScalarResolver(org.snakeyaml.engine.v2.resolver.ScalarResolver)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param resolver the resolver to set
         * @return the builder
         * @see DumpSettingsBuilder#setScalarResolver(ScalarResolver)
         */
        public Builder setScalarResolver(@NotNull ScalarResolver resolver) {
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
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setEnvConfig(java.util.Optional)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param envConfig the config to set
         * @return the builder
         * @see DumpSettingsBuilder#setScalarResolver(ScalarResolver)
         */
        public Builder setEnvironmentConfig(@Nullable EnvConfig envConfig) {
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