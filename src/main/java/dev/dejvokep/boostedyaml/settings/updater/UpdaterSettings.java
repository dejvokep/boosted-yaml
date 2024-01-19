/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.settings.updater;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.versioning.AutomaticVersioning;
import dev.dejvokep.boostedyaml.dvs.versioning.ManualVersioning;
import dev.dejvokep.boostedyaml.dvs.versioning.Versioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.route.RouteFactory;
import dev.dejvokep.boostedyaml.settings.Settings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.utils.supplier.MapSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Updater settings cover all options related explicitly (only) to file updating.
 * <p>
 * Settings introduced by BoostedYAML follow builder design pattern, e.g. you may build your own settings using
 * <code>UpdaterSettings.builder() //configure// .build()</code>
 */
@SuppressWarnings("unused")
public class UpdaterSettings implements Settings {

    /**
     * Enum defining how options in sections should be sorted during merging.
     */
    public enum OptionSorting {

        /**
         * Does not sort. This setting ensures that new entries from the defaults will be placed as the last into the
         * section <b>map</b>.
         * <p>
         * <b>This setting does not introduce any additional memory consumption and should be used if the map
         * implementation defined by {@link GeneralSettings.Builder#setDefaultMap(MapSupplier)} doesn't preserve element
         * order (for example {@link HashMap}).</b>
         */
        NONE,

        /**
         * Sorts options by their order in the defaults. This setting ensures that entries which will appear in the
         * merged document will be placed into the section <b>map</b> in order defined by the defaults.
         * <p>
         * <b>Note that the order depends on the map implementation defined by {@link
         * GeneralSettings.Builder#setDefaultMap(MapSupplier)}. If the used implementation does not preserve order of
         * the elements (as they were put into the map), avoid additional memory consumption and use {@link #NONE}
         * instead.</b>
         */
        SORT_BY_DEFAULTS
    }

    /**
     * If to, by default, automatically save the file after updating.
     */
    public static final boolean DEFAULT_AUTO_SAVE = true;
    /**
     * If to enable file downgrading by default.
     */
    public static final boolean DEFAULT_ENABLE_DOWNGRADING = true;
    /**
     * If to keep all non-merged content (present in the document, but not in the defaults) inside the document by
     * default.
     */
    public static final boolean DEFAULT_KEEP_ALL = false;
    /**
     * Default option sorting.
     */
    public static final OptionSorting DEFAULT_OPTION_SORTING = OptionSorting.SORT_BY_DEFAULTS;
    /**
     * Default merge preservation rules.
     */
    public static final Map<MergeRule, Boolean> DEFAULT_MERGE_RULES = Collections.unmodifiableMap(new HashMap<MergeRule, Boolean>() {{
        put(MergeRule.MAPPINGS, true);
        put(MergeRule.MAPPING_AT_SECTION, false);
        put(MergeRule.SECTION_AT_MAPPING, false);
    }});
    /**
     * Default versioning.
     */
    public static final Versioning DEFAULT_VERSIONING = null;

    /**
     * Default updater settings.
     */
    public static final UpdaterSettings DEFAULT = builder().build();

    //Save automatically
    private final boolean autoSave;
    //Enable downgrading
    private final boolean enableDowngrading;
    //Keep all contents
    private final boolean keepAll;
    //Merge rules
    private final Map<MergeRule, Boolean> mergeRules;
    //Routes to ignore
    private final Map<String, RouteSet> ignored;
    //Relocations
    private final Map<String, RouteMap<Route, String>> relocations;
    //Mappers
    private final Map<String, Map<Route, ValueMapper>> mappers;
    //Custom logic
    private final Map<String, List<Consumer<YamlDocument>>> customLogic;
    //Versioning
    private final Versioning versioning;
    //Option sorting
    private final OptionSorting optionSorting;

    /**
     * Creates final, immutable updater settings from the given builder.
     *
     * @param builder the builder
     */
    public UpdaterSettings(Builder builder) {
        this.autoSave = builder.autoSave;
        this.enableDowngrading = builder.enableDowngrading;
        this.keepAll = builder.keepAll;
        this.optionSorting = builder.optionSorting;
        this.mergeRules = builder.mergeRules;
        this.ignored = builder.ignored;
        this.relocations = builder.relocations;
        this.mappers = builder.mappers;
        this.customLogic = builder.customLogic;
        this.versioning = builder.versioning;
    }

    /**
     * Returns merge preservation rules.
     * <p>
     * The given map contains the merge rule as the key, with the value representing if to preserve content already in
     * the document instead of the equivalent from the defaults.
     *
     * @return the merge rules
     */
    public Map<MergeRule, Boolean> getMergeRules() {
        return mergeRules;
    }

