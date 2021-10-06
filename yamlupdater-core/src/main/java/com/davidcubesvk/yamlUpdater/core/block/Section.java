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
 * Represents one YAML section (map), while storing it's contents and comments. Section can also be referred to as
 * <i>collection of mappings (key=value pairs)</i>.
 * <p>
 * Functionality of this class is heavily dependent on the root file (instance of root {@link YamlFile}), to be more
 * specific, on it's settings.
 * <p>
 * The public methods of this class are divided into 2 groups, by what they require as the path and therefore, what should be used for certain path mode:
 * <ul>
 *     <li>{@link Path} - works <b>independently</b> of the root's path mode, please see {@link #getBlockSafe(Path)}</li>
 *     <li>{@link Object} - works (functions) <b>dependently</b> of the root's path mode, please see {@link #getBlockSafe(Object)}</li>
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
     * the time of initialization. In such a scenario, it is needed to call
     * {@link #init(YamlFile, Node, MappingNode, LibConstructor)} afterwards.</b>
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

    /**
     * Initializes this section and it's contents using the given parameters, while also initializing the superclass by
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
            //If a mapping or non empty section
            if (value instanceof Mapping || (value instanceof Section && !((Section) value).isEmpty(true)))
                return false;
        }

        //Empty
        return true;
    }

    /**
     * Adapts the given key, so it fits the path mode configured via the root's general settings
     * ({@link YamlFile#getGeneralSettings()}).
     * <p>
     * More formally, if {@link com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings.PathMode} returned by
     * the settings is {@link com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings.PathMode#STRING_BASED},
     * returns the result of {@link Object#toString()} on the given key object, the key object given otherwise.
     * <p>
     * If the given key is <code>null</code>, returns <code>null</code>.
     *
     * @param key the key object to adapt
     * @return the adapted key
     */
    public Object adaptKey(@Nullable Object key) {
        return key == null ? null : root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.OBJECT_BASED ? key : key.toString();
    }

    /**
     * Returns set of paths in this section; while not keeping any reference to this (or sub-) sections, therefore,
     * enabling the caller to modify it freely.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, (practically) returns the result of {@link #getKeys()} with
     * the keys converted to paths each with one element - the key itself.
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete set of
     * paths relative to this section; including paths to sections.
     *
     * @param deep if to get paths deeply
     * @return the complete set of paths
     */
    public Set<Path> getKeys(boolean deep) {
        //Create set
        Set<Path> keys = new HashSet<>();
        //Add
        addData((path, entry) -> keys.add(path), null, deep);
        //Return
        return keys;
    }

    /**
     * Returns a complete set of paths in this section only (not deep), including paths to sections. More formally,
     * returns the key set of the underlying map.
     * The set, however, is a <i>shallow</i> copy of the map's key set, therefore, the caller is able to modify it
     * freely, without modifying this section.
     *
     * @return the complete set of paths directly contained by this section
     */
    public Set<Object> getKeys() {
        return new HashSet<>(getValue().keySet());
    }

    /**
     * Returns a complete map of path=value pairs; while not keeping any reference to this (or sub-) sections,
     * therefore, enabling the caller to modify it freely.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns a copy of the underlying map with keys (which are
     * stored as object instances) converted to paths each with one element - the key itself; with their appropriate
     * values (obtained from the block, if not a section, stored at those paths).
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete map of
     * path=value (obtained from the block, if not a section, stored at those paths) pairs, with paths relative to this
     * section.
     * <p>
     * Practically, it is a result of {@link #getKeys(boolean)} with appropriate values to each path assigned. <b>It is
     * also a copy of {@link #getValue()} converted from nested to flat map (with the blocks, if a mapping - not a
     * section, represented by their values).</b>
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of path=value pairs, including sections
     */
    public Map<Path, Object> getValues(boolean deep) {
        //Create map
        Map<Path, Object> values = new HashMap<>();
        //Add
        addData((path, entry) -> values.put(path, entry.getValue() instanceof Section ? entry.getValue() : entry.getValue().getValue()), null, deep);
        //Return
        return values;
    }

    /**
     * Returns a complete map of path=block pairs; while not keeping any reference to this (or sub-) sections,
     * therefore, enabling the caller to modify it freely.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns a copy of the underlying map with keys (which are
     * stored as object instances) converted to paths each with one element - the key itself; with their appropriate
     * blocks.
     * Otherwise, iterates through <b>all</b> (direct and indirect) sub-sections, while returning a complete map of
     * path=block pairs, with paths relative to this section.
     * <p>
     * Practically, it is a result of {@link #getKeys(boolean)} with blocks to each path assigned, or <b>a copy of
     * {@link #getValue()} converted from nested to flat map.</b>
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of path=value pairs
     */
    public Map<Path, Block<?>> getBlocks(boolean deep) {
        //Create map
        Map<Path, Block<?>> blocks = new HashMap<>();
        //Add
        addData((path, entry) -> blocks.put(path, entry.getValue()), null, deep);
        //Return
        return blocks;
    }

    /**
     * Iterates through all entries in the underlying map, while calling the given consumer for each entry. The path
     * given is the path relative to this section.
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
            consumer.accept(current, entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, entryPath, true);
        }
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
     * Returns whether this section contains anything at the given path.
     *
     * @param path the path to check
     * @return if this section contains anything at the given path
     * @see #contains(Object)
     */
    public boolean contains(@NotNull Path path) {
        return getSafe(path).isPresent();
    }

    /**
     * Returns whether this section contains any object at the given direct key (not sub-key, use
     * {@link #contains(Path)} instead).
     * <p>
     * More formally, returns the result of {@link Optional#isPresent()} called on {@link #getSafe(Object)}, with the
     * given key as the parameter.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key, or full string path to check
     * @return if this section contains anything at the given key, or full string path
     * @see #contains(Path)
     */
    public boolean contains(@Nullable Object key) {
        return getSafe(key).isPresent();
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
    private void adapt(@NotNull YamlFile root, @Nullable Section parent, @NotNull Object name, @NotNull Path path) {
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
     * Internal method which sets the given value into the appropriate section, at path derived from the given path,
     * starting from the given index.
     * <p>
     * If <code>i</code> represents the last element in the given path or if the path is <code>null</code>, calls
     * {@link #set(Object, Object)} to set the value into this section.
     * If not, gets the block at the current key (<code>path.getKey(i)</code>). If it is a mapping or does not exist,
     * (overwrites it and) creates a new section at the key. Finally, calls this method recursively on the section
     * currently present at the key.
     *
     * @param path  the path to set the value at
     * @param value value to set
     * @param i     the current index of the path
     */
    private void setInternal(@Nullable Path path, @Nullable Object value, int i) {
        //If null or at the last index
        if (path == null || i + 1 >= path.getLength()) {
            //Call the direct method
            set(path == null ? null : adaptKey(path.get(i)), value);
            return;
        }

        //Key
        Object key = adaptKey(path.get(i));
        //The block at the key
        Block<?> block = getValue().getOrDefault(key, null);
        //If null
        if (block == null || block instanceof Mapping)
            //Create
            createSection(key, block).setInternal(path, value, i + 1);
        else
            //Call subsection
            ((Section) block).setInternal(path, value, i + 1);
    }

    /**
     * Attempts to create a section at the given path and overwrites a mapping if there is one in the way; returns the
     * section. If there is a section already, nothing is overwritten and the already existing section is returned.
     * <p>
     * Calls {@link #createSection(Object)} subsequently for each path's element (in the appropriate sub-sections).
     * <p>
     * If the given path is <code>null</code>, calls and returns the result of {@link #createSection(Object)} with
     * <code>null</code> as the key.
     *
     * @param path the path to create a section at
     * @return the created section at the given path, or already existing one
     * @see #createSection(Object)
     * @see #createSection(Object, Block)
     */
    public Section createSection(@Nullable Path path) {
        //If null
        if (path == null)
            return createSection((Object) null);

        //Current section
        Section current = this;
        //All keys
        for (int i = 0; i < path.getLength(); i++)
            //Create
            current = current.createSection(path.get(i));
        //Return
        return current;
    }

    /**
     * Attempts to create a section at the given direct key in this section and returns it. If there already is a
     * mapping existing at the key, it is overwritten with a new section. If there is a section already, does not
     * overwrite anything and the already existing section is returned.
     * <p>
     * Calls {@link #createSection(Object, Block)} to do so.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the key to create a section at
     * @return the newly created section or the already existing one
     * @see #createSection(Object, Block)
     */
    public Section createSection(@Nullable Object key) {
        return createSection(key, null);
    }

    /**
     * Attempts to create a section at the given direct key in this section and returns it. If there already is a
     * mapping existing at the key, it is overwritten with a new section. If there is a section already, does not
     * overwrite anything and the already existing section is returned.
     * <p>
     * If the method ends up creating a new section, previous block's comments are copied (see
     * {@link #Section(YamlFile, Section, Object, Path, Block, Map)}).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key      the key to create a section at
     * @param previous the previous block at this key
     * @return the newly created section or the already existing one
     */
    public Section createSection(@Nullable Object key, @Nullable Block<?> previous) {
        //Adapt
        Object adapted = adaptKey(key);

        return getSectionSafe(adapted).orElseGet(() -> {
            //The new section
            Section section = new Section(root, Section.this, adapted, getSubPath(adapted), previous, root.getGeneralSettings().getDefaultMap());
            //Add
            getValue().put(adapted, section);
            //Return
            return section;
        });
    }

    /**
     * Sets the given value at the given path in this section. If there are sections missing to the path where the
     * object should be set, they are created along the way using {@link #createSection(Object, Block)}.
     * <p>
     * At the end, this method ends up calling {@link #set(Object, Object)}, meaning the limitations to the given value
     * are the same for both methods. Please read more regarding that there.
     * <p>
     * If the given path is <code>null</code>, calls {@link #set(Object, Object)} (indirectly) to set the value at
     * <code>null</code> key.
     *
     * @param path  the path to set at
     * @param value the value to set
     * @see #set(Object, Object)
     */
    public void set(Path path, Object value) {
        setInternal(path, value, 0);
    }

    /**
     * Sets the given value at the given direct key in this section. If there is an already existing value for this key
     * in this section, it is overwritten.
     * <p>
     * As the value to set, you can give instances of:
     * <ul>
     *     <li><code>null</code>: the object currently stored at the given key in the section will be removed (if any);
     *     only implemented to support Spigot API (usage like that is <b>deprecated and will likely be removed</b>) -
     *     please use {@link #remove(Object)} to do so,</li>
     *     <li>{@link Section}: the given section will be <i>pasted</i> here,</li>
     *     <li>{@link Mapping}: the given mapping will be <i>pasted</i> here,</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Object, Path, Block, Map)} for more information),</li>
     *     <li><i>anything else</i>: the given value will be encapsulated with {@link Mapping} object and set at the
     *     given key.</li>
     * </ul>
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key   the key/string path to set the value at, or <code>null</code> if to delete (<b>deprecated, please
     *              use {@link #remove(Object)}</b>)
     * @param value the value to set, processed as described above
     */
    public void set(Object key, Object value) {
        //Adapt
        key = adaptKey(key);

        //If null (remove)
        // TODO: 2. 10. 2021 Deprecated?
        if (value == null)
            remove(key);

        //If is string mode
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.STRING_BASED) {
            //String key
            String stringKey = key.toString();
            //Last separator
            int lastSeparator = key.toString().indexOf(root.getGeneralSettings().getSeparator());
            //If found
            if (lastSeparator != -1) {
                //Create sections
                createSection(key.toString().substring(0, lastSeparator));
                //Change the key
                key = stringKey.substring(lastSeparator + 1);
            }
        }

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

    /**
     * Removes value at the given path (if any); returns <code>true</code> if successfully removed. The method returns
     * <code>false</code> if and only value at the path does not exist.
     * <p>
     * If the given path is <code>null</code>, calls and returns the result of {@link #remove(Object)} with
     * <code>null</code> as the key.
     *
     * @param path the path to remove the object at
     * @return if any value has been removed
     */
    public boolean remove(@Nullable Path path) {
        //If null
        if (path == null)
            //Call other method
            remove((Object) null);
        //Remove
        return removeInternal(getParent(path).orElse(null), adaptKey(path.get(path.getLength() - 1)));
    }

    /**
     * Removes value at the given path (if any); returns <code>true</code> if successfully removed. The method returns
     * <code>false</code> if value at the path does not exist.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the key/string path to remove the object at in this section (supports <code>null</code> keys)
     * @return if any value has been removed
     */
    public boolean remove(@Nullable Object key) {
        //Adapt
        key = adaptKey(key);
        //If object based, does not contain a sub-key or is null
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.OBJECT_BASED || key == null || key.toString().indexOf(root.getGeneralSettings().getSeparator()) == -1)
            return getValue().remove(key) != null;

        //String key
        String stringKey = key.toString();
        //Last index of the separator
        int lastSeparator = stringKey.lastIndexOf(root.getGeneralSettings().getSeparator());
        //Remove
        return removeInternal(getSection(stringKey.substring(0, lastSeparator)), stringKey.substring(lastSeparator + 1));
    }

    /**
     * An internal method used to actually remove the value. Created to extract common parts from both removal-oriented
     * methods.
     * <p>
     * Returns <code>false</code> if the parent section is <code>null</code> (meaning there is not any value at the
     * given key as there is not a parent section), then returns the result of {@link #remove(Object)} with the given
     * key.
     *
     * @param parent the parent section, or <code>null</code> if does not exist
     * @param key    the last key; key to check in the parent section, adapted using {@link #adaptKey(Object)}
     * @return if any value has been removed
     */
    private boolean removeInternal(@Nullable Section parent, @Nullable Object key) {
        //If the parent is null
        if (parent == null)
            return false;
        //Remove
        return parent.remove(key);
    }

    /**
     * Returns block at the given path encapsulated in an instance of {@link Optional}. If there is no block present (no
     * value) at the given path, returns an empty optional.
     * <p>
     * If the given path is <code>null</code>, provides the same functionality as {@link #getBlockSafe(Object)} does -
     * returns block at <code>null</code> key in the underlying map.
     * <p>
     * Each value is encapsulated in a {@link Mapping} (if the value is not a section) or {@link Section} (the value is
     * a section) block instances. Their values can then be obtained by calling {@link Block#getValue()}. See the comments attached to the declaration of this class.
     * <p>
     * <p>
     * <b>Functionality notes:</b> This method works independently of the root's path mode (as opposed to object-oriented
     * version of this method - {@link #getBlockSafe(Object)}).
     * However, when individual elements (keys) of the given path are traversed, they are (without modifying the path
     * object given - it is immutable) adapted to the current path mode setting (see {@link #adaptKey(Object)}).
     * <p>
     * <b>This is one of the foundation methods, upon which the functionality of other methods in this class is built.</b>
     *
     * @param path the path to get the block from
     * @return block at the given path encapsulated in an optional
     */
    public Optional<Block<?>> getBlockSafe(@Nullable Path path) {
        return getSafeInternal(path, 0, false);
    }

    /**
     * Returns block at the given key encapsulated in an instance of {@link Optional}. If there is no block present (no
     * value) at the given key, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Mapping} (if the value is not a section) or {@link Section} (the value is
     * a section) block instances. Their values can then be obtained by calling {@link Block#getValue()}. See the comments attached to the declaration of this class.
     * <p>
     * <p>
     * <b>Functionality notes:</b> If root's path mode (general settings) is set to
     * {@link com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings.PathMode#OBJECT_BASED}, treats the key
     * as direct one (e.g. searches and returns block at the given key in the underlying map).
     * <p>
     * Otherwise (with path mode set to {@link com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings.PathMode#STRING_BASED}),
     * treats the key like a string key representing full path to the value (like in Spigot/BungeeCord API). Therefore,
     * if the given key contains any separator (configured in root's general settings), appropriate subsections are
     * traversed (determined by the individual keys separated by the separator) - for example, for
     * <code>section.value</code> key (with separator set to <code>.</code>), attempts to get the section at key
     * <code>section</code> in <b>this</b> section and <b>then</b> the block at key <code>value</code> in
     * <b>that</b> section. We can also interpret this behaviour as a call to {@link #getBlockSafe(Path)} with path
     * created via constructor {@link Path#fromString(String, char)} (which effectively splits the given string path into
     * separate keys according to the separator).
     * <p>
     * If no separator is contained within the given key, treats the key as a direct one (returns block at the given key
     * in this section - not subsections).
     * <p>
     * <p>
     * <b>Please note</b> that this class also supports <code>null</code> keys, or empty string keys (<code>""</code>)
     * as specified by YAML 1.2 spec. This also means that compatibility with Spigot/BungeeCord API is not maintained in
     * regards to empty string keys, where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>This is one of the foundation methods, upon which the functionality of other methods in this class is built.</b>
     *
     * @param key the key/string path to get the object from
     * @return block at the given path encapsulated in an optional
     */
    public Optional<Block<?>> getBlockSafe(@Nullable Object key) {
        //Adapt
        key = adaptKey(key);
        //If is string mode and contains sub-key
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.STRING_BASED && key != null && key.toString().indexOf(root.getGeneralSettings().getSeparator()) != -1)
            //Return
            return getSafeInternalString(key.toString(), -1, false);
        //Return
        return Optional.ofNullable(getValue().get(key));
    }

    /**
     * Returns section for the parent path of the given one, encapsulated in an instance of {@link Optional}. Said
     * differently, the returned section is the result of {@link #getSection(Path)} called for the same path as given
     * here, with the last path element removed.
     * <p>
     * That means, this method ignores if the given path represents an existing block and if block at that parent path
     * is not a section (is a mapping), returns an empty optional.
     * <p>
     * If the given path is <code>null</code>, provides the same functionality as {@link #getParent(Object)} called
     * with <code>null</code> parameter, effectively returning encapsulated instance of this section.
     *
     * @param path the path to get the parent section from
     * @return section at the parent path from the given one encapsulated in an optional
     */
    public Optional<Section> getParent(@Nullable Path path) {
        return getSafeInternal(path, 0, true).map(block -> block instanceof Section ? (Section) block : null);
    }

    /**
     * Returns parent section for the (not necessarily existing) block at the given key in this section, encapsulated in
     * an instance of {@link Optional}. Said differently, unless the given key refers, according to the root's path mode
     * (see {@link #getBlockSafe(Object)}), to a full string path (is not a direct key), the returned instance is
     * <b>always</b> an encapsulation of this section.
     * <p>
     * Similarly to {@link #getParent(Path)}, this method ignores if the given key represents an existing block. If
     * the key represents a full string path (according to the path mode) and block at parent path of that path is not a
     * section (is a mapping), returns an empty optional.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the key/string path to get the parent section from
     * @return this section, or section at the parent path from the given string path (by the root's path mode),
     * encapsulated in an instance of {@link Optional}
     */
    public Optional<Section> getParent(@Nullable Object key) {
        //Adapt
        key = adaptKey(key);
        //If is string mode and contains sub-key
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.STRING_BASED && key != null && key.toString().indexOf(root.getGeneralSettings().getSeparator()) != -1)
            //Return
            return getSafeInternalString(key.toString(), -1, true).map(block -> block instanceof Section ? (Section) block : null);
        //Return
        return Optional.of(this);
    }

    /**
     * Internal method which returns a block, encapsulated in an instance of {@link Optional}, at the given string path,
     * starting from the given index, in this section.
     * <p>
     * If the given path does not contain a separator (as configured in root's general settings), returns the result of
     * {@link #getSectionSafe(Object)} called on this section, with <code>path.substring(i+1)</code> as the key.
     * <p>
     * If it does, calls this method recursively with the index of the next separator.
     *
     * @param path   the full string path that's searched
     * @param i      index of the last separator found by the caller (in case of recursive call of this same method), or
     *               <code>-1</code> if starting to search from the start of the given path (if called by a public
     *               method)
     * @param parent if searching for the parent section of the given path
     * @return the block at the given string path, starting from the given index, in this section, or it's parent
     * section
     */
    private Optional<Block<?>> getSafeInternalString(@NotNull String path, int i, boolean parent) {
        //Next separator
        int next = path.indexOf(i + 1, root.getGeneralSettings().getSeparator());
        //If -1
        if (next == -1)
            return parent ? Optional.of(this) : getBlockSafe(path.substring(i + 1));
        //Call subsection
        return getSectionSafe(path.substring(i + 1, next)).flatMap(section -> section.getSafeInternalString(path, next, parent));
    }

    /**
     * Internal method which returns a block, encapsulated in an instance of {@link Optional}, at the given path,
     * starting from the given index, in this section.
     * <p>
     * If the given path is <code>null</code>, returns the result of {@link #getParent()} if <code>parent</code> is
     * <code>true</code>, result of {@link #getBlockSafe(Object)} (with <code>null</code> parameter) otherwise.
     * <p>
     * If <code>i</code> represents the last element (key) in the given path, returns this section if
     * <code>parent</code> is <code>true</code>, or the result of {@link #getBlockSafe(Object)}.
     *
     * @param path   the path that's searched
     * @param i      current index in the path
     * @param parent if searching for the parent section of the given path
     * @return the block at the given path, starting from the given index, in this section, or it's parent section
     */
    private Optional<Block<?>> getSafeInternal(@Nullable Path path, int i, boolean parent) {
        //If null or at last index
        if (path == null || i + 1 >= path.getLength())
            return path == null ? parent ? Optional.of(getParent()) : getBlockSafe((Object) null) : parent ? Optional.of(this) : getBlockSafe(path.get(i));
        //Return
        return getBlockSafe(path.get(i)).flatMap(block -> block instanceof Section ? ((Section) block).getSafeInternal(path, i + 1, parent) : Optional.empty());
    }

    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getBlockSafe(Path)}. If the returned optional is not empty and
     * does not contain an instance of {@link Section}, returns the encapsulated value returned by
     * {@link Block#getValue()}.
     * <p>
     * If the given path is <code>null</code>, effectively returns the same as {@link #getSafe(Object)} called with
     * <code>null</code> parameter. Please read more about this behaviour here: {@link #getBlockSafe(Path)}.
     *
     * @param path the path to get value at
     * @return the value, or section at the given path
     */
    public Optional<Object> getSafe(@Nullable Path path) {
        return getBlockSafe(path).map(block -> block instanceof Section ? block : block.getValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer) at the given key/string path in the underlying
     * map, or if it is a section, the corresponding {@link Section} instance, encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If the key represents a full string path (according to the path mode) processes the block at that path and
     * returns by the rules specified above.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getBlockSafe(Object)}. If the returned optional is not empty and
     * does not contain an instance of {@link Section}, returns the encapsulated value returned by
     * {@link Block#getValue()} - the actual value (list, integer).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the key/string path to get the value from
     * @return the value, or section at the given path
     */
    public Optional<Object> getSafe(@Nullable Object key) {
        return getBlockSafe(key).map(block -> block instanceof Section ? block : block.getValue());
    }

    /**
     * Returns the value of the block (the actual value) at the given path, or if it is a section, the corresponding
     * {@link Section} instance, in both cases casted to instance of the given class; encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If there is no block present at the given path (therefore no value can be returned), or the value (mapping's
     * value or section instance) is not castable to the given type class, returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getSafe(Path)} casted to the given class if not empty (or an empty
     * optional if types are incompatible).
     *
     * @param path the path to get value from
     * @return the value (result of {@link #getSafe(Path)}) casted to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(@Nullable Path path, Class<T> clazz) {
        return getSafe(path).map((object) -> clazz.isInstance(object) ? (T) object : null);
    }


    /**
     * Returns the value of the block (the actual value) at the given key/string path (according to the root's path
     * mode), or if it is a section, the corresponding {@link Section} instance, in both cases casted to instance of the
     * given class; encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given key (therefore no value can be returned), or the value (mapping's value
     * or section instance) is not castable to the given type class, returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getSafe(Object)} casted to the given class if not empty (or an empty
     * optional if types are incompatible).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the key/string path to get the value from
     * @return the value (result of {@link #getSafe(Object)}) casted to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(@Nullable Object key, Class<T> clazz) {
        return getSafe(key).map((object) -> clazz.isInstance(object) ? (T) object : null);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getSafe(Path)}. If the returned optional is empty,
     * returns default value as defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     * <p>
     * According to {@link #getSafe(Object)} documentation, the returned object might also be an instance of
     * {@link Section}, if a section is present at that path. If not, it is the actual value (list, integer...).
     *
     * @param path the path to get the value from
     * @return the value at the given path, or default if not found
     * @see #getSafe(Path)
     */
    public Object get(Path path) {
        return getSafe(path).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value encapsulated in the result of {@link #getSafe(Object)}. If the returned optional is empty,
     * returns default value as defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     * <p>
     * According to {@link #getSafe(Object)} documentation, the returned object might also be an instance of
     * {@link Section}, if a section is present at that path. If not, it is the actual value (list, integer...).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the key/string path to get the value from
     * @return the value at the given key/string path, or default if not found
     * @see #getSafe(Object)
     */
    public Object get(Object key) {
        return getSafe(key).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value encapsulated in the result of {@link #getAsSafe(Path, Class)} (indirectly). If the returned
     * optional is empty, returns <code>null</code> (configurable global default value is not implemented as the only
     * "instance" that is compatible with all the possible types in Java is <code>null</code>).
     * <p>
     * For custom defaults, please use {@link #getAs(Path, Class, Object)}.
     *
     * @param path  the path to get the value from
     * @param clazz target class
     * @return the value at the given path, or default if not found
     * @see #getAsSafe(Path, Class)
     * @see #getAs(Path, Class, Object)
     */
    public <T> T getAs(Path path, Class<T> clazz) {
        return getAs(path, clazz, null);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getAsSafe(Object, Class)} (indirectly). If the returned
     * optional is empty, returns <code>null</code> (configurable default value is not implemented as the only
     * "instance" that is compatible with all the possible types in Java is <code>null</code>).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     * <p>
     * For custom defaults, please use {@link #getAs(Object, Class, Object)}.
     *
     * @param key   the key/string path to get the value from
     * @param clazz target class
     * @return the value at the given key/string path, or default if not found
     * @see #getAsSafe(Object, Class)
     * @see #getAs(Object, Class, Object)
     */
    public <T> T getAs(Object key, Class<T> clazz) {
        return getAs(key, clazz, null);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getSafe(Path)}. If the returned optional is empty,
     * returns the provided default.
     * <p>
     * According to {@link #getSafe(Path)} documentation, the returned object might also be an instance of
     * {@link Section}, if a section is present at that path. If not, it is the actual value (list, integer...).
     *
     * @param path the path to get the value from
     * @param def  the default object to return if there is nothing at the given path
     * @return the value at the given path, or default if not found
     * @see #getSafe(Path)
     */
    public Object get(Path path, Object def) {
        return getSafe(path).orElse(def);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getSafe(Object)}. If the returned optional is empty,
     * returns the provided default.
     * <p>
     * According to {@link #getSafe(Object)} documentation, the returned object might also be an instance of
     * {@link Section}, if a section is present at that path. If not, it is the actual value (list, integer...).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path (determined by the root's path mode) to get the value from
     * @param def the default object to return if there is nothing at the given path
     * @return the value at the given direct key/string path, or default if not found
     * @see #getSafe(Path)
     */
    public Object get(Object key, Object def) {
        return getSafe(key).orElse(def);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getAsSafe(Path, Class)}. If the returned optional is
     * empty, returns the provided default.
     *
     * @param path  the path to get the value from
     * @param clazz target class
     * @param def   default value returned if no value is present, or is not an instance of the given class
     * @return the value at the given path, or default if not found
     * @see #getAsSafe(Path, Class)
     */
    public <T> T getAs(Path path, Class<T> clazz, T def) {
        return getAsSafe(path, clazz).orElse(def);
    }

    /**
     * Returns the value encapsulated in the result of {@link #getAsSafe(Object, Class)}. If the returned optional is
     * empty, returns the provided default.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key   the direct key/string path (determined by the root's path mode) to get the value from
     * @param clazz target class
     * @param def   default value returned if no value is present, or is not an instance of the given class
     * @return the value at the given key/string path, or default if not found
     * @see #getAsSafe(Object, Class)
     */
    public <T> T getAs(Object key, Class<T> clazz, T def) {
        return getAsSafe(key, clazz).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only value at the given path exists and it is an instance of the given class.
     * <p>
     * More formally, returns {@link Optional#isPresent()} called on the result of {@link #getAsSafe(Path, Class)}.
     *
     * @param path  the path to check
     * @param clazz target class
     * @param <T>   the type of of the target class
     * @return if a value exists at the path and it is an instance of the given class
     */
    public <T> boolean is(Path path, Class<T> clazz) {
        return getAsSafe(path, clazz).isPresent();
    }

    /**
     * Returns <code>true</code> if and only value at the given direct key/string path (determined by the root's path
     * mode) exists and it is an instance
     * of the given class.
     * <p>
     * More formally, returns {@link Optional#isPresent()} called on the result of {@link #getAsSafe(Object, Class)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key   the direct key/string path to check
     * @param clazz target class
     * @param <T>   the type of of the target class
     * @return if a value exists at the path and it is an instance of the given class
     */
    public <T> boolean is(Object key, Class<T> clazz) {
        return getAsSafe(key, clazz).isPresent();
    }

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
     * Returns section at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a section, returns an empty optional.
     *
     * @param path the path to get the section from
     * @return the section at the given path
     * @see #getSafe(Path)
     */
    public Optional<Section> getSectionSafe(Path path) {
        return getAsSafe(path, Section.class);
    }

    /**
     * Returns section at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a section, returns an empty optional.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the section from
     * @return the section at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Section> getSectionSafe(Object key) {
        return getAsSafe(key, Section.class);
    }

    /**
     * Returns section at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a section, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultSection()}.
     *
     * @param path the path to get the section from
     * @return the section at the given path, or default if no supported type (specified above) was found
     * @see #getSection(Path, Section)
     */
    public Section getSection(Path path) {
        return getSection(path, root.getGeneralSettings().getDefaultSection());
    }

    /**
     * Returns section at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a section, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultSection()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the section from
     * @return the section at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getSection(Object, Section)
     */
    public Section getSection(Object key) {
        return getSection(key, root.getGeneralSettings().getDefaultSection());
    }

    /**
     * Returns section at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a section, returns the provided default.
     *
     * @param path the path to get the section from
     * @param def  default value returned if no value convertible to section is present (or no value at all)
     * @return the section at the given path, or default if no supported type (specified above) was found
     * @see #getSectionSafe(Path)
     */
    public Section getSection(Path path, Section def) {
        return getSectionSafe(path).orElse(def);
    }

    /**
     * Returns section at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a section, returns the provided default.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the section from
     * @param def default value returned if no value convertible to section is present (or no value at all)
     * @return the section at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getSectionSafe(Object)
     */
    public Section getSection(Object key, Section def) {
        return getSectionSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a section.
     *
     * @param path the path to get the section from
     * @return the section at the given path
     * @see #getSectionSafe(Path)
     */
    public boolean isSection(Path path) {
        return getSectionSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given direct key/string path (determined by the root's path
     * mode) exists and it is a section.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the section from
     * @return the section at the given direct key/string path
     * @see #getSectionSafe(Object)
     */
    public boolean isSection(Object key) {
        return getSectionSafe(key).isPresent();
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
     * Returns string at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not an instance of any of the compatible types (see below), returns an empty optional.
     * <p>
     * If there is an instance of {@link Number} or {@link Boolean} (or their primitive variant) present instead of a
     * {@link String}, they are treated like if they were strings, by converting {@link Object#toString()} and returning them as one.
     *
     * @param path the path to get the string from
     * @return the string at the given path
     * @see #getSafe(Path)
     */
    public Optional<String> getStringSafe(Path path) {
        return getSafe(path).map((object) -> object instanceof String || object instanceof Number || object instanceof Boolean ? object.toString() : null);
    }

    /**
     * Returns string at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not an instance of any of the compatible types (see below), returns an empty optional.
     * <p>
     * If there is an instance of {@link Number} or {@link Boolean} (or their primitive variant) present instead of a
     * {@link String}, they are treated like if they were strings, by converting {@link Object#toString()} and returning them as one.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the string from
     * @return the string at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<String> getStringSafe(Object key) {
        return getSafe(key).map((object) -> object instanceof String || object instanceof Number || object instanceof Boolean ? object.toString() : null);
    }

    /**
     * Returns string at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not an instance of any of the compatible types (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultString()}.
     * <p>
     * If there is an instance of {@link Number} or {@link Boolean} (or their primitive variant) present instead of a
     * {@link String}, they are treated like if they were strings, by converting {@link Object#toString()} and returning them as one.
     *
     * @param path the path to get the string from
     * @return the string at the given path, or default if no supported type (specified above) was found
     * @see #getString(Path, String)
     */
    public String getString(Path path) {
        return getString(path, root.getGeneralSettings().getDefaultString());
    }

    /**
     * Returns string at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not an instance of any of the compatible types (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultString()}.
     * <p>
     * If there is an instance of {@link Number} or {@link Boolean} (or their primitive variant) present instead of a
     * {@link String}, they are treated like if they were strings, by converting {@link Object#toString()} and returning them as one.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the string from
     * @return the string at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getString(Object, String)
     */
    public String getString(Object key) {
        return getString(key, root.getGeneralSettings().getDefaultString());
    }

    /**
     * Returns string at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not an instance of any of the compatible types (see below), returns the provided default.
     * <p>
     * If there is an instance of {@link Number} or {@link Boolean} (or their primitive variant) present instead of a
     * {@link String}, they are treated like if they were strings, by converting {@link Object#toString()} and returning them as one.
     *
     * @param path the path to get the string from
     * @param def  default value returned if no value convertible to string is present (or no value at all)
     * @return the string at the given path, or default if no supported type (specified above) was found
     * @see #getStringSafe(Path)
     */
    public String getString(Path path, String def) {
        return getStringSafe(path).orElse(def);
    }

    /**
     * Returns string at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not an instance of any of the compatible types (see below), returns the provided default.
     * <p>
     * If there is an instance of {@link Number} or {@link Boolean} (or their primitive variant) present instead of a
     * {@link String}, they are treated like if they were strings, by converting {@link Object#toString()} and returning them as one.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the string from
     * @param def default value returned if no value convertible to string is present (or no value at all)
     * @return the string at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getStringSafe(Object)
     */
    public String getString(Object key, String def) {
        return getStringSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a string, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     *
     * @param path the path to get the string from
     * @return the string at the given path
     * @see #getStringSafe(Path)
     */
    public boolean isString(Path path) {
        return getStringSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a string, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the string from
     * @return the string at the given direct key/string path
     * @see #getStringSafe(Object)
     */
    public boolean isString(Object key) {
        return getStringSafe(key).isPresent();
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
     * Returns char at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a char, string or integer (see below), returns an empty optional.
     * <p>
     * If there is an instance of {@link String} and it is exactly 1 character in length, returns that character. If it
     * is an {@link Integer} (or primitive variant), it is converted to a character (by casting, see the
     * <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char from
     * @return the char at the given path
     * @see #getSafe(Path)
     */
    public Optional<Character> getCharSafe(Path path) {
        return getSafe(path).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
    }

    /**
     * Returns char at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a char, string or integer (see below), returns an empty optional.
     * <p>
     * If there is an instance of {@link String} and it is exactly 1 character in length, returns that character. If it
     * is an {@link Integer} (or primitive variant), it is converted to a character (by casting, see the
     * <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the char from
     * @return the char at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Character> getCharSafe(Object key) {
        return getSafe(key).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
    }

    /**
     * Returns char at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a char, string or integer (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultChar()}.
     * <p>
     * If there is an instance of {@link String} and it is exactly 1 character in length, returns that character. If it
     * is an {@link Integer} (or primitive variant), it is converted to a character (by casting, see the
     * <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char from
     * @return the char at the given path, or default if no supported type (specified above) was found
     * @see #getChar(Path, Character)
     */
    public Character getChar(Path path) {
        return getChar(path, root.getGeneralSettings().getDefaultChar());
    }

    /**
     * Returns char at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a char, string or integer (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultChar()}.
     * <p>
     * If there is an instance of {@link String} and it is exactly 1 character in length, returns that character. If it
     * is an {@link Integer} (or primitive variant), it is converted to a character (by casting, see the
     * <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the char from
     * @return the char at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getChar(Object, Character)
     */
    public Character getChar(Object key) {
        return getChar(key, root.getGeneralSettings().getDefaultChar());
    }

    /**
     * Returns char at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a char, string or integer (see below), returns the provided default.
     * <p>
     * If there is an instance of {@link String} and it is exactly 1 character in length, returns that character. If it
     * is an {@link Integer} (or primitive variant), it is converted to a character (by casting, see the
     * <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param path the path to get the char from
     * @param def  default value returned if no value convertible to char is present (or no value at all)
     * @return the char at the given path, or default if no supported type (specified above) was found
     * @see #getCharSafe(Path)
     */
    public Character getChar(Path path, Character def) {
        return getCharSafe(path).orElse(def);
    }

    /**
     * Returns char at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a char, string or integer (see below), returns the provided default.
     * <p>
     * If there is an instance of {@link String} and it is exactly 1 character in length, returns that character. If it
     * is an {@link Integer} (or primitive variant), it is converted to a character (by casting, see the
     * <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the char from
     * @param def default value returned if no value convertible to char is present (or no value at all)
     * @return the char at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getCharSafe(Object)
     */
    public Character getChar(Object key, Character def) {
        return getCharSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a char, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getCharSafe(Path)}.
     *
     * @param path the path to get the char from
     * @return the char at the given path
     * @see #getCharSafe(Path)
     */
    public boolean isChar(Path path) {
        return getCharSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a char, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getCharSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the char from
     * @return the char at the given direct key/string path
     * @see #getCharSafe(Object)
     */
    public boolean isChar(Object key) {
        return getCharSafe(key).isPresent();
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
     * Returns integer at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a number (see below), returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#intValue()}.
     *
     * @param path the path to get the integer from
     * @return the integer at the given path
     * @see #getSafe(Path)
     */
    public Optional<Integer> getIntSafe(Path path) {
        return toInt(getAsSafe(path, Number.class));
    }

    /**
     * Returns integer at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a number (see below), returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#intValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @return the integer at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Integer> getIntSafe(Object key) {
        return toInt(getAsSafe(key, Number.class));
    }

    /**
     * Returns integer at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a number (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Integer}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#intValue()}.
     *
     * @param path the path to get the integer from
     * @return the integer at the given path, or default if no supported type (specified above) was found
     * @see #getInt(Path, Integer)
     */
    public Integer getInt(Path path) {
        return getInt(path, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    /**
     * Returns integer at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a number (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Integer}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#intValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @return the integer at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getInt(Object, Integer)
     */
    public Integer getInt(Object key) {
        return getInt(key, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    /**
     * Returns integer at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a number (see below), returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#intValue()}.
     *
     * @param path the path to get the integer from
     * @param def  default value returned if no value convertible to integer is present (or no value at all)
     * @return the integer at the given path, or default if no supported type (specified above) was found
     * @see #getIntSafe(Path)
     */
    public Integer getInt(Path path, Integer def) {
        return getIntSafe(path).orElse(def);
    }

    /**
     * Returns integer at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a number (see below), returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#intValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @param def default value returned if no value convertible to integer is present (or no value at all)
     * @return the integer at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getIntSafe(Object)
     */
    public Integer getInt(Object key, Integer def) {
        return getIntSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a integer, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     *
     * @param path the path to get the integer from
     * @return the integer at the given path
     * @see #getIntSafe(Path)
     */
    public boolean isInt(Path path) {
        return getIntSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a integer, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @return the integer at the given direct key/string path
     * @see #getIntSafe(Object)
     */
    public boolean isInt(Object key) {
        return getIntSafe(key).isPresent();
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
     * Returns big integer at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a big integer/number (see below), returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the returned value is big integer created from {@link Number#longValue()}.
     *
     * @param path the path to get the integer from
     * @return the integer at the given path
     * @see #getSafe(Path)
     */
    public Optional<BigInteger> getBigIntSafe(Path path) {
        return getAsSafe(path, Number.class).map(number -> number instanceof BigInteger ? (BigInteger) number : BigInteger.valueOf(number.longValue()));
    }

    /**
     * Returns big integer at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a big integer/number (see below), returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the returned value is big integer created from {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @return the integer at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<BigInteger> getBigIntSafe(Object key) {
        return getAsSafe(key, Number.class).map(number -> number instanceof BigInteger ? (BigInteger) number : BigInteger.valueOf(number.longValue()));
    }

    /**
     * Returns big integer at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a big integer/number (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the returned value is big integer created from {@link Number#longValue()}.
     *
     * @param path the path to get the integer from
     * @return the integer at the given path, or default if no supported type (specified above) was found
     * @see #getInt(Path, Integer)
     */
    public BigInteger getBigInt(Path path) {
        return getBigInt(path, BigInteger.valueOf(root.getGeneralSettings().getDefaultNumber().longValue()));
    }

    /**
     * Returns big integer at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a big integer/number (see below), returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the returned value is big integer created from {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @return the integer at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getInt(Object, Integer)
     */
    public BigInteger getBigInt(Object key) {
        return getBigInt(key, BigInteger.valueOf(root.getGeneralSettings().getDefaultNumber().longValue()));
    }

    /**
     * Returns big integer at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a big integer/number (see below), returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the returned value is big integer created from {@link Number#longValue()}.
     *
     * @param path the path to get the integer from
     * @param def  default value returned if no value convertible to integer is present (or no value at all)
     * @return the integer at the given path, or default if no supported type (specified above) was found
     * @see #getIntSafe(Path)
     */
    public BigInteger getBigInt(Path path, BigInteger def) {
        return getBigIntSafe(path).orElse(def);
    }

    /**
     * Returns big integer at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a big integer/number (see below), returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the returned value is big integer created from {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the integer from
     * @param def default value returned if no value convertible to integer is present (or no value at all)
     * @return the integer at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getIntSafe(Object)
     */
    public BigInteger getBigInt(Object key, BigInteger def) {
        return getBigIntSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a big integer, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     *
     * @param path the path to get the big integer from
     * @return the big integer at the given path
     * @see #getBigIntSafe(Path)
     */
    public boolean isBigInt(Path path) {
        return getBigIntSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a big integer, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the big integer from
     * @return the big integer at the given direct key/string path
     * @see #getBigIntSafe(Object)
     */
    public boolean isBigInt(Object key) {
        return getBigIntSafe(key).isPresent();
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
     * Returns boolean at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a boolean, returns an empty optional.
     *
     * @param path the path to get the boolean from
     * @return the boolean at the given path
     * @see #getSafe(Path)
     */
    public Optional<Boolean> getBooleanSafe(Path path) {
        return getAsSafe(path, Boolean.class);
    }

    /**
     * Returns boolean at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a boolean, returns an empty optional.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the boolean from
     * @return the boolean at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Boolean> getBooleanSafe(Object key) {
        return getAsSafe(key, Boolean.class);
    }

    /**
     * Returns boolean at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a boolean, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultBoolean()}.
     *
     * @param path the path to get the boolean from
     * @return the boolean at the given path, or default if no supported type (specified above) was found
     * @see #getBoolean(Path, Boolean)
     */
    public Boolean getBoolean(Path path) {
        return getBoolean(path, root.getGeneralSettings().getDefaultBoolean());
    }

    /**
     * Returns boolean at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a boolean, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultBoolean()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the boolean from
     * @return the boolean at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getBoolean(Object, Boolean)
     */
    public Boolean getBoolean(Object key) {
        return getBoolean(key, root.getGeneralSettings().getDefaultBoolean());
    }

    /**
     * Returns boolean at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a boolean, returns the provided default.
     *
     * @param path the path to get the boolean from
     * @param def  default value returned if no value convertible to boolean is present (or no value at all)
     * @return the boolean at the given path, or default if no supported type (specified above) was found
     * @see #getBooleanSafe(Path)
     */
    public Boolean getBoolean(Path path, Boolean def) {
        return getBooleanSafe(path).orElse(def);
    }

    /**
     * Returns boolean at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a boolean, returns the provided default.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the boolean from
     * @param def default value returned if no value convertible to boolean is present (or no value at all)
     * @return the boolean at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getBooleanSafe(Object)
     */
    public Boolean getBoolean(Object key, Boolean def) {
        return getBooleanSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a boolean, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getBooleanSafe(Path)}.
     *
     * @param path the path to get the boolean from
     * @return the boolean at the given path
     * @see #getBooleanSafe(Path)
     */
    public boolean isBoolean(Path path) {
        return getBooleanSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a boolean, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getBooleanSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the boolean from
     * @return the boolean at the given direct key/string path
     * @see #getBooleanSafe(Object)
     */
    public boolean isBoolean(Object key) {
        return getBooleanSafe(key).isPresent();
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
     * Returns double at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a double, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#doubleValue()}.
     *
     * @param path the path to get the double from
     * @return the double at the given path
     * @see #getSafe(Path)
     */
    public Optional<Double> getDoubleSafe(Path path) {
        return toDouble(getAsSafe(path, Number.class));
    }

    /**
     * Returns double at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a double, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#doubleValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the double from
     * @return the double at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Double> getDoubleSafe(Object key) {
        return toDouble(getAsSafe(key, Number.class));
    }

    /**
     * Returns double at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a double, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Double}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#doubleValue()}.
     *
     * @param path the path to get the double from
     * @return the double at the given path, or default if no supported type (specified above) was found
     * @see #getDouble(Path, Double)
     */
    public Double getDouble(Path path) {
        return getDouble(path, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    /**
     * Returns double at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a double, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Double}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#doubleValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the double from
     * @return the double at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getDouble(Object, Double)
     */
    public Double getDouble(Object key) {
        return getDouble(key, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    /**
     * Returns double at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a double, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#doubleValue()}.
     *
     * @param path the path to get the double from
     * @param def  default value returned if no value convertible to double is present (or no value at all)
     * @return the double at the given path, or default if no supported type (specified above) was found
     * @see #getDoubleSafe(Path)
     */
    public Double getDouble(Path path, Double def) {
        return getDoubleSafe(path).orElse(def);
    }

    /**
     * Returns double at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a double, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#doubleValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the double from
     * @param def default value returned if no value convertible to double is present (or no value at all)
     * @return the double at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getDoubleSafe(Object)
     */
    public Double getDouble(Object key, Double def) {
        return getDoubleSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a double, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     *
     * @param path the path to get the double from
     * @return the double at the given path
     * @see #getDoubleSafe(Path)
     */
    public boolean isDouble(Path path) {
        return getDoubleSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a double, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the double from
     * @return the double at the given direct key/string path
     * @see #getDoubleSafe(Object)
     */
    public boolean isDouble(Object key) {
        return getDoubleSafe(key).isPresent();
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
     * Returns float at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a float, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#floatValue()}.
     *
     * @param path the path to get the float from
     * @return the float at the given path
     * @see #getSafe(Path)
     */
    public Optional<Float> getFloatSafe(Path path) {
        return toFloat(getAsSafe(path, Number.class));
    }

    /**
     * Returns float at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a float, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#floatValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the float from
     * @return the float at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Float> getFloatSafe(Object key) {
        return toFloat(getAsSafe(key, Number.class));
    }

    /**
     * Returns float at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a float, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Float}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#floatValue()}.
     *
     * @param path the path to get the float from
     * @return the float at the given path, or default if no supported type (specified above) was found
     * @see #getFloat(Path, Float)
     */
    public Float getFloat(Path path) {
        return getFloat(path, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    /**
     * Returns float at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a float, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Float}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#floatValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the float from
     * @return the float at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getFloat(Object, Float)
     */
    public Float getFloat(Object key) {
        return getFloat(key, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    /**
     * Returns float at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a float, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#floatValue()}.
     *
     * @param path the path to get the float from
     * @param def  default value returned if no value convertible to float is present (or no value at all)
     * @return the float at the given path, or default if no supported type (specified above) was found
     * @see #getFloatSafe(Path)
     */
    public Float getFloat(Path path, Float def) {
        return getFloatSafe(path).orElse(def);
    }

    /**
     * Returns float at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a float, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#floatValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the float from
     * @param def default value returned if no value convertible to float is present (or no value at all)
     * @return the float at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getFloatSafe(Object)
     */
    public Float getFloat(Object key, Float def) {
        return getFloatSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a float, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     *
     * @param path the path to get the float from
     * @return the float at the given path
     * @see #getFloatSafe(Path)
     */
    public boolean isFloat(Path path) {
        return getFloatSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a float, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the float from
     * @return the float at the given direct key/string path
     * @see #getFloatSafe(Object)
     */
    public boolean isFloat(Object key) {
        return getFloatSafe(key).isPresent();
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
     * Returns byte at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a byte, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#byteValue()}.
     *
     * @param path the path to get the byte from
     * @return the byte at the given path
     * @see #getSafe(Path)
     */
    public Optional<Byte> getByteSafe(Path path) {
        return toByte(getAsSafe(path, Number.class));
    }

    /**
     * Returns byte at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a byte, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#byteValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the byte from
     * @return the byte at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Byte> getByteSafe(Object key) {
        return toByte(getAsSafe(key, Number.class));
    }

    /**
     * Returns byte at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a byte, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Byte}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#byteValue()}.
     *
     * @param path the path to get the byte from
     * @return the byte at the given path, or default if no supported type (specified above) was found
     * @see #getByte(Path, Byte)
     */
    public Byte getByte(Path path) {
        return getByte(path, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    /**
     * Returns byte at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a byte, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Byte}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#byteValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the byte from
     * @return the byte at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getByte(Object, Byte)
     */
    public Byte getByte(Object key) {
        return getByte(key, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    /**
     * Returns byte at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a byte, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#byteValue()}.
     *
     * @param path the path to get the byte from
     * @param def  default value returned if no value convertible to byte is present (or no value at all)
     * @return the byte at the given path, or default if no supported type (specified above) was found
     * @see #getByteSafe(Path)
     */
    public Byte getByte(Path path, Byte def) {
        return getByteSafe(path).orElse(def);
    }

    /**
     * Returns byte at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a byte, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#byteValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the byte from
     * @param def default value returned if no value convertible to byte is present (or no value at all)
     * @return the byte at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getByteSafe(Object)
     */
    public Byte getByte(Object key, Byte def) {
        return getByteSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a byte, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     *
     * @param path the path to get the byte from
     * @return the byte at the given path
     * @see #getByteSafe(Path)
     */
    public boolean isByte(Path path) {
        return getByteSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a byte, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the byte from
     * @return the byte at the given direct key/string path
     * @see #getByteSafe(Object)
     */
    public boolean isByte(Object key) {
        return getByteSafe(key).isPresent();
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
     * Returns long at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a long, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     *
     * @param path the path to get the long from
     * @return the long at the given path
     * @see #getSafe(Path)
     */
    public Optional<Long> getLongSafe(Path path) {
        return toLong(getAsSafe(path, Number.class));
    }

    /**
     * Returns long at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a long, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the long from
     * @return the long at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Long> getLongSafe(Object key) {
        return toLong(getAsSafe(key, Number.class));
    }

    /**
     * Returns long at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a long, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Long}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     *
     * @param path the path to get the long from
     * @return the long at the given path, or default if no supported type (specified above) was found
     * @see #getLong(Path, Long)
     */
    public Long getLong(Path path) {
        return getLong(path, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    /**
     * Returns long at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a long, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Long}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the long from
     * @return the long at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getLong(Object, Long)
     */
    public Long getLong(Object key) {
        return getLong(key, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    /**
     * Returns long at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a long, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     *
     * @param path the path to get the long from
     * @param def  default value returned if no value convertible to long is present (or no value at all)
     * @return the long at the given path, or default if no supported type (specified above) was found
     * @see #getLongSafe(Path)
     */
    public Long getLong(Path path, Long def) {
        return getLongSafe(path).orElse(def);
    }

    /**
     * Returns long at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a long, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the long from
     * @param def default value returned if no value convertible to long is present (or no value at all)
     * @return the long at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getLongSafe(Object)
     */
    public Long getLong(Object key, Long def) {
        return getLongSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a long, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     *
     * @param path the path to get the long from
     * @return the long at the given path
     * @see #getLongSafe(Path)
     */
    public boolean isLong(Path path) {
        return getLongSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a long, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the long from
     * @return the long at the given direct key/string path
     * @see #getLongSafe(Object)
     */
    public boolean isLong(Object key) {
        return getLongSafe(key).isPresent();
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
     * Returns short at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a short, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     *
     * @param path the path to get the short from
     * @return the short at the given path
     * @see #getSafe(Path)
     */
    public Optional<Short> getShortSafe(Path path) {
        return toShort(getAsSafe(path, Number.class));
    }

    /**
     * Returns short at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a short, returns an empty optional.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the short from
     * @return the short at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<Short> getShortSafe(Object key) {
        return toShort(getAsSafe(key, Number.class));
    }

    /**
     * Returns short at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a short, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Short}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     *
     * @param path the path to get the short from
     * @return the short at the given path, or default if no supported type (specified above) was found
     * @see #getShort(Path, Short)
     */
    public Short getShort(Path path) {
        return getShort(path, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    /**
     * Returns short at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a short, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultNumber()} (converted to {@link Short}).
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the short from
     * @return the short at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getShort(Object, Short)
     */
    public Short getShort(Object key) {
        return getShort(key, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    /**
     * Returns short at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a short, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     *
     * @param path the path to get the short from
     * @param def  default value returned if no value convertible to short is present (or no value at all)
     * @return the short at the given path, or default if no supported type (specified above) was found
     * @see #getShortSafe(Path)
     */
    public Short getShort(Path path, Short def) {
        return getShortSafe(path).orElse(def);
    }

    /**
     * Returns short at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a short, returns the provided default.
     * <p>
     * If there is <b>any</b> instance of {@link Number}, the value returned is the result of {@link Number#longValue()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the short from
     * @param def default value returned if no value convertible to short is present (or no value at all)
     * @return the short at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getShortSafe(Object)
     */
    public Short getShort(Object key, Short def) {
        return getShortSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a short, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     *
     * @param path the path to get the short from
     * @return the short at the given path
     * @see #getShortSafe(Path)
     */
    public boolean isShort(Path path) {
        return getShortSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a short, or any other compatible type.
     * Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the short from
     * @return the short at the given direct key/string path
     * @see #getShortSafe(Object)
     */
    public boolean isShort(Object key) {
        return getShortSafe(key).isPresent();
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
     * Returns list at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list, returns an empty optional.
     *
     * @param path the path to get the list from
     * @return the list at the given path
     * @see #getSafe(Path)
     */
    public Optional<List<?>> getListSafe(Path path) {
        return getAsSafe(path, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list, returns an empty optional.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list from
     * @return the list at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<List<?>> getListSafe(Object key) {
        return getAsSafe(key, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     *
     * @param path the path to get the list from
     * @return the list at the given path, or default if no supported type (specified above) was found
     * @see #getList(Path, List)
     */
    public List<?> getList(Path path) {
        return getList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list from
     * @return the list at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getList(Object, List)
     */
    public List<?> getList(Object key) {
        return getList(key, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list, returns the provided default.
     *
     * @param path the path to get the list from
     * @param def  default value returned if no value convertible to list is present (or no value at all)
     * @return the list at the given path, or default if no supported type (specified above) was found
     * @see #getListSafe(Path)
     */
    public List<?> getList(Path path, List<?> def) {
        return getListSafe(path).orElse(def);
    }

    /**
     * Returns list at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list, returns the provided default.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list from
     * @param def default value returned if no value convertible to list is present (or no value at all)
     * @return the list at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getListSafe(Object)
     */
    public List<?> getList(Object key, List<?> def) {
        return getListSafe(key).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a list.
     *
     * @param path the path to get the list from
     * @return the list at the given path
     * @see #getListSafe(Path)
     */
    public boolean isList(Path path) {
        return getListSafe(path).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given path exists and it is a list.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list from
     * @return the list at the given direct key/string path
     * @see #getListSafe(Object)
     */
    public boolean isList(Object key) {
        return getListSafe(key).isPresent();
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
     * Returns list of strings at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of strings, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     *
     * @param path the path to get the list of strings from
     * @return the list of strings at the given path
     * @see #getSafe(Path)
     * @see #getStringSafe(Path)
     */
    public Optional<List<String>> getStringListSafe(Path path) {
        return toStringList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of strings at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of strings, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of strings from
     * @return the list of strings at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getStringSafe(Path)
     */
    public Optional<List<String>> getStringListSafe(Object key) {
        return toStringList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of strings at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of strings, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     *
     * @param path the path to get the list of strings from
     * @param def  default value returned if no value convertible to list of strings is present (or no value at all)
     * @return the list of strings at the given path, or default if no supported type (specified above) was found
     * @see #getStringListSafe(Path)
     * @see #getStringSafe(Path)
     */
    public List<String> getStringList(Path path, List<String> def) {
        return getStringListSafe(path).orElse(def);
    }

    /**
     * Returns list of strings at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of strings, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of strings from
     * @param def default value returned if no value convertible to list of strings is present (or no value at all)
     * @return the list of strings at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getStringListSafe(Object)
     * @see #getStringSafe(Path)
     */
    public List<String> getStringList(Object key, List<String> def) {
        return getStringListSafe(key).orElse(def);
    }

    /**
     * Returns list of strings at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of strings, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     *
     * @param path the path to get the list of strings from
     * @return the list of strings at the given path, or default if no supported type (specified above) was found
     * @see #getStringList(Path, List)
     * @see #getStringSafe(Path)
     */
    public List<String> getStringList(Path path) {
        return getStringList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of strings at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of strings, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getStringSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of strings from
     * @return the list of strings at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getStringList(Object, List)
     * @see #getStringSafe(Path)
     */
    public List<String> getStringList(Object key) {
        return getStringList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of integers at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of integers, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     *
     * @param path the path to get the list of integers from
     * @return the list of integers at the given path
     * @see #getSafe(Path)
     * @see #getIntSafe(Path)
     */
    public Optional<List<Integer>> getIntListSafe(Path path) {
        return toIntList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of integers at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of integers, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of integers from
     * @return the list of integers at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getIntSafe(Path)
     */
    public Optional<List<Integer>> getIntListSafe(Object key) {
        return toIntList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of integers at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of integers, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     *
     * @param path the path to get the list of integers from
     * @param def  default value returned if no value convertible to list of integers is present (or no value at all)
     * @return the list of integers at the given path, or default if no supported type (specified above) was found
     * @see #getIntListSafe(Path)
     * @see #getIntSafe(Path)
     */
    public List<Integer> getIntList(Path path, List<Integer> def) {
        return getIntListSafe(path).orElse(def);
    }

    /**
     * Returns list of integers at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of integers, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of integers from
     * @param def default value returned if no value convertible to list of integers is present (or no value at all)
     * @return the list of integers at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getIntListSafe(Object)
     * @see #getIntSafe(Path)
     */
    public List<Integer> getIntList(Object key, List<Integer> def) {
        return getIntListSafe(key).orElse(def);
    }

    /**
     * Returns list of integers at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of integers, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     *
     * @param path the path to get the list of integers from
     * @return the list of integers at the given path, or default if no supported type (specified above) was found
     * @see #getIntList(Path, List)
     * @see #getIntSafe(Path)
     */
    public List<Integer> getIntList(Path path) {
        return getIntList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of integers at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of integers, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of integers from
     * @return the list of integers at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getIntList(Object, List)
     * @see #getIntSafe(Path)
     */
    public List<Integer> getIntList(Object key) {
        return getIntList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of big integers at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of big integers, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     *
     * @param path the path to get the list of big integers from
     * @return the list of big integers at the given path
     * @see #getSafe(Path)
     * @see #getBigIntSafe(Path)
     */
    public Optional<List<BigInteger>> getBigIntListSafe(Path path) {
        return toBigIntList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of big integers at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of big integers, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of big integers from
     * @return the list of big integers at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getBigIntSafe(Path)
     */
    public Optional<List<BigInteger>> getBigIntListSafe(Object key) {
        return toBigIntList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of big integers at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of big integers, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     *
     * @param path the path to get the list of big integers from
     * @param def  default value returned if no value convertible to list of big integers is present (or no value at all)
     * @return the list of big integers at the given path, or default if no supported type (specified above) was found
     * @see #getBigIntListSafe(Path)
     * @see #getBigIntSafe(Path)
     */
    public List<BigInteger> getBigIntList(Path path, List<BigInteger> def) {
        return getBigIntListSafe(path).orElse(def);
    }

    /**
     * Returns list of big integers at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of big integers, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of big integers from
     * @param def default value returned if no value convertible to list of big integers is present (or no value at all)
     * @return the list of big integers at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getBigIntListSafe(Object)
     * @see #getBigIntSafe(Path)
     */
    public List<BigInteger> getBigIntList(Object key, List<BigInteger> def) {
        return getBigIntListSafe(key).orElse(def);
    }

    /**
     * Returns list of big integers at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of big integers, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     *
     * @param path the path to get the list of big integers from
     * @return the list of big integers at the given path, or default if no supported type (specified above) was found
     * @see #getBigIntList(Path, List)
     * @see #getBigIntSafe(Path)
     */
    public List<BigInteger> getBigIntList(Path path) {
        return getBigIntList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of big integers at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of big integers, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getBigIntSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of big integers from
     * @return the list of big integers at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getBigIntList(Object, List)
     * @see #getBigIntSafe(Path)
     */
    public List<BigInteger> getBigIntList(Object key) {
        return getBigIntList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of bytes at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of bytes, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     *
     * @param path the path to get the list of bytes from
     * @return the list of bytes at the given path
     * @see #getSafe(Path)
     * @see #getByteSafe(Path)
     */
    public Optional<List<Byte>> getByteListSafe(Path path) {
        return toByteList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of bytes at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of bytes, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of bytes from
     * @return the list of bytes at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getByteSafe(Path)
     */
    public Optional<List<Byte>> getByteListSafe(Object key) {
        return toByteList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of bytes at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of bytes, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     *
     * @param path the path to get the list of bytes from
     * @param def  default value returned if no value convertible to list of bytes is present (or no value at all)
     * @return the list of bytes at the given path, or default if no supported type (specified above) was found
     * @see #getByteListSafe(Path)
     * @see #getByteSafe(Path)
     */
    public List<Byte> getByteList(Path path, List<Byte> def) {
        return getByteListSafe(path).orElse(def);
    }

    /**
     * Returns list of bytes at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of bytes, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of bytes from
     * @param def default value returned if no value convertible to list of bytes is present (or no value at all)
     * @return the list of bytes at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getByteListSafe(Object)
     * @see #getByteSafe(Path)
     */
    public List<Byte> getByteList(Object key, List<Byte> def) {
        return getByteListSafe(key).orElse(def);
    }

    /**
     * Returns list of bytes at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of bytes, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     *
     * @param path the path to get the list of bytes from
     * @return the list of bytes at the given path, or default if no supported type (specified above) was found
     * @see #getByteList(Path, List)
     * @see #getByteSafe(Path)
     */
    public List<Byte> getByteList(Path path) {
        return getByteList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of bytes at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of bytes, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getByteSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of bytes from
     * @return the list of bytes at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getByteList(Object, List)
     * @see #getByteSafe(Path)
     */
    public List<Byte> getByteList(Object key) {
        return getByteList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of longs at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of longs, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     *
     * @param path the path to get the list of longs from
     * @return the list of longs at the given path
     * @see #getSafe(Path)
     * @see #getLongSafe(Path)
     */
    public Optional<List<Long>> getLongListSafe(Path path) {
        return toLongList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of longs at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of longs, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of longs from
     * @return the list of longs at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getLongSafe(Path)
     */
    public Optional<List<Long>> getLongListSafe(Object key) {
        return toLongList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of longs at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of longs, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     *
     * @param path the path to get the list of longs from
     * @param def  default value returned if no value convertible to list of longs is present (or no value at all)
     * @return the list of longs at the given path, or default if no supported type (specified above) was found
     * @see #getLongListSafe(Path)
     * @see #getLongSafe(Path)
     */
    public List<Long> getLongList(Path path, List<Long> def) {
        return getLongListSafe(path).orElse(def);
    }

    /**
     * Returns list of longs at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of longs, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of longs from
     * @param def default value returned if no value convertible to list of longs is present (or no value at all)
     * @return the list of longs at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getLongListSafe(Object)
     * @see #getLongSafe(Path)
     */
    public List<Long> getLongList(Object key, List<Long> def) {
        return getLongListSafe(key).orElse(def);
    }

    /**
     * Returns list of longs at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of longs, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     *
     * @param path the path to get the list of longs from
     * @return the list of longs at the given path, or default if no supported type (specified above) was found
     * @see #getLongList(Path, List)
     * @see #getLongSafe(Path)
     */
    public List<Long> getLongList(Path path) {
        return getLongList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of longs at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of longs, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getLongSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of longs from
     * @return the list of longs at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getLongList(Object, List)
     * @see #getLongSafe(Path)
     */
    public List<Long> getLongList(Object key) {
        return getLongList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of doubles at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of doubles, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     *
     * @param path the path to get the list of doubles from
     * @return the list of doubles at the given path
     * @see #getSafe(Path)
     * @see #getDoubleSafe(Path)
     */
    public Optional<List<Double>> getDoubleListSafe(Path path) {
        return toDoubleList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of doubles at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of doubles, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of doubles from
     * @return the list of doubles at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getDoubleSafe(Path)
     */
    public Optional<List<Double>> getDoubleListSafe(Object key) {
        return toDoubleList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of doubles at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of doubles, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     *
     * @param path the path to get the list of doubles from
     * @param def  default value returned if no value convertible to list of doubles is present (or no value at all)
     * @return the list of doubles at the given path, or default if no supported type (specified above) was found
     * @see #getDoubleListSafe(Path)
     * @see #getDoubleSafe(Path)
     */
    public List<Double> getDoubleList(Path path, List<Double> def) {
        return getDoubleListSafe(path).orElse(def);
    }

    /**
     * Returns list of doubles at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of doubles, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of doubles from
     * @param def default value returned if no value convertible to list of doubles is present (or no value at all)
     * @return the list of doubles at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getDoubleListSafe(Object)
     * @see #getDoubleSafe(Path)
     */
    public List<Double> getDoubleList(Object key, List<Double> def) {
        return getDoubleListSafe(key).orElse(def);
    }

    /**
     * Returns list of doubles at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of doubles, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     *
     * @param path the path to get the list of doubles from
     * @return the list of doubles at the given path, or default if no supported type (specified above) was found
     * @see #getDoubleList(Path, List)
     * @see #getDoubleSafe(Path)
     */
    public List<Double> getDoubleList(Path path) {
        return getDoubleList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of doubles at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of doubles, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getDoubleSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of doubles from
     * @return the list of doubles at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getDoubleList(Object, List)
     * @see #getDoubleSafe(Path)
     */
    public List<Double> getDoubleList(Object key) {
        return getDoubleList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of floats at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of floats, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     *
     * @param path the path to get the list of floats from
     * @return the list of floats at the given path
     * @see #getSafe(Path)
     * @see #getFloatSafe(Path)
     */
    public Optional<List<Float>> getFloatListSafe(Path path) {
        return toFloatList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of floats at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of floats, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of floats from
     * @return the list of floats at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getFloatSafe(Path)
     */
    public Optional<List<Float>> getFloatListSafe(Object key) {
        return toFloatList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of floats at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of floats, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     *
     * @param path the path to get the list of floats from
     * @param def  default value returned if no value convertible to list of floats is present (or no value at all)
     * @return the list of floats at the given path, or default if no supported type (specified above) was found
     * @see #getFloatListSafe(Path)
     * @see #getFloatSafe(Path)
     */
    public List<Float> getFloatList(Path path, List<Float> def) {
        return getFloatListSafe(path).orElse(def);
    }

    /**
     * Returns list of floats at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of floats, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of floats from
     * @param def default value returned if no value convertible to list of floats is present (or no value at all)
     * @return the list of floats at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getFloatListSafe(Object)
     * @see #getFloatSafe(Path)
     */
    public List<Float> getFloatList(Object key, List<Float> def) {
        return getFloatListSafe(key).orElse(def);
    }

    /**
     * Returns list of floats at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of floats, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     *
     * @param path the path to get the list of floats from
     * @return the list of floats at the given path, or default if no supported type (specified above) was found
     * @see #getFloatList(Path, List)
     * @see #getFloatSafe(Path)
     */
    public List<Float> getFloatList(Path path) {
        return getFloatList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of floats at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of floats, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getFloatSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of floats from
     * @return the list of floats at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getFloatList(Object, List)
     * @see #getFloatSafe(Path)
     */
    public List<Float> getFloatList(Object key) {
        return getFloatList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of shorts at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of shorts, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     *
     * @param path the path to get the list of shorts from
     * @return the list of shorts at the given path
     * @see #getSafe(Path)
     * @see #getShortSafe(Path)
     */
    public Optional<List<Short>> getShortListSafe(Path path) {
        return toShortList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of shorts at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of shorts, returns an empty optional.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of shorts from
     * @return the list of shorts at the given direct key/string path
     * @see #getSafe(Object)
     * @see #getShortSafe(Path)
     */
    public Optional<List<Short>> getShortListSafe(Object key) {
        return toShortList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of shorts at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of shorts, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     *
     * @param path the path to get the list of shorts from
     * @param def  default value returned if no value convertible to list of shorts is present (or no value at all)
     * @return the list of shorts at the given path, or default if no supported type (specified above) was found
     * @see #getShortListSafe(Path)
     * @see #getShortSafe(Path)
     */
    public List<Short> getShortList(Path path, List<Short> def) {
        return getShortListSafe(path).orElse(def);
    }

    /**
     * Returns list of shorts at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of shorts, returns the provided default.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of shorts from
     * @param def default value returned if no value convertible to list of shorts is present (or no value at all)
     * @return the list of shorts at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getShortListSafe(Object)
     * @see #getShortSafe(Path)
     */
    public List<Short> getShortList(Object key, List<Short> def) {
        return getShortListSafe(key).orElse(def);
    }

    /**
     * Returns list of shorts at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of shorts, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     *
     * @param path the path to get the list of shorts from
     * @return the list of shorts at the given path, or default if no supported type (specified above) was found
     * @see #getShortList(Path, List)
     * @see #getShortSafe(Path)
     */
    public List<Short> getShortList(Path path) {
        return getShortList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of shorts at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of shorts, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * The individual elements of the list are processed each one by one. If there is an element incompatible, it is skipped and will not
     * appear in the returned list. Please learn more about compatible types at the main content method {@link #getShortSafe(Path)}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of shorts from
     * @return the list of shorts at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getShortList(Object, List)
     * @see #getShortSafe(Path)
     */
    public List<Short> getShortList(Object key) {
        return getShortList(key, root.getGeneralSettings().getDefaultList());
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
     * Returns list of maps at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of maps, returns an empty optional.
     *
     * @param path the path to get the list of maps from
     * @return the list of maps at the given path
     * @see #getSafe(Path)
     */
    public Optional<List<Map<?, ?>>> getMapListSafe(Path path) {
        return toMapList(getAsSafe(path, List.class));
    }

    /**
     * Returns list of maps at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of maps, returns an empty optional.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of maps from
     * @return the list of maps at the given direct key/string path
     * @see #getSafe(Object)
     */
    public Optional<List<Map<?, ?>>> getMapListSafe(Object key) {
        return toMapList(getAsSafe(key, List.class));
    }

    /**
     * Returns list of maps at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of maps, returns the provided default.
     *
     * @param path the path to get the list of maps from
     * @param def  default value returned if no value convertible to list of maps is present (or no value at all)
     * @return the list of maps at the given path, or default if no supported type (specified above) was found
     * @see #getMapListSafe(Path)
     */
    public List<Map<?, ?>> getMapList(Path path, List<Map<?, ?>> def) {
        return getMapListSafe(path).orElse(def);
    }

    /**
     * Returns list of maps at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of maps, returns the provided default.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of maps from
     * @param def default value returned if no value convertible to list of maps is present (or no value at all)
     * @return the list of maps at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getMapListSafe(Object)
     */
    public List<Map<?, ?>> getMapList(Object key, List<Map<?, ?>> def) {
        return getMapListSafe(key).orElse(def);
    }

    /**
     * Returns list of maps at the given path encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * path, or is not a list of maps, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     *
     * @param path the path to get the list of maps from
     * @return the list of maps at the given path, or default if no supported type (specified above) was found
     * @see #getMapList(Path, List)
     */
    public List<Map<?, ?>> getMapList(Path path) {
        return getMapList(path, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of maps at the given direct key/string path (determined by the root's path mode) encapsulated in an instance of {@link Optional}. If nothing exists at the given
     * direct key/string path, or is not a list of maps, returns default value as defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * <b>This method is chained and/or based on {@link #getBlockSafe(Object)} and therefore, supports the same pathing
     * (keying) mechanics. Please look at the description of that method for more detailed information regarding the
     * usage.</b>
     *
     * @param key the direct key/string path to get the list of maps from
     * @return the list of maps at the given direct key/string path, or default if no supported type (specified above) was found
     * @see #getMapList(Object, List)
     */
    public List<Map<?, ?>> getMapList(Object key) {
        return getMapList(key, root.getGeneralSettings().getDefaultList());
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
    public Path getSubPath(Object key) {
        return Path.addTo(path, key);
    }

}