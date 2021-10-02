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
import java.util.function.Consumer;

import static com.davidcubesvk.yamlUpdater.core.utils.conversion.NumericConversions.*;
import static com.davidcubesvk.yamlUpdater.core.utils.conversion.ListConversions.*;

/**
 * Represents one YAML section, while storing it's contents and comments. Section can also be referred to as
 * <i>collection of mappings (key=value pairs)</i>.
 */
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
        this.name = "";
        this.path = new Path();
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
                    new Section(root, this, key, path.add(key), superNodeComments ? tuple.getKeyNode() : valueNode, (MappingNode) tuple.getValueNode(), constructor) :
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
     * mapping (not a subsection), returns <code>false</code>. Similarly, if it is a section, runs
     * {@link #isEmpty(boolean)} (with <code>deep</code> set to <code>true</code>) and returns <code>false</code> if the
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
     * returns the result of {@link Object#toString()} on the given key object, the key object otherwise.
     *
     * @param key the key object to adapt
     * @return the adapted key
     */
    public Object adaptKey(Object key) {
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
        addData((path, entry) -> keys.add(path), new Path(), deep);
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
        addData((path, entry) -> values.put(path, entry.getValue() instanceof Section ? entry.getValue() : entry.getValue().getValue()), new Path(), deep);
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
        addData((path, entry) -> blocks.put(path, entry.getValue()), new Path(), deep);
        //Return
        return blocks;
    }

    /**
     * Iterates through all entries in the underlying map, while calling the given consumer for each entry. The path
     * given is the path relative to this section.
     *
     * If any of the entries contain an instance of {@link Section} as their value and <code>deep</code> is set to
     * <code>true</code>, this method is called on each sub-section with the same consumer (while the path is managed to
     * be always relative to this section).
     * @param consumer the consumer to call for each entry
     * @param current the path to the currently iterated section, relative to the main caller section
     * @param deep if to iterate deeply
     */
    private void addData(@NotNull BiConsumer<Path, Map.Entry<?, Block<?>>> consumer, @Nullable Path current, boolean deep) {
        //All keys
        for (Map.Entry<?, Block<?>> entry : getValue().entrySet()) {
            //Path to this entry
            Path entryPath = current.add(entry.getKey());
            //Call
            consumer.accept(current, entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, entryPath, true);
        }
    }

    /**
     * Returns whether this section is simultaneously the root section (file).
     * @return if this section is the root section
     */
    public boolean isRoot() {
        return false;
    }

    /**
     * Returns whether this section contains anything at the given path.
     * @param path the path to check
     * @return if this section contains anything at the given path
     */
    public boolean contains(@NotNull Path path) {
        return getSafe(path).isPresent();
    }

    /**
     * Returns whether this section contains any direct object at the given key. More formally, returns the result of
     * {@link Map#containsKey(Object)} called on the underlying map, with the given key as the parameter.
     * @param key the key to check
     * @return if this section contains anything (directly) at the given key
     */
    public boolean contains(@Nullable Object key) {
        return getValue().containsKey(key);
    }

    /**
     * Adapts this section (including sub-sections) to the new relatives. This method should be called if and only this
     * section was relocated to a new parent section.
     *
     * @param root new root file
     * @param parent new parent section
     * @param name new name for this section
     * @param path new path for this section
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

    private void setInternal(Path path, Object value, int i) {
        //Key
        Object key = path.getKey(i);
        //If at the last index
        if (i + 1 >= path.getLength()) {
            //Call the direct method
            set(key, value);
            return;
        }

        //The block at the key
        Block<?> block = getValue().getOrDefault(path.getKey(i), null);
        //If null
        if (block == null || block instanceof Mapping)
            //Create
            createSection(key, block).setInternal(path, value, i + 1);
        else
            //Call subsection
            ((Section) block).setInternal(path, value, i + 1);
    }

    public Section createSection(Path path) {
        //Current section
        Section current = this;
        //All keys
        for (int i = 0; i < path.getLength(); i++)
            //Create
            current = current.createSection(path.getKey(i));
        //Return
        return current;
    }

    public Section createSection(Object key) {
        return createSection(key, null);
    }

    public Section createSection(Object key, Block<?> previous) {
        return getSectionSafe(key).orElseGet(() -> {
            Section section = new Section(root, Section.this, key, path.add(key), previous, root.getGeneralSettings().getDefaultMap());
            getValue().put(key, section);
            return section;
        });
    }

    public void set(Path path, Object value) {
        setInternal(path, value, 0);
    }

    public void set(Object key, Object value) {
        //If null (delete)
        if (value == null)
            remove(key);

        System.out.println("SETTING " + value + " at key=" + key);

        //If a section
        if (value instanceof Section) {
            //Cast
            Section section = (Section) value;
            //Set
            getValue().put(key, section);
            //Adapt
            section.adapt(root, this, key, path.add(key));
            return;
        } else if (value instanceof Mapping) {
            //Set
            getValue().put(key, (Mapping) value);
            return;
        }

        //If a map
        if (value instanceof Map) {
            //Add
            getValue().put(key, new Section(root, this, key, path.add(key), getValue().getOrDefault(key, null), (Map<?, ?>) value));
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

    public boolean remove(Object key) {
        return getValue().remove(key) != null;
    }

    public Optional<Block<?>> getBlock(Path path) {
        return getSafeInternal(path, 0, false);
    }

    public Optional<Block<?>> getBlock(Object key) {
        //If is string mode and contains sub-key
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.STRING_BASED && key.toString().indexOf(root.getGeneralSettings().getSeparator()) != -1)
            //Return
            return getSafeInternalString(key.toString(), 0, false);
        //If does not contain
        if (!getValue().containsKey(key))
            return Optional.empty();

        //Return
        return Optional.of(getValue().get(key));
    }

    public Optional<Section> getParent(Path path) {
        return getSafeInternal(path, 0, true).map(block -> block instanceof Section ? (Section) block : null);
    }

    public Optional<Section> getParent(Object key) {
        //If is string mode and contains sub-key
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.STRING_BASED && key.toString().indexOf(root.getGeneralSettings().getSeparator()) != -1)
            //Return
            return getSafeInternalString(key.toString(), 0, true).map(block -> block instanceof Section ? (Section) block : null);
        //If does not contain
        if (!getValue().containsKey(key))
            return Optional.empty();

        //Return
        return Optional.of(getValue().get(key)).map(block -> block instanceof Section ? (Section) block : null);
    }

    private Optional<Block<?>> getSafeInternalString(String path, int i, boolean parent) {
        //Next separator
        int next = path.indexOf(i + 1, root.getGeneralSettings().getSeparator());
        //If -1
        if (next == -1)
            return parent ? Optional.of(this) : getBlock(path.substring(i));
        //Call subsection
        return getSectionSafe(path.substring(i, next)).flatMap(section -> section.getSafeInternalString(path, next, parent));
    }

    private Optional<Block<?>> getSafeInternal(Path path, int i, boolean parent) {
        //If length is 0
        if (path.getLength() == 0)
            return Optional.of(this);

        //If at last index
        if (i + 1 >= path.getLength())
            return parent ? Optional.of(this) : getBlock(path.getKey(i));

        //Section
        Optional<Block<?>> section = getBlock(path.getKey(i));
        //If not present
        if (!section.isPresent())
            return Optional.empty();
        //If not a section
        if (!(section.get() instanceof Section))
            return Optional.empty();
        //Return
        return ((Section) section.get()).getSafeInternal(path, i + 1, parent);
    }

    public Optional<Object> getSafe(Path path) {
        return getSafeInternal(path, 0, false).map(Block::getValue);
    }

    public Optional<Object> getSafe(Object key) {
        //If is string mode and contains sub-key
        if (root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.STRING_BASED && key.toString().indexOf(root.getGeneralSettings().getSeparator()) != -1)
            //Return
            return getSafeInternalString(key.toString(), 0, false).map(Block::getValue);
        //If does not contain
        if (!getValue().containsKey(key))
            return Optional.empty();

        //Return
        return Optional.of(getValue().get(key).getValue());
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(Path path, Class<T> clazz) {
        //The value
        Optional<?> value = getSafe(path);
        //If empty or if not an instance of the target type
        if (!value.isPresent() || !clazz.isInstance(value.get()))
            return Optional.empty();

        //Return
        return Optional.of((T) value.get());
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(Object key, Class<T> clazz) {
        //The value
        Optional<?> value = getSafe(key);
        //If empty or if not an instance of the target type
        if (!value.isPresent() || !clazz.isInstance(value.get()))
            return Optional.empty();

        //Return
        return Optional.of((T) value.get());
    }


    public Object get(Path path) {
        return getSafe(path).orElse(root.getGeneralSettings().getDefaultObject());
    }

    public Object get(Object key) {
        return getSafe(key).orElse(root.getGeneralSettings().getDefaultObject());
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Path path, Class<T> clazz) {
        //The value
        Optional<?> value = getSafe(path);
        //If empty
        if (!value.isPresent())
            return null;
        //If not an instance of the target type
        if (!clazz.isInstance(value.get()))
            return null;

        //Return
        return (T) value.get();
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Object key, Class<T> clazz) {
        //The value
        Optional<?> value = getSafe(key);
        //If empty
        if (!value.isPresent())
            return null;
        //If not an instance of the target type
        if (!clazz.isInstance(value.get()))
            return null;

        //Return
        return (T) value.get();
    }

    public Object get(Path path, Object def) {
        return getSafe(path).orElse(def);
    }

    public Object get(Object key, Object def) {
        return getSafe(key).orElse(def);
    }

    public <T> T getAs(Path path, Class<T> clazz, T def) {
        return getAsSafe(path, clazz).orElse(def);
    }

    public <T> T getAs(Object key, Class<T> clazz, T def) {
        return getAsSafe(key, clazz).orElse(def);
    }

    public <T> boolean is(Path path, Class<T> clazz) {
        return getAsSafe(path, clazz).isPresent();
    }

    public <T> boolean is(Object key, Class<T> clazz) {
        return getAsSafe(key, clazz).isPresent();
    }

    public Optional<Section> getSectionSafe(Path path) {
        return getAsSafe(path, Section.class);
    }

    public Optional<Section> getSectionSafe(Object key) {
        return getAsSafe(key, Section.class);
    }

    public Section getSection(Path path) {
        return getSection(path, root.getGeneralSettings().getDefaultSection());
    }

    public Section getSection(Object key) {
        return getSection(key, root.getGeneralSettings().getDefaultSection());
    }

    public Section getSection(Path path, Section def) {
        return getSectionSafe(path).orElse(def);
    }

    public Section getSection(Object key, Section def) {
        return getSectionSafe(key).orElse(def);
    }

    public boolean isSection(Path path) {
        return getSectionSafe(path).isPresent();
    }

    public boolean isSection(Object key) {
        return getSectionSafe(key).isPresent();
    }

    public Optional<String> getStringSafe(Path path) {
        return getAsSafe(path, String.class);
    }

    public Optional<String> getStringSafe(Object key) {
        return getAsSafe(key, String.class);
    }

    public String getString(Path path) {
        return getString(path, root.getGeneralSettings().getDefaultString());
    }

    public String getString(Object key) {
        return getString(key, root.getGeneralSettings().getDefaultString());
    }

    public String getString(Path path, String def) {
        return getStringSafe(path).orElse(def);
    }

    public String getString(Object key, String def) {
        return getStringSafe(key).orElse(def);
    }

    public boolean isString(Path path) {
        return getStringSafe(path).isPresent();
    }

    public boolean isString(Object key) {
        return getStringSafe(key).isPresent();
    }

    public Optional<Character> getCharSafe(Path path) {
        return parseChar(getStringSafe(path));
    }

    public Optional<Character> getCharSafe(Object key) {
        return parseChar(getStringSafe(key));
    }

    private Optional<Character> parseChar(Optional<String> value) {
        //If empty or the string is longer
        if (!value.isPresent() || value.get().length() != 1)
            return Optional.empty();
        //Return
        return Optional.of(value.get().charAt(0));
    }

    public Character getChar(Path path) {
        return getChar(path, root.getGeneralSettings().getDefaultChar());
    }

    public Character getChar(Object key) {
        return getChar(key, root.getGeneralSettings().getDefaultChar());
    }

    public Character getChar(Path path, Character def) {
        return getCharSafe(path).orElse(def);
    }

    public Character getChar(Object key, Character def) {
        return getCharSafe(key).orElse(def);
    }

    public boolean isChar(Path path) {
        return getCharSafe(path).isPresent();
    }

    public boolean isChar(Object key) {
        return getCharSafe(key).isPresent();
    }

    public Optional<Integer> getIntSafe(Path path) {
        return toInt(getAsSafe(path, Number.class));
    }

    public Optional<Integer> getIntSafe(Object key) {
        return toInt(getAsSafe(key, Number.class));
    }

    public Integer getInt(Path path, Integer def) {
        return getIntSafe(path).orElse(def);
    }

    public Integer getInt(Object key, Integer def) {
        return getIntSafe(key).orElse(def);
    }

    public Integer getInt(Path path) {
        return getInt(path, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    public Integer getInt(Object key) {
        return getInt(key, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    public boolean isInt(Path path) {
        return getIntSafe(path).isPresent();
    }

    public boolean isInt(Object key) {
        return getIntSafe(key).isPresent();
    }

    public Optional<Boolean> getBooleanSafe(Path path) {
        return getAsSafe(path, Boolean.class);
    }

    public Optional<Boolean> getBooleanSafe(Object key) {
        return getAsSafe(key, Boolean.class);
    }

    public Boolean getBoolean(Path path, Boolean def) {
        return getBooleanSafe(path).orElse(def);
    }

    public Boolean getBoolean(Object key, Boolean def) {
        return getBooleanSafe(key).orElse(def);
    }

    public Boolean getBoolean(Path path) {
        return getBoolean(path, root.getGeneralSettings().getDefaultBoolean());
    }

    public Boolean getBoolean(Object key) {
        return getBoolean(key, root.getGeneralSettings().getDefaultBoolean());
    }

    public boolean isBoolean(Path path) {
        return getBooleanSafe(path).isPresent();
    }

    public boolean isBoolean(Object key) {
        return getBooleanSafe(key).isPresent();
    }

    public Optional<Double> getDoubleSafe(Path path) {
        return toDouble(getAsSafe(path, Number.class));
    }

    public Optional<Double> getDoubleSafe(Object key) {
        return toDouble(getAsSafe(key, Number.class));
    }

    public Double getDouble(Path path, Double def) {
        return getDoubleSafe(path).orElse(def);
    }

    public Double getDouble(Object key, Double def) {
        return getDoubleSafe(key).orElse(def);
    }

    public Double getDouble(Path path) {
        return getDouble(path, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    public Double getDouble(Object key) {
        return getDouble(key, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    public boolean isDouble(Path path) {
        return getDoubleSafe(path).isPresent();
    }

    public boolean isDouble(Object key) {
        return getDoubleSafe(key).isPresent();
    }

    public Optional<Float> getFloatSafe(Path path) {
        return toFloat(getAsSafe(path, Number.class));
    }

    public Optional<Float> getFloatSafe(Object key) {
        return toFloat(getAsSafe(key, Number.class));
    }

    public Float getFloat(Path path, Float def) {
        return getFloatSafe(path).orElse(def);
    }

    public Float getFloat(Object key, Float def) {
        return getFloatSafe(key).orElse(def);
    }

    public Float getFloat(Path path) {
        return getFloat(path, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    public Float getFloat(Object key) {
        return getFloat(key, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    public boolean isFloat(Path path) {
        return getFloatSafe(path).isPresent();
    }

    public boolean isFloat(Object key) {
        return getFloatSafe(key).isPresent();
    }

    public Optional<Byte> getByteSafe(Path path) {
        return toByte(getAsSafe(path, Number.class));
    }

    public Optional<Byte> getByteSafe(Object key) {
        return toByte(getAsSafe(key, Number.class));
    }

    public Byte getByte(Path path, Byte def) {
        return getByteSafe(path).orElse(def);
    }

    public Byte getByte(Object key, Byte def) {
        return getByteSafe(key).orElse(def);
    }

    public Byte getByte(Path path) {
        return getByte(path, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    public Byte getByte(Object key) {
        return getByte(key, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    public boolean isByte(Path path) {
        return getByteSafe(path).isPresent();
    }

    public boolean isByte(Object key) {
        return getByteSafe(key).isPresent();
    }

    public Optional<Long> getLongSafe(Path path) {
        return toLong(getAsSafe(path, Number.class));
    }

    public Optional<Long> getLongSafe(Object key) {
        return toLong(getAsSafe(key, Number.class));
    }

    public Long getLong(Path path, Long def) {
        return getLongSafe(path).orElse(def);
    }

    public Long getLong(Object key, Long def) {
        return getLongSafe(key).orElse(def);
    }

    public Long getLong(Path path) {
        return getLong(path, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    public Long getLong(Object key) {
        return getLong(key, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    public boolean isLong(Path path) {
        return getLongSafe(path).isPresent();
    }

    public boolean isLong(Object key) {
        return getLongSafe(key).isPresent();
    }

    public Optional<Short> getShortSafe(Path path) {
        return toShort(getAsSafe(path, Number.class));
    }

    public Optional<Short> getShortSafe(Object key) {
        return toShort(getAsSafe(key, Number.class));
    }

    public Short getShort(Path path, Short def) {
        return getShortSafe(path).orElse(def);
    }

    public Short getShort(Object key, Short def) {
        return getShortSafe(key).orElse(def);
    }

    public Short getShort(Path path) {
        return getShort(path, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    public Short getShort(Object key) {
        return getShort(key, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    public boolean isShort(Path path) {
        return getShortSafe(path).isPresent();
    }

    public boolean isShort(Object key) {
        return getShortSafe(key).isPresent();
    }

    public Optional<List<?>> getListSafe(Path path) {
        return getAsSafe(path, List.class).map(list -> (List<?>) list);
    }

    public Optional<List<?>> getListSafe(Object key) {
        return getAsSafe(key, List.class).map(list -> (List<?>) list);
    }

    public List<?> getList(Path path, List<?> def) {
        return getListSafe(path).orElse(def);
    }

    public List<?> getList(Object key, List<?> def) {
        return getListSafe(key).orElse(def);
    }

    public List<?> getList(Path path) {
        return getList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<?> getList(Object key) {
        return getList(key, root.getGeneralSettings().getDefaultList());
    }

    public boolean isList(Path path) {
        return getListSafe(path).isPresent();
    }

    public boolean isList(Object key) {
        return getListSafe(key).isPresent();
    }

    public Optional<List<String>> getStringListSafe(Path path) {
        return toStringList(getAsSafe(path, List.class));
    }

    public Optional<List<String>> getStringListSafe(Object key) {
        return toStringList(getAsSafe(key, List.class));
    }

    public List<String> getStringList(Path path, List<String> def) {
        return getStringListSafe(path).orElse(def);
    }

    public List<String> getStringList(Object key, List<String> def) {
        return getStringListSafe(key).orElse(def);
    }

    public List<String> getStringList(Path path) {
        return getStringList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<String> getStringList(Object key) {
        return getStringList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Integer>> getIntListSafe(Path path) {
        return toIntList(getAsSafe(path, List.class));
    }

    public Optional<List<Integer>> getIntListSafe(Object key) {
        return toIntList(getAsSafe(key, List.class));
    }

    public List<Integer> getIntList(Path path, List<Integer> def) {
        return getIntListSafe(path).orElse(def);
    }

    public List<Integer> getIntList(Object key, List<Integer> def) {
        return getIntListSafe(key).orElse(def);
    }

    public List<Integer> getIntList(Path path) {
        return getIntList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Integer> getIntList(Object key) {
        return getIntList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<BigInteger>> getBigIntListSafe(Path path) {
        return toBigIntList(getAsSafe(path, List.class));
    }

    public Optional<List<BigInteger>> getBigIntListSafe(Object key) {
        return toBigIntList(getAsSafe(key, List.class));
    }

    public List<BigInteger> getBigIntList(Path path, List<BigInteger> def) {
        return getBigIntListSafe(path).orElse(def);
    }

    public List<BigInteger> getBigIntList(Object key, List<BigInteger> def) {
        return getBigIntListSafe(key).orElse(def);
    }

    public List<BigInteger> getBigIntList(Path path) {
        return getBigIntList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<BigInteger> getBigIntList(Object key) {
        return getBigIntList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Byte>> getByteListSafe(Path path) {
        return toByteList(getAsSafe(path, List.class));
    }

    public Optional<List<Byte>> getByteListSafe(Object key) {
        return toByteList(getAsSafe(key, List.class));
    }

    public List<Byte> getByteList(Path path, List<Byte> def) {
        return getByteListSafe(path).orElse(def);
    }

    public List<Byte> getByteList(Object key, List<Byte> def) {
        return getByteListSafe(key).orElse(def);
    }

    public List<Byte> getByteList(Path path) {
        return getByteList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Byte> getByteList(Object key) {
        return getByteList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Long>> getLongListSafe(Path path) {
        return toLongList(getAsSafe(path, List.class));
    }

    public Optional<List<Long>> getLongListSafe(Object key) {
        return toLongList(getAsSafe(key, List.class));
    }

    public List<Long> getLongList(Path path, List<Long> def) {
        return getLongListSafe(path).orElse(def);
    }

    public List<Long> getLongList(Object key, List<Long> def) {
        return getLongListSafe(key).orElse(def);
    }

    public List<Long> getLongList(Path path) {
        return getLongList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Long> getLongList(Object key) {
        return getLongList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Double>> getDoubleListSafe(Path path) {
        return toDoubleList(getAsSafe(path, List.class));
    }

    public Optional<List<Double>> getDoubleListSafe(Object key) {
        return toDoubleList(getAsSafe(key, List.class));
    }

    public List<Double> getDoubleList(Path path, List<Double> def) {
        return getDoubleListSafe(path).orElse(def);
    }

    public List<Double> getDoubleList(Object key, List<Double> def) {
        return getDoubleListSafe(key).orElse(def);
    }

    public List<Double> getDoubleList(Path path) {
        return getDoubleList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Double> getDoubleList(Object key) {
        return getDoubleList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Float>> getFloatListSafe(Path path) {
        return toFloatList(getAsSafe(path, List.class));
    }

    public Optional<List<Float>> getFloatListSafe(Object key) {
        return toFloatList(getAsSafe(key, List.class));
    }

    public List<Float> getFloatList(Path path, List<Float> def) {
        return getFloatListSafe(path).orElse(def);
    }

    public List<Float> getFloatList(Object key, List<Float> def) {
        return getFloatListSafe(key).orElse(def);
    }

    public List<Float> getFloatList(Path path) {
        return getFloatList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Float> getFloatList(Object key) {
        return getFloatList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Short>> getShortListSafe(Path path) {
        return toShortList(getAsSafe(path, List.class));
    }

    public Optional<List<Short>> getShortListSafe(Object key) {
        return toShortList(getAsSafe(key, List.class));
    }

    public List<Short> getShortList(Path path, List<Short> def) {
        return getShortListSafe(path).orElse(def);
    }

    public List<Short> getShortList(Object key, List<Short> def) {
        return getShortListSafe(key).orElse(def);
    }

    public List<Short> getShortList(Path path) {
        return getShortList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Short> getShortList(Object key) {
        return getShortList(key, root.getGeneralSettings().getDefaultList());
    }

    public Optional<List<Map<?, ?>>> getMapListSafe(Path path) {
        return toMapList(getAsSafe(path, List.class));
    }

    public Optional<List<Map<?, ?>>> getMapListSafe(Object key) {
        return toMapList(getAsSafe(key, List.class));
    }

    public List<Map<?, ?>> getMapList(Path path, List<Map<?, ?>> def) {
        return getMapListSafe(path).orElse(def);
    }

    public List<Map<?, ?>> getMapList(Object key, List<Map<?, ?>> def) {
        return getMapListSafe(key).orElse(def);
    }

    public List<Map<?, ?>> getMapList(Path path) {
        return getMapList(path, root.getGeneralSettings().getDefaultList());
    }

    public List<Map<?, ?>> getMapList(Object key) {
        return getMapList(key, root.getGeneralSettings().getDefaultList());
    }

    public YamlFile getRoot() {
        return root;
    }

    public Section getParent() {
        return parent;
    }

    public Object getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

}