    /**
     * Returns which routes to ignore if updating to the specified version ID.
     *
     * @param versionId version for which to return the routes
     * @param separator separator used to parse the string routes
     * @return the set of routes representing blocks to ignore at the version ID
     */
    public Set<Route> getIgnoredRoutes(@NotNull String versionId, char separator) {
        RouteSet ignored = this.ignored.get(versionId);
        return ignored == null ? Collections.emptySet() : ignored.merge(separator);
    }

    /**
     * Returns relocations (in <code>from route = to route</code> format) that took effect at the given version ID.
     *
     * @param versionId the version ID for which to return relocations
     * @param separator separator used to parse the string routes
     * @return relocations that took effect at the version ID
     */
    public Map<Route, Route> getRelocations(@NotNull String versionId, char separator) {
        RouteMap<Route, String> relocations = this.relocations.get(versionId);
        return relocations == null ? Collections.emptyMap() : relocations.merge(Function.identity(), route -> Route.fromString(route, separator), separator);
    }

    /**
     * Returns mappers to apply at the given version ID.
     *
     * @param versionId the version ID for which to return mappers
     * @param separator separator used to parse the string routes
     * @return the mappers to apply at the given version ID
     */
    public Map<Route, ValueMapper> getMappers(@NotNull String versionId, char separator) {
        return mappers.getOrDefault(versionId, Collections.emptyMap());
    }

    /**
     * Returns custom logic to run at the given version ID.
     *
     * @param versionId the version ID for which to return custom logic
     * @return the custom logic to run at the given version ID
     */
    public List<Consumer<YamlDocument>> getCustomLogic(@NotNull String versionId) {
        return customLogic.getOrDefault(versionId, Collections.emptyList());
    }

    /**
     * Returns the versioning.
     *
     * @return the versioning
     */
    public Versioning getVersioning() {
        return versioning;
    }

    /**
     * Returns if to enable downgrading.
     *
     * @return if to enable downgrading
     */
    public boolean isEnableDowngrading() {
        return enableDowngrading;
    }

    /**
     * Returns if to keep all non-merged (that don't have an equivalent in the defaults) blocks in the document.
     *
     * @return if to keep all non-merged blocks
     */
    public boolean isKeepAll() {
        return keepAll;
    }

    /**
     * Sets if the file should automatically be saved using {@link YamlDocument#save()} after the updater has finished
     * updating (does not save if nothing's changed).
     *
     * @return if the file should automatically be saved
     */
    public boolean isAutoSave() {
        return autoSave;
    }

