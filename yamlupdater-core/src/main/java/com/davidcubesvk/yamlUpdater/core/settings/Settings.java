package com.davidcubesvk.yamlUpdater.core.settings;

import com.davidcubesvk.yamlUpdater.core.YamlUpdaterCore;
import com.davidcubesvk.yamlUpdater.core.files.UpdatedFile;
import com.davidcubesvk.yamlUpdater.core.version.Pattern;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class covering all the update settings. Looking at the API wiki is strongly advised. Mainly, if you are working
 * with relocations and section values.
 */
public class Settings {

    /**
     * The default path (key) separator.
     */
    public static final char DEFAULT_SEPARATOR = '.';
    /**
     * The string form of the default separator.
     */
    public static final String DEFAULT_STRING_SEPARATOR = String.valueOf(DEFAULT_SEPARATOR);
    /**
     * The escaped form (for regex compatibility at all time) of the default separator.
     */
    public static final String DEFAULT_ESCAPED_SEPARATOR = java.util.regex.Pattern.quote(DEFAULT_STRING_SEPARATOR);
    /**
     * Default amount of spaces per one indentation level.
     */
    public static final int DEFAULT_INDENTATION = 2;
    /**
     * Default value for copy-header option.
     */
    public static final boolean DEFAULT_COPY_HEADER = true;
    /**
     * Default value for automatically updating disk file option.
     */
    public static final boolean DEFAULT_UPDATE_DISK_FILE = true;

    //The class loader
    private ClassLoader classLoader;
    //Folder and files
    private File folder, diskFile, resourceFile;
    //The separator
    private char separator = DEFAULT_SEPARATOR;
    //The string and quotes separator
    private String stringSeparator = DEFAULT_STRING_SEPARATOR, escapedSeparator = DEFAULT_ESCAPED_SEPARATOR;
    //Indentation spaces
    private int indentSpaces = DEFAULT_INDENTATION;
    //If to copy header
    private boolean copyHeader = DEFAULT_COPY_HEADER;
    //If to update disk file
    private boolean updateDiskFile = DEFAULT_UPDATE_DISK_FILE;
    //The version pattern
    private Pattern versioningPattern;
    //Path to the version and version strings
    private String versionIdPath = "", diskFileVersionId, resourceFileVersionId;
    //Relocations
    private final Map<Object, Object> relocations = new HashMap<>();
    //Section values
    private final Map<String, Set<String>> sectionValues = new HashMap<>();

    //The file loader
    private final SettingsFile settingsFile = new SettingsFile(this);

    /**
     * Initializes the settings with data (to be more specific, disk folder and class loader) from the given main class.
     *
     * @param updater the main updater class
     */
    public Settings(YamlUpdaterCore<?> updater) {
        //Set
        this.folder = updater.getDiskFolder();
        this.classLoader = updater.getClassLoader();
    }

    /**
     * Loads all settings from <strong>resource</strong> (packaged into the plugin) file at the given path. To read the
     * file, class loader given on initialization is used. The file does not need to be an YAML-type file (ending with
     * <code>.yml</code>), however, it must contain a valid YAML.
     *
     * @param path the path to the file
     * @return this settings object (to allow builder-like structure)
     * @throws URISyntaxException       if the URL cannot be converted to URI (used to identify the file), please refer
     *                                  to {@link URL#toURI()} for more information
     * @throws IOException              if an IO error occurred
     * @throws IllegalArgumentException if the file at the specified path is not a file (is a folder)
     * @throws YAMLException            if the YAML in the file is not valid
     * @throws ClassCastException       if the settings specification itself is invalid (YAML is valid, but the
     *                                  specification is not)
     */
    public Settings fromFile(String path) throws URISyntaxException, IOException, IllegalArgumentException, YAMLException {
        //The URL
        URL resourceURL = classLoader.getResource(path);
        //If null
        if (resourceURL == null)
            throw new IllegalArgumentException("Resource at path \"" + path + "\" could not be loaded!");

        //The file
        File file = new File(resourceURL.toURI());
        //If not a file
        if (!file.isFile())
            throw new IllegalArgumentException("Resource at path \"" + path + "\" is not a file!");
        //Load
        settingsFile.load(new FileInputStream(file));
        return this;
    }

    /**
     * Sets the given class loader as loader used to load resource files. Does not affect already loaded files (for
     * example, after calling {@link #setResourceFile(String)}, the resource file is loaded). To reset those, call
     * desired methods again.
     *
     * @param classLoader the new class loader
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setClassLoader(ClassLoader classLoader) {
        //Set
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Sets the given disk file folder as folder used to load disk files. Does not affect already loaded files (for
     * example, after calling {@link #setDiskFile(String)}, the disk file is loaded). To reset those, call desired
     * methods again.
     *
     * @param folder the new folder
     * @return this settings object (to allow builder-like structure)
     * @throws IllegalArgumentException if the given folder is not a directory
     */
    public Settings setDiskFolder(File folder) throws IllegalArgumentException {
        //If not a directory
        if (!folder.isDirectory())
            throw new IllegalArgumentException("Given disk path \"" + folder.getAbsolutePath() + "\" does not point to a directory!");
        //Set
        this.folder = folder;
        return this;
    }

