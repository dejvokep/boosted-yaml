package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.AutomaticVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.ManualVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.Versioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdaterSettings {

    public static final UpdaterSettings DEFAULT = builder().build();

    public static final boolean DEFAULT_MANAGE_USER_FILE = true;
    public static final boolean DEFAULT_ENABLE_DOWNGRADING = false;
    public static final boolean DEFAULT_SILENT_ERRORS = false;
    public static final boolean DEFAULT_FORCE_COPY_ALL = false;
    public static final Map<MergeRule, Boolean> DEFAULT_MERGE_RULES = new HashMap<MergeRule, Boolean>(){{
        put(MergeRule.MAPPINGS, true);
        put(MergeRule.MAPPING_AT_SECTION, false);
        put(MergeRule.SECTION_AT_MAPPING, false);
    }};
    public static final Versioning DEFAULT_VERSIONING = null;

    //If to update disk file
    private final boolean autoSave;
    private final boolean enableDowngrading;
    private final boolean silentErrors;
    private final boolean forceCopyAll;
    private final Map<MergeRule, Boolean> mergeRules;
    private final Map<String, Set<Path>> forceCopy;
    private final Map<String, Map<Path, Path>> relocations;
    private final Versioning versioning;

    public UpdaterSettings(Builder builder) {
        this.autoSave = builder.autoSave;
        this.enableDowngrading = builder.enableDowngrading;
        this.silentErrors = builder.silentErrors;
        this.forceCopyAll = builder.forceCopyAll;
        this.mergeRules = builder.mergeRules;
        this.forceCopy = builder.forceCopy;
        this.relocations = builder.relocations;
        this.versioning = builder.versioning;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean autoSave = DEFAULT_MANAGE_USER_FILE;
        private boolean enableDowngrading = DEFAULT_ENABLE_DOWNGRADING;
        private boolean silentErrors = DEFAULT_SILENT_ERRORS;
        private boolean forceCopyAll = DEFAULT_FORCE_COPY_ALL;
        private final Map<MergeRule, Boolean> mergeRules = new HashMap<>(DEFAULT_MERGE_RULES);
        private final Map<String, Set<Path>> forceCopy = new HashMap<>();
        private final Map<String, Map<Path, Path>> relocations = new HashMap<>();
        private Versioning versioning = DEFAULT_VERSIONING;

        private Builder() {
        }

        public Builder setAutoSave(boolean autoSave) {
            this.autoSave = autoSave;
            return this;
        }

        public Builder setEnableDowngrading(boolean enableDowngrading) {
            this.enableDowngrading = enableDowngrading;
            return this;
        }

        public Builder setForceCopyAll(boolean forceCopyAll) {
            this.forceCopyAll = forceCopyAll;
            return this;
        }

        public Builder setSilentErrors(boolean silentErrors) {
            this.silentErrors = silentErrors;
            return this;
        }

        public Builder setMergeRules(Map<MergeRule, Boolean> mergeRules) {
            this.mergeRules.putAll(mergeRules);
            return this;
        }

        public Builder setMergeRule(MergeRule rule, boolean preserveUser) {
            this.mergeRules.put(rule, preserveUser);
            return this;
        }

        public Builder setForceCopy(Map<String, Set<Path>> forceCopy) {
            this.forceCopy.putAll(forceCopy);
            return this;
        }

        public Builder setForceCopy(String versionId, Set<Path> paths) {
            this.forceCopy.put(versionId, paths);
            return this;
        }

        public Builder setRelocations(Map<String, Map<Path, Path>> relocations) {
            this.relocations.putAll(relocations);
            return this;
        }

        public Builder setRelocations(String versionId, Map<Path, Path> relocations) {
            this.relocations.put(versionId, relocations);
            return this;
        }

        public Builder setVersioning(Versioning versioning) {
            this.versioning = versioning;
            return this;
        }

        public Builder setVersioning(Pattern pattern, String userFileVersionId, String defaultFileVersionId) {
            return setVersioning(new ManualVersioning(pattern, userFileVersionId, defaultFileVersionId));
        }

        public Builder setVersioning(Pattern pattern, String path) {
            return setVersioning(new AutomaticVersioning(pattern, path));
        }

        public UpdaterSettings build() {
            return new UpdaterSettings(this);
        }
    }

    public Map<MergeRule, Boolean> getMergeRules() {
        return mergeRules;
    }

    public Map<String, Set<Path>> getForceCopy() {
        return forceCopy;
    }

    public Map<String, Map<Path, Path>> getRelocations() {
        return relocations;
    }

    public Versioning getVersioning() {
        return versioning;
    }

    public boolean isEnableDowngrading() {
        return enableDowngrading;
    }

    public boolean isSilentErrors() {
        return silentErrors;
    }

    public boolean isForceCopyAll() {
        return forceCopyAll;
    }

    public boolean isAutoSave() {
        return autoSave;
    }
}