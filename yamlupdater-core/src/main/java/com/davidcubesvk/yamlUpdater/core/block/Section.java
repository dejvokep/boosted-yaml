package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.engine.LibConstructor;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.nodes.*;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;

import static com.davidcubesvk.yamlUpdater.core.utils.conversion.NumericConversions.*;
import static com.davidcubesvk.yamlUpdater.core.utils.conversion.ListConversions.*;

/**
 * Represents one YAML section (map), while storing its contents and comments. Section can also be referred to as
 * <i>collection of mappings (key=value pairs)</i>.
 * <p>
 * Functionality of this class is heavily dependent on the root file (instance of root {@link YamlFile}), to be more
 * specific, on its settings.
 * <p>
 * The public methods of this class are divided into 2 groups, by what they require as the path and therefore, what should be used for certain key mode:
 * <ul>
 *     <li>{@link Path} - works <b>independently</b> of the root's key mode, please see {@link #getBlockSafe(Path)}</li>
 *     <li>{@link Object} - works (functions) <b>dependently</b> of the root's key mode, please see {@link #getDirectBlockSafe(Object)}</li>
 * </ul>
 * <p>
 * Also, it is important to note that mappings stored in sections are key=value pairs, where the value is the actual
 * value encapsulated in a {@link Block}. If the actual value is a map, it is treated as section and therefore,
 * encapsulated in an instance of {@link Section}, otherwise {@link Mapping}. There is no other encapsulation object provided.
 * <p>
 * Modification/extension of those two classes, or additional extension of {@link Block} class is rather complicated and
 * rather dangerous action. If doing so, the source code of this library should properly be examined (and in detail).
 */
@SuppressWarnings("unused")
public class Section extends Block<Map<Object, Block<?>>> {

    //Root file
    private YamlFile root;
    //Parent section
    private Section parent;
    //Key to the section
    private Object name;
    //Full key
    private Path path;

    /**
     * Creates a section using the given relatives, nodes and constructor, which is used to retrieve the actual Java
     * instances of nodes (and sub-nodes).
     *
     * @param root        root file
     * @param parent      parent section (or <code>null</code> if this is the root section)
     * @param name        name of the section (key to this section in the parent section, or <code>null</code> if this
     *                    is the root section)
     * @param path        full (starting from the root file) path to this section
     * @param keyNode     node which represents the key to this section, used <b>only</b> to retrieve comments
     * @param valueNode   node which represents this section's contents
     * @param constructor constructor used to construct all the nodes contained within the root file, used to retrieve
     *                    Java instances of the given nodes
     * @see Block#Block(Node, Node, Object) superclass constructor used
     */
    public Section(@NotNull YamlFile root, @Nullable Section parent, @Nullable Object name, @NotNull Path path, @Nullable Node keyNode, @NotNull MappingNode valueNode, @NotNull LibConstructor constructor) {
        //Call superclass (value node is null because there can't be any value comments)
        super(keyNode, null, root.getGeneralSettings().getDefaultMap());
        //Set
        this.root = root;
        this.parent = parent;
        this.name = adaptKey(name);
        this.path = path;
        //Init
        init(root, keyNode, valueNode, constructor);
    }