    /**
     * Loads the file from associated disk folder at the specified path from the disk.
     *
     * @param path the path inside the disk folder to the given file
     * @return this settings object (to allow builder-like structure)
     * @throws IllegalStateException    if {@link #getDiskFolder()} returns <code>null</code>
     * @throws IllegalArgumentException if disk file in the disk folder at the specified path is not a file (is a
     *                                  directory)
     */
    public Settings setDiskFile(String path) throws IllegalStateException, IllegalArgumentException {
        //If folder is null
        if (folder == null)
            throw new IllegalStateException("Disk folder has not been set yet! Could not load the file.");
        //The file
        File file = new File(folder, path);
        //If not a file
        if (!file.isFile())
            throw new IllegalArgumentException("Given disk file path \"" + file.getAbsolutePath() + "\" does not point to a file!");
        //Set
        this.diskFile = file;
        return this;
    }

    /**
     * Loads the resource file at the specified path (resource file is a file bundled with the plugin) using associated
     * class loader.
     *
     * @param path the path inside the resource folder (in plugin source) to the given file
     * @return this settings object (to allow builder-like structure)
     * @throws IllegalStateException    if {@link #getClassLoader()} returns <code>null</code>
     * @throws IllegalArgumentException if disk file in the disk folder at the specified path is not a file (is a
     *                                  directory)
     */
    public Settings setResourceFile(String path) throws URISyntaxException {
        //If class loader is null
        if (classLoader == null)
            throw new IllegalStateException("Class loader is null! Could not load the file.");
        //The URL
        URL resourceURL = classLoader.getResource(path);
        //If null
        if (resourceURL == null)
            throw new IllegalArgumentException("Resource file at path \"" + path + "\" could not be loaded!");

        //The file
        File file = new File(resourceURL.toURI());
        //If not a file
        if (!file.isFile())
            throw new IllegalArgumentException("Given resource file path \"" + path + "\" does not point to a file!");
        //Set
        this.resourceFile = file;
        return this;
    }

    /**
     * Sets whether to copy the header of the file. This can only be used while on a Spigot (or any other fork) server,
     * where the value configured here is forwarded to YAML configuration options when converting the updated YAML file
     * in {@link UpdatedFile#toFile()}.
     *
     * @param copyHeader if to copy header
     * @return this settings object (to allow builder-like structure)
     * @see <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/file/YamlConfigurationOptions.html#copyHeader(boolean)">the main Spigot API method</a>
     * for more information about this setting
     */
    public Settings setCopyHeader(boolean copyHeader) {
        //Set
        this.copyHeader = copyHeader;
        return this;
    }

    /**
     * Sets whether the disk file content should automatically be replaced with the updated content (after successful
     * update).
     *
     * @param updateDiskFile if to update the disk file automatically
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setUpdateDiskFile(boolean updateDiskFile) {
        //Set
        this.updateDiskFile = updateDiskFile;
        return this;
    }

    /**
     * Sets a new path separator. A path separator is a special character which separates keys and sub-keys in a path
     * (e.g. path <code>a.b</code> refers to <code>b</code> located in <code>a</code>). Paths given in
     * {@link #setRelocations(Map)} and {@link #setSectionValues(Map)} (or similar, other path-related methods) must be
     * constructed with keys separated by this separator.<br>The set separator is also forwarded to YAML configuration
     * options when converting the updated YAML file in {@link UpdatedFile#toFile()} (available only while on a Spigot
     * server).
     *
     * @param separator the new separator
     * @return this settings object (to allow builder-like structure)
     * @see <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/file/YamlConfigurationOptions.html#pathSeparator(char)">the main Spigot API method</a>
     * for more information about this setting
     */
    public Settings setSeparator(char separator) {
        //Set
        this.separator = separator;
        this.stringSeparator = String.valueOf(separator);
        this.escapedSeparator = java.util.regex.Pattern.quote(stringSeparator);
        return this;
    }

    /**
     * Sets amount of spaces which form one indent. The disk file (before updating) does not need to have indents formed
     * from the set amount of spaces; this setting is used when outputting the updated file.<br>The set indentation is
     * also forwarded to YAML configuration options when converting the updated YAML file in
     * {@link UpdatedFile#toFile()} (available only while on a Spigot server).
     *
     * @param spaces the amount of spaces per one indent level
     * @return this settings object (to allow builder-like structure)
     * @see <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/file/YamlConfigurationOptions.html#indent(int)">the main Spigot API method</a>
     * for more information about this setting
     */
    public Settings setIndentSpaces(int spaces) {
        //Set
        this.indentSpaces = spaces;
        return this;
    }