    /**
     * Returns how to sort options in sections during merging.
     *
     * @return option sorting to use
     */
    public OptionSorting getOptionSorting() {
        return optionSorting;
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
    public static Builder builder(UpdaterSettings settings) {
        return builder()
                .setAutoSave(settings.autoSave)
                .setEnableDowngrading(settings.enableDowngrading)
                .setKeepAll(settings.keepAll)
                .setOptionSorting(settings.optionSorting)
                .setMergeRules(settings.mergeRules)
                .setIgnoredRoutesInternal(settings.ignored)
                .setRelocationsInternal(settings.relocations)
                .addMappers(settings.mappers)
                .addCustomLogic(settings.customLogic)
                .setVersioning(settings.versioning);
    }

    /**
     * Builder for updater settings.
     */
    public static class Builder {

        //Save automatically
        private boolean autoSave = DEFAULT_AUTO_SAVE;
        //Enable downgrading
        private boolean enableDowngrading = DEFAULT_ENABLE_DOWNGRADING;
        //Keep all contents
        private boolean keepAll = DEFAULT_KEEP_ALL;
        //Merge rules
        private final Map<MergeRule, Boolean> mergeRules = new HashMap<>(DEFAULT_MERGE_RULES);
        //Routes to ignore
        private final Map<String, RouteSet> ignored = new HashMap<>();
        //Relocations
        private final Map<String, RouteMap<Route, String>> relocations = new HashMap<>();
        //Mappers
        private final Map<String, Map<Route, ValueMapper>> mappers = new HashMap<>();
        //Custom logic
        private final Map<String, List<Consumer<YamlDocument>>> customLogic = new HashMap<>();
        //Versioning
        private Versioning versioning = DEFAULT_VERSIONING;
        //Option sorting
        private OptionSorting optionSorting = DEFAULT_OPTION_SORTING;

        /**
         * Creates a new builder will all the default settings applied.
         */
        private Builder() {
        }

        /**
         * Sets if the file should automatically be saved using {@link YamlDocument#save()} after the updater has
         * finished updating (does not save if nothing's changed).
         * <p>
         * Not effective if there is no {@link YamlDocument#getFile() file associated} with the document.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_AUTO_SAVE}
         *
         * @param autoSave if to save automatically after update
         * @return the builder
         */
        public Builder setAutoSave(boolean autoSave) {
            this.autoSave = autoSave;
            return this;
        }

        /**
         * Sets if to enable downgrading.
         * <p>
         * <b>Downgrading is considered to be a situation:</b>
         * <ul>
         *     <li>when there are valid version IDs found for both files (supplied manually or automatically from files),</li>
         *     <li>the version ID of the document represents newer version than version ID of the defaults.</li>
         * </ul>
         * Please note that by specification, the defaults must have a valid ID supplied/specified.
         * <p>
         * If the updater detects downgrading, and it is enabled, the updater will skip relocations, proceeding directly
         * to merging; throws an error otherwise. If configured like so, you may also want to disable
         * {@link LoaderSettings.Builder#setAutoUpdate(boolean)} (if an error is thrown, you won't be able to create
         * the document).
         * <p>
         * <b>Effective if and only a versioning is specified.</b>
         * <p>
         * <b>Default: </b>{@link #DEFAULT_ENABLE_DOWNGRADING}
         *
         * @param enableDowngrading if to enable downgrading
         * @return the builder
         */
        public Builder setEnableDowngrading(boolean enableDowngrading) {
            this.enableDowngrading = enableDowngrading;
            return this;
        }

        /**
         * Sets if to keep all non-merged (that don't have an equivalent in the defaults) blocks in the document instead
         * of deleting them.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_KEEP_ALL}
         *
         * @param keepAll if to keep all non-merged blocks in the document
         * @return the builder
         */
        public Builder setKeepAll(boolean keepAll) {
            this.keepAll = keepAll;
            return this;
        }

        /**
         * Sets how to sort options in sections during merging.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_OPTION_SORTING}
         *
         * @param optionSorting option sorting to use
         * @return the builder
         */
        public Builder setOptionSorting(@NotNull OptionSorting optionSorting) {
            this.optionSorting = optionSorting;
            return this;
        }

        /**
         * Sets merge preservation rules. Overwrites only rules that are defined in the given map. You can learn more at
         * {@link MergeRule}.
         * <p>
         * The given map should contain the merge rule as the key, with value representing if to preserve content
         * already in the document instead of the equivalent from defaults.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_MERGE_RULES}
         *
         * @param mergeRules the merge rules to set
         * @return the builder
         * @see #setMergeRule(MergeRule, boolean)
         */
        public Builder setMergeRules(@NotNull Map<MergeRule, Boolean> mergeRules) {
            this.mergeRules.putAll(mergeRules);
            return this;
        }

        /**
         * Sets merge preservation rule and overwrites the already existing value for the given rule. You can learn more
         * at {@link MergeRule}.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_MERGE_RULES}
         *
         * @param rule             the rule to set
         * @param preserveDocument if to preserve content already in the document instead of the equivalent from
         *                         defaults for this rule
         * @return the builder
         */
        public Builder setMergeRule(@NotNull MergeRule rule, boolean preserveDocument) {
            this.mergeRules.put(rule, preserveDocument);
            return this;
        }

        /**
         * Sets initial ignored routes. <b>Internal method.</b>
         *
         * @param routes the routes to set
         * @return the builder
         */
        private Builder setIgnoredRoutesInternal(@NotNull Map<String, RouteSet> routes) {
            this.ignored.putAll(routes);
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setIgnoredRoutes(@NotNull Map<String, Set<Route>> routes) {
            routes.forEach((versionId, set) -> this.ignored.computeIfAbsent(versionId, key -> new RouteSet()).getRouteSet().addAll(set));
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setIgnoredRoutes(@NotNull String versionId, @NotNull Set<Route> routes) {
            this.ignored.computeIfAbsent(versionId, key -> new RouteSet()).getRouteSet().addAll(routes);
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setIgnoredStringRoutes(@NotNull Map<String, Set<String>> routes) {
            routes.forEach((versionId, set) -> this.ignored.computeIfAbsent(versionId, key -> new RouteSet()).getStringSet().addAll(set));
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setIgnoredStringRoutes(@NotNull String versionId, @NotNull Set<String> routes) {
            this.ignored.computeIfAbsent(versionId, key -> new RouteSet()).getStringSet().addAll(routes);
            return this;
        }

        /**
         * Adds a route to ignore if updating to the provided version ID. Ignored routes and their corresponding {@link
         * dev.dejvokep.boostedyaml.block.Block blocks} and contents will be excluded from merging and will appear in
         * the updated document (no matter the merge rules).
         * <p>
         * <b>Ignoring routes is generally useful for sections which users can freely extend and their content is not
         * strictly defined. Effective if and only a versioning is specified.</b>
         *
         * @param versionId the version ID
         * @param route     the route to ignore
         * @return the builder
         */
        public Builder addIgnoredRoute(@NotNull String versionId, @NotNull Route route) {
            return addIgnoredRoutes(versionId, Collections.singleton(route));
        }

        /**
         * Adds routes to ignore if updating to the provided version ID. Ignored routes and their corresponding {@link
         * dev.dejvokep.boostedyaml.block.Block blocks} and contents will be excluded from merging and will appear in
         * the updated document (no matter the merge rules).
         * <p>
         * <b>Ignoring routes is generally useful for sections which users can freely extend and their content is not
         * strictly defined. Effective if and only a versioning is specified.</b>
         *
         * @param versionId the version ID
         * @param routes    the routes to ignore
         * @return the builder
         */
        public Builder addIgnoredRoutes(@NotNull String versionId, @NotNull Set<Route> routes) {
            return addIgnoredRoutes(Collections.singletonMap(versionId, routes));
        }

        /**
         * Adds routes to ignore if updating to a certain version ID. Ignored routes and their corresponding {@link
         * dev.dejvokep.boostedyaml.block.Block blocks} and contents will be excluded from merging and will appear in
         * the updated document (no matter the merge rules).
         * <p>
         * <b>Ignoring routes is generally useful for sections which users can freely extend and their content is not
         * strictly defined. Effective if and only a versioning is specified.</b>
         *
         * @param routes the routes to ignore, per version ID
         * @return the builder
         */
        public Builder addIgnoredRoutes(@NotNull Map<String, Set<Route>> routes) {
            routes.forEach((versionId, set) -> this.ignored.computeIfAbsent(versionId, key -> new RouteSet()).getRouteSet().addAll(set));
            return this;
        }

        /**
         * Adds a route to ignore if updating to the provided version ID. Ignored routes and their corresponding {@link
         * dev.dejvokep.boostedyaml.block.Block blocks} and contents will be excluded from merging and will appear in
         * the updated document (no matter the merge rules).
         * <p>
         * <b>Ignoring routes is generally useful for sections which users can freely extend and their content is not
         * strictly defined. Effective if and only a versioning is specified.</b>
         *
         * @param versionId the version ID
         * @param route     the route to ignore
         * @param separator separator used to parse the route
         * @return the builder
         */
        public Builder addIgnoredRoute(@NotNull String versionId, @NotNull String route, char separator) {
            return addIgnoredRoutes(versionId, Collections.singleton(route), separator);
        }

        /**
         * Adds routes to ignore if updating to the provided version ID. Ignored routes and their corresponding {@link
         * dev.dejvokep.boostedyaml.block.Block blocks} and contents will be excluded from merging and will appear in
         * the updated document (no matter the merge rules).
         * <p>
         * <b>Ignoring routes is generally useful for sections which users can freely extend and their content is not
         * strictly defined. Effective if and only a versioning is specified.</b>
         *
         * @param versionId the version ID
         * @param routes    the routes to ignore
         * @param separator separator used to parse the routes
         * @return the builder
         */
        public Builder addIgnoredRoutes(@NotNull String versionId, @NotNull Set<String> routes, char separator) {
            addIgnoredRoutes(versionId, routes, new RouteFactory(separator));
            return this;
        }

        /**
         * Adds routes to ignore if updating to a certain version ID. Ignored routes and their corresponding {@link
         * dev.dejvokep.boostedyaml.block.Block blocks} and contents will be excluded from merging and will appear in
         * the updated document (no matter the merge rules).
         * <p>
         * <b>Ignoring routes is generally useful for sections which users can freely extend and their content is not
         * strictly defined. Effective if and only a versioning is specified.</b>
         *
         * @param routes    the routes to ignore, per version ID
         * @param separator separator used to parse the routes
         * @return the builder
         */
        public Builder addIgnoredRoutes(@NotNull Map<String, Set<String>> routes, char separator) {
            RouteFactory factory = new RouteFactory(separator);
            routes.forEach((versionId, collection) -> addIgnoredRoutes(versionId, collection, factory));
            return this;
        }

        /**
         * Adds routes to ignore if updating to the provided version ID. <b>Internal method.</b>
         *
         * @param versionId the version ID
         * @param routes    the routes to ignore
         * @param factory   provider of the separator used to parse the routes
         */
        private void addIgnoredRoutes(@NotNull String versionId, @NotNull Set<String> routes, @NotNull RouteFactory factory) {
            Set<Route> set = this.ignored.computeIfAbsent(versionId, key -> new RouteSet()).getRouteSet();
            routes.forEach(route -> set.add(factory.create(route)));
        }

        /**
         * Sets initial relocations. <b>Internal method.</b>
         *
         * @param relocations the relocations to set
         * @return the builder
         */
        private Builder setRelocationsInternal(@NotNull Map<String, RouteMap<Route, String>> relocations) {
            this.relocations.putAll(relocations);
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setRelocations(@NotNull Map<String, Map<Route, Route>> relocations) {
            relocations.forEach((versionId, map) -> this.relocations.computeIfAbsent(versionId, key -> new RouteMap<>()).getRouteMap().putAll(map));
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setRelocations(@NotNull String versionId, @NotNull Map<Route, Route> relocations) {
            this.relocations.computeIfAbsent(versionId, key -> new RouteMap<>()).getRouteMap().putAll(relocations);
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setStringRelocations(@NotNull Map<String, Map<String, String>> relocations) {
            relocations.forEach((versionId, map) -> this.relocations.computeIfAbsent(versionId, key -> new RouteMap<>()).getStringMap().putAll(map));
            return this;
        }

        /**
         * @deprecated Method with confusing name, use <code>addX()</code> instead of <code>setX()</code>. Subject for
         * removal.
         */
        @Deprecated
        public Builder setStringRelocations(@NotNull String versionId, @NotNull Map<String, String> relocations) {
            this.relocations.computeIfAbsent(versionId, key -> new RouteMap<>()).getStringMap().putAll(relocations);
            return this;
        }

        /**
         * Adds a relocation that took effect at the provided version ID.
         * <p>
         * Relocations represent that some mapping was moved from route <i>x</i> to <i>y</i>, which implies no content
         * loss while updating. The ID at which a relocation took effect is the ID of the document which included the
         * changes. If there is already a relocation defined for the version ID which is from the provided route, it is
         * overwritten.
         * <p>
         * <b>Relocations are useful if you moved something in the document to another place. Effective if and only a
         * versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param fromRoute route from which a mapping was relocated
         * @param toRoute   route to which the mapping was relocated
         * @return the builder
         */
        public Builder addRelocation(@NotNull String versionId, @NotNull Route fromRoute, @NotNull Route toRoute) {
            return addRelocations(versionId, Collections.singletonMap(fromRoute, toRoute));
        }

        /**
         * Adds relocations (in <code>from route = to route</code> format) that took effect at the provided version ID.
         * <p>
         * Relocations represent that some mapping was moved from route <i>x</i> to <i>y</i>, which implies no content
         * loss while updating. The ID at which a relocation took effect is the ID of the document which included the
         * changes. If there is already a relocation defined for the version ID and for a from route contained within
         * the given map, it is overwritten.
         * <p>
         * <b>Relocations are useful if you moved something in the document to another place. Effective if and only a
         * versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId   the version ID
         * @param relocations relocations which took effect at the version ID
         * @return the builder
         */
        public Builder addRelocations(@NotNull String versionId, @NotNull Map<Route, Route> relocations) {
            return addRelocations(Collections.singletonMap(versionId, relocations));
        }

        /**
         * Adds relocations (in <code>from route = to route</code> format) per version ID, at which they took effect.
         * <p>
         * Relocations represent that some mapping was moved from route <i>x</i> to <i>y</i>, which implies no content
         * loss while updating. The ID at which a relocation took effect is the ID of the document which included the
         * changes. If there is already a relocation defined for a version ID and from route contained within the given
         * map, it is overwritten.
         * <p>
         * <b>Relocations are useful if you moved something in the document to another place. Effective if and only a
         * versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param relocations the relocations, per version ID
         * @return the builder
         */
        public Builder addRelocations(@NotNull Map<String, Map<Route, Route>> relocations) {
            relocations.forEach((versionId, map) -> this.relocations.computeIfAbsent(versionId, key -> new RouteMap<>()).getRouteMap().putAll(map));
            return this;
        }

        /**
         * Adds a relocation that took effect at the provided version ID.
         * <p>
         * Relocations represent that some mapping was moved from route <i>x</i> to <i>y</i>, which implies no content
         * loss while updating. The ID at which a relocation took effect is the ID of the document which included the
         * changes. If there is already a relocation defined for the version ID which is from the provided route, it is
         * overwritten.
         * <p>
         * <b>Relocations are useful if you moved something in the document to another place. Effective if and only a
         * versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param fromRoute route from which a mapping was relocated
         * @param toRoute   route to which the mapping was relocated
         * @param separator separator used to parse the routes
         * @return the builder
         */
        public Builder addRelocation(@NotNull String versionId, @NotNull String fromRoute, @NotNull String toRoute, char separator) {
            return addRelocations(versionId, Collections.singletonMap(fromRoute, toRoute), separator);
        }

        /**
         * Adds relocations (in <code>from route = to route</code> format) that took effect at the provided version ID.
         * <p>
         * Relocations represent that some mapping was moved from route <i>x</i> to <i>y</i>, which implies no content
         * loss while updating. The ID at which a relocation took effect is the ID of the document which included the
         * changes. If there is already a relocation defined for the version ID and for a from route contained within
         * the given map, it is overwritten.
         * <p>
         * <b>Relocations are useful if you moved something in the document to another place. Effective if and only a
         * versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId   the version ID
         * @param relocations relocations which took effect at the version ID
         * @param separator   separator used to parse the routes
         * @return the builder
         */
        public Builder addRelocations(@NotNull String versionId, @NotNull Map<String, String> relocations, char separator) {
            addRelocations(Collections.singletonMap(versionId, relocations), separator);
            return this;
        }

        /**
         * Adds relocations (in <code>from route = to route</code> format) per version ID, at which they took effect.
         * <p>
         * Relocations represent that some mapping was moved from route <i>x</i> to <i>y</i>, which implies no content
         * loss while updating. The ID at which a relocation took effect is the ID of the document which included the
         * changes. If there is already a relocation defined for a version ID and from route contained within the given
         * map, it is overwritten.
         * <p>
         * <b>Relocations are useful if you moved something in the document to another place. Effective if and only a
         * versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param relocations the relocations, per version ID
         * @param separator   separator used to parse the routes
         * @return the builder
         */
        public Builder addRelocations(@NotNull Map<String, Map<String, String>> relocations, char separator) {
            RouteFactory factory = new RouteFactory(separator);
            relocations.forEach((versionId, collection) -> {
                Map<Route, Route> map = this.relocations.computeIfAbsent(versionId, key -> new RouteMap<>()).getRouteMap();
                collection.forEach((from, to) -> map.put(factory.create(from), factory.create(to)));
            });
            return this;
        }

        /**
         * Adds a mapper to apply to the given route at the provided version ID.
         * <p>
         * Mappers can be used to map (= transform) a value at a route, to different value or from one type to another
         * (for example from a boolean to an enum). The ID at which to apply a mapper is the ID of the document which
         * included the datatype change. If there is already a mapper defined for the version ID and the provided route,
         * it is overwritten.
         * <p>
         * <b>Mappers are useful if you decided to change a value to represent the same, but using another datatype
         * (for example, you now have a multi-constant enum for which was previously only true/false setting). Effective
         * if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param route     route to apply the mapper at
         * @param mapper    mapper to apply at the version ID
         * @return the builder
         */
        public Builder addMapper(@NotNull String versionId, @NotNull Route route, @NotNull ValueMapper mapper) {
            return addMappers(versionId, Collections.singletonMap(route, mapper));
        }


        /**
         * Adds mappers to apply to their respective routes at the provided version ID.
         * <p>
         * Mappers can be used to map (= transform) a value at a route, to different value or from one type to another
         * (for example from a boolean to an enum). The ID at which to apply a mapper is the ID of the document which
         * included the datatype change. If there is already a mapper defined for the version ID and for a route
         * contained within the given map, it is overwritten.
         * <p>
         * <b>Mappers are useful if you decided to change a value to represent the same, but using another datatype
         * (for example, you now have a multi-constant enum for which was previously only true/false setting). Effective
         * if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param mappers   mappers to apply at the version ID
         * @return the builder
         */
        public Builder addMappers(@NotNull String versionId, @NotNull Map<Route, ValueMapper> mappers) {
            return addMappers(Collections.singletonMap(versionId, mappers));
        }

        /**
         * Adds mappers to apply to their respective routes, per version ID.
         * <p>
         * Mappers can be used to map (= transform) a value at a route, to different value or from one type to another
         * (for example from a boolean to an enum). The ID at which to apply a mapper is the ID of the document which
         * included the datatype change. If there is already a mapper defined for a version ID and route contained
         * within the given map, it is overwritten.
         * <p>
         * <b>Mappers are useful if you decided to change a value to represent the same, but using another datatype
         * (for example, you now have a multi-constant enum for which was previously only true/false setting). Effective
         * if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param mappers the mappers, per version ID
         * @return the builder
         */
        public Builder addMappers(@NotNull Map<String, Map<Route, ValueMapper>> mappers) {
            mappers.forEach((versionId, map) -> this.mappers.computeIfAbsent(versionId, key -> new HashMap<>()).putAll(map));
            return this;
        }

        /**
         * Adds a mapper to apply to the given route at the provided version ID.
         * <p>
         * Mappers can be used to map (= transform) a value at a route, to different value or from one type to another
         * (for example from a boolean to an enum). The ID at which to apply a mapper is the ID of the document which
         * included the datatype change. If there is already a mapper defined for the version ID and the provided route,
         * it is overwritten.
         * <p>
         * <b>Mappers are useful if you decided to change a value to represent the same, but using another datatype
         * (for example, you now have a multi-constant enum for which was previously only true/false setting). Effective
         * if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param route     route to apply the mapper at
         * @param mapper    mapper to apply at the version ID
         * @param separator separator used to parse the routes
         * @return the builder
         */
        public Builder addMapper(@NotNull String versionId, @NotNull String route, @NotNull ValueMapper mapper, char separator) {
            return addMappers(versionId, Collections.singletonMap(route, mapper), separator);
        }

        /**
         * Adds mappers to apply to their respective routes at the provided version ID.
         * <p>
         * Mappers can be used to map (= transform) a value at a route, to different value or from one type to another
         * (for example from a boolean to an enum). The ID at which to apply a mapper is the ID of the document which
         * included the datatype change. If there is already a mapper defined for the version ID and for a route
         * contained within the given map, it is overwritten.
         * <p>
         * <b>Mappers are useful if you decided to change a value to represent the same, but using another datatype
         * (for example, you now have a multi-constant enum for which was previously only true/false setting). Effective
         * if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param mappers   mappers to apply at the version ID
         * @param separator separator used to parse the routes
         * @return the builder
         */
        public Builder addMappers(@NotNull String versionId, @NotNull Map<String, ValueMapper> mappers, char separator) {
            return addMappers(Collections.singletonMap(versionId, mappers), separator);
        }

        /**
         * Adds mappers to apply to their respective routes, per version ID.
         * <p>
         * Mappers can be used to map (= transform) a value at a route, to different value or from one type to another
         * (for example from a boolean to an enum). The ID at which to apply a mapper is the ID of the document which
         * included the datatype change. If there is already a mapper defined for a version ID and route contained
         * within the given map, it is overwritten.
         * <p>
         * <b>Mappers are useful if you decided to change a value to represent the same, but using another datatype
         * (for example, you now have a multi-constant enum for which was previously only true/false setting). Effective
         * if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param mappers   the mappers, per version ID
         * @param separator separator used to parse the routes
         * @return the builder
         */
        public Builder addMappers(@NotNull Map<String, Map<String, ValueMapper>> mappers, char separator) {
            RouteFactory factory = new RouteFactory(separator);
            mappers.forEach((versionId, collection) -> {
                Map<Route, ValueMapper> map = this.mappers.computeIfAbsent(versionId, key -> new HashMap<>());
                collection.forEach((route, mapper) -> map.put(factory.create(route), mapper));
            });
            return this;
        }

        /**
         * Adds custom logic to run on the document at the provided version ID.
         * <p>
         * You can use your own logic to make changes to the document which are not available via the ignored routes,
         * relocations or mappers. The order in which the consumers are run is undefined and may vary.
         * <p>
         * <b>Effective if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param consumer  consumer to run
         * @return the builder
         */
        public Builder addCustomLogic(@NotNull String versionId, @NotNull Consumer<YamlDocument> consumer) {
            return addCustomLogic(versionId, Collections.singletonList(consumer));
        }

        /**
         * Adds custom logic to run on the document, per version ID.
         * <p>
         * You can use your own logic to make changes to the document which are not available via the ignored routes,
         * relocations or mappers. The order in which the consumers are run is undefined and may vary.
         * <p>
         * <b>Effective if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param consumers consumers to run, per version ID
         * @return the builder
         */
        public Builder addCustomLogic(@NotNull Map<String, List<Consumer<YamlDocument>>> consumers) {
            consumers.forEach(this::addCustomLogic);
            return this;
        }

        /**
         * Adds custom logic to run on the document at the provided version ID.
         * <p>
         * You can use your own logic to make changes to the document which are not available via the ignored routes,
         * relocations or mappers. The order in which the consumers are run is undefined and may vary.
         * <p>
         * <b>Effective if and only a versioning is specified.</b>
         * <p>
         * <i>Updating cycle for each version ID in the corresponding range (first to last): relocations -&gt; mappers -&gt;
         * custom logic.</i>
         *
         * @param versionId the version ID
         * @param consumers consumers to run at the version ID
         * @return the builder
         */
        public Builder addCustomLogic(@NotNull String versionId, @NotNull Collection<Consumer<YamlDocument>> consumers) {
            customLogic.computeIfAbsent(versionId, key -> new ArrayList<>()).addAll(consumers);
            return this;
        }

        /**
         * Sets versioning information.
         * <p>
         * If the version ID of the defaults supplied by the versioning is <code>null</code>, it is considered illegal
         * and will throw a {@link NullPointerException}. If such value is returned for the document itself, the updater
         * will assign {@link Versioning#getFirstVersion()} to it. Please see the implementation documentation.
         *
         * @param versioning the versioning
         * @return the builder
         * @see #setVersioning(Pattern, String, String)
         * @see #setVersioning(Pattern, Route)
         */
        public Builder setVersioning(@NotNull Versioning versioning) {
            this.versioning = versioning;
            return this;
        }

        /**
         * Sets versioning information. Please see {@link ManualVersioning#ManualVersioning(Pattern, String, String)}.
         *
         * @param pattern           the pattern
         * @param documentVersionId version ID of the document that's being updated
         * @param defaultsVersionId version ID of the defaults
         * @return the builder
         * @see #setVersioning(Versioning)
         */
        public Builder setVersioning(@NotNull Pattern pattern, @Nullable String documentVersionId, @NotNull String defaultsVersionId) {
            return setVersioning(new ManualVersioning(pattern, documentVersionId, defaultsVersionId));
        }

        /**
         * Sets versioning information. Please see {@link AutomaticVersioning#AutomaticVersioning(Pattern, Route)}.
         *
         * @param pattern the pattern
         * @param route   the route to version IDs (of both files)
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull Route route) {
            return setVersioning(new AutomaticVersioning(pattern, route));
        }

        /**
         * Sets versioning information. Please see {@link AutomaticVersioning#AutomaticVersioning(Pattern, String)}.
         *
         * @param pattern the pattern
         * @param route   the route to version IDs (of both files)
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull String route) {
            return setVersioning(new AutomaticVersioning(pattern, route));
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public UpdaterSettings build() {
            return new UpdaterSettings(this);
        }
    }

    /**
     * A collection container which represents data mapped to a route. Each instance keeps two {@link Map maps}, one of
     * which has {@link Route} and the other one {@link String} (routes) as the key type. Value type for each of the
     * respective maps are defined by {@link R} and {@link S} parameters.
     * <p>
     * Creating an instance of the class does not introduce any additional overhead, because the contained collections
     * are initially <code>null</code> and are initialized only on call to {@link #getRouteMap()} or {@link
     * #getStringMap()} (still only the corresponding one).
     *
     * @param <R> value type of the {@link Route}-keyed {@link Map map} (<code>Map&lt;Route, R&gt;</code>)
     * @param <S> value type of the {@link String}-keyed {@link Map map} (<code>Map&lt;String, S&gt;</code>)
     */
    private static class RouteMap<R, S> {

        // Maps
        private Map<Route, R> routes = null;
        private Map<String, S> strings = null;

        /**
         * Merges the contained maps into one single map with customizable value type.
         * <p>
         * String routes are converted to {@link Route routes} using the provided separator. The {@link #getRouteMap()}
         * has higher priority during merging, that means any key duplicates present in {@link #getStringMap()} will be
         * discarded.
         * <p>
         * <b>The returned object might (but not necessarily) be immutable.</b>
         *
         * @param routeMapper  mapping function applied to {@link R} values (those from {@link #getRouteMap()})
         * @param stringMapper mapping function applied to {@link S} values (those from {@link #getStringMap()})
         * @param separator    route key separator (see {@link Route#fromString(String, char)})
         * @param <T>          value type of the returned map
         * @return the merged map
         */
        @NotNull
        public <T> Map<Route, T> merge(@NotNull Function<R, T> routeMapper, @NotNull Function<S, T> stringMapper, char separator) {
            if ((routes == null || routes.isEmpty()) && (strings == null || strings.isEmpty()))
                return Collections.emptyMap();
            Map<Route, T> map = new HashMap<>();
            if (strings != null)
                strings.forEach((key, value) -> map.put(Route.fromString(key, separator), stringMapper.apply(value)));
            if (routes != null)
                routes.forEach((key, value) -> map.put(key, routeMapper.apply(value)));
            return map;
        }

        /**
         * Returns the {@link Route}-keyed map.
         *
         * @return the {@link Route}-keyed map
         */
        @NotNull
        public Map<Route, R> getRouteMap() {
            return routes == null ? routes = new HashMap<>() : routes;
        }

        /**
         * Returns the {@link String}-keyed map.
         *
         * @return the {@link String}-keyed map
         */
        @NotNull
        public Map<String, S> getStringMap() {
            return strings == null ? strings = new HashMap<>() : strings;
        }
    }

    /**
     * A collection container which represents a {@link Set set} of routes. Each instance keeps two {@link Set sets},
     * one of which has {@link Route} and the other one {@link String} (routes) as the type
     * (<code>Set&lt;Route&gt;</code> and <code>Set&lt;String&gt;</code>, respectively).
     * <p>
     * Creating an instance of the class does not introduce any additional overhead, because the contained collections
     * are initially <code>null</code> and are initialized only on call to {@link #getRouteSet()} or {@link
     * #getStringSet()} (still only the corresponding one).
     */
    private static class RouteSet {

        // Sets
        private Set<Route> routes = null;
        private Set<String> strings = null;

        /**
         * Merges the contained sets into one single set.
         * <p>
         * String routes are converted to {@link Route routes} using the provided separator. <b>The returned object
         * might (but not necessarily) be immutable.</b>
         *
         * @param separator route key separator (see {@link Route#fromString(String, char)})
         * @return the merged set
         */
        public Set<Route> merge(char separator) {
            if ((routes == null || routes.isEmpty()) && (strings == null || strings.isEmpty()))
                return Collections.emptySet();
            Set<Route> set = new HashSet<>();
            if (strings != null)
                strings.forEach(route -> set.add(Route.fromString(route, separator)));
            if (routes != null)
                set.addAll(routes);
            return set;
        }

        /**
         * Returns the set of {@link Route} objects.
         *
         * @return the set of {@link Route} objects
         */
        public Set<Route> getRouteSet() {
            return routes == null ? routes = new HashSet<>() : routes;
        }

        /**
         * Returns the set of {@link String} objects.
         *
         * @return the set of {@link String} objects
         */
        public Set<String> getStringSet() {
            return strings == null ? strings = new HashSet<>() : strings;
        }

    }
}