    /**
     * Creates a section using the given relatives, previous block and mappings.
     *
     * @param root     root file
     * @param parent   parent section (or <code>null</code> if this is the root section)
     * @param name     name of the section (key to this section in the parent section, or <code>null</code> if this is
     *                 the root section)
     * @param path     full (starting from the root file) path to this section
     * @param previous previous block at the same position, used to reference comments from
     * @param mappings raw (containing Java values directly; no {@link Block} instances) content map
     * @see Block#Block(Block, Object) superclass constructor used
     */
    public Section(@NotNull YamlFile root, @Nullable Section parent, @Nullable Object name, @NotNull Path path, @Nullable Block<?> previous, @NotNull Map<?, ?> mappings) {
        //Call superclass
        super(previous, root.getGeneralSettings().getDefaultMap());
        //Set
        this.root = root;
        this.parent = parent;
        this.name = adaptKey(name);
        this.path = path;
        //Loop through all mappings
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            //Key and value
            Object key = adaptKey(entry.getKey()), value = entry.getValue();
            //Add
            getValue().put(key, value instanceof Map ? new Section(root, this, key, path.add(key), null, (Map<?, ?>) value) : new Mapping(null, value));
        }
    }

    /**
     * Creates a section using the given (not necessarily empty) instance of default map, while setting the root file,
     * parent section to <code>null</code>, section name to an empty string and path to an empty path.
     * <p>
     * <b>This constructor is only used by extending class {@link YamlFile}, where the respective nodes are unknown at
     * the time of initialization. It is needed to call {@link #init(YamlFile, Node, MappingNode, LibConstructor)} afterwards.</b>
     *
     * @param defaultMap the content map
     * @see Block#Block(Object) superclass constructor used
     */
    protected Section(@NotNull Map<Object, Block<?>> defaultMap) {
        //Call superclass
        super(defaultMap);
        //Set
        this.root = null;
        this.parent = null;
        this.name = null;
        this.path = null;
    }

    //
    //
    //      -----------------------
    //
    //
    //    General and utility methods
    //
    //
    //      -----------------------
    //
    //

    protected void initEmpty(@NotNull YamlFile root) {
        //Call superclass
        super.init(null, null);
        //Set
        this.root = root;
    }

    /**
     * Initializes this section, and it's contents using the given parameters, while also initializing the superclass by
     * calling {@link Block#init(Node, Node)}.
     * <p>
     * This method can also be referred to as <i>secondary</i> constructor.
     *
     * @param root        the root file of this section
     * @param keyNode     node which represents the key to this section, used <b>only</b> to retrieve comments
     * @param valueNode   node which represents this section's contents
     * @param constructor constructor used to construct all the nodes contained within the root file, used to retrieve
     *                    Java instances of the given nodes
     */
    protected void init(@NotNull YamlFile root, @Nullable Node keyNode, @NotNull MappingNode valueNode, @NotNull LibConstructor constructor) {
        //Call superclass
        super.init(keyNode, null);
        //Set
        this.root = root;
        //If comments of the super node were assigned
        boolean superNodeComments = false;
        //Loop through all mappings
        for (NodeTuple tuple : valueNode.getValue()) {
            //Key and value
            Object key = adaptKey(constructor.getConstructed(tuple.getKeyNode())), value = constructor.getConstructed(tuple.getValueNode());
            //Add
            getValue().put(key, value instanceof Map ?
                    new Section(root, this, key, getSubPath(key), superNodeComments ? tuple.getKeyNode() : valueNode, (MappingNode) tuple.getValueNode(), constructor) :
                    new Mapping(superNodeComments ? tuple.getKeyNode() : valueNode, tuple.getValueNode(), value));
            //Set to true
            superNodeComments = true;
        }
    }

    /**
     * Returns <code>true</code> if this section is empty, <code>false</code> otherwise. The parameter indicates if to
     * search subsections too, which gives the <b>true</b> indication if the section is empty.
     * <p>
     * If <code>deep</code> is <code>false</code>, returns only the result of {@link Map#isEmpty()} called on the
     * content map represented by this section.
     * However, the section (underlying map) might also contain sections, which are empty, resulting in incorrect
     * returned value by this method <code>false</code>.
     * <p>
     * More formally, if <code>deep == true</code>, contents of this section are iterated. If any of the values is a
     * mapping (not a subsection), returns <code>false</code>. Similarly, if it is a section, runs this method
     * recursively (with <code>deep</code> set to <code>true</code>) and returns <code>false</code> if the
     * result of that call is <code>false</code>. If the iteration finished and none of these conditions were met,
     * returns <code>true</code>.
     *
     * @param deep if to search deeply
     * @return whether this section is empty
     */
    public boolean isEmpty(boolean deep) {
        //If no values are present
        if (getValue().isEmpty())
            return true;
        //If not deep
        if (!deep)
            return false;

        //Loop through all values
        for (Block<?> value : getValue().values()) {
            //If a mapping or non-empty section
            if (value instanceof Mapping || (value instanceof Section && !((Section) value).isEmpty(true)))
                return false;
        }

        //Empty
        return true;
    }

    /**
     * Returns whether this section is simultaneously the root section (file).
     *
     * @return if this section is the root section
     */
    public boolean isRoot() {
        return false;
    }

    /**
     * Returns the root file of this section.
     *
     * @return the root file
     */
    public YamlFile getRoot() {
        return root;
    }

    /**
     * Returns the parent section, or <code>null</code> if this section has no parent - represents the root file (check {@link #isRoot()}).
     *
     * @return the parent section, or <code>null</code> if unavailable
     */
    public Section getParent() {
        return parent;
    }

    /**
     * Returns the name of this section. A name is considered to be the direct key to this section in the parent section.
     * If this section represents the root file (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * This is incompatible with Spigot/BungeeCord APIs, where those, if this section represented the root file, would return an empty string.
     *
     * @return the name of this section
     */
    public Object getName() {
        return name;
    }

    /**
     * Returns the path to this section from the root file. If this section represents the root file (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * This is incompatible with Spigot/BungeeCord APIs, where those, if this section represented the root file, would return an empty string.
     *
     * @return the name of this section
     * @see #getRoot()
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns sub-path for this section; for this specified key. This section, therefore, patches nullable result of
     * {@link #getPath()}.
     * <p>
     * More formally, returns the result of {@link Path#addTo(Path, Object)}.
     *
     * @return sub-path for path of this section
     * @see Path#addTo(Path, Object)
     */
    public Path getSubPath(@Nullable Object key) {
        return Path.addTo(path, key);
    }

    /**
     * Adapts this section (including sub-sections) to the new relatives. This method should be called if and only this
     * section was relocated to a new parent section.
     *
     * @param root   new root file
     * @param parent new parent section
     * @param name   new name for this section
     * @param path   new path for this section
     * @see #adapt(YamlFile, Path)
     */
    private void adapt(@NotNull YamlFile root, @Nullable Section parent, @Nullable Object name, @NotNull Path path) {
        //Set
        this.parent = parent;
        this.name = name;
        //Adapt
        adapt(root, path);
    }

    /**
     * Adapts this section (including sub-sections) to the new relatives. This method should only be called by
     * {@link #adapt(YamlFile, Section, Object, Path)}.
     *
     * @param root new root file
     * @param path new path for this section
     */
    private void adapt(@NotNull YamlFile root, @NotNull Path path) {
        //Set
        this.root = root;
        this.path = path;
        //Loop through all entries
        for (Map.Entry<Object, Block<?>> entry : getValue().entrySet())
            //If a section
            if (entry.getValue() instanceof Section)
                //Adapt
                ((Section) entry.getValue()).adapt(root, path.add(entry.getKey()));
    }

    /**
     * Adapts the given key, so it fits the key mode configured via the root's general settings
     * ({@link GeneralSettings#getKeyMode()}).
     * <p>
     * More formally, if key mode is {@link GeneralSettings.KeyMode#STRING STRING}, returns the result of
     * {@link Object#toString()} on the given key object, the key object given otherwise.
     * <p>
     * If the given key is <code>null</code>, returns <code>null</code>.
     *
     * @param key the key object to adapt
     * @return the adapted key
     */
    public Object adaptKey(@Nullable Object key) {
        return key == null ? null : root.getGeneralSettings().getKeyMode() == GeneralSettings.KeyMode.OBJECT ? key : key.toString();
    }

    //
    //
    //      -----------------------
    //
    //
    //           Path methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns set of paths in this section; while not keeping any reference to this (or sub-) sections, therefore,
     * enabling the caller to modify it freely. The returned set is an instance of {@link GeneralSettings#getDefaultSet()}.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, (effectively) returns the result of {@link #getKeys()} with
     * the keys converted to paths each with one element - the key itself.
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete set of
     * paths relative to this section; including paths to sections.
     * <p>
     * It is guaranteed that call to {@link #contains(Path)} with any path from the returned set will always return
     * <code>true</code> (unless modified in between).
     *
     * @param deep if to get paths deeply
     * @return the complete set of paths
     */
    public Set<Path> getPaths(boolean deep) {
        //Create a set
        Set<Path> keys = root.getGeneralSettings().getDefaultSet();
        //Add
        addData((path, entry) -> keys.add(path), null, deep);
        //Return
        return keys;
    }

    /**
     * Returns set of string paths in this section; while not keeping any reference to this (or sub-) sections, therefore,
     * enabling the caller to modify it freely. The returned set is an instance of {@link GeneralSettings#getDefaultSet()}.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, (effectively) returns the result of {@link #getKeys()}.
     * <p>
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete set of
     * paths relative to this section; including paths to sections. The returned paths will have their keys separated by
     * the root's separator ({@link GeneralSettings#getSeparator()}).
     * <p>
     * It is guaranteed that call to {@link #contains(String)} with any path from the returned set will always return
     * <code>true</code> (unless modified in between).
     * <p>
     * This is, however, not guaranteed if the root's key mode is set to {@link GeneralSettings.KeyMode#OBJECT OBJECT},
     * therefore, in this case, <b>an {@link UnsupportedOperationException} will be thrown.</b>
     *
     * @param deep if to get paths deeply
     * @return the complete set of string paths
     */
    public Set<String> getStrPaths(boolean deep) {
        //If not string mode
        if (root.getGeneralSettings().getKeyMode() != GeneralSettings.KeyMode.STRING)
            throw new UnsupportedOperationException("Cannot build string paths if the key mode is not set to STRING!");

        //Create a set
        Set<String> keys = root.getGeneralSettings().getDefaultSet();
        //Add
        addData((path, entry) -> keys.add(path), new StringBuilder(), root.getGeneralSettings().getSeparator(), deep);
        //Return
        return keys;
    }

    /**
     * Returns a complete set of direct keys - in this section only (not deep), including keys to sections. More formally,
     * returns the key set of the underlying map. The returned set is an instance of {@link GeneralSettings#getDefaultSet()}.
     * <p>
     * The set, however, is a <i>shallow</i> copy of the map's key set, therefore, the caller is able to modify it
     * freely, without modifying this section.
     *
     * @return the complete set of keys directly contained by this section
     */
    public Set<Object> getKeys() {
        //Create a set
        Set<Object> set = root.getGeneralSettings().getDefaultSet(getValue().size());
        //Add all
        set.addAll(getValue().keySet());
        //Return
        return set;
    }

    //
    //
    //      -----------------------
    //
    //
    //          Value methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns a complete map of <i>path=value</i> pairs; while not keeping any reference to this (or sub-) sections,
     * therefore, enabling the caller to modify it freely. The returned map is an instance of {@link GeneralSettings#getDefaultMap()}.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to paths each with one element - the key itself; with their
     * appropriate values (obtained from the block, if not a section, stored at those paths).
     * <p>
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete map of
     * <i>path=value</i> (obtained from the block, if not a section, stored at those paths) pairs, with paths relative to this
     * section.
     * <p>
     * Practically, it is a result of {@link #getPaths(boolean)} with appropriate values to each path assigned.
     * <p>
     * It is guaranteed that call to {@link #get(Path)} with any path from the returned map's key set will always
     * return the value assigned to that path in the returned map. (unless modified in between).
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of <i>path=value</i> pairs, including sections
     */
    public Map<Path, Object> getValues(boolean deep) {
        //Create a map
        Map<Path, Object> values = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((path, entry) -> values.put(path, entry.getValue() instanceof Section ? entry.getValue() : entry.getValue().getValue()), null, deep);
        //Return
        return values;
    }

    /**
     * Returns a complete map of <i>string path=value</i> pairs; while not keeping any reference to this (or sub-) sections,
     * therefore, enabling the caller to modify it freely. The returned map is an instance of {@link GeneralSettings#getDefaultMap()}.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to paths each with one element - the key itself; with their
     * appropriate values (obtained from the block, if not a section, stored at those paths).
     * <p>
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete map of
     * <i>string path=value</i> (obtained from the block, if not a section, stored at those paths) pairs, with string paths
     * relative to this section. The returned paths will have their keys separated by the root's separator
     * ({@link GeneralSettings#getSeparator()}).
     * <p>
     * Practically, it is a result of {@link #getStrPaths(boolean)} with appropriate values to each path assigned.
     * <p>
     * It is guaranteed that call to {@link #get(String)} with any path from the returned map's key set will always
     * return the value assigned to that path in the returned map. (unless modified in between).
     * <p>
     * This is, however, not guaranteed if the root's key mode is set to {@link GeneralSettings.KeyMode#OBJECT OBJECT},
     * therefore, in this case, <b>an {@link UnsupportedOperationException} will be thrown.</b>
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of <i>string path=value</i> pairs, including sections
     */
    public Map<String, Object> getStrPathValues(boolean deep) {
        //If not string mode
        if (root.getGeneralSettings().getKeyMode() != GeneralSettings.KeyMode.STRING)
            throw new UnsupportedOperationException("Cannot build string paths if the key mode is not set to STRING!");

        //Create a map
        Map<String, Object> values = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((path, entry) -> values.put(path, entry.getValue() instanceof Section ? entry.getValue() : entry.getValue().getValue()), new StringBuilder(), root.getGeneralSettings().getSeparator(), deep);
        //Return
        return values;
    }

    //
    //
    //      -----------------------
    //
    //
    //           Block methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns a complete map of <i>path=block</i> pairs; while not keeping any reference to this (or sub-) sections,
     * therefore, enabling the caller to modify it freely. The returned map is an instance of {@link GeneralSettings#getDefaultMap()}.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns a copy of the underlying map with keys (which are
     * stored as object instances) converted to paths each with one element - the key itself; with their appropriate
     * blocks.
     * <p>
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete map of
     * <i>path=block</i> pairs, with paths relative to this section.
     * <p>
     * Practically, it is a result of {@link #getPaths(boolean)} with blocks to each path assigned, or <b>a copy of
     * {@link #getValue()} converted from nested to flat map.</b>
     * <p>
     * It is guaranteed that call to {@link #getBlock(Path)} with any path from the returned map's key set will always
     * return the block assigned to that path in the returned map. (unless modified in between).
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of <i>path=value</i> pairs
     */
    public Map<Path, Block<?>> getBlocks(boolean deep) {
        //Create map
        Map<Path, Block<?>> blocks = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((path, entry) -> blocks.put(path, entry.getValue()), null, deep);
        //Return
        return blocks;
    }

    /**
     * Returns a complete map of <i>string path=block</i> pairs; while not keeping any reference to this (or sub-) sections,
     * therefore, enabling the caller to modify it freely. The returned map is an instance of {@link GeneralSettings#getDefaultMap()}.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to paths each with one element - the key itself; with their
     * appropriate values (obtained from the block, if not a section, stored at those paths).
     * <p>
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete map of
     * <i>string path=block</i> (obtained from the block, if not a section, stored at those paths) pairs, with string paths
     * relative to this section. The returned paths will have their keys separated by the root's separator
     * ({@link GeneralSettings#getSeparator()}).
     * <p>
     * Practically, it is a result of {@link #getStrPaths(boolean)} with appropriate blocks to each path assigned. <b>It is
     * also a copy of {@link #getValue()} converted from nested to flat map (with the blocks, if a mapping - not a
     * section, represented by their values).</b>
     * <p>
     * It is guaranteed that call to {@link #getBlock(String)} with any path from the returned map's key set will always
     * return the value assigned to that path in the returned map. (unless modified in between).
     * <p>
     * This is, however, not guaranteed if the root's key mode is set to {@link GeneralSettings.KeyMode#OBJECT OBJECT},
     * therefore, in this case, <b>an {@link UnsupportedOperationException} will be thrown.</b>
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of <i>string path=block</i> pairs, including sections
     */
    public Map<String, Block<?>> getStrPathBlocks(boolean deep) {
        //Create map
        Map<String, Block<?>> blocks = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((path, entry) -> blocks.put(path, entry.getValue()), new StringBuilder(), root.getGeneralSettings().getSeparator(), deep);
        //Return
        return blocks;
    }

    //
    //
    //      -----------------------
    //
    //
    //          Data handlers
    //
    //
    //      -----------------------
    //
    //

    /**
     * Iterates through all entries in the underlying map, while calling the given consumer for each entry. The path
     * given is the path relative to the main caller section.
     * <p>
     * If any of the entries contain an instance of {@link Section} as their value and <code>deep</code> is set to
     * <code>true</code>, this method is called on each sub-section with the same consumer (while the path is managed to
     * be always relative to this section).
     *
     * @param consumer the consumer to call for each entry
     * @param current  the path to the currently iterated section, relative to the main caller section
     * @param deep     if to iterate deeply
     */
    private void addData(@NotNull BiConsumer<Path, Map.Entry<?, Block<?>>> consumer, @Nullable Path current, boolean deep) {
        //All keys
        for (Map.Entry<?, Block<?>> entry : getValue().entrySet()) {
            //Path to this entry
            Path entryPath = Path.addTo(current, entry.getKey());
            //Call
            consumer.accept(entryPath, entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, entryPath, true);
        }
    }

    /**
     * Iterates through all entries in the underlying map, while calling the given consumer for each entry. The string
     * path builder given must contain path relative to the main caller section.
     * <p>
     * If any of the entries contain an instance of {@link Section} as their value and <code>deep</code> is set to
     * <code>true</code>, this method is called on each sub-section with the same consumer (while the path is managed to
     * be always relative to this section).
     *
     * @param consumer    the consumer to call for each entry
     * @param pathBuilder the path to the currently iterated section, relative to the main caller section
     * @param separator   the separator to use to separate individual keys in the string paths
     * @param deep        if to iterate deeply
     */
    private void addData(@NotNull BiConsumer<String, Map.Entry<?, Block<?>>> consumer, @NotNull StringBuilder pathBuilder, char separator, boolean deep) {
        //All keys
        for (Map.Entry<?, Block<?>> entry : getValue().entrySet()) {
            //Current length
            int length = pathBuilder.length();
            //Add separator if there is a key already
            if (length > 0)
                pathBuilder.append(separator);
            //Call
            consumer.accept(pathBuilder.append(entry.getKey().toString()).toString(), entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, pathBuilder, separator, true);
            //Reset
            pathBuilder.setLength(length);
        }
    }

    //
    //
    //      -----------------------
    //
    //
    //         Contains methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns whether this section contains anything at the given path.
     *
     * @param path the path to check
     * @return if this section contains anything at the given path
     * @see #getBlockSafe(Path)
     */
    public boolean contains(@NotNull Path path) {
        return getBlockSafe(path).isPresent();
    }

    /**
     * Returns whether this section contains anything at the given path.
     *
     * @param path the path to check
     * @return if this section contains anything at the given path
     * @see #getBlockSafe(String)
     */
    public boolean contains(@NotNull String path) {
        return getBlockSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //      Create section methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Attempts to create sections at the given path, overwrites a mapping if there is one in the way and creates
     * sections along the way; returns the deepest section created (the one at the given path). If there is a section
     * already, nothing is overwritten and the already existing section is returned.
     *
     * @param path the path to create a section at (with all parent sections)
     * @return the created section at the given path, or already existing one
     * @see #createSectionInternal(Object, Block)
     */
    public Section createSection(@NotNull Path path) {
        //Current section
        Section current = this;
        //All keys
        for (int i = 0; i < path.length(); i++)
            //Create
            current = current.createSectionInternal(path.get(i), null);
        //Return
        return current;
    }

    /**
     * Attempts to create sections at the given path, overwrites a mapping if there is one in the way and creates
     * sections along the way; returns the deepest section created (the one at the given path). If there is a section
     * already, nothing is overwritten and the already existing section is returned.
     *
     * @param path the path to create a section at (with all parent sections)
     * @return the created section at the given path, or already existing one
     * @see #createSectionInternal(Object, Block)
     */
    public Section createSection(@NotNull String path) {
        //Index of the last separator + 1
        int lastSeparator = 0;
        //Section
        Section section = this;

        //While true (control statements are inside)
        while (true) {
            //Next separator
            int nextSeparator = path.indexOf(root.getGeneralSettings().getSeparator(), lastSeparator);
            //If found
            if (nextSeparator != -1)
                //Create section
                section = section.createSectionInternal(path.substring(lastSeparator, nextSeparator), null);
            else
                //Break
                break;
            //Set
            lastSeparator = nextSeparator + 1;
        }

        //Return
        return section.createSectionInternal(path.substring(lastSeparator), null);
    }

    /**
     * Attempts to create a section at the given direct key in this section and returns it. If there already is a
     * mapping existing at the key, it is overwritten with a new section. If there is a section already, does not
     * overwrite anything and the already existing section is returned.
     * <p>
     * If the method ends up creating a new section, previous block's comments are copied (see
     * {@link #Section(YamlFile, Section, Object, Path, Block, Map)}).
     *
     * @param key      the key to create a section at
     * @param previous the previous block at this key
     * @return the newly created section or the already existing one
     */
    private Section createSectionInternal(@Nullable Object key, @Nullable Block<?> previous) {
        //Adapt
        Object adapted = adaptKey(key);

        return getSectionSafe(Path.from(adapted)).orElseGet(() -> {
            //The new section
            Section section = new Section(root, Section.this, adapted, getSubPath(adapted), previous, root.getGeneralSettings().getDefaultMap());
            //Add
            getValue().put(adapted, section);
            //Return
            return section;
        });
    }

    //
    //
    //      -----------------------
    //
    //
    //          Setter methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Sets the given value at the given path in this section - overwrites the already existing value (if any). If there
     * are sections missing to the path where the object should be set, they are created along the way.
     * <p>
     * As the value to set, you can give instances of:
     * <ul>
     *     <li><code>null</code>: the object currently stored at the given key in the section will be removed (if any);
     *     only implemented to support Spigot API (such usage is <b>deprecated and will likely be removed</b>) -
     *     please use {@link #remove(Path)} to do so,</li>
     *     <li>{@link Section}: the given section will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Mapping}: the given mapping will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Object, Path, Block, Map)} for more information),</li>
     *     <li><i>anything else</i>: the given value will be encapsulated with {@link Mapping} object and set at the
     *     given key.</li>
     * </ul>
     *
     * @param path  the path to set at
     * @param value the value to set, or <code>null</code> if to delete (<b>deprecated, please use {@link #remove(String)}</b>)
     */
    public void set(@NotNull Path path, @Nullable Object value) {
        //Starting index
        int i = -1;
        //Section
        Section section = this;

        //While not out of bounds
        while (++i < path.length()) {
            //If at the last index
            if (i + 1 >= path.length()) {
                //Call the direct method
                section.setInternal(adaptKey(path.get(i)), value);
                return;
            }

            //Key
            Object key = adaptKey(path.get(i));
            //The block at the key
            Block<?> block = section.getValue().getOrDefault(key, null);
            //Set next section
            section = !(block instanceof Section) ? section.createSectionInternal(key, block) : (Section) block;
        }
    }

    /**
     * Sets the given value at the given path in this section - overwrites the already existing value (if any). If there
     * are sections missing to the path where the object should be set, they are created along the way.
     * <p>
     * As the value to set, you can give instances of:
     * <ul>
     *     <li><code>null</code>: the object currently stored at the given key in the section will be removed (if any);
     *     only implemented to support Spigot API (such usage is <b>deprecated and will likely be removed</b>) -
     *     please use {@link #remove(String)} to do so,</li>
     *     <li>{@link Section}: the given section will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Mapping}: the given mapping will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Object, Path, Block, Map)} for more information),</li>
     *     <li><i>anything else</i>: the given value will be encapsulated with {@link Mapping} object and set at the
     *     given key.</li>
     * </ul>
     * <p>
     * <b>This method is chained and/or based on {@link #getDirectBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param path  the path to set at
     * @param value the value to set, or <code>null</code> if to delete (<b>deprecated, please use {@link #remove(String)}</b>)
     */
    public void set(@NotNull String path, @Nullable Object value) {
        //Index of the last separator + 1
        int lastSeparator = 0;
        //Section
        Section section = this;

        //While true (control statements are inside)
        while (true) {
            //Next separator
            int nextSeparator = path.indexOf(root.getGeneralSettings().getSeparator(), lastSeparator);
            //If found
            if (nextSeparator != -1) {
                //Create section
                section = section.createSection(path.substring(lastSeparator, nextSeparator));
            } else {
                //Set
                section.setInternal(path.substring(lastSeparator), value);
                return;
            }
            //Set
            lastSeparator = nextSeparator + 1;
        }
    }

    /**
     * Internally sets the given value at the given direct key in this section and overwrites the already existing value
     * (if any).
     * <p>
     * <i>Copied from {@link #set(Path, Object)} documentation:</i> As the value to set, you can give instances of:
     * <ul>
     *     <li><code>null</code>: the object currently stored at the given key in the section will be removed (if any);
     *     only implemented to support Spigot API (such usage is <b>deprecated and will likely be removed</b>),</li>
     *     <li>{@link Section}: the given section will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Mapping}: the given mapping will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Object, Path, Block, Map)} for more information),</li>
     *     <li><i>anything else</i>: the given value will be encapsulated with {@link Mapping} object and set at the
     *     given key.</li>
     * </ul>
     *
     * @param key   the (already adapted) key at which to set the value
     * @param value the value to set, or <code>null</code> if to delete (<b>deprecated</b>)
     */
    public void setInternal(@Nullable Object key, @Nullable Object value) {
        //If null (remove)
        // TODO: 2. 10. 2021 Deprecated?
        if (value == null)
            getValue().remove(key);

        //If a section
        if (value instanceof Section) {
            //Cast
            Section section = (Section) value;
            //Set
            getValue().put(key, section);
            //Adapt
            section.adapt(root, this, key, getSubPath(key));
            return;
        } else if (value instanceof Mapping) {
            //Set
            getValue().put(key, (Mapping) value);
            return;
        }

        //If a map
        if (value instanceof Map) {
            //Add
            getValue().put(key, new Section(root, this, key, getSubPath(key), getValue().getOrDefault(key, null), (Map<?, ?>) value));
            return;
        }

        //Block at the path
        Block<?> previous = getValue().get(key);
        //If already existing block is not present
        if (previous == null) {
            //Add
            getValue().put(key, new Mapping(null, null, value));
            return;
        }

        //Add with existing block's comments
        getValue().put(key, new Mapping(previous, value));
    }

    //
    //
    //      -----------------------
    //
    //
    //          Remove methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Removes value (block, to be more specific) at the given path (if any); returns <code>true</code> if successfully
     * removed. The method returns <code>false</code> if and only value at the path does not exist.
     *
     * @param path the path to remove the value at
     * @return if any value was removed
     */
    public boolean remove(@NotNull Path path) {
        return removeInternal(getParent(path).orElse(null), adaptKey(path.get(path.length() - 1)));
    }

    /**
     * Removes value (block, to be more specific) at the given path (if any); returns <code>true</code> if successfully
     * removed. The method returns <code>false</code> if and only value at the path does not exist.
     * <p>
     * <b>This method is chained and/or based on {@link #getDirectBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param path the path to remove the value at
     * @return if any value was removed
     */
    public boolean remove(@NotNull String path) {
        return removeInternal(getParent(path).orElse(null), path.substring(path.lastIndexOf(root.getGeneralSettings().getSeparator()) + 1));
    }

    /**
     * An internal method used to actually remove the value. Created to extract common parts from both removal-oriented
     * methods.
     * <p>
     * Returns <code>false</code> if the parent section is <code>null</code> (meaning there is not any value at the
     * given key as there is not a parent section), otherwise, returns if anything was present (and removed) at the
     * given key.
     *
     * @param parent the parent section, or <code>null</code> if it does not exist
     * @param key    the last key; key to check in the parent section, adapted using {@link #adaptKey(Object)}
     * @return if any value has been removed
     */
    private boolean removeInternal(@Nullable Section parent, @Nullable Object key) {
        //If the parent is null
        if (parent == null)
            return false;
        //Remove
        return parent.getValue().remove(key) != null;
    }

    //
    //
    //      -----------------------
    //
    //
    //          Block methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns block at the given path encapsulated in an instance of {@link Optional}. If there is no block present (no
     * value) at the given path, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Mapping} (section map entry) or {@link Section} (the value
     * is a section) instances. See the {wiki} for more information.
     * <p>
     * <b>Functionality notes:</b> When individual elements (keys) of the given path are traversed, they are (without
     * modifying the path object given - it is immutable) adapted to the current key mode setting (see {@link #adaptKey(Object)}).
     * <p>
     * <b>This is one of the foundation methods, upon which the functionality of other methods in this class is built.</b>
     *
     * @param path the path to get the block at
     * @return block at the given path encapsulated in an optional
     */
    public Optional<Block<?>> getBlockSafe(@NotNull Path path) {
        return getSafeInternal(path, false);
    }

    /**
     * Returns block at the given direct key encapsulated in an instance of {@link Optional}. If there is no block
     * present (no value) at the given key, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Mapping} (section map entry) or {@link Section} (the value
     * is a section) instances. See the {wiki} for more information.
     * <p>
     * <b>A direct key</b> means the key is referring to object in this section directly (e.g. does not work like path,
     * which might - if consisting of multiple keys - refer to subsections) - similar to {@link Map#get(Object)}.
     * <p>
     * <b>Please note</b> that this class also supports <code>null</code> keys, or empty string keys (<code>""</code>)
     * as specified by YAML 1.2 spec. This also means that compatibility with Spigot/BungeeCord API is not maintained
     * regarding empty string keys, where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>This is one of the foundation methods, upon which the functionality of other methods in this class is built.</b>
     *
     * @param key the key to get the block at
     * @return block at the given path encapsulated in an optional
     */
    private Optional<Block<?>> getDirectBlockSafe(@Nullable Object key) {
        return Optional.ofNullable(getValue().get(adaptKey(key)));
    }

    /**
     * Returns block at the given string path encapsulated in an instance of {@link Optional}. If there is no block
     * present (no value) at the given path, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Mapping} (section map entry) or {@link Section} (the value
     * is a section) instances. See the {wiki} for more information.
     * <p>
     * <b>Functionality notes:</b> The given path must contain individual keys separated using the separator character
     * configured using {@link GeneralSettings.Builder#setSeparator(char)}, unlike storing each key as an array
     * element as {@link Path} objects do, which allows for easier accessibility.
     * <p>
     * If the given string path does not contain the separator character (is only one key), returns the result of
     * {@link #getDirectBlockSafe(Object)} with the given path as the parameter.
     * <p>
     * Otherwise, traverses appropriate subsections determined by the keys contained (in order as defined except the last one)
     * and returns the block at the last key defined in the given path. For example, for path separator <code>'.'</code>
     * and path <code>a.b.c</code>, this method firstly attempts to get the section at key <code>"a"</code> in
     * <b>this</b> section, <b>then</b> section <code>b</code> in <b>that</b> (keyed as <code>"a"</code>)
     * section and <b>finally</b> the block at <code>"c"</code> in <b>that</b> (keyed as <code>"a.b"</code>) section.
     * <p>
     * We can also interpret this behaviour as a call to {@link #getBlockSafe(Path)} with path created via constructor
     * {@link Path#fromString(String, char)} (which effectively splits the given string path into separate string keys
     * according to the separator).
     * <p>
     * This method works independently of the root's {@link GeneralSettings#getKeyMode()}. However, as the given path
     * contains individual <b>string</b> keys, if set to {@link GeneralSettings.KeyMode#OBJECT}, you will
     * only be able to get (traversing included) keys which were parsed as strings (no integer, boolean... or
     * <code>null</code> keys) by SnakeYAML Engine (please see <a href="https://yaml.org/spec/1.2.2/">YAML 1.2 spec</a>).
     * If such functionality is needed, use {@link #getBlockSafe(Path)} instead, please.
     * <p>
     * <b>Please note</b> that compatibility with Spigot/BungeeCord API is not maintained regarding empty string keys,
     * where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>This is one of the foundation methods, upon which the functionality of other methods in this class is built.</b>
     *
     * @param path the string path to get the block at
     * @return block at the given path encapsulated in an optional
     */
    public Optional<Block<?>> getBlockSafe(@NotNull String path) {
        return path.indexOf(root.getGeneralSettings().getSeparator()) != -1 ? getSafeInternalString(path, false) : getDirectBlockSafe(path);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getBlockSafe(Path)}.
     * <p>
     * If it's an empty {@link Optional}, returns <code>null</code>.
     *
     * @param path the string path to get the block at
     * @return block at the given path, or <code>null</code> if it doesn't exist
     */
    public Block<?> getBlock(@NotNull Path path) {
        return getBlockSafe(path).orElse(null);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getBlockSafe(String)}.
     * <p>
     * If it's an empty {@link Optional}, returns <code>null</code>.
     *
     * @param path the string path to get the block at
     * @return block at the given path, or <code>null</code> if it doesn't exist
     */
    public Block<?> getBlock(@NotNull String path) {
        return getBlockSafe(path).orElse(null);
    }

    //
    //
    //       -----------------------
    //
    //
    // Internal block getter method processors
    //
    //
    //       -----------------------
    //
    //

    /**
     * Internal method which returns a block, encapsulated in an instance of {@link Optional}, at the given string path
     * in this section. If there is no block present, returns an empty optional.
     * <p>
     * This method does not interact with any others defined in this class.
     *
     * @param path   the path to get the block at
     * @param parent if searching for the parent section of the given path
     * @return block at the given path encapsulated in an optional
     */
    private Optional<Block<?>> getSafeInternalString(@NotNull String path, boolean parent) {
        //Index of the last separator + 1
        int lastSeparator = 0;
        //Section
        Section section = this;

        //While true (control statements are inside)
        while (true) {
            //Next separator
            int nextSeparator = path.indexOf(root.getGeneralSettings().getSeparator(), lastSeparator);
            //If not found
            if (nextSeparator == -1)
                break;

            //The block at the key
            Block<?> block = section.getValue().getOrDefault(path.substring(lastSeparator, nextSeparator), null);
            //If not a section
            if (!(block instanceof Section))
                return Optional.empty();
            //Set next section
            section = (Section) block;
            //Set
            lastSeparator = nextSeparator + 1;
        }

        //Return
        return Optional.ofNullable(parent ? section : section.getValue().get(path.substring(lastSeparator)));
    }

    /**
     * Internal method which returns a block, encapsulated in an instance of {@link Optional}, at the given string path
     * in this section. If there is no block present, returns an empty optional.
     * <p>
     * This method does not interact with any others defined in this class.
     *
     * @param path   the path to get the block at
     * @param parent if searching for the parent section of the given path
     * @return block at the given path encapsulated in an optional
     */
    private Optional<Block<?>> getSafeInternal(@NotNull Path path, boolean parent) {
        //Starting index
        int i = -1;
        //Section
        Section section = this;

        //While not at the parent section
        while (++i < path.length() - 1) {
            //The block at the key
            Block<?> block = section.getValue().getOrDefault(adaptKey(path.get(i)), null);
            //If not a section
            if (!(block instanceof Section))
                return Optional.empty();
            //Set next section
            section = (Section) block;
        }

        //Return
        return Optional.ofNullable(parent ? section : section.getValue().get(adaptKey(path.get(i))));
    }

    //
    //
    //      -----------------------
    //
    //
    //       Parent section methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns section at the parent path of the given one, encapsulated in an instance of {@link Optional}. Said
     * differently, the returned section is the result of {@link #getSection(Path)} called for the same path as given
     * here, with the last path element removed.
     * <p>
     * That means, this method ignores if the given path represents an existing block and if block at that parent path
     * is not a section (is a mapping), returns an empty optional.
     *
     * @param path the path to get the parent section from
     * @return section at the parent path from the given one encapsulated in an optional
     */
    public Optional<Section> getParent(@NotNull Path path) {
        return getSafeInternal(path, true).map(block -> block instanceof Section ? (Section) block : null);
    }

    /**
     * Returns section at the parent path of the given one, encapsulated in an instance of {@link Optional}. Said
     * differently, the returned section is effectively the result of {@link #getSection(Path)} called for the same path
     * as given here, with the last path element removed ({@link Path#parent()}).
     * <p>
     * That means, this method ignores if the given path represents an existing block and if block at that parent path
     * is not a section (is a mapping), returns an empty optional.
     *
     * @param path the path to get the parent section from
     * @return section at the parent path from the given one encapsulated in an optional
     */
    public Optional<Section> getParent(@NotNull String path) {
        return getSafeInternalString(path, true).map(block -> block instanceof Section ? (Section) block : null);
    }

    //
    //
    //      -----------------------
    //
    //
    //       General getter methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given path, or if it is a section, the
     * corresponding {@link Section} instance; encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getBlockSafe(Path)}. If the returned optional is not empty and
     * does not contain an instance of {@link Section}, returns the encapsulated value returned by
     * {@link Block#getValue()} - the actual value (list, integer...).
     *
     * @param path the path to get the value at
     * @return the value, or section at the given path
     */
    public Optional<Object> getSafe(@NotNull Path path) {
        return getBlockSafe(path).map(block -> block instanceof Section ? block : block.getValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given path, or if it is a section, the
     * corresponding {@link Section} instance; encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getBlockSafe(String)}. If the returned optional is not empty and
     * does not contain an instance of {@link Section}, returns the encapsulated value returned by
     * {@link Block#getValue()} - the actual value (list, integer...).
     *
     * @param path the path to get the value at
     * @return the value, or section at the given path
     */
    public Optional<Object> getSafe(@NotNull String path) {
        return getBlockSafe(path).map(block -> block instanceof Section ? block : block.getValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given path, or if it is a section, the
     * corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns default value
     * defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     *
     * @param path the path to get the value at
     * @return the value at the given path, or default according to the documentation above
     */
    public Object get(@NotNull Path path) {
        return getSafe(path).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given path, or if it is a section, the
     * corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns default value
     * defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     *
     * @param path the path to get the value at
     * @return the value at the given path, or default according to the documentation above
     */
    public Object get(@NotNull String path) {
        return getSafe(path).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given path, or if it is a section, the
     * corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns the provided default.
     *
     * @param path the path to get the value at
     * @param def  the default value
     * @return the value at the given path, or default according to the documentation above
     */
    public Object get(@NotNull Path path, @Nullable Object def) {
        return getSafe(path).orElse(def);
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given path, or if it is a section, the
     * corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns the provided default.
     *
     * @param path the path to get the value at
     * @param def  the default value
     * @return the value at the given path, or default according to the documentation above
     */
    public Object get(@NotNull String path, @Nullable Object def) {
        return getSafe(path).orElse(def);
    }

    //
    //
    //      -----------------------
    //
    //
    //        Custom type methods
    //
    //
    //      -----------------------
    //
    //


    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class; encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (block's actual
     * value or {@link Section} instance) is not castable to the given type, returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getSafe(Path)} cast to the given class if not empty (or an empty
     * optional if the returned is empty, or types are incompatible).
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(@NotNull Path path, @NotNull Class<T> clazz) {
        return getSafe(path).map((object) -> clazz.isInstance(object) ? (T) object :
                isNumber(object.getClass()) && isNumber(clazz) ? (T) convertNumber(object, clazz) :
                        NON_NUMERICAL_CONVERSIONS.containsKey(object.getClass()) && NON_NUMERICAL_CONVERSIONS.containsKey(clazz) ? (T) object : null);
    }


    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class; encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (block's actual
     * value or {@link Section} instance) is not castable to the given type, returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getSafe(String)} cast to the given class if not empty (or an empty
     * optional if the returned is empty, or types are incompatible).
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(@NotNull String path, @NotNull Class<T> clazz) {
        return getSafe(path).map((object) -> clazz.isInstance(object) ? (T) object :
                isNumber(object.getClass()) && isNumber(clazz) ? (T) convertNumber(object, clazz) :
                        NON_NUMERICAL_CONVERSIONS.containsKey(object.getClass()) && NON_NUMERICAL_CONVERSIONS.containsKey(clazz) ? (T) object : null);
    }

    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (block's actual
     * value or {@link Section} instance) is not castable to the given type, returns <code>null</code>.
     * <p>
     * More formally, returns the result of {@link #getAs(Path, Class, Object)} with <code>null</code> default.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type, or <code>null</code> according to the documentation above
     */
    public <T> T getAs(@NotNull Path path, @NotNull Class<T> clazz) {
        return getAs(path, clazz, null);
    }

    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (block's actual
     * value or {@link Section} instance) is not castable to the given type, returns <code>null</code>.
     * <p>
     * More formally, returns the result of {@link #getAs(String, Class, Object)} with <code>null</code> default.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type, or <code>null</code> according to the documentation above
     */
    public <T> T getAs(@NotNull String path, @NotNull Class<T> clazz) {
        return getAs(path, clazz, null);
    }

    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (block's actual
     * value or {@link Section} instance) is not castable to the given type, returns the provided default.
     * <p>
     * More formally, returns the result of {@link #getAsSafe(Path, Class)} or the provided default if the returned
     * optional is empty.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to get the value at
     * @param clazz class of the target type
     * @param def   the default value
     * @param <T>   the target type
     * @return the value cast to the given type, or default according to the documentation above
     */
    public <T> T getAs(@NotNull Path path, @NotNull Class<T> clazz, @Nullable T def) {
        return getAsSafe(path, clazz).orElse(def);
    }

    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (block's actual
     * value or {@link Section} instance) is not castable to the given type, returns the provided default.
     * <p>
     * More formally, returns the result of {@link #getAsSafe(String, Class)} or the provided default if the returned
     * optional is empty.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to get the value at
     * @param clazz class of the target type
     * @param def   the default value
     * @param <T>   the target type
     * @return the value cast to the given type, or default according to the documentation above
     */
    public <T> T getAs(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return getAsSafe(path, clazz).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only value (list, integer... or {@link Section}) at the given path exists, and it
     * is an instance of the given class.
     * <p>
     * More formally, returns {@link Optional#isPresent()} called on the result of {@link #getAsSafe(Path, Class)}.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to check the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return if a value exists at the given path, and it is an instance of the given class
     */
    public <T> boolean is(@NotNull Path path, @NotNull Class<T> clazz) {
        return getAsSafe(path, clazz).isPresent();
    }

    /**
     * Returns <code>true</code> if and only value (list, integer... or {@link Section}) at the given path exists, and it
     * is an instance of the given class.
     * <p>
     * More formally, returns {@link Optional#isPresent()} called on the result of {@link #getAsSafe(String, Class)}.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations and one
     * of each kind. Casting between any primitive type, and it's non-primitive representation is also supported.
     *
     * @param path  the path to check the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return if a value exists at the given path, and it is an instance of the given class
     */
    public <T> boolean is(@NotNull String path, @NotNull Class<T> clazz) {
        return getAsSafe(path, clazz).isPresent();
    }

    // END OF BASE METHODS, DEPENDENT (DERIVED) METHODS FOLLOWING
    //
    //
    //      -----------------------
    //
    //
    //          Section methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns section at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given path, or is not a {@link Section}, returns an empty optional.
     *
     * @param path the path to get the section at
     * @return the section at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Section> getSectionSafe(@NotNull Path path) {
        return getAsSafe(path, Section.class);
    }

    /**
     * Returns section at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given path, or is not a {@link Section}, returns an empty optional.
     *
     * @param path the path to get the section at
     * @return the section at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Section> getSectionSafe(@NotNull String path) {
        return getAsSafe(path, Section.class);
    }

    /**
     * Returns section at the given path. If nothing is present given path, or is not a {@link Section}, returns
     * <code>null</code>.
     *
     * @param path the path to get the section at
     * @return the section at the given path, or default according to the documentation above
     * @see #getSection(Path, Section)
     */
    public Section getSection(@NotNull Path path) {
        return getSection(path, null);
    }

    /**
     * Returns section at the given path. If nothing is present at the given path, or is not a {@link Section}, returns
     * <code>null</code>.
     *
     * @param path the path to get the section at
     * @return the section at the given path, or default according to the documentation above
     * @see #getSection(String, Section)
     */
    public Section getSection(@NotNull String path) {
        return getSection(path, null);
    }

    /**
     * Returns section at the given path. If nothing is present at the given path, or is not a {@link Section}, returns
     * the provided default.
     *
     * @param path the path to get the section at
     * @param def  the default value
     * @return the section at the given path, or default according to the documentation above
     * @see #getSectionSafe(Path)
     */
    public Section getSection(@NotNull Path path, @Nullable Section def) {
        return getSectionSafe(path).orElse(def);
    }

    /**
     * Returns section at the given path. If nothing is present at the given path, or is not a {@link Section}, returns
     * the provided default.
     *
     * @param path the path to get the section at
     * @param def  the default value
     * @return the section at the given path, or default according to the documentation above
     * @see #getSectionSafe(String)
     */
    public Section getSection(@NotNull String path, @Nullable Section def) {
        return getSectionSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Section}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a section
     * @see #getSectionSafe(Path)
     */
    public boolean isSection(@NotNull Path path) {
        return getSectionSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Section}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a section
     * @see #getSectionSafe(String)
     */
    public boolean isSection(@NotNull String path) {
        return getSectionSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          String methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns string at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or
     * {@link Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by
     * converting them to one using {@link Object#toString()}.
     *
     * @param path the path to get the string at
     * @return the string at the given path
     * @see #getSafe(Path)
     */
    public Optional<String> getStringSafe(@NotNull Path path) {
        return getSafe(path).map((object) -> object instanceof String || object instanceof Number || object instanceof Boolean ? object.toString() : null);
    }

    /**
     * Returns string at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or
     * {@link Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by
     * converting them to one using {@link Object#toString()}.
     *
     * @param path the path to get the string at
     * @return the string at the given path
     * @see #getSafe(String)
     */
    public Optional<String> getStringSafe(@NotNull String path) {
        return getSafe(path).map((object) -> object instanceof String || object instanceof Number || object instanceof Boolean ? object.toString() : null);
    }

    /**
     * Returns string at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings {@link GeneralSettings#getDefaultString()}.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or
     * {@link Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by
     * converting them to one using {@link Object#toString()}.
     *
     * @param path the path to get the string at
     * @return the string at the given path, or default according to the documentation above
     * @see #getString(Path, String)
     */
    public String getString(@NotNull Path path) {
        return getString(path, root.getGeneralSettings().getDefaultString());
    }

    /**
     * Returns string at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings {@link GeneralSettings#getDefaultString()}.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or
     * {@link Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by
     * converting them to one using {@link Object#toString()}.
     *
     * @param path the path to get the string at
     * @return the string at the given path, or default according to the documentation above
     * @see #getString(String, String)
     */
    public String getString(@NotNull String path) {
        return getString(path, root.getGeneralSettings().getDefaultString());
    }

    /**
     * Returns string at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or
     * {@link Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by
     * converting them to one using {@link Object#toString()}.
     *
     * @param path the path to get the string at
     * @param def  the default value
     * @return the string at the given path, or default according to the documentation above
     * @see #getStringSafe(Path)
     */
    public String getString(@NotNull Path path, @Nullable String def) {
        return getStringSafe(path).orElse(def);
    }

    /**
     * Returns string at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or
     * {@link Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by
     * converting them to one using {@link Object#toString()}.
     *
     * @param path the path to get the string at
     * @param def  the default value
     * @return the string at the given path, or default according to the documentation above
     * @see #getStringSafe(String)
     */
    public String getString(@NotNull String path, @Nullable String def) {
        return getStringSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link String}, or any other
     * compatible type. Please learn more at {@link #getStringSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a string, or any other compatible type according to the
     * documentation above
     * @see #getStringSafe(Path)
     */
    public boolean isString(@NotNull Path path) {
        return getStringSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link String}, or any other
     * compatible type. Please learn more at {@link #getStringSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a string, or any other compatible type according to the
     * documentation above
     * @see #getStringSafe(String)
     */
    public boolean isString(@NotNull String path) {
        return getStringSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //         Character methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns char at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char at
     * @return the char at the given path
     * @see #getSafe(Path)
     */
    public Optional<Character> getCharSafe(@NotNull Path path) {
        return getSafe(path).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
    }

    /**
     * Returns char at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char at
     * @return the char at the given path
     * @see #getSafe(String)
     */
    public Optional<Character> getCharSafe(@NotNull String path) {
        return getSafe(path).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
    }

    /**
     * Returns char at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultChar()}.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char at
     * @return the char at the given path, or default according to the documentation above
     * @see #getChar(Path, Character)
     */
    public Character getChar(@NotNull Path path) {
        return getChar(path, root.getGeneralSettings().getDefaultChar());
    }

    /**
     * Returns char at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultChar()}.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char at
     * @return the char at the given path, or default according to the documentation above
     * @see #getChar(String, Character)
     */
    public Character getChar(@NotNull String path) {
        return getChar(path, root.getGeneralSettings().getDefaultChar());
    }

    /**
     * Returns char at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char at
     * @param def  the default value
     * @return the char at the given path, or default according to the documentation above
     * @see #getCharSafe(Path)
     */
    public Character getChar(@NotNull Path path, @Nullable Character def) {
        return getCharSafe(path).orElse(def);
    }

    /**
     * Returns char at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char at
     * @param def  the default value
     * @return the char at the given path, or default according to the documentation above
     * @see #getCharSafe(String)
     */
    public Character getChar(@NotNull String path, @Nullable Character def) {
        return getCharSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Character}, or any other
     * compatible type. Please learn more at {@link #getCharSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a character, or any other compatible type according to the
     * documentation above
     * @see #getCharSafe(Path)
     */
    public boolean isChar(@NotNull Path path) {
        return getCharSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Character}, or any other
     * compatible type. Please learn more at {@link #getCharSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a character, or any other compatible type according to the
     * documentation above
     * @see #getCharSafe(String)
     */
    public boolean isChar(@NotNull String path) {
        return getCharSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //         Integer methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns integer at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the integer at
     * @return the integer at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Integer> getIntSafe(@NotNull Path path) {
        return toInt(getAsSafe(path, Number.class));
    }

    /**
     * Returns integer at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the integer at
     * @return the integer at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Integer> getIntSafe(@NotNull String path) {
        return toInt(getAsSafe(path, Number.class));
    }

    /**
     * Returns integer at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Integer} as defined below).
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the integer at
     * @return the integer at the given path, or default according to the documentation above
     * @see #getInt(Path, Integer)
     */
    public Integer getInt(@NotNull Path path) {
        return getInt(path, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    /**
     * Returns integer at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Integer} as defined below).
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the integer at
     * @return the integer at the given path, or default according to the documentation above
     * @see #getInt(String, Integer)
     */
    public Integer getInt(@NotNull String path) {
        return getInt(path, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    /**
     * Returns integer at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the integer at
     * @param def  the default value
     * @return the integer at the given path, or default according to the documentation above
     * @see #getIntSafe(Path)
     */
    public Integer getInt(@NotNull Path path, @Nullable Integer def) {
        return getIntSafe(path).orElse(def);
    }

    /**
     * Returns integer at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the integer at
     * @param def  the default value
     * @return the integer at the given path, or default according to the documentation above
     * @see #getIntSafe(Path)
     */
    public Integer getInt(@NotNull String path, @Nullable Integer def) {
        return getIntSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is an {@link Integer}, or any other
     * compatible type. Please learn more at {@link #getIntSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getIntSafe(Path)
     */
    public boolean isInt(@NotNull Path path) {
        return getIntSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is an {@link Integer}, or any other
     * compatible type. Please learn more at {@link #getIntSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getIntSafe(String)
     */
    public boolean isInt(@NotNull String path) {
        return getIntSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //         BigInteger methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns big integer at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link BigInteger#valueOf(long)}
     * (which might involve rounding or truncating).
     *
     * @param path the path to get the big integer at
     * @return the big integer at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<BigInteger> getBigIntSafe(@NotNull Path path) {
        return getAsSafe(path, Number.class).map(number -> number instanceof BigInteger ? (BigInteger) number : BigInteger.valueOf(number.longValue()));
    }

    /**
     * Returns big integer at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link BigInteger#valueOf(long)}
     * (which might involve rounding or truncating).
     *
     * @param path the path to get the big integer at
     * @return the big integer at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<BigInteger> getBigIntSafe(@NotNull String path) {
        return getAsSafe(path, Number.class).map(number -> number instanceof BigInteger ? (BigInteger) number : BigInteger.valueOf(number.longValue()));
    }

    /**
     * Returns big integer at the given path. If nothing is present at the given path, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger} as defined below).
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link BigInteger#valueOf(long)}
     * (which might involve rounding or truncating).
     *
     * @param path the path to get the big integer at
     * @return the big integer at the given path
     * @see #getBigInt(Path, BigInteger)
     */
    public BigInteger getBigInt(@NotNull Path path) {
        return getBigInt(path, BigInteger.valueOf(root.getGeneralSettings().getDefaultNumber().longValue()));
    }

    /**
     * Returns big integer at the given path. If nothing is present at the given path, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger} as defined below).
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link BigInteger#valueOf(long)}
     * (which might involve rounding or truncating).
     *
     * @param path the path to get the big integer at
     * @return the big integer at the given path
     * @see #getBigInt(Path, BigInteger)
     */
    public BigInteger getBigInt(@NotNull String path) {
        return getBigInt(path, BigInteger.valueOf(root.getGeneralSettings().getDefaultNumber().longValue()));
    }

    /**
     * Returns big integer at the given path. If nothing is present at the given path, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link BigInteger#valueOf(long)}
     * (which might involve rounding or truncating).
     *
     * @param path the path to get the big integer at
     * @param def  the default value
     * @return the big integer at the given path
     * @see #getBigIntSafe(Path)
     */
    public BigInteger getBigInt(@NotNull Path path, @Nullable BigInteger def) {
        return getBigIntSafe(path).orElse(def);
    }

    /**
     * Returns big integer at the given path. If nothing is present at the given path, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link BigInteger#valueOf(long)}
     * (which might involve rounding or truncating).
     *
     * @param path the path to get the big integer at
     * @param def  the default value
     * @return the big integer at the given path
     * @see #getBigIntSafe(String)
     */
    public BigInteger getBigInt(@NotNull String path, @Nullable BigInteger def) {
        return getBigIntSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link BigInteger}, or any other
     * compatible type. Please learn more at {@link #getBigIntSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getBigIntSafe(Path)
     */
    public boolean isBigInt(@NotNull Path path) {
        return getBigIntSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link BigInteger}, or any other
     * compatible type. Please learn more at {@link #getBigIntSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getBigIntSafe(Path)
     */
    public boolean isBigInt(@NotNull String path) {
        return getBigIntSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //         Boolean methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns boolean at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given path, or is not a {@link Boolean} (or the primitive variant), returns an empty optional.
     *
     * @param path the path to get the boolean at
     * @return the boolean at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Boolean> getBooleanSafe(@NotNull Path path) {
        return getAsSafe(path, Boolean.class);
    }

    /**
     * Returns boolean at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given path, or is not a {@link Boolean} (or the primitive variant), returns an empty optional.
     *
     * @param path the path to get the boolean at
     * @return the boolean at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Boolean> getBooleanSafe(@NotNull String path) {
        return getAsSafe(path, Boolean.class);
    }

    /**
     * Returns boolean at the given path. If nothing is present at the given path, or is not a {@link Boolean} (or the
     * primitive variant), returns default value defined by root's general settings {@link GeneralSettings#getDefaultBoolean()}.
     *
     * @param path the path to get the boolean at
     * @return the boolean at the given path, or default according to the documentation above
     * @see #getBoolean(Path, Boolean)
     */
    public Boolean getBoolean(@NotNull Path path) {
        return getBoolean(path, root.getGeneralSettings().getDefaultBoolean());
    }

    /**
     * Returns boolean at the given path. If nothing is present at the given path, or is not a {@link Boolean} (or the
     * primitive variant), returns default value defined by root's general settings {@link GeneralSettings#getDefaultBoolean()}.
     *
     * @param path the path to get the boolean at
     * @return the boolean at the given path, or default according to the documentation above
     * @see #getBoolean(String, Boolean)
     */
    public Boolean getBoolean(@NotNull String path) {
        return getBoolean(path, root.getGeneralSettings().getDefaultBoolean());
    }

    /**
     * Returns boolean at the given path. If nothing is present at the given path, or is not a {@link Boolean} (or the
     * primitive variant), returns the provided default.
     *
     * @param path the path to get the boolean at
     * @param def  the default value
     * @return the boolean at the given path, or default according to the documentation above
     * @see #getBooleanSafe(Path)
     */
    public Boolean getBoolean(@NotNull Path path, @Nullable Boolean def) {
        return getBooleanSafe(path).orElse(def);
    }

    /**
     * Returns boolean at the given path. If nothing is present at the given path, or is not a {@link Boolean} (or the
     * primitive variant), returns the provided default.
     *
     * @param path the path to get the boolean at
     * @param def  the default value
     * @return the boolean at the given path, or default according to the documentation above
     * @see #getBooleanSafe(String)
     */
    public Boolean getBoolean(@NotNull String path, @Nullable Boolean def) {
        return getBooleanSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Boolean} (or the
     * primitive variant).
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a boolean
     * @see #getBooleanSafe(Path)
     */
    public boolean isBoolean(@NotNull Path path) {
        return getBooleanSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Boolean} (or the
     * primitive variant).
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a boolean
     * @see #getBooleanSafe(String)
     */
    public boolean isBoolean(@NotNull String path) {
        return getBooleanSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          Double methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns double at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the double at
     * @return the double at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Double> getDoubleSafe(@NotNull Path path) {
        return toDouble(getAsSafe(path, Number.class));
    }

    /**
     * Returns double at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the double at
     * @return the double at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Double> getDoubleSafe(@NotNull String path) {
        return toDouble(getAsSafe(path, Number.class));
    }

    /**
     * Returns double at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Double} as defined below).
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the double at
     * @return the double at the given path, or default according to the documentation above
     * @see #getDouble(Path, Double)
     */
    public Double getDouble(@NotNull Path path) {
        return getDouble(path, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    /**
     * Returns double at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Double} as defined below).
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the double at
     * @return the double at the given path, or default according to the documentation above
     * @see #getDouble(String, Double)
     */
    public Double getDouble(@NotNull String path) {
        return getDouble(path, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    /**
     * Returns double at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the double at
     * @param def  the default value
     * @return the double at the given path, or default according to the documentation above
     * @see #getDoubleSafe(Path)
     */
    public Double getDouble(@NotNull Path path, @Nullable Double def) {
        return getDoubleSafe(path).orElse(def);
    }

    /**
     * Returns double at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the double at
     * @param def  the default value
     * @return the double at the given path, or default according to the documentation above
     * @see #getDoubleSafe(String)
     */
    public Double getDouble(@NotNull String path, @Nullable Double def) {
        return getDoubleSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Double}, or any other
     * compatible type. Please learn more at {@link #getDoubleSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a double, or any other compatible type according to the
     * documentation above
     * @see #getDoubleSafe(Path)
     */
    public boolean isDouble(@NotNull Path path) {
        return getDoubleSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Double}, or any other
     * compatible type. Please learn more at {@link #getDoubleSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a double, or any other compatible type according to the
     * documentation above
     * @see #getDoubleSafe(String)
     */
    public boolean isDouble(@NotNull String path) {
        return getDoubleSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          Float methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns float at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the float at
     * @return the float at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Float> getFloatSafe(@NotNull Path path) {
        return toFloat(getAsSafe(path, Number.class));
    }

    /**
     * Returns float at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the float at
     * @return the float at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Float> getFloatSafe(@NotNull String path) {
        return toFloat(getAsSafe(path, Number.class));
    }

    /**
     * Returns float at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Float} as defined below).
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the float at
     * @return the float at the given path, or default according to the documentation above
     * @see #getFloat(Path, Float)
     */
    public Float getFloat(@NotNull Path path) {
        return getFloat(path, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    /**
     * Returns float at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Float} as defined below).
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the float at
     * @return the float at the given path, or default according to the documentation above
     * @see #getFloat(Path, Float)
     */
    public Float getFloat(@NotNull String path) {
        return getFloat(path, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    /**
     * Returns float at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the float at
     * @param def  the default value
     * @return the float at the given path, or default according to the documentation above
     * @see #getFloatSafe(Path)
     */
    public Float getFloat(@NotNull Path path, @Nullable Float def) {
        return getFloatSafe(path).orElse(def);
    }

    /**
     * Returns float at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the float at
     * @param def  the default value
     * @return the float at the given path, or default according to the documentation above
     * @see #getFloatSafe(String)
     */
    public Float getFloat(@NotNull String path, @Nullable Float def) {
        return getFloatSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Float}, or any other
     * compatible type. Please learn more at {@link #getFloatSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a float, or any other compatible type according to the
     * documentation above
     * @see #getFloatSafe(Path)
     */
    public boolean isFloat(@NotNull Path path) {
        return getFloatSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Float}, or any other
     * compatible type. Please learn more at {@link #getFloatSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a float, or any other compatible type according to the
     * documentation above
     * @see #getFloatSafe(String)
     */
    public boolean isFloat(@NotNull String path) {
        return getFloatSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          Byte methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns byte at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the byte at
     * @return the byte at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Byte> getByteSafe(@NotNull Path path) {
        return toByte(getAsSafe(path, Number.class));
    }

    /**
     * Returns byte at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the byte at
     * @return the byte at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Byte> getByteSafe(@NotNull String path) {
        return toByte(getAsSafe(path, Number.class));
    }

    /**
     * Returns byte at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Byte} as defined below).
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the byte at
     * @return the byte at the given path, or default according to the documentation above
     * @see #getByte(Path, Byte)
     */
    public Byte getByte(@NotNull Path path) {
        return getByte(path, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    /**
     * Returns byte at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Byte} as defined below).
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the byte at
     * @return the byte at the given path, or default according to the documentation above
     * @see #getByte(String, Byte)
     */
    public Byte getByte(@NotNull String path) {
        return getByte(path, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    /**
     * Returns byte at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the byte at
     * @return the byte at the given path, or default according to the documentation above
     * @see #getByteSafe(Path)
     */
    public Byte getByte(@NotNull Path path, @Nullable Byte def) {
        return getByteSafe(path).orElse(def);
    }

    /**
     * Returns byte at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the byte at
     * @return the byte at the given path, or default according to the documentation above
     * @see #getByteSafe(String)
     */
    public Byte getByte(@NotNull String path, @Nullable Byte def) {
        return getByteSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Byte}, or any other
     * compatible type. Please learn more at {@link #getByteSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a byte, or any other compatible type according to the
     * documentation above
     * @see #getByteSafe(Path)
     */
    public boolean isByte(@NotNull Path path) {
        return getByteSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Byte}, or any other
     * compatible type. Please learn more at {@link #getByteSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a byte, or any other compatible type according to the
     * documentation above
     * @see #getByteSafe(String)
     */
    public boolean isByte(@NotNull String path) {
        return getByteSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          Long methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns long at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the long at
     * @return the long at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Long> getLongSafe(@NotNull Path path) {
        return toLong(getAsSafe(path, Number.class));
    }

    /**
     * Returns long at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the long at
     * @return the long at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Long> getLongSafe(String path) {
        return toLong(getAsSafe(path, Number.class));
    }

    /**
     * Returns long at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Long} as defined below).
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the long at
     * @return the long at the given path, or default according to the documentation above
     * @see #getLong(Path, Long)
     */
    public Long getLong(@NotNull Path path) {
        return getLong(path, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    /**
     * Returns long at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Long} as defined below).
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the long at
     * @return the long at the given path, or default according to the documentation above
     * @see #getLong(Path, Long)
     */
    public Long getLong(@NotNull String path) {
        return getLong(path, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    /**
     * Returns long at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the long at
     * @return the long at the given path, or default according to the documentation above
     * @see #getLongSafe(Path)
     */
    public Long getLong(@NotNull Path path, @Nullable Long def) {
        return getLongSafe(path).orElse(def);
    }

    /**
     * Returns long at the given path. If nothing is present at the given path, or is not an instance of any compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the long at
     * @return the long at the given path, or default according to the documentation above
     * @see #getLongSafe(String)
     */
    public Long getLong(@NotNull String path, @Nullable Long def) {
        return getLongSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Long}, or any other
     * compatible type. Please learn more at {@link #getLongSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a long, or any other compatible type according to the
     * documentation above
     * @see #getLongSafe(Path)
     */
    public boolean isLong(@NotNull Path path) {
        return getLongSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Long}, or any other
     * compatible type. Please learn more at {@link #getLongSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a long, or any other compatible type according to the
     * documentation above
     * @see #getLongSafe(String)
     */
    public boolean isLong(@NotNull String path) {
        return getLongSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          Short methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns short at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the short at
     * @return the short at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<Short> getShortSafe(@NotNull Path path) {
        return toShort(getAsSafe(path, Number.class));
    }

    /**
     * Returns short at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the short at
     * @return the short at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<Short> getShortSafe(@NotNull String path) {
        return toShort(getAsSafe(path, Number.class));
    }

    /**
     * Returns short at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Short} as defined below).
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the short at
     * @return the short at the given path, or default according to the documentation above
     * @see #getShort(Path, Short)
     */
    public Short getShort(@NotNull Path path) {
        return getShort(path, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    /**
     * Returns short at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns default value defined by root's general settings
     * {@link GeneralSettings#getDefaultNumber()} (converted to {@link Short} as defined below).
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the short at
     * @return the short at the given path, or default according to the documentation above
     * @see #getShort(String, Short)
     */
    public Short getShort(@NotNull String path) {
        return getShort(path, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    /**
     * Returns short at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the short at
     * @return the short at the given path, or default according to the documentation above
     * @see #getShortSafe(Path)
     */
    public Short getShort(@NotNull Path path, @Nullable Short def) {
        return getShortSafe(path).orElse(def);
    }

    /**
     * Returns short at the given path. If nothing is present at the given path, or is not an instance of any compatible
     * type (see below), returns the provided default.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param path the path to get the short at
     * @return the short at the given path, or default according to the documentation above
     * @see #getShortSafe(Path)
     */
    public Short getShort(@NotNull String path, @Nullable Short def) {
        return getShortSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Short}, or any other
     * compatible type. Please learn more at {@link #getShortSafe(Path)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a short, or any other compatible type according to the
     * documentation above
     * @see #getShortSafe(Path)
     */
    public boolean isShort(@NotNull Path path) {
        return getShortSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link Short}, or any other
     * compatible type. Please learn more at {@link #getShortSafe(String)}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a short, or any other compatible type according to the
     * documentation above
     * @see #getShortSafe(String)
     */
    public boolean isShort(@NotNull String path) {
        return getShortSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //          List<?> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not a {@link List}, returns an empty optional.
     *
     * @param path the path to get the list at
     * @return the list at the given path
     * @see #getAsSafe(Path, Class)
     */
    public Optional<List<?>> getListSafe(@NotNull Path path) {
        return getAsSafe(path, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given path encapsulated in an instance of {@link Optional}. If nothing is present at the given
     * path, or is not a {@link List}, returns an empty optional.
     *
     * @param path the path to get the list at
     * @return the list at the given path
     * @see #getAsSafe(String, Class)
     */
    public Optional<List<?>> getListSafe(@NotNull String path) {
        return getAsSafe(path, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given path. If nothing is present at the given path, or is not a {@link List}, returns
     * default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     *
     * @param path the path to get the list at
     * @return the list at the given path, or default according to the documentation above
     * @see #getList(Path, List)
     */
    public List<?> getList(@NotNull Path path) {
        return getList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list at the given path. If nothing is present at the given path, or is not a {@link List}, returns
     * default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     *
     * @param path the path to get the list at
     * @return the list at the given path, or default according to the documentation above
     * @see #getList(String, List)
     */
    public List<?> getList(@NotNull String path) {
        return getList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list at the given path. If nothing is present at the given path, or is not a {@link List}, returns the
     * provided default.
     *
     * @param path the path to get the list at
     * @param def  the default value
     * @return the list at the given path, or default according to the documentation above
     * @see #getListSafe(Path)
     */
    public List<?> getList(@NotNull Path path, @Nullable List<?> def) {
        return getListSafe(path).orElse(def);
    }

    /**
     * Returns list at the given path. If nothing is present at the given path, or is not a {@link List}, returns the
     * provided default.
     *
     * @param path the path to get the list at
     * @param def  the default value
     * @return the list at the given path, or default according to the documentation above
     * @see #getListSafe(String)
     */
    public List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return getListSafe(path).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link List}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a list
     * @see #getListSafe(Path)
     */
    public boolean isList(@NotNull Path path) {
        return getListSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists, and it is a {@link List}.
     *
     * @param path the path to check the value at
     * @return if the value at the given path exists and is a list
     * @see #getListSafe(String)
     */
    public boolean isList(@NotNull String path) {
        return getListSafe(path).isPresent();
    }

    //
    //
    //      -----------------------
    //
    //
    //       List<String> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of strings at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getStringSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the string list at
     * @return the string list at the given path
     * @see #getListSafe(Path)
     * @see #getStringSafe(Path)
     */
    public Optional<List<String>> getStringListSafe(@NotNull Path path) {
        return toStringList(getListSafe(path));
    }

    /**
     * Returns list of strings at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getStringSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the string list at
     * @return the string list at the given path
     * @see #getListSafe(String)
     * @see #getStringSafe(String)
     */
    public Optional<List<String>> getStringListSafe(@NotNull String path) {
        return toStringList(getListSafe(path));
    }

    /**
     * Returns list of strings at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getStringSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the string list at
     * @param def  the default value
     * @return the string list at the given path, or default according to the documentation above
     * @see #getStringListSafe(Path)
     * @see #getStringSafe(Path)
     */
    public List<String> getStringList(@NotNull Path path, @Nullable List<String> def) {
        return getStringListSafe(path).orElse(def);
    }

    /**
     * Returns list of strings at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getStringSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the string list at
     * @param def  the default value
     * @return the string list at the given path, or default according to the documentation above
     * @see #getStringListSafe(String)
     * @see #getStringSafe(String)
     */
    public List<String> getStringList(@NotNull String path, @Nullable List<String> def) {
        return getStringListSafe(path).orElse(def);
    }

    /**
     * Returns list of strings at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getStringSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the string list at
     * @return the string list at the given path, or default according to the documentation above
     * @see #getStringList(Path, List)
     * @see #getStringSafe(Path)
     */
    public List<String> getStringList(@NotNull Path path) {
        return getStringList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of strings at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getStringSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the string list at
     * @return the string list at the given path, or default according to the documentation above
     * @see #getStringList(String, List)
     * @see #getStringSafe(String)
     */
    public List<String> getStringList(@NotNull String path) {
        return getStringList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //       List<Integer> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of integers at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getIntSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the integer list at
     * @return the integer list at the given path
     * @see #getListSafe(Path)
     * @see #getIntSafe(Path)
     */
    public Optional<List<Integer>> getIntListSafe(@NotNull Path path) {
        return toIntList(getListSafe(path));
    }

    /**
     * Returns list of integers at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getIntSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the integer list at
     * @return the integer list at the given path
     * @see #getListSafe(String)
     * @see #getIntSafe(String)
     */
    public Optional<List<Integer>> getIntListSafe(@NotNull String path) {
        return toIntList(getListSafe(path));
    }

    /**
     * Returns list of integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getIntSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the integer list at
     * @param def  the default value
     * @return the integer list at the given path, or default according to the documentation above
     * @see #getIntListSafe(Path)
     * @see #getIntSafe(Path)
     */
    public List<Integer> getIntList(@NotNull Path path, @Nullable List<Integer> def) {
        return getIntListSafe(path).orElse(def);
    }

    /**
     * Returns list of integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getIntSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the integer list at
     * @param def  the default value
     * @return the integer list at the given path, or default according to the documentation above
     * @see #getIntListSafe(String)
     * @see #getIntSafe(String)
     */
    public List<Integer> getIntList(@NotNull String path, @Nullable List<Integer> def) {
        return getIntListSafe(path).orElse(def);
    }

    /**
     * Returns list of integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getIntSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the integer list at
     * @return the integer list at the given path, or default according to the documentation above
     * @see #getIntList(Path, List)
     * @see #getIntSafe(Path)
     */
    public List<Integer> getIntList(@NotNull Path path) {
        return getIntList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getIntSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the integer list at
     * @return the integer list at the given path, or default according to the documentation above
     * @see #getIntList(String, List)
     * @see #getIntSafe(String)
     */
    public List<Integer> getIntList(@NotNull String path) {
        return getIntList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //     List<BigInteger> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of big integers at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getBigIntSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the big integer list at
     * @return the big integer list at the given path
     * @see #getListSafe(Path)
     * @see #getBigIntSafe(Path)
     */
    public Optional<List<BigInteger>> getBigIntListSafe(@NotNull Path path) {
        return toBigIntList(getListSafe(path));
    }

    /**
     * Returns list of big integers at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getBigIntSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the big integer list at
     * @return the big integer list at the given path
     * @see #getListSafe(String)
     * @see #getBigIntSafe(String)
     */
    public Optional<List<BigInteger>> getBigIntListSafe(@NotNull String path) {
        return toBigIntList(getListSafe(path));
    }

    /**
     * Returns list of big integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getBigIntSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the big integer list at
     * @param def  the default value
     * @return the big integer list at the given path, or default according to the documentation above
     * @see #getBigIntListSafe(Path)
     * @see #getBigIntSafe(Path)
     */
    public List<BigInteger> getBigIntList(@NotNull Path path, @Nullable List<BigInteger> def) {
        return getBigIntListSafe(path).orElse(def);
    }

    /**
     * Returns list of big integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getBigIntSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the big integer list at
     * @param def  the default value
     * @return the big integer list at the given path, or default according to the documentation above
     * @see #getBigIntListSafe(String)
     * @see #getBigIntSafe(String)
     */
    public List<BigInteger> getBigIntList(@NotNull String path, @Nullable List<BigInteger> def) {
        return getBigIntListSafe(path).orElse(def);
    }

    /**
     * Returns list of big integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getBigIntSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the big integer list at
     * @return the big integer list at the given path, or default according to the documentation above
     * @see #getBigIntList(Path, List)
     * @see #getBigIntSafe(Path)
     */
    public List<BigInteger> getBigIntList(@NotNull Path path) {
        return getBigIntList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of big integers at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getBigIntSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the big integer list at
     * @return the big integer list at the given path, or default according to the documentation above
     * @see #getBigIntList(String, List)
     * @see #getBigIntSafe(String)
     */
    public List<BigInteger> getBigIntList(@NotNull String path) {
        return getBigIntList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //         List<Byte> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of bytes at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getByteSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the byte list at
     * @return the byte list at the given path
     * @see #getListSafe(Path)
     * @see #getByteSafe(Path)
     */
    public Optional<List<Byte>> getByteListSafe(@NotNull Path path) {
        return toByteList(getListSafe(path));
    }

    /**
     * Returns list of bytes at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getByteSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the byte list at
     * @return the byte list at the given path
     * @see #getListSafe(String)
     * @see #getByteSafe(String)
     */
    public Optional<List<Byte>> getByteListSafe(@NotNull String path) {
        return toByteList(getListSafe(path));
    }

    /**
     * Returns list of bytes at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getByteSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the byte list at
     * @param def  the default value
     * @return the byte list at the given path, or default according to the documentation above
     * @see #getByteListSafe(Path)
     * @see #getByteSafe(Path)
     */
    public List<Byte> getByteList(@NotNull Path path, @Nullable List<Byte> def) {
        return getByteListSafe(path).orElse(def);
    }

    /**
     * Returns list of bytes at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getByteSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the byte list at
     * @param def  the default value
     * @return the byte list at the given path, or default according to the documentation above
     * @see #getByteListSafe(String)
     * @see #getByteSafe(String)
     */
    public List<Byte> getByteList(@NotNull String path, @Nullable List<Byte> def) {
        return getByteListSafe(path).orElse(def);
    }

    /**
     * Returns list of bytes at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getByteSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the byte list at
     * @return the byte list at the given path, or default according to the documentation above
     * @see #getByteList(Path, List)
     * @see #getByteSafe(Path)
     */
    public List<Byte> getByteList(@NotNull Path path) {
        return getByteList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of bytes at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getByteSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the byte list at
     * @return the byte list at the given path, or default according to the documentation above
     * @see #getByteList(String, List)
     * @see #getByteSafe(String)
     */
    public List<Byte> getByteList(@NotNull String path) {
        return getByteList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //         List<Long> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of longs at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getLongSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the long list at
     * @return the long list at the given path
     * @see #getListSafe(Path)
     * @see #getLongSafe(Path)
     */
    public Optional<List<Long>> getLongListSafe(@NotNull Path path) {
        return toLongList(getListSafe(path));
    }

    /**
     * Returns list of longs at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getLongSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the long list at
     * @return the long list at the given path
     * @see #getListSafe(String)
     * @see #getLongSafe(String)
     */
    public Optional<List<Long>> getLongListSafe(@NotNull String path) {
        return toLongList(getListSafe(path));
    }

    /**
     * Returns list of longs at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getLongSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the long list at
     * @param def  the default value
     * @return the long list at the given path, or default according to the documentation above
     * @see #getLongListSafe(Path)
     * @see #getLongSafe(Path)
     */
    public List<Long> getLongList(@NotNull Path path, @Nullable List<Long> def) {
        return getLongListSafe(path).orElse(def);
    }

    /**
     * Returns list of longs at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getLongSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the long list at
     * @param def  the default value
     * @return the long list at the given path, or default according to the documentation above
     * @see #getLongListSafe(String)
     * @see #getLongSafe(String)
     */
    public List<Long> getLongList(@NotNull String path, @Nullable List<Long> def) {
        return getLongListSafe(path).orElse(def);
    }

    /**
     * Returns list of longs at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getLongSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the long list at
     * @return the long list at the given path, or default according to the documentation above
     * @see #getLongList(Path, List)
     * @see #getLongSafe(Path)
     */
    public List<Long> getLongList(@NotNull Path path) {
        return getLongList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of longs at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getLongSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the long list at
     * @return the long list at the given path, or default according to the documentation above
     * @see #getLongList(String, List)
     * @see #getLongSafe(String)
     */
    public List<Long> getLongList(@NotNull String path) {
        return getLongList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //       List<Double> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of doubles at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getDoubleSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the double list at
     * @return the double list at the given path
     * @see #getListSafe(Path)
     * @see #getDoubleSafe(Path)
     */
    public Optional<List<Double>> getDoubleListSafe(@NotNull Path path) {
        return toDoubleList(getListSafe(path));
    }

    /**
     * Returns list of doubles at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getDoubleSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the double list at
     * @return the double list at the given path
     * @see #getListSafe(String)
     * @see #getDoubleSafe(String)
     */
    public Optional<List<Double>> getDoubleListSafe(@NotNull String path) {
        return toDoubleList(getListSafe(path));
    }

    /**
     * Returns list of doubles at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getDoubleSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the double list at
     * @param def  the default value
     * @return the double list at the given path, or default according to the documentation above
     * @see #getDoubleListSafe(Path)
     * @see #getDoubleSafe(Path)
     */
    public List<Double> getDoubleList(@NotNull Path path, @Nullable List<Double> def) {
        return getDoubleListSafe(path).orElse(def);
    }

    /**
     * Returns list of doubles at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getDoubleSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the double list at
     * @param def  the default value
     * @return the double list at the given path, or default according to the documentation above
     * @see #getDoubleListSafe(String)
     * @see #getDoubleSafe(String)
     */
    public List<Double> getDoubleList(@NotNull String path, @Nullable List<Double> def) {
        return getDoubleListSafe(path).orElse(def);
    }

    /**
     * Returns list of doubles at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getDoubleSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the double list at
     * @return the double list at the given path, or default according to the documentation above
     * @see #getDoubleList(Path, List)
     * @see #getDoubleSafe(Path)
     */
    public List<Double> getDoubleList(@NotNull Path path) {
        return getDoubleList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of doubles at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getDoubleSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the double list at
     * @return the double list at the given path, or default according to the documentation above
     * @see #getDoubleList(String, List)
     * @see #getDoubleSafe(String)
     */
    public List<Double> getDoubleList(@NotNull String path) {
        return getDoubleList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //        List<Float> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of floats at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getFloatSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the float list at
     * @return the float list at the given path
     * @see #getListSafe(Path)
     * @see #getFloatSafe(Path)
     */
    public Optional<List<Float>> getFloatListSafe(@NotNull Path path) {
        return toFloatList(getListSafe(path));
    }

    /**
     * Returns list of floats at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getFloatSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the float list at
     * @return the float list at the given path
     * @see #getListSafe(String)
     * @see #getFloatSafe(String)
     */
    public Optional<List<Float>> getFloatListSafe(@NotNull String path) {
        return toFloatList(getListSafe(path));
    }

    /**
     * Returns list of floats at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getFloatSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the float list at
     * @param def  the default value
     * @return the float list at the given path, or default according to the documentation above
     * @see #getFloatListSafe(Path)
     * @see #getFloatSafe(Path)
     */
    public List<Float> getFloatList(@NotNull Path path, @Nullable List<Float> def) {
        return getFloatListSafe(path).orElse(def);
    }

    /**
     * Returns list of floats at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getFloatSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the float list at
     * @param def  the default value
     * @return the float list at the given path, or default according to the documentation above
     * @see #getFloatListSafe(String)
     * @see #getFloatSafe(String)
     */
    public List<Float> getFloatList(@NotNull String path, @Nullable List<Float> def) {
        return getFloatListSafe(path).orElse(def);
    }

    /**
     * Returns list of floats at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getFloatSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the float list at
     * @return the float list at the given path, or default according to the documentation above
     * @see #getFloatList(Path, List)
     * @see #getFloatSafe(Path)
     */
    public List<Float> getFloatList(@NotNull Path path) {
        return getFloatList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of floats at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getFloatSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the float list at
     * @return the float list at the given path, or default according to the documentation above
     * @see #getFloatList(String, List)
     * @see #getFloatSafe(String)
     */
    public List<Float> getFloatList(@NotNull String path) {
        return getFloatList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //        List<Short> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of shorts at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getShortSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the short list at
     * @return the short list at the given path
     * @see #getListSafe(Path)
     * @see #getShortSafe(Path)
     */
    public Optional<List<Short>> getShortListSafe(@NotNull Path path) {
        return toShortList(getListSafe(path));
    }

    /**
     * Returns list of shorts at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getShortSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the short list at
     * @return the short list at the given path
     * @see #getListSafe(String)
     * @see #getShortSafe(String)
     */
    public Optional<List<Short>> getShortListSafe(@NotNull String path) {
        return toShortList(getListSafe(path));
    }

    /**
     * Returns list of shorts at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getShortSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the short list at
     * @param def  the default value
     * @return the short list at the given path, or default according to the documentation above
     * @see #getShortListSafe(Path)
     * @see #getShortSafe(Path)
     */
    public List<Short> getShortList(@NotNull Path path, @Nullable List<Short> def) {
        return getShortListSafe(path).orElse(def);
    }

    /**
     * Returns list of shorts at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getShortSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the short list at
     * @param def  the default value
     * @return the short list at the given path, or default according to the documentation above
     * @see #getShortListSafe(String)
     * @see #getShortSafe(String)
     */
    public List<Short> getShortList(@NotNull String path, @Nullable List<Short> def) {
        return getShortListSafe(path).orElse(def);
    }

    /**
     * Returns list of shorts at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getShortSafe(Path)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the short list at
     * @return the short list at the given path, or default according to the documentation above
     * @see #getShortList(Path, List)
     * @see #getShortSafe(Path)
     */
    public List<Short> getShortList(@NotNull Path path) {
        return getShortList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of shorts at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not compatible as documented at {@link #getShortSafe(String)}, it is skipped and will not
     * appear in the returned list.
     *
     * @param path the path to get the short list at
     * @return the short list at the given path, or default according to the documentation above
     * @see #getShortList(String, List)
     * @see #getShortSafe(String)
     */
    public List<Short> getShortList(@NotNull String path) {
        return getShortList(path, root.getGeneralSettings().getDefaultList());
    }

    //
    //
    //      -----------------------
    //
    //
    //      List<Map<?, ?>> methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns list of maps at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section. It is, however, if needed, still recommended to call {@link #set(Path, Object)} afterwards.
     *
     * @param path the path to get the map list at
     * @return the map list at the given path
     * @see #getListSafe(Path)
     */
    public Optional<List<Map<?, ?>>> getMapListSafe(@NotNull Path path) {
        return toMapList(getListSafe(path));
    }

    /**
     * Returns list of maps at the given path encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given path, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section. It is, however, if needed, still recommended to call {@link #set(String, Object)} afterwards.
     *
     * @param path the path to get the map list at
     * @return the map list at the given path
     * @see #getListSafe(String)
     */
    public Optional<List<Map<?, ?>>> getMapListSafe(@NotNull String path) {
        return toMapList(getListSafe(path));
    }

    /**
     * Returns list of maps at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call
     * {@link #set(Path, Object)} afterwards.
     *
     * @param path the path to get the map list at
     * @param def  the default value
     * @return the map list at the given path, or default according to the documentation above
     * @see #getMapListSafe(Path)
     */
    public List<Map<?, ?>> getMapList(@NotNull Path path, @Nullable List<Map<?, ?>> def) {
        return getMapListSafe(path).orElse(def);
    }

    /**
     * Returns list of maps at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call
     * {@link #set(String, Object)} afterwards.
     *
     * @param path the path to get the map list at
     * @param def  the default value
     * @return the map list at the given path, or default according to the documentation above
     * @see #getMapListSafe(String)
     */
    public List<Map<?, ?>> getMapList(@NotNull String path, @Nullable List<Map<?, ?>> def) {
        return getMapListSafe(path).orElse(def);
    }

    /**
     * Returns list of maps at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call
     * {@link #set(Path, Object)} afterwards.
     *
     * @param path the path to get the map list at
     * @return the map list at the given path, or default according to the documentation above
     * @see #getMapList(Path, List)
     */
    public List<Map<?, ?>> getMapList(@NotNull Path path) {
        return getMapList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of maps at the given path. If nothing is present at the given path, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings
     * {@link GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the
     * (source) list at the given path one by one, in order determined by the list iterator. If any of the elements of
     * the source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call
     * {@link #set(String, Object)} afterwards.
     *
     * @param path the path to get the map list at
     * @return the map list at the given path, or default according to the documentation above
     * @see #getMapList(String, List)
     */
    public List<Map<?, ?>> getMapList(@NotNull String path) {
        return getMapList(path, root.getGeneralSettings().getDefaultList());
    }

}