    /**
     * Sets the versioning pattern. The pattern must be followed by both file version IDs
     * ({@link #setDiskFileVersionId(String)} and {@link #setResourceFileVersionId(String)}), otherwise unexpected behaviour
     * might occur.
     *
     * @param pattern the file version pattern
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setVersioningPattern(Pattern pattern) {
        //Set
        this.versioningPattern = pattern;
        return this;
    }

    /**
     * Sets the path at which, in both (resource and disk) files, version ID of that certain file can be found. ID at
     * that path must follow the version pattern (see {@link #setVersioningPattern(Pattern)}).<br>
     * This can be overridden by methods {@link #setDiskFileVersionId(String)}
     * (or {@link #setResourceFileVersionId(String)}). That means, when the disk file's version ID is needed during the
     * update process, result of {@link #getDiskFileVersionId()} is used. If not set, the ID is obtained directly from the
     * file, from the path set here.
     *
     * @param versionIdPath the path in a file, where version ID of that certain file can be found (must apply to both
     *                    disk and resource file)
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setFileVersionIdPath(String versionIdPath) {
        //Set
        this.versionIdPath = versionIdPath;
        return this;
    }

    /**
     * Sets the version ID of the disk file. If not set, the version is obtained directly from the file -
     * {@link #setFileVersionIdPath(String)} must be called.
     *
     * @param versionId the disk file version ID
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setDiskFileVersionId(String versionId) {
        //Set
        this.diskFileVersionId = versionId;
        return this;
    }

    /**
     * Sets the version ID of the resource file. If not set, the version is obtained directly from the file -
     * {@link #setFileVersionIdPath(String)} must be called.
     *
     * @param versionId the resource file version ID
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setResourceFileVersionId(String versionId) {
        //Set
        this.resourceFileVersionId = versionId;
        return this;
    }

    /**
     * Sets all relocations from the given map, where the key is the file version ID (following the
     * {@link #getVersioningPattern()}), when the relocations have been made) to which the relocations (specified by the
     * value) belong. The value map has structure: relocate from key to value path.<br>
     * If there is a version ID, which already has relocations assigned and is also in the given map, it's
     * relocations are overwritten with given ones. <strong>Please read the API wiki or see
     * {@link #setRelocations(String, Map)} for more information.</strong>
     *
     * @param relocations map containing new relocations
     * @return this settings object (to allow builder-like structure)
     * @see #setRelocations(String, Map) for more detailed and easier explanation
     */
    public Settings setRelocations(Map<String, Map<String, String>> relocations) {
        //Put
        this.relocations.putAll(relocations);
        return this;
    }

    /**
     * Sets all relocations. Used only by the settings file loader.
     *
     * @param relocations the relocations to set
     * @see #setRelocations(Map) for more detailed information
     */
    void setRelocationsFromConfig(Map<?, ?> relocations) {
        //Put
        for (Map.Entry<?, ?> entry : relocations.entrySet())
            this.relocations.put(entry.getKey(), entry.getValue());
    }

    /**
     * Sets relocations (represented by mappings, where the key indicates from which and value to which path to
     * relocate) which occurred at the given file version ID. Relocations are generally useful when some setting(s)
     * moved from one path to another (within the file).<br>
     * If some setting was located at path <code>a</code> at version ID <code>1.0</code>, but at ID <code>1.1</code>
     * at path <code>b</code>, the setting is taken as moved in version <code>1.1</code> and therefore, specify that
     * version with the relocation map formatted like <code>{"a": "b"}</code>.<br>
     * If there are already relocations for this version ID, they are overwritten. <strong>Please read the API wiki for
     * more information.</strong>
     *
     * @param versionId     the version ID when the relocations occurred
     * @param relocations the relocations
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setRelocations(String versionId, Map<String, String> relocations) {
        //Put
        this.relocations.put(versionId, relocations);
        return this;
    }

    /**
     * Sets all paths (identified by the file version ID in which they were present) to section values. A section value
     * is a configuration section in YAML terminology, which is not used as a section containing the actual settings
     * (mappings), but itself is a mapping. Please refer to the wiki for more information.<br>
     * The given map must have a version ID as the key (following the {@link #getVersioningPattern()}) and all section
     * value paths as the value. This essentially means <i>"at this file version ID, there were/are these paths to load as
     * section values"</i>.<br>
     * If there is a version ID, which already has section values assigned and is also in the given map, it's
     * section values are overwritten with given ones. <strong>Please read the API wiki or see
     * {@link #setSectionValues(String, Set)} for more information.</strong>
     *
     * @param sectionValues the section values by file version IDs in which they are present
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setSectionValues(Map<String, Set<String>> sectionValues) {
        //Put
        this.sectionValues.putAll(sectionValues);
        return this;
    }

    /**
     * Sets all paths which to load as section values (for the given file version ID). A section value is a
     * configuration section in YAML terminology, but it's contents are meant to be changed (e.g. <b>direct</b> mappings
     * added/removed) Please refer to the wiki for more information.<br>
     * Version ID should be the ID (following the {@link #getVersioningPattern()}) at which there were section values
     * present at the given paths. That means, if at file version ID <code>1.2</code> there were <code>a.b</code> and
     * <code>a.c</code> section values (their paths), use <code>setSectionValues("1.2", set{"a.b", "a.c"})</code>.<br>
     * If there are already section values for this version ID, they are overwritten. <strong>Please read the API wiki
     * for more information.</strong>
     *
     * @param versionId       the file version ID at which the section values were present
     * @param sectionValues the paths of sections to load as section values
     * @return this settings object (to allow builder-like structure)
     */
    public Settings setSectionValues(String versionId, Set<String> sectionValues) {
        //Put
        this.sectionValues.put(versionId, sectionValues);
        return this;
    }

