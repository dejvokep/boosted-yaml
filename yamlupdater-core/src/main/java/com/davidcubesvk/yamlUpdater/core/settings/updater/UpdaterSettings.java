package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.AutomaticVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.ManualVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.Versioning;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Updater settings; immutable object.
 */
@SuppressWarnings("unused")
public class UpdaterSettings {

    /**
     * Default updater settings.
     */
    public static final UpdaterSettings DEFAULT = builder().build();

    /**
     * If to, by default, automatically save the file after updating.
     */
    public static final boolean DEFAULT_AUTO_SAVE = true;
    /**
     * If to enable file downgrading by default.
     */
    public static final boolean DEFAULT_ENABLE_DOWNGRADING = true;
    /**
     * If to force copy all contents from the user file by default.
     */
    public static final boolean DEFAULT_FORCE_COPY_ALL = false;
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

    //Save automatically
    private final boolean autoSave;
    //Enable downgrading
    private final boolean enableDowngrading;
    //Force copy all user file contents
    private final boolean forceCopyAll;
    //Merge rules
    private final Map<MergeRule, Boolean> mergeRules;
    //Paths to force copy
    private final Map<String, Set<Path>> forceCopy;
    //Relocations
    private final Map<String, Map<Path, Path>> relocations;
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
        this.forceCopyAll = builder.forceCopyAll;
        this.mergeRules = builder.mergeRules;
        this.forceCopy = builder.forceCopy;
        this.relocations = builder.relocations;
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
     * Returns which blocks (represented by their paths) to force copy to the updated file (regardless if contained
     * in the default file), if updating from that certain version ID (if the user's file has that version ID).
     * <p>
     * The given map contains version ID (in string format) as the key, with corresponding set of paths to copy
     * as value. It is not required and does not need to be guaranteed, that all version IDs between version ID
     * of the user and default file, must have their force copy paths specified.
     *
     * @return force copy paths, per version ID
     */
    public Map<String, Set<Path>> getForceCopy() {
        return forceCopy;
    }

    /**
     * Returns relocations (in <code>from path = to path</code> format) per version ID string.
     *
     * @return the relocations
     */
    public Map<String, Map<Path, Path>> getRelocations() {
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
     * Returns if to force copy all contents of the user file, not only those contained in the default (newest) file.
     *
     * @return if to force copy all user file contents
     */
    public boolean isForceCopyAll() {
        return forceCopyAll;
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
        //Force copy all user file contents
        private boolean forceCopyAll = DEFAULT_FORCE_COPY_ALL;
        //Merge rules
        private final Map<MergeRule, Boolean> mergeRules = new HashMap<>(DEFAULT_MERGE_RULES);
        //Paths to force copy
        private final Map<String, Set<Path>> forceCopy = new HashMap<>();
        //Relocations
        private final Map<String, Map<Path, Path>> relocations = new HashMap<>();
        //Versioning
        private Versioning versioning = DEFAULT_VERSIONING;

        /**
         * Creates a new builder will all the default settings applied.
         */
        private Builder() {
        }

        /**
         * Sets if the file should automatically be saved after finished updating (does not require successful update).
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
         * Effective if and only there are version IDs found for both files (supplied via
         * {@link #setVersioning(Pattern, String, String)} or automatically from files via
         * {@link #setVersioning(Pattern, Path)}) and ID of the user file represents newer
         * file version than default file's version ID.
         * <p>
         * In this case, if this option is set to <code>true</code>, skips version-dependent operations (relocations)
         * directly to merging. Throws an error otherwise.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_ENABLE_DOWNGRADING}
         * <p>
         * <i>If disabled, you may want to disable {@link LoaderSettings.Builder#setAutoUpdate(boolean)}
         * and rather update manually by calling {@link YamlFile#update()} (because if an error is thrown, you won't be able to initialize the file).</i>
         *
         * @param enableDowngrading if to enable downgrading
         * @return the builder
         */
        public Builder setEnableDowngrading(boolean enableDowngrading) {
            this.enableDowngrading = enableDowngrading;
            return this;
        }

        /**
         * Sets if to force copy all contents of the user file, not only those contained in the default (newest) file.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_FORCE_COPY_ALL}
         *
         * @param forceCopyAll if to force copy all user file contents
         * @return the builder
         */
        public Builder setForceCopyAll(boolean forceCopyAll) {
            this.forceCopyAll = forceCopyAll;
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
        public Builder setMergeRules(Map<MergeRule, Boolean> mergeRules) {
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
        public Builder setMergeRule(MergeRule rule, boolean preserveUser) {
            this.mergeRules.put(rule, preserveUser);
            return this;
        }

        /**
         * Sets which blocks (represented by their paths) to force copy to the updated file (regardless if contained
         * in the default file), if updating from that certain version ID (if the user's file has that version ID). You
         * can learn more at {@link #setForceCopy(String, Set)} or {wiki}. If there already are paths defined for version
         * ID, which is also present in the given map, they are overwritten.
         * <p>
         * The given map should contain version ID (in string format) as the key, with corresponding set of paths to copy
         * as value. It is not required and does not need to be guaranteed, that all version IDs between version ID
         * of the user and default file, must have their force copy paths specified.
         * <p>
         * <b>Default: </b><i>none</i>
         *
         * @param forceCopy force copy paths to set, per version ID
         * @return the builder
         * @see #setForceCopy(String, Set)
         */
        public Builder setForceCopy(Map<String, Set<Path>> forceCopy) {
            this.forceCopy.putAll(forceCopy);
            return this;
        }

        /**
         * Sets which blocks (represented by their paths) to force copy from the user file (being updated) to the updated
         * file (regardless if contained in the default file), if user file that's being updated has the given version ID.
         * If there already are paths defined for the given version ID, they are overwritten.
         * <p>
         * A block can either represent a section, or a mapping (section entry); while storing corresponding comments,
         * as written in the file. Blocks are copied, that means their contents including comments are also copied.
         * Please learn more about blocks at {@link Block} and {wiki}.
         * <p>
         * At the start of each updating process, set of paths representing blocks which to copy is obtained using the
         * user file's version ID (if available, see {@link #setVersioning(Pattern, Path)}) from the force copy map
         * {@link #getForceCopy()}. Then, each block in the user file, whose path is contained in the set, is marked
         * to be copied (via {@link Block#setForceCopy(boolean)}).
         * <p>
         * At the end, during merging, all blocks which have this option enabled and do not exist in the default file
         * (those would have already been merged), will be copied and included in the updated file.
         * <p>
         * For examples and in-depth explanation, please visit {wiki}. It is not required and does not need to be
         * guaranteed, that all version IDs between version ID of the user and default file, must have their force copy
         * paths specified.
         * <p>
         * <b>Default: </b><i>none</i>
         *
         * @param versionId the version ID string to set paths for
         * @param paths     the set of paths representing blocks to force copy
         * @return the builder
         */
        public Builder setForceCopy(String versionId, Set<Path> paths) {
            this.forceCopy.put(versionId, paths);
            return this;
        }

        /**
         * Sets relocations (in <code>from path = to path</code> format) per version ID string. You can learn more at
         * {@link #setRelocations(String, Map)} or {wiki}. If there already are relocations defined for version ID which
         * is also present in the given map, they are overwritten.
         *
         * @param relocations the relocations to add
         * @return the builder
         * @see #setRelocations(String, Map)
         */
        public Builder setRelocations(Map<String, Map<Path, Path>> relocations) {
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
        public Builder setRelocations(String versionId, Map<Path, Path> relocations) {
            this.relocations.put(versionId, relocations);
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
        public Builder setVersioning(Versioning versioning) {
            this.versioning = versioning;
            return this;
        }

        /**
         * Sets versioning information manually. The given string version IDs must follow the given pattern.
         * <p>
         * If the user file version ID is <code>null</code> (e.g. user file was created before your plugin started using
         * this library/updater), force copy paths are not effective and it's version will be treated like the oldest
         * one specified by the given pattern (which effectively means all relocations given will be applied to it).
         * <p>
         * If any of the version IDs do not follow the given pattern (cannot be parsed), an
         * {@link IllegalArgumentException} will be thrown during the updating process. Please read the documentation of {@link ManualVersioning}.
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
         * If the user file version ID could not be found (e.g. user file was created before your plugin started using
         * this library/updater), force copy paths are not effective and it's version will be treated like the oldest
         * one specified by the given pattern (which effectively means all relocations given will be applied to it).
         * <p>
         * If any of the version IDs obtained from the files do not follow the given pattern an
         * {@link IllegalArgumentException} will be thrown. Please read the documentation of {@link AutomaticVersioning}.
         * <p>
         * <i>You may want to disable {@link LoaderSettings.Builder#setAutoUpdate(boolean)}
         * and rather update manually by calling {@link YamlFile#update()} (because if an error is thrown, you won't be able to initialize the file).</i>
         *
         * @param pattern the pattern
         * @param path    the path to version IDs (of both files) in both files
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull Path path) {
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