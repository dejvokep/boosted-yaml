package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.settings.*;
import com.davidcubesvk.yamlUpdater.core.version.Pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdaterSettings {

    /**
     * Default value for automatically updating disk file option.
     */
    public static final boolean DEFAULT_UPDATE_PHYSICAL_FILE = true;
    public static final boolean DEFAULT_UPDATE_UNSUPPORTED_FILES = false;

    public static final Map<MergeRule, Boolean> DEFAULT_MERGE_RULES = new HashMap<MergeRule, Boolean>(){{
        //All rules
        for (MergeRule rule : MergeRule.values())
            //Set to true
            put(rule, true);
    }};

    private boolean updatePhysicalFile = DEFAULT_UPDATE_PHYSICAL_FILE;
    private boolean updateUnsupportedFiles = DEFAULT_UPDATE_UNSUPPORTED_FILES;
    private final Map<MergeRule, Boolean> mergeRules = new HashMap<>(DEFAULT_MERGE_RULES);
    private final Map<String, Set<String>> forceCopy = new HashMap<>();
    private final Map<String, Map<String, String>> relocations = new HashMap<>();
    private Versioning versioning = null;
    private UpdaterCallback postLoadedCallback = null, postRelocatedCallback = null;

    public UpdaterSettings setUpdatePhysicalFile(boolean updatePhysicalFile) {
        this.updatePhysicalFile = updatePhysicalFile;
        return this;
    }

    public void setUpdateUnsupportedFiles(boolean updateUnsupportedFiles) {
        this.updateUnsupportedFiles = updateUnsupportedFiles;
    }

    public UpdaterSettings setMergeRules(Map<MergeRule, Boolean> mergeRules) {
        this.mergeRules.putAll(mergeRules);
        return this;
    }

    public UpdaterSettings setMergeRule(MergeRule rule, boolean preserveUser) {
        this.mergeRules.put(rule, preserveUser);
        return this;
    }

    public UpdaterSettings setForceCopy(Map<String, Set<String>> forceCopy) {
        this.forceCopy.putAll(forceCopy);
        return this;
    }

    public UpdaterSettings setForceCopy(String versionId, Set<String> paths) {
        this.forceCopy.put(versionId, paths);
        return this;
    }

    public UpdaterSettings setRelocations(Map<String, Map<String, String>> relocations) {
        this.relocations.putAll(relocations);
        return this;
    }

    public UpdaterSettings setRelocations(String versionId, Map<String, String> relocations) {
        this.relocations.put(versionId, relocations);
        return this;
    }

    public UpdaterSettings setVersioning(Versioning versioning) {
        this.versioning = versioning;
        return this;
    }

    public UpdaterSettings setVersioning(Pattern pattern, String userFileVersionId, String defaultFileVersionId) {
        setVersioning(new ManualVersioning(pattern, userFileVersionId, defaultFileVersionId));
        return this;
    }

    public UpdaterSettings setVersioning(Pattern pattern, String path) {
        setVersioning(new AutomaticVersioning(pattern, path));
        return this;
    }

    public UpdaterSettings setPostLoadedCallback(UpdaterCallback postLoadedCallback) {
        this.postLoadedCallback = postLoadedCallback;
        return this;
    }

    public UpdaterSettings setPostRelocatedCallback(UpdaterCallback postRelocatedCallback) {
        this.postRelocatedCallback = postRelocatedCallback;
        return this;
    }

    public boolean isUpdatePhysicalFile() {
        return updatePhysicalFile;
    }

    public Map<MergeRule, Boolean> getMergeRules() {
        return mergeRules;
    }

    public Map<String, Set<String>> getForceCopy() {
        return forceCopy;
    }

    public Map<String, Map<String, String>> getRelocations() {
        return relocations;
    }

    public Versioning getVersioning() {
        return versioning;
    }

    public UpdaterCallback getPostLoadedCallback() {
        return postLoadedCallback;
    }

    public UpdaterCallback getPostRelocatedCallback() {
        return postRelocatedCallback;
    }
}