package com.davidcubesvk.yamlUpdater.core.settings;

import com.davidcubesvk.yamlUpdater.core.YamlUpdaterCore;
import com.davidcubesvk.yamlUpdater.core.version.Pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Settings {

    private ClassLoader classLoader;
    private File folder, diskFile, resourceFile;
    private char separator = '.';
    private int indentSpaces = 2;
    private boolean copyHeader = true;
    private boolean updateDiskFile = true;
    private Pattern versionPattern;
    private String versionPath = "", diskFileVersion, resourceFileVersion;
    private final Map<Object, Object> relocations = new HashMap<>();
    private final Map<String, Set<String>> sectionValues = new HashMap<>();

    private SettingsFile settingsFile = new SettingsFile(this);

    public Settings() {}
    public Settings(YamlUpdaterCore updater) {
        this.folder = updater.getDiskFolder();
        this.classLoader = updater.getClassLoader();
    }

    public Settings fromFile(String path) throws IOException {
        if (folder == null)
            throw new IllegalStateException();
        File file = new File(folder, path);
        if (!file.isFile())
            throw new IllegalArgumentException();
        settingsFile.load(new FileInputStream(file));
        return this;
    }

    public Settings setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public Settings setDiskFolder(File folder) {
        if (!folder.isDirectory())
            throw new IllegalArgumentException();
        this.folder = folder;
        return this;
    }

    public Settings setDiskFile(String path) {
        if (folder == null)
            throw new IllegalStateException();
        File file = new File(folder, path);
        if (!file.isFile())
            throw new IllegalArgumentException();
        this.diskFile = file;
        return this;
    }
    public Settings setResourceFile(String path) throws URISyntaxException {
        if (classLoader == null)
            throw new IllegalStateException();
        File file = new File(classLoader.getResource(path).toURI());
        if (!file.isFile())
            throw new IllegalArgumentException();
        this.resourceFile = file;
        return this;
    }

    public void setCopyHeader(boolean copyHeader) {
        this.copyHeader = copyHeader;
    }

    public Settings setUpdateDiskFile(boolean updateDiskFile) {
        this.updateDiskFile = updateDiskFile;
        return this;
    }

    public Settings setSeparator(char separator) {
        this.separator = separator;
        return this;
    }
    public Settings setIndentSpaces(int spaces) {
        this.indentSpaces = spaces;
        return this;
    }
    public Settings setVersionPattern(Pattern pattern) {
        this.versionPattern = pattern;
        return this;
    }
    public Settings setVersionPath(String versionPath) {
        this.versionPath = versionPath;
        return this;
    }

    public Settings setDiskFileVersion(String diskFileVersion) {
        this.diskFileVersion = diskFileVersion;
        return this;
    }

    public Settings setResourceFileVersion(String resourceFileVersion) {
        this.resourceFileVersion = resourceFileVersion;
        return this;
    }

    public Settings setRelocations(Map<String, Map<String, String>> relocations) {
        for (Map.Entry<String, Map<String, String>> entry : relocations.entrySet())
            this.relocations.put(entry.getKey(), entry.getValue());
        return this;
    }
    Settings setRelocationsFromConfig(Map<?, ?> relocations) {
        for (Map.Entry<?, ?> entry : relocations.entrySet())
            this.relocations.put(entry.getKey(), entry.getValue());
        return this;
    }
    public Settings setRelocations(String version, Map<String, String> relocations) {
        this.relocations.put(version, relocations);
        return this;
    }
    public Settings setSectionValues(Map<String, Set<String>> sectionValues) {
        for (Map.Entry<String, Set<String>> entry : sectionValues.entrySet())
            this.sectionValues.put(entry.getKey(), entry.getValue());
        return this;
    }
    public Settings setSectionValues(String version, Set<String> sectionValues) {
        this.sectionValues.put(version, sectionValues);
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public File getDiskFolder() {
        return folder;
    }

    public File getDiskFile() {
        return diskFile;
    }

    public File getResourceFile() {
        return resourceFile;
    }

    public boolean isUpdateDiskFile() {
        return updateDiskFile;
    }

    public boolean isCopyHeader() {
        return copyHeader;
    }

    public char getSeparator() {
        return separator;
    }

    public String getSeparatorString() {
        return String.valueOf(separator);
    }

    public int getIndentSpaces() {
        return indentSpaces;
    }

    public Pattern getVersionPattern() {
        return versionPattern;
    }

    public String getVersionPath() {
        return versionPath;
    }

    public String getDiskFileVersion() {
        return diskFileVersion;
    }

    public String getResourceFileVersion() {
        return resourceFileVersion;
    }

    public Map<Object, Object> getRelocations() {
        return relocations;
    }

    public Map<String, Set<String>> getSectionValues() {
        return sectionValues;
    }
}