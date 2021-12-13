package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.path.PathFactory;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.AutomaticVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.ManualVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.Versioning;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Updater settings cover all options related explicitly (only) to file updating.
 * <p>
 * To start using this library, it is recommended to take a look at the following methods:
 * <ul>
 *     <li>{@link Builder#setAutoSave(boolean)}</li>
 *     <li>{@link Builder#setVersioning(Versioning)}</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class UpdaterSettings {

    /**
     * If to, by default, automatically save the file after updating.
     */
    public static final boolean DEFAULT_AUTO_SAVE = true;
    /**
     * If to enable file downgrading by default.
     */
    public static final boolean DEFAULT_ENABLE_DOWNGRADING = true;
    /**
     * If to keep all contents of the user file not contained in the default file by default.
     */
    public static final boolean DEFAULT_KEEP_ALL = false;
    /**
     * Default merge preservation rules.
     */
    public static final Map<MergeRule, Boolean> DEFAULT_MERGE_RULES = new HashMap<MergeRule, Boolean>() {{
        put(MergeRule.MAPPINGS, true);
        put(MergeRule.MAPPING_AT_SECTION, false);
        put(MergeRule.SECTION_AT_MAPPING, false);
    }};
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
    //Keep all user file contents
    private final boolean keepAll;
    //Merge rules
    private final Map<MergeRule, Boolean> mergeRules;
    //Paths to keep
    private final Map<String, Set<Path>> keep;
    private final Map<String, Set<String>> stringKeep;
    //Relocations
    private final Map<String, Map<Path, Path>> relocations;
    private final Map<String, Map<String, String>> stringRelocations;
    //Versioning
    private final Versioning versioning;

    /**
     * Creates final, immutable updater settings from the given builder.
     *
     * @param builder the builder
     */
    public UpdaterSettings(Builder builder) {
        this.autoSave = builder.autoSave;
        this.enableDowngrading = builder.enableDowngrading;
        this.keepAll = builder.keepAll;
        this.mergeRules = builder.mergeRules;
        this.keep = builder.keep;
        this.stringKeep = builder.stringKeep;
        this.relocations = builder.relocations;
        this.stringRelocations = builder.stringRelocations;
        this.versioning = builder.versioning;
    }

    /**
     * Returns merge preservation rules.
     * <p>
     * The given map contains the merge rule as the key, with value representing if to preserve content from
     * the user file instead of default.
     *
     * @return the merge rules
     */
    public Map<MergeRule, Boolean> getMergeRules() {
        return mergeRules;
    }

    /**
     * Returns which blocks (represented by their paths) to keep in the user file (will not be deleted); if updating
     * from that certain version ID (if the user's file has that version ID). Merges the string-based paths with path
     * objects.
     * <p>
     * Note that this applies to blocks which were not merged (e.g. they don't have equivalent block in the defaults).
     * <p>
     * The given map contains version ID (in string format) as the key, with corresponding set of paths to keep
     * as value. It, naturally, is not required and not guaranteed, that all version IDs between version ID of the user
     * and default file, must have their paths specified.
     *
     * @param separator separator to split string based paths by
     * @return paths representing blocks to keep, per version ID
     */
    public Map<String, Set<Path>> getKeep(char separator) {
        //If string relocations are defined
        if (stringKeep.size() > 0) {
            //Create factory
            PathFactory factory = new PathFactory(separator);

            //All entries
            for (Map.Entry<String, Set<String>> entry : stringKeep.entrySet()) {
                //The set
                Set<Path> paths = keep.computeIfAbsent(entry.getKey(), (key) -> new HashSet<>());
                //Add all
                for (String path : entry.getValue())
                    paths.add(factory.create(path));
            }
        }

        //Return
        return keep;
    }

    /**
     * Returns relocations (in <code>from path = to path</code> format) per version ID string. Merges the string-based
     * relocations.
     *
     * @param separator separator to split string based relocation paths by
     * @return the relocations
     */
    public Map<String, Map<Path, Path>> getRelocations(char separator) {
        //If string relocations are defined
        if (stringRelocations.size() > 0) {
            //Create factory
            PathFactory factory = new PathFactory(separator);

            //All entries
            for (Map.Entry<String, Map<String, String>> entry : stringRelocations.entrySet()) {
                //The map
                Map<Path, Path> relocations = this.relocations.computeIfAbsent(entry.getKey(), (key) -> new HashMap<>());
                //Add all
                for (Map.Entry<String, String> relocation : entry.getValue().entrySet()) {
                    // From path
                    Path from = factory.create(relocation.getKey());
                    // If not already present
                    if (!relocations.containsKey(from))
                        relocations.put(from, factory.create(relocation.getValue()));
                }
            }
        }

        return relocations;
    }

    /**
     * Returns versioning information.
     *
     * @return the versioning
     */
    public Versioning getVersioning() {
        return versioning;
    }

    /**
     * Returns if to enable downgrading.
     * <p>
     * Effective if and only the version ID of the user file represents newer file version than default file's version
     * ID.
     * <p>
     * In this case, if this option is set to <code>true</code>, skips version-dependent operations (relocations)
     * directly to merging. Throws an error otherwise.
     *
     * @return if to enable downgrading
     */
    public boolean isEnableDowngrading() {
        return enableDowngrading;
    }

    /**
     * Returns if to keep all non-merged contents of the user file.
     *
     * @return if to keep all non-merged user file contents
     */
    public boolean isKeepAll() {
        return keepAll;
    }

    /**
     * Returns if the file should automatically be saved after finished updating (does not require successful update).
     *
     * @return if to save automatically after update
     */
    public boolean isAutoSave() {
        return autoSave;
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
     * Builder for updater settings.
     */
    public static class Builder {

        //Save automatically
        private boolean autoSave = DEFAULT_AUTO_SAVE;
        //Enable downgrading
        private boolean enableDowngrading = DEFAULT_ENABLE_DOWNGRADING;
        //Keep all user file contents
        private boolean keepAll = DEFAULT_KEEP_ALL;
        //Merge rules
        private final Map<MergeRule, Boolean> mergeRules = new HashMap<>(DEFAULT_MERGE_RULES);
        //Paths to keep
        private final Map<String, Set<Path>> keep = new HashMap<>();
        private final Map<String, Set<String>> stringKeep = new HashMap<>();
        //Relocations
        private final Map<String, Map<Path, Path>> relocations = new HashMap<>();
        private final Map<String, Map<String, String>> stringRelocations = new HashMap<>();
        //Versioning
        private Versioning versioning = DEFAULT_VERSIONING;

        /**
         * Creates a new builder will all the default settings applied.
         */
        private Builder() {
        }

        /**
         * Sets if the file should automatically be saved after the updater has finished updating (does not require successful update) using {@link YamlFile#save()}.
         * <p>
         * Not effective if there is no user file associated with the YamlFile that's being loaded.
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
         *     <li>the version ID of the user file represents newer version than default file's version ID.</li>
         * </ul>
         * Please note that by specification, the default file has to have a valid ID supplied/specified.
         * <p>
         * That means, if no versioning is supplied, if the version ID of the user file was not found (automatic FVS) or
         * not supplied (manual FVS), or the ID of the user file is not parsable by the given pattern, this method is not effective.
         * <p>
         * If enabled and the updater detects downgrading, the updater will skip keep paths and relocations, proceeding directly to merging. Throws an error otherwise (if disabled).
         * <p>
         * If disabled, throws an error if downgrading. If configured like so, you may also want to disable
         * {@link LoaderSettings.Builder#setAutoUpdate(boolean)} (if an error is thrown, you won't be able to initialize the file - update manually).
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
         * Sets if to keep all non-merged (they don't have equivalent in the default file) blocks of the user file.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_KEEP_ALL}
         *
         * @param keepAll if to keep all user file blocks
         * @return the builder
         */
        public Builder setKeepAll(boolean keepAll) {
            this.keepAll = keepAll;
            return this;
        }

        /**
         * Sets merge preservation rules. Overwrites only rules that are defined in the given map. You can learn more
         * at {@link MergeRule}.
         * <p>
         * The given map should contain the merge rule as the key, with value representing if to preserve content from
         * the user file instead of default.
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
         * @param rule         the rule to set
         * @param preserveUser if to preserve contents from the user file instead of default contents when the given rule is met
         * @return the builder
         */
        public Builder setMergeRule(@NotNull MergeRule rule, boolean preserveUser) {
            this.mergeRules.put(rule, preserveUser);
            return this;
        }

        /**
         * Sets which blocks (represented by their paths) to keep in the user file (will not be deleted); if updating
         * from that certain version ID (if the user's file has that version ID). You can learn more at
         * {@link #setKeep(String, Set)} or {wiki}. If there already are paths defined for version ID, which is also
         * present in the given map, they are overwritten.
         * <p>
         * Note that this applies to blocks which had not been merged (e.g. they don't have equivalent block in the defaults).
         * <p>
         * The given map should contain version ID (in string format) as the key, with corresponding set of paths to keep
         * as value. It, naturally, is not required and does not need to be guaranteed, that all version IDs between
         * version ID of the user and default file, must have their paths specified.
         * <p>
         * <b>Default: </b><i>none</i>
         *
         * @param paths paths to set, per version ID
         * @return the builder
         * @see #setKeep(String, Set)
         */
        public Builder setKeep(@NotNull Map<String, Set<Path>> paths) {
            this.keep.putAll(paths);
            return this;
        }

        /**
         * Sets which blocks (represented by their paths) to keep in the user file (will not be deleted); if user file
         * that's being updated has the given version ID. If there already are paths defined for the given ID, they are
         * overwritten.
         * <p>
         * Note that this applies to blocks which had not been merged (e.g. they don't have equivalent block in the
         * defaults). For examples and in-depth explanation, please visit {wiki}.
         * <p>
         * It, naturally, is not required and does not need to be guaranteed, that all version IDs between version ID of
         * the user and default file, must have their paths specified.
         * <p>
         * <b>Default: </b><i>none</i>
         *
         * @param versionId the version ID string to set paths for
         * @param paths     the set of paths representing blocks to keep
         * @return the builder
         */
        public Builder setKeep(@NotNull String versionId, @NotNull Set<Path> paths) {
            this.keep.put(versionId, paths);
            return this;
        }

        /**
         * Sets which blocks (represented by their <i>string</i> paths) to keep in the user file (will not be deleted); if updating
         * from that certain version ID (if the user's file has that version ID). You can learn more at
         * {@link #setStrKeep(String, Set)} or {wiki}. If there already are paths defined for version ID, which is also
         * present in the given map, they are overwritten.
         * <p>
         * Note that this applies to blocks which had not been merged (e.g. they don't have equivalent block in the defaults).
         * <p>
         * The given map should contain version ID (in string format) as the key, with corresponding set of paths to keep
         * as value. It, naturally, is not required and does not need to be guaranteed, that all version IDs between
         * version ID of the user and default file, must have their paths specified.
         * <p>
         * <b>Please note</b> that, as the documentation above suggests, string paths supplied via this and
         * {@link #setStrKeep(String, Set)} method are cached differently from paths supplied via
         * {@link Path}-based methods (e.g. {@link #setKeep(Map)}) and will not overwrite each other.
         * <p>
         * String path-based keep paths are stored till the updating process, where they are converted to
         * {@link Path}-based ones and merged with the ones given via other methods. <b>{@link Path}-based relocations
         * have higher priority.</b>
         * <p>
         * <b>Default: </b><i>none</i>
         *
         * @param paths <i>string</i> paths to set, per version ID
         * @return the builder
         * @see #setStrKeep(String, Set)
         */
        public Builder setStrKeep(@NotNull Map<String, Set<String>> paths) {
            this.stringKeep.putAll(paths);
            return this;
        }

        /**
         * Sets which blocks (represented by their <i>string</i> paths) to keep in the user file (will not be deleted); if user file
         * that's being updated has the given version ID. If there already are paths defined for the given ID, they are
         * overwritten.
         * <p>
         * Note that this applies to blocks which had not been merged (e.g. they don't have equivalent block in the
         * defaults). For examples and in-depth explanation, please visit {wiki}.
         * <p>
         * It, naturally, is not required and does not need to be guaranteed, that all version IDs between version ID of
         * the user and default file, must have their paths specified.
         * <p>
         * <b>Please note</b> that, as the documentation above suggests, string paths supplied via this and
         * {@link #setStrKeep(Map)} method are cached differently from paths supplied via
         * {@link Path}-based methods (e.g. {@link #setKeep(Map)}) and will not overwrite each other.
         * <p>
         * String path-based keep paths are stored till the updating process, where they are converted to
         * {@link Path}-based ones and merged with the ones given via other methods. <b>{@link Path}-based relocations
         * have higher priority.</b>
         * <p>
         * <b>Default: </b><i>none</i>
         *
         * @param versionId the version ID string to set paths for
         * @param paths     the set of <i>string</i> paths representing blocks to keep
         * @return the builder
         */
        public Builder setStrKeep(@NotNull String versionId, @NotNull Set<String> paths) {
            this.stringKeep.put(versionId, paths);
            return this;
        }

        /**
         * Sets relocations (in <code>from path = to path</code> format) per version ID string. You can learn more at
         * {@link #setRelocations(String, Map)} or {wiki}. If there already are relocations defined for version ID which
         * is also present in the given map, they are overwritten.
         *
         * @param relocations the relocations to add
         * @return the builder
         * @see #setStrRelocations(String, Map)
         */
        public Builder setRelocations(@NotNull Map<String, Map<Path, Path>> relocations) {
            this.relocations.putAll(relocations);
            return this;
        }

        /**
         * Sets relocations for the given version ID string. If there already are relocations defined for the version ID,
         * they are overwritten.
         * <p>
         * The given version ID represents version, at which the relocations were made - at which they took effect. That
         * means, if certain setting was at path <code>a</code> in config with version ID <code>2</code>, but you
         * decided you want to move that setting to path <code>b</code> (and then released with version <code>3</code>
         * or whatever), the relocation is considered to be made at version ID <code>2</code>.
         *
         * @param versionId   the version ID to set relocations for
         * @param relocations relocations to set
         * @return the builder
         */
        public Builder setRelocations(@NotNull String versionId, @NotNull Map<Path, Path> relocations) {
            this.relocations.put(versionId, relocations);
            return this;
        }

        /**
         * Sets relocations (in <code>from path = to path</code> format) per version ID. You can learn more at
         * {@link #setStrRelocations(String, Map)} or {wiki}. If there already are string-based relocations defined for
         * version ID which is also present in the given map, they are overwritten.
         * <p>
         * <b>Please note</b> that, as the documentation above suggests, string paths supplied via this and
         * {@link #setStrRelocations(String, Map)} method are cached differently from paths supplied via
         * {@link Path}-based methods (e.g. {@link #setRelocations(Map)}) and will not overwrite each other.
         * <p>
         * String path-based relocations are stored till the updating process, where they are converted to
         * {@link Path}-based ones and merged with the ones given via other methods. <b>{@link Path}-based relocations
         * have higher priority.</b>
         *
         * @param relocations the relocations to add
         * @return the builder
         * @see #setStrRelocations(String, Map)
         */
        public Builder setStrRelocations(@NotNull Map<String, Map<String, String>> relocations) {
            this.stringRelocations.putAll(relocations);
            return this;
        }

        /**
         * Sets relocations (in <code>from path = to path</code> format) per version ID. If there already are
         * string-based relocations defined for version ID which is also present in the given map, they are overwritten.
         * <p>
         * The given version ID represents version, at which the relocations were made - at which they took effect. That
         * means, if certain setting was at path <code>a</code> in config with version ID <code>2</code>, but you
         * decided you want to move that setting to path <code>b</code> (and then released with version <code>3</code>
         * or whatever), the relocation is considered to be made at version ID <code>2</code>.
         * <p>
         * <b>Please note</b> that, as the documentation above suggests, string paths supplied via this and
         * {@link #setStrRelocations(Map)} method are cached differently from paths supplied via
         * {@link Path}-based methods (e.g. {@link #setRelocations(Map)}) and will not overwrite each other.
         * <p>
         * String path-based relocations are stored till the updating process, where they are converted to
         * {@link Path}-based ones and merged with the ones given via other methods. <b>{@link Path}-based relocations
         * have higher priority.</b>
         *
         * @param versionId   the version ID to set relocations for
         * @param relocations relocations to set
         * @return the builder
         */
        public Builder setStrRelocations(@NotNull String versionId, @NotNull Map<String, String> relocations) {
            this.stringRelocations.put(versionId, relocations);
            return this;
        }

        /**
         * Sets versioning information. An {@link IllegalArgumentException}
         * might be thrown during updating process in certain cases (always make sure to read the documentation of
         * the object you are giving).
         *
         * @param versioning the versioning
         * @return the builder
         * @see #setVersioning(Pattern, String, String)
         * @see #setVersioning(Pattern, Path)
         */
        public Builder setVersioning(@NotNull Versioning versioning) {
            this.versioning = versioning;
            return this;
        }

        /**
         * Sets versioning information manually. The given string version IDs must follow the given pattern.
         * <p>
         * If the user file version ID is <code>null</code> (e.g. user file was created before your plugin started using
         * this library/updater), keep paths are not effective, and it's version will be treated like the oldest
         * one specified by the given pattern (which effectively means all relocations given will be applied to it).
         * <p>
         * If any of the version IDs do not follow the given pattern (cannot be parsed), an
         * {@link IllegalArgumentException} will be thrown now. Please read the documentation of {@link ManualVersioning}.
         * <p>
         * <i>You may want to disable {@link LoaderSettings.Builder#setAutoUpdate(boolean)}
         * and rather update manually by calling {@link YamlFile#update()} (because if an error is thrown, you won't be able to initialize the file).</i>
         *
         * @param pattern              the pattern
         * @param userFileVersionId    version ID of the user file
         * @param defaultFileVersionId version ID of the default file
         * @return the builder
         * @see #setVersioning(Versioning)
         */
        public Builder setVersioning(@NotNull Pattern pattern, @Nullable String userFileVersionId, @NotNull String defaultFileVersionId) {
            return setVersioning(new ManualVersioning(pattern, userFileVersionId, defaultFileVersionId));
        }

        /**
         * Sets versioning information to be obtained automatically (directly from the user and default file).
         * <p>
         * It must be guaranteed that version ID of the default file is present at the path and is valid (following the
         * pattern), an {@link IllegalArgumentException} will be thrown during the updating process otherwise. If no
         * version ID is found in the user file at the path, or is invalid, the updater will treat the user file version
         * ID as the oldest specified by the given pattern.
         * <p>
         * Please read the documentation of {@link AutomaticVersioning}.
         *
         * @param pattern the pattern
         * @param path    the path to version IDs (of both files) in both files
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull Path path) {
            return setVersioning(new AutomaticVersioning(pattern, path));
        }

        /**
         * Sets versioning information to be obtained automatically (directly from the user and default file).
         * <p>
         * It must be guaranteed that version ID of the default file is present at the path and is valid (following the
         * pattern), an {@link IllegalArgumentException} will be thrown during the updating process otherwise. If no
         * version ID is found in the user file at the path, or is invalid, the updater will treat the user file version
         * ID as the oldest specified by the given pattern.
         * <p>
         * Please read the documentation of {@link AutomaticVersioning}.
         *
         * @param pattern the pattern
         * @param path    the path to version IDs (of both files) in both files
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull String path) {
            return setVersioning(new AutomaticVersioning(pattern, path));
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
}