    /**
     * Returns the class loader associated with these settings. See {@link #setClassLoader(ClassLoader)} for more
     * information.
     *
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the disk folder associated with these settings. See {@link #setDiskFolder(File)} for more information.
     *
     * @return the disk folder
     */
    public File getDiskFolder() {
        return folder;
    }

    /**
     * Returns the disk file (to be updated) associated with these settings. See {@link #setDiskFile(String)} for more
     * information.
     *
     * @return the disk file to be updated
     */
    public File getDiskFile() {
        return diskFile;
    }

    /**
     * Returns the resource file (the latest file) associated with these settings. See {@link #setResourceFile(String)}
     * for more information.
     *
     * @return the resource file, the latest file version
     */
    public File getResourceFile() {
        return resourceFile;
    }

    /**
     * Returns whether to automatically update the disk file after successful update. See
     * {@link #setUpdateDiskFile(boolean)} for more information.
     *
     * @return if to update the disk file automatically when finished
     */
    public boolean isUpdateDiskFile() {
        return updateDiskFile;
    }

    /**
     * Returns whether to copy file header. See {@link #setCopyHeader(boolean)} for more information.
     *
     * @return whether to copy file header
     */
    public boolean isCopyHeader() {
        return copyHeader;
    }

    /**
     * Returns the key separator associated with these settings. See {@link #setSeparator(char)} for more information.
     *
     * @return the key separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Returns the key separator (in string format) associated with these settings.
     *
     * @return the key separator in string format
     * @see #getSeparator() to get the separator character
     */
    public String getSeparatorString() {
        return stringSeparator;
    }

    /**
     * Returns the escaped key separator associated with these settings.
     *
     * @return the escaped key separator
     * @see #getSeparator() to get the separator character
     */
    public String getEscapedSeparator() {
        return escapedSeparator;
    }

    /**
     * Returns how much spaces to use to form one indent (indentation level). See {@link #setIndentSpaces(int)} for more
     * information.
     *
     * @return how much spaces to use for one indentation level
     */
    public int getIndentSpaces() {
        return indentSpaces;
    }

    /**
     * Returns the version pattern associated with these settings. See {@link #setVersioningPattern(Pattern)} for more
     * information.
     *
     * @return the version pattern
     */
    public Pattern getVersioningPattern() {
        return versioningPattern;
    }

    /**
     * Returns the version path associated with these settings. See {@link #setFileVersionIdPath(String)} for more
     * information.
     *
     * @return the version path
     */
    public String getVersionIdPath() {
        return versionIdPath;
    }

    /**
     * Returns the disk file version associated with these settings, set using {@link #setDiskFileVersionId(String)}.
     *
     * @return the disk file version
     */
    public String getDiskFileVersionId() {
        return diskFileVersionId;
    }

    /**
     * Returns the resource file version associated with these settings, set using
     * {@link #setResourceFileVersionId(String)}.
     *
     * @return the resource file version
     */
    public String getResourceFileVersionId() {
        return resourceFileVersionId;
    }

    /**
     * Returns the complete map of relocations associated with these settings. Can be mutated. See
     * {@link #setRelocations(Map)} for more information.
     *
     * @return the map of relocations
     */
    public Map<Object, Object> getRelocations() {
        return relocations;
    }

    /**
     * Returns the complete map of section values associated with these settings. Can be mutated. See
     * {@link #setSectionValues(Map)} for more information.
     *
     * @return the map of section values
     */
    public Map<String, Set<String>> getSectionValues() {
        return sectionValues;
    }
}