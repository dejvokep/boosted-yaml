package com.davidcubesvk.yamlUpdater.core.block.implementation;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.engine.ExtendedConstructor;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings.KeyMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.davidcubesvk.yamlUpdater.core.utils.conversion.ListConversions.*;
import static com.davidcubesvk.yamlUpdater.core.utils.conversion.NumericConversions.*;

/**
 * Represents one YAML section (map), while storing its contents and comments. Section can also be referred to as
 * <i>collection of mapping entries (key=value pairs)</i>.
 * <p>
 * BoostedYAML represents every entry (key=value pair) in the file as a block, which also applies to sections - sections
 * actually store {@link Block blocks}, which carry the raw value - Java object. Please learn more about implementations
 * at {wiki}.
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
    private Route route;

    /**
     * Creates a section using the given relatives, nodes and constructor, which is used to retrieve the actual Java
     * representations of nodes (and sub-nodes).
     *
     * @param root        root file
     * @param parent      parent section (or <code>null</code> if this is the root section)
     * @param route       absolute (starting from the root file) route to this section
     * @param keyNode     node which represents the key to this section, used <b>only</b> to retrieve comments
     * @param valueNode   node which represents this section's contents
     * @param constructor constructor used to construct all the nodes contained within the root file, used to retrieve
     *                    Java instances of the nodes
     * @see Block#Block(Node, Node, Object) superclass constructor used
     */
    public Section(@NotNull YamlFile root, @Nullable Section parent, @NotNull Route route, @Nullable Node keyNode, @NotNull MappingNode valueNode, @NotNull ExtendedConstructor constructor) {
        //Call superclass (value node is null because there can't be any value comments)
        super(keyNode, valueNode, root.getGeneralSettings().getDefaultMap());
        //Set
        this.root = root;
        this.parent = parent;
        this.name = adaptKey(route.get(route.length() - 1));
        this.route = route;
        //Init
        init(root, keyNode, valueNode, constructor);
    }

    /**
     * Creates a section using the given relatives, previous block and mappings.
     *
     * @param root     root file
     * @param parent   parent section (or <code>null</code> if this is the root section)
     * @param route    absolute (starting from the root file) route to this section
     * @param previous previous block at the same position, used to reference comments from
     * @param mappings raw (containing Java values directly; no {@link Block} instances) content map
     * @see Block#Block(Block, Object) superclass constructor used
     */
    public Section(@NotNull YamlFile root, @Nullable Section parent, @NotNull Route route, @Nullable Block<?> previous, @NotNull Map<?, ?> mappings) {
        //Call superclass
        super(previous, root.getGeneralSettings().getDefaultMap());
        //Set
        this.root = root;
        this.parent = parent;
        this.name = adaptKey(route.get(route.length() - 1));
        this.route = route;
        //Loop through all mappings
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            //Key and value
            Object key = adaptKey(entry.getKey()), value = entry.getValue();
            //Add
            getStoredValue().put(key, value instanceof Map ? new Section(root, this, route.add(key), null, (Map<?, ?>) value) : new Entry(null, value));
        }
    }

    /**
     * Creates a section using the given (not necessarily empty) instance of default map.
     * <p>
     * Sets the root file, parent section, name and route to <code>null</code>.
     * <p>
     * <b>This constructor is only used by {@link YamlFile the extending class}, where nodes are unknown at the time of
     * initialization. It is needed to call {@link #init(YamlFile, Node, MappingNode, ExtendedConstructor)} afterwards.</b>
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
        this.route = null;
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

    /**
     * Initializes this section as an empty one.
     *
     * @param root the root file
     */
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
     *                    Java instances of the nodes
     */
    protected void init(@NotNull YamlFile root, @Nullable Node keyNode, @NotNull MappingNode valueNode, @NotNull ExtendedConstructor constructor) {
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
            getStoredValue().put(key, value instanceof Map ?
                    new Section(root, this, getSubRoute(key), superNodeComments ? tuple.getKeyNode() : valueNode, (MappingNode) tuple.getValueNode(), constructor) :
                    new Entry(superNodeComments ? tuple.getKeyNode() : valueNode, tuple.getValueNode(), value));
            //Set to true
            superNodeComments = true;
        }
    }

    /**
     * Returns <code>true</code> if this section is empty, <code>false</code> otherwise. The parameter indicates if to
     * search subsections too, which gives the <b>true</b> indication if the section is empty.
     * <p>
     * If <code>deep</code> is <code>false</code>, returns only the result of {@link Map#isEmpty()} called on the
     * content map represented by this section. However, the section (underlying map) might also contain sections, which
     * are empty, resulting in incorrect returned value by this method <code>false</code>.
     * <p>
     * If <code>deep == true</code>, contents of this section are iterated. If any of the values is a mapping entry (not
     * a subsection), returns <code>false</code>. If it is a section, runs this method recursively (with
     * <code>deep</code> set to <code>true</code>) and returns <code>false</code> if the result of that call is
     * <code>false</code>. If the iteration finished and none of the sub-calls returned otherwise, returns
     * <code>true</code>.
     *
     * @param deep if to search deeply
     * @return whether this section is empty
     */
    public boolean isEmpty(boolean deep) {
        //If no values are present
        if (getStoredValue().isEmpty())
            return true;
        //If not deep
        if (!deep)
            return false;

        //Loop through all values
        for (Block<?> value : getStoredValue().values()) {
            //If an entry or non-empty section
            if (value instanceof Entry || (value instanceof Section && !((Section) value).isEmpty(true)))
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
    @NotNull
    public YamlFile getRoot() {
        return root;
    }

    /**
     * Returns the parent section, or <code>null</code> if this section has no parent - the section is also the root
     * (check {@link #isRoot()}).
     *
     * @return the parent section, or <code>null</code> if none
     */
    @Nullable
    public Section getParent() {
        return parent;
    }

    /**
     * Returns the name of this section. Name is considered to be the key used to access <b>this</b> section from the
     * parent section.
     * <p>
     * If this section is the root (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * <b>Incompatible with Spigot/BungeeCord API</b>, where those, if this section represented the root file, would
     * return an empty string.
     *
     * @return the name of this section
     */
    @Nullable
    public Object getName() {
        return name;
    }

    /**
     * Returns the name of this section as a string. Name is considered to be the key used to access <b>this</b> section
     * from the parent section.
     * <p>
     * This method will throw an {@link UnsupportedOperationException} if using {@link KeyMode#OBJECT}, as the key might
     * not be correctly interpreted. Use {@link #getName()} instead.
     * <p>
     * If this section is the root (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * <b>Incompatible with Spigot/BungeeCord API</b>, where those, if this section represented the root file, would
     * return an empty string.
     *
     * @return the name of this section
     */
    @Nullable
    public String getNameAsString() {
        //If not string mode
        if (root.getGeneralSettings().getKeyMode() != KeyMode.STRING)
            throw new UnsupportedOperationException("Cannot return name as string if the key mode is not set to STRING!");
        return name == null ? null : name.toString();
    }

    /**
     * Returns the name of this section as a single-key route. Name is considered to be the key used to access
     * <b>this</b> section from the parent section.
     * <p>
     * If this section is the root (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * <b>Incompatible with Spigot/BungeeCord API</b>, where those, if this section represented the root file, would
     * return an empty string.
     *
     * @return the name of this section
     */
    @NotNull
    public Route getNameAsRoute() {
        return Route.from(name);
    }

    /**
     * Returns the absolute route of this section (route to this section from the root {@link #getRoot()}). If this
     * section represents the root file (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * <b>Incompatible with Spigot/BungeeCord API</b>, where those, if this section represented the root file, would
     * return an empty string.
     *
     * @return the name of this section
     * @see #getRoot()
     */
    @Nullable
    public Route getRoute() {
        return route;
    }

    /**
     * Returns the absolute route of this section (route to this section from the root {@link #getRoot()}) as a string.
     * If this section represents the root file (check {@link #isRoot()}), returns <code>null</code>.
     * <p>
     * <b>Incompatible with Spigot/BungeeCord API</b>, where those, if this section represented the root file, would
     * return an empty string.
     *
     * @return the name of this section
     * @see #getRoot()
     */
    @Nullable
    public String getRouteAsString() {
        return route == null ? null : route.join(root.getGeneralSettings().getSeparator());
    }

    /**
     * Returns sub-route for the specified key derived from this section's {@link #getRoute() route}.
     * <p>
     * More formally, calls {@link Route#addTo(Route, Object)}.
     *
     * @return sub-route derived from the {@link #getRoute() section's absolute route}
     * @see Route#addTo(Route, Object)
     */
    @NotNull
    public Route getSubRoute(@NotNull Object key) {
        return Route.addTo(route, key);
    }

    /**
     * Adapts this section (including sub-sections) to the new relatives. This method should be called if and only this
     * section was relocated to a new parent section.
     *
     * @param root   new root file
     * @param parent new parent section
     * @param route  new absolute route to this section (from the new root)
     * @see #adapt(YamlFile, Route)
     */
    private void adapt(@NotNull YamlFile root, @Nullable Section parent, @NotNull Route route) {
        //Delete from the previous parent
        if (parent != null)
            parent.remove(route);
        //Set
        this.parent = parent;
        this.name = route.get(route.length() - 1);
        //Adapt
        adapt(root, route);
    }

    /**
     * Recursively adapts this section (including sub-sections) to the new relatives. This method should only
     * sequentially after {@link #adapt(YamlFile, Section, Route)}.
     *
     * @param root  new root file
     * @param route new absolute route to this section (from the new root)
     */
    private void adapt(@NotNull YamlFile root, @NotNull Route route) {
        //Set
        this.root = root;
        this.route = route;
        //Loop through all entries
        for (Map.Entry<Object, Block<?>> entry : getStoredValue().entrySet())
            //If a section
            if (entry.getValue() instanceof Section)
                //Adapt
                ((Section) entry.getValue()).adapt(root, route.add(entry.getKey()));
    }

    /**
     * Adapts the given key, as defined by the key mode currently in use ({@link GeneralSettings#getKeyMode()}).
     * <p>
     * More formally, if key mode is {@link KeyMode#STRING STRING}, returns the result of {@link Object#toString()} on
     * the given key object, the key object given otherwise.
     *
     * @param key the key object to adapt
     * @return the adapted key
     */
    @NotNull
    public Object adaptKey(@NotNull Object key) {
        return root.getGeneralSettings().getKeyMode() == KeyMode.OBJECT ? key : key.toString();
    }

    //
    //
    //      -----------------------
    //
    //
    //           Route methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Returns set of routes in this section; while not keeping any reference to this (or sub-) sections. The returned
     * set is an instance of {@link GeneralSettings#getDefaultSet()}. The routes are <b>relative to this section</b>.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, (effectively) returns the result of {@link #getKeys()} with
     * the keys converted to {@link Route routes}, each with one element - the key itself.
     * <p>
     * Otherwise, also iterates through <b>all</b> sub-sections, while returning <b>truly</b> complete set of routes
     * within this section.
     * <p>
     * It is guaranteed that call to {@link #contains(Route)} with any route from the returned set will return
     * <code>true</code> (unless modified in between).
     *
     * @param deep if to get routes deeply (from sub-sections)
     * @return the complete set of routes
     */
    @NotNull
    public Set<Route> getRoutes(boolean deep) {
        //Create a set
        Set<Route> keys = root.getGeneralSettings().getDefaultSet();
        //Add
        addData((route, entry) -> keys.add(route), null, deep);
        //Return
        return keys;
    }

    /**
     * Returns set of routes (interpreted as strings) in this section; while not keeping any reference to this (or sub-)
     * sections. The returned set is an instance of {@link GeneralSettings#getDefaultSet()}. The routes are <b>relative
     * to this section</b>.
     * <p>
     * This method will throw an {@link UnsupportedOperationException} if using {@link KeyMode#OBJECT}. Use {@link
     * #getRoutes(boolean)} instead.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, (effectively) returns the result of {@link #getKeys()} with
     * the keys converted to string.
     * <p>
     * Otherwise, also iterates through <b>all</b> sub-sections, while returning <b>truly</b> complete set of routes
     * within this section.
     * <p>
     * The returned routes will have the keys separated by the root's separator ({@link
     * GeneralSettings#getSeparator()}).
     * <p>
     * It is guaranteed that call to {@link #contains(String)} with any route from the returned set will return
     * <code>true</code> (unless modified in between).
     *
     * @param deep if to get routes deeply
     * @return the complete set of string routes
     */
    @NotNull
    public Set<String> getRoutesAsStrings(boolean deep) {
        //If not string mode
        if (root.getGeneralSettings().getKeyMode() != KeyMode.STRING)
            throw new UnsupportedOperationException("Cannot build string routes if the key mode is not set to STRING!");

        //Create a set
        Set<String> keys = root.getGeneralSettings().getDefaultSet();
        //Add
        addData((route, entry) -> keys.add(route), new StringBuilder(), root.getGeneralSettings().getSeparator(), deep);
        //Return
        return keys;
    }

    /**
     * Returns set of direct keys (in this section only - not deep); while not keeping any reference to this section.
     * <p>
     * More formally, returns the key set of the underlying map. The returned set is an instance of {@link
     * GeneralSettings#getDefaultSet()}.
     *
     * @return the complete set of keys directly contained within this (only; not sub-) section
     * @see #getRoutes(boolean)
     * @see #getRoutesAsStrings(boolean)
     */
    @NotNull
    public Set<Object> getKeys() {
        //Create a set
        Set<Object> set = root.getGeneralSettings().getDefaultSet(getStoredValue().size());
        //Add all
        set.addAll(getStoredValue().keySet());
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
     * Returns a complete map of <i>route=value</i> pairs; while not keeping any reference to this (or sub-) sections.
     * The returned map is an instance of {@link GeneralSettings#getDefaultMap()}. The routes are <b>relative to this
     * section</b>.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to {@link Route routes} each with one element - the key itself;
     * with their corresponding values (might also be a {@link Section section}).
     * <p>
     * Otherwise, also iterates through <b>all</b> sub-sections, while returning <b>truly</b> complete map of
     * <i>route=value</i> pairs within this section.
     * <p>
     * It is guaranteed that call to {@link #get(Route)} with any route from the returned map will return the value
     * assigned to that route in the returned map (unless modified in between).
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of <i>route=value</i> pairs
     */
    @NotNull
    public Map<Route, Object> getRouteMappedValues(boolean deep) {
        //Create a map
        Map<Route, Object> values = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((route, entry) -> values.put(route, entry.getValue() instanceof Section ? entry.getValue() : entry.getValue().getStoredValue()), null, deep);
        //Return
        return values;
    }

    /**
     * Returns a complete map of <i>route=value</i> pairs; while not keeping any reference to this (or sub-) sections.
     * The returned map is an instance of {@link GeneralSettings#getDefaultMap()}. The routes are <b>relative to this
     * section</b>.
     * <p>
     * This method will throw an {@link UnsupportedOperationException} if using {@link KeyMode#OBJECT}. Use {@link
     * #getRouteMappedValues(boolean)} instead.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to string routes containing one key - the key itself; with their
     * corresponding values (might also be a {@link Section section}).
     * <p>
     * Otherwise, also iterates through <b>all</b> sub-sections, while returning <b>truly</b> complete map of
     * <i>route=value</i> pairs within this section.
     * <p>
     * The returned routes will have the keys separated by the root's separator ({@link
     * GeneralSettings#getSeparator()}).
     * <p>
     * It is guaranteed that call to {@link #get(String)} with any route from the returned map will return the value
     * assigned to that route in the returned map (unless modified in between).
     *
     * @param deep if to get values from sub-sections too
     * @return the complete map of <i>string route=value</i> pairs, including sections
     */
    @NotNull
    public Map<String, Object> getStringMappedValues(boolean deep) {
        //If not string mode
        if (root.getGeneralSettings().getKeyMode() != KeyMode.STRING)
            throw new UnsupportedOperationException("Cannot build string routes if the key mode is not set to STRING!");

        //Create a map
        Map<String, Object> values = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((route, entry) -> values.put(route, entry.getValue() instanceof Section ? entry.getValue() : entry.getValue().getStoredValue()), new StringBuilder(), root.getGeneralSettings().getSeparator(), deep);
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
     * Returns a complete map of <i>route=block</i> pairs; while not keeping any reference to this (or sub-) sections.
     * The returned map is an instance of {@link GeneralSettings#getDefaultMap()}. The routes are <b>relative to this
     * section</b>.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to {@link Route routes} each with one element - the key itself;
     * with their corresponding blocks.
     * <p>
     * Otherwise, also iterates through <b>all</b> sub-sections, while returning <b>truly</b> complete map of
     * <i>route=block</i> pairs within this section.
     * <p>
     * It is guaranteed that call to {@link #getBlock(Route)} with any route from the returned map will return the block
     * assigned to that route in the returned map (unless modified in between).
     *
     * @param deep if to get blocks from sub-sections too
     * @return the complete map of <i>route=block</i> pairs
     */
    @NotNull
    public Map<Route, Block<?>> getRouteMappedBlocks(boolean deep) {
        //Create map
        Map<Route, Block<?>> blocks = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((route, entry) -> blocks.put(route, entry.getValue()), null, deep);
        //Return
        return blocks;
    }

    /**
     * Returns a complete map of <i>route=block</i> pairs; while not keeping any reference to this (or sub-) sections.
     * The returned map is an instance of {@link GeneralSettings#getDefaultMap()}. The routes are <b>relative to this
     * section</b>.
     * <p>
     * This method will throw an {@link UnsupportedOperationException} if using {@link KeyMode#OBJECT}. Use {@link
     * #getRouteMappedBlocks(boolean)} instead.
     * <p>
     * If <code>deep</code> is set to <code>false</code>, returns (effectively) a copy of the underlying map with keys
     * (which are stored as object instances) converted to string routes containing one key - the key itself; with their
     * corresponding blocks.
     * <p>
     * Otherwise, also iterates through <b>all</b> sub-sections, while returning <b>truly</b> complete map of
     * <i>route=block</i> pairs within this section.
     * <p>
     * The returned routes will have the keys separated by the root's separator ({@link
     * GeneralSettings#getSeparator()}).
     * <p>
     * It is guaranteed that call to {@link #getBlock(String)} with any route from the returned map will return the
     * block assigned to that route in the returned map (unless modified in between).
     *
     * @param deep if to get blocks from sub-sections too
     * @return the complete map of <i>route=block</i> pairs
     */
    @NotNull
    public Map<String, Block<?>> getStringMappedBlocks(boolean deep) {
        //If not string mode
        if (root.getGeneralSettings().getKeyMode() != KeyMode.STRING)
            throw new UnsupportedOperationException("Cannot build string routes if the key mode is not set to STRING!");

        //Create map
        Map<String, Block<?>> blocks = root.getGeneralSettings().getDefaultMap();
        //Add
        addData((route, entry) -> blocks.put(route, entry.getValue()), new StringBuilder(), root.getGeneralSettings().getSeparator(), deep);
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
     * Iterates through all entries in the underlying map, while calling the given consumer for each entry. The route
     * given is always relative to the main caller section.
     * <p>
     * If any of the entries contain an instance of {@link Section} as their value and <code>deep</code> is set to
     * <code>true</code>, this method is called on each sub-section with the same consumer (while the route is managed
     * to be always relative to this section).
     *
     * @param consumer the consumer to call for each entry
     * @param current  the route to the currently iterated section, relative to the main caller section
     * @param deep     if to iterate deeply
     */
    private void addData(@NotNull BiConsumer<Route, Map.Entry<?, Block<?>>> consumer, @Nullable Route current, boolean deep) {
        //All keys
        for (Map.Entry<?, Block<?>> entry : getStoredValue().entrySet()) {
            //Route to this entry
            Route entryRoute = Route.addTo(current, entry.getKey());
            //Call
            consumer.accept(entryRoute, entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, entryRoute, true);
        }
    }

    /**
     * Iterates through all entries in the underlying map, while calling the given consumer for each entry. The string
     * route builder given must contain route relative to the main caller section.
     * <p>
     * If any of the entries contain an instance of {@link Section} as their value and <code>deep</code> is set to
     * <code>true</code>, this method is called on each sub-section with the same consumer (while the route is managed
     * to be always relative to this section).
     *
     * @param consumer     the consumer to call for each entry
     * @param routeBuilder the route to the currently iterated section, relative to the caller section
     * @param separator    the separator to use to separate individual keys in the string routes
     * @param deep         if to iterate deeply
     */
    private void addData(@NotNull BiConsumer<String, Map.Entry<?, Block<?>>> consumer, @NotNull StringBuilder routeBuilder, char separator, boolean deep) {
        //All keys
        for (Map.Entry<?, Block<?>> entry : getStoredValue().entrySet()) {
            //Current length
            int length = routeBuilder.length();
            //Add separator if there is a key already
            if (length > 0)
                routeBuilder.append(separator);
            //Call
            consumer.accept(routeBuilder.append(entry.getKey().toString()).toString(), entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, routeBuilder, separator, true);
            //Reset
            routeBuilder.setLength(length);
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
     * Returns whether this section contains anything at the given route.
     *
     * @param route the route to check
     * @return if this section contains anything at the given route
     * @see #getBlockSafe(Route)
     */
    public boolean contains(@NotNull Route route) {
        return getBlockSafe(route).isPresent();
    }

    /**
     * Returns whether this section contains anything at the given route.
     *
     * @param route the route to check
     * @return if this section contains anything at the given route
     * @see #getBlockSafe(String)
     */
    public boolean contains(@NotNull String route) {
        return getBlockSafe(route).isPresent();
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
     * Creates sections along the whole route, including the route itself and returns the section created at the end (at
     * the route given).
     * <p>
     * If there is a section already at the route, nothing is overwritten and the already existing section is returned.
     * <p>
     * <b>Please note</b> that anything in the way will be overwritten. However, comments of each already existing
     * block along the route will be kept and will be assigned to the new section created at its place.
     *
     * @param route the route to create a section at (with all parent sections)
     * @return the created section at the given route, or the already existing one
     */
    public Section createSection(@NotNull Route route) {
        //Current section
        Section current = this;
        //All keys
        for (int i = 0; i < route.length(); i++)
            //Create
            current = current.createSectionInternal(route.get(i), null);
        //Return
        return current;
    }

    /**
     * Creates sections along the whole route, including the route itself and returns the section created at the end (at
     * the route given).
     * <p>
     * If there is a section already at the route, nothing is overwritten and the already existing section is returned.
     * <p>
     * <b>Please note</b> that anything in the way will be overwritten. However, comments of each already existing
     * block along the route will be kept and will be assigned to the new section created at its place.
     *
     * @param route the route to create a section at (with all parent sections)
     * @return the created section at the given route, or the already existing one
     */
    public Section createSection(@NotNull String route) {
        //Index of the last separator + 1
        int lastSeparator = 0;
        //Section
        Section section = this;

        //While true (control statements are inside)
        while (true) {
            //Next separator
            int nextSeparator = route.indexOf(root.getGeneralSettings().getSeparator(), lastSeparator);
            //If found
            if (nextSeparator != -1)
                //Create section
                section = section.createSectionInternal(route.substring(lastSeparator, nextSeparator), null);
            else
                //Break
                break;
            //Set
            lastSeparator = nextSeparator + 1;
        }

        //Return
        return section.createSectionInternal(route.substring(lastSeparator), null);
    }

    /**
     * Creates a section at the given direct key in this section and returns it.
     * <p>
     * If there already is a mapping entry already existing at the key, it is overwritten. If there is a section
     * already, does not overwrite anything and the already existing section is returned.
     * <p>
     * Comments of already existing block will be kept and will be assigned to the new section created at its place.
     *
     * @param key      the key to create a section at
     * @param previous the previous block at this key
     * @return the newly created section or the already existing one
     */
    private Section createSectionInternal(@NotNull Object key, @Nullable Block<?> previous) {
        //Adapt
        Object adapted = adaptKey(key);

        return getSectionSafe(Route.from(adapted)).orElseGet(() -> {
            //The new section
            Section section = new Section(root, Section.this, getSubRoute(adapted), previous, root.getGeneralSettings().getDefaultMap());
            //Add
            getStoredValue().put(adapted, section);
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
     * Sets the given value at the given route in this section - overwrites the already existing value (if any). If
     * there are sections missing to the route where the object should be set, they are created along the way.
     * <p>
     * As the value to set, you can give instances of <b>anything</b>, with the following warnings:
     * <ul>
     *     <li><code>null</code>: valid value (please use {@link #remove(Route)} to remove entries),</li>
     *     <li>{@link Section}: the given section will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Entry}: the given mapping entry will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Route, Block, Map)} for more information),</li>
     * </ul>
     * <p>
     * If there's any entry at the given route, it's comments are kept and assigned to the new entry (does not apply
     * when the value is an instance of {@link Block}.
     *
     * @param route the route to set at
     * @param value the value to set
     */
    public void set(@NotNull Route route, @Nullable Object value) {
        //Starting index
        int i = -1;
        //Section
        Section section = this;

        //While not out of bounds
        while (++i < route.length()) {
            //If at the last index
            if (i + 1 >= route.length()) {
                //Call the direct method
                section.setInternal(adaptKey(route.get(i)), value);
                return;
            }

            //Key
            Object key = adaptKey(route.get(i));
            //The block at the key
            Block<?> block = section.getStoredValue().getOrDefault(key, null);
            //Set next section
            section = !(block instanceof Section) ? section.createSectionInternal(key, block) : (Section) block;
        }
    }

    /**
     * Sets the given value at the given route in this section - overwrites the already existing value (if any). If
     * there are sections missing to the route where the object should be set, they are created along the way.
     * <p>
     * As the value to set, you can give instances of <b>anything</b>, with the following warnings:
     * <ul>
     *     <li><code>null</code>: valid value (please use {@link #remove(Route)} to remove entries),</li>
     *     <li>{@link Section}: the given section will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Entry}: the given mapping entry will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Route, Block, Map)} for more information),</li>
     * </ul>
     * <p>
     * If there's any entry at the given route, it's comments are kept and assigned to the new entry (does not apply
     * when the value is an instance of {@link Block}.
     *
     * @param route the route to set at
     * @param value the value to set
     */
    public void set(@NotNull String route, @Nullable Object value) {
        //Index of the last separator + 1
        int lastSeparator = 0;
        //Section
        Section section = this;

        //While true (control statements are inside)
        while (true) {
            //Next separator
            int nextSeparator = route.indexOf(root.getGeneralSettings().getSeparator(), lastSeparator);
            //If found
            if (nextSeparator != -1) {
                //Create section
                section = section.createSection(route.substring(lastSeparator, nextSeparator));
            } else {
                //Set
                section.setInternal(route.substring(lastSeparator), value);
                return;
            }
            //Set
            lastSeparator = nextSeparator + 1;
        }
    }

    /**
     * Internally sets the given value at the given key in this section - overwrites the already existing value (if
     * any).
     * <p>
     * Please read more about the implementation at {@link #set(Route, Object)}.
     *
     * @param key   the (already adapted) key at which to set the value
     * @param value the value to set
     */
    public void setInternal(@NotNull Object key, @Nullable Object value) {
        //If a section
        if (value instanceof Section) {
            //Cast
            Section section = (Section) value;
            //Set
            getStoredValue().put(key, section);
            //Adapt
            section.adapt(root, this, getSubRoute(key));
            return;
        } else if (value instanceof Entry) {
            //Set
            getStoredValue().put(key, (Entry) value);
            return;
        }

        //If a map
        if (value instanceof Map) {
            //Add
            getStoredValue().put(key, new Section(root, this, getSubRoute(key), getStoredValue().getOrDefault(key, null), (Map<?, ?>) value));
            return;
        }

        //Block at the route
        Block<?> previous = getStoredValue().get(key);
        //If already existing block is not present
        if (previous == null) {
            //Add
            getStoredValue().put(key, new Entry(null, null, value));
            return;
        }

        //Add with existing block's comments
        getStoredValue().put(key, new Entry(previous, value));
    }

    //
    //
    //      -----------------------
    //
    //
    //          Removal methods
    //
    //
    //      -----------------------
    //
    //

    /**
     * Removes entry at the given route (if any); returns <code>true</code> if successfully removed. The method returns
     * <code>false</code> if and only no entry exists at the route.
     *
     * @param route the route to remove the entry at
     * @return if anything was removed
     */
    public boolean remove(@NotNull Route route) {
        return removeInternal(getParent(route).orElse(null), adaptKey(route.get(route.length() - 1)));
    }

    /**
     * Removes entry at the given route (if any); returns <code>true</code> if successfully removed. The method returns
     * <code>false</code> if and only no entry exists at the route.
     *
     * @param route the route to remove the entry at
     * @return if anything was removed
     */
    public boolean remove(@NotNull String route) {
        return removeInternal(getParent(route).orElse(null), route.substring(route.lastIndexOf(root.getGeneralSettings().getSeparator()) + 1));
    }

    /**
     * An internal method used to actually remove the entry. Created to extract common parts from both removal-oriented
     * methods.
     * <p>
     * Returns <code>false</code> if the parent section is <code>null</code>, or if nothing is present at the key in the
     * given section. Returns <code>true</code> otherwise.
     *
     * @param parent the parent section, or <code>null</code> if it does not exist
     * @param key    the last key in the route, key to check in the given section (already adapted using {@link
     *               #adaptKey(Object)})
     * @return if any entry has been removed
     */
    private boolean removeInternal(@Nullable Section parent, @Nullable Object key) {
        //If the parent is null
        if (parent == null)
            return false;
        //Remove
        return parent.getStoredValue().remove(key) != null;
    }

    /**
     * Clears all data in the section.
     */
    public void clear() {
        getStoredValue().clear();
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
     * Returns block at the given route encapsulated in an instance of {@link Optional}. If there is no block present
     * (no value) at the given route, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Section} or {@link Entry} instances. See the {wiki} for
     * more information.
     * <p>
     * <b>Functionality notes:</b> When individual elements (keys) of the given route are traversed, they are (without
     * modifying the route object given - it is immutable) adapted to the current key mode setting (see {@link
     * #adaptKey(Object)}).
     * <p>
     * <b>This is one of the fundamental methods, upon which the functionality of other methods in this class is
     * built.</b>
     *
     * @param route the route to get the block at
     * @return block at the given route encapsulated in an optional
     */
    public Optional<Block<?>> getBlockSafe(@NotNull Route route) {
        return getSafeInternal(route, false);
    }

    /**
     * Returns block at the given direct key encapsulated in an instance of {@link Optional}. If there is no block
     * present (no value) at the given key, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Section} or {@link Entry} instances. See the {wiki} for
     * more information.
     * <p>
     * <b>A direct key</b> means the key is referring to object in this section directly (e.g. does not work like
     * route, which might - if consisting of multiple keys - refer to subsections) - similar to {@link
     * Map#get(Object)}.
     * <p>
     * <b>Please note</b> that this class also supports <code>null</code> keys, or empty string keys (<code>""</code>)
     * as allowed by YAML 1.2 spec. This also means that compatibility with Spigot/BungeeCord API is not maintained
     * regarding empty string keys, where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>This is one of the fundamental methods, upon which the functionality of other methods in this class is
     * built.</b>
     *
     * @param key the key to get the block at
     * @return block at the given route encapsulated in an optional
     */
    private Optional<Block<?>> getDirectBlockSafe(@NotNull Object key) {
        return Optional.ofNullable(getStoredValue().get(adaptKey(key)));
    }

    /**
     * Returns block at the given string route encapsulated in an instance of {@link Optional}. If there is no block
     * present (no value) at the given route, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Section} or {@link Entry} instances. See the {wiki} for
     * more information.
     * <p>
     * <b>Functionality notes:</b> The given route must contain individual keys separated using the separator character
     * configured using {@link GeneralSettings.Builder#setSeparator(char)}.
     * <p>
     * If the given string route does not contain the separator character (is only one key), the route refers to content
     * in this section.
     * <p>
     * Otherwise, traverses appropriate subsections determined by the keys contained (in order as defined except the
     * last one) and returns the block at the last key defined in the given route. For example, for route separator
     * <code>'.'</code> and route <code>a.b.c</code>, this method firstly attempts to get the section at key
     * <code>"a"</code> in <b>this</b> section, <b>then</b> section <code>b</code> in <b>that</b> (keyed as
     * <code>"a"</code>) section and <b>finally</b> the block at <code>"c"</code> in <b>that</b> (keyed as
     * <code>"a.b"</code>) section.
     * <p>
     * We can also interpret this behaviour as a call to {@link #getBlockSafe(Route)} with route created via constructor
     * {@link Route#fromString(String, char)} (which effectively splits the given string route into separate string keys
     * according to the separator).
     * <p>
     * This method works independently of the root's {@link GeneralSettings#getKeyMode()}. However, as the given route
     * contains individual <b>string</b> keys, if set to {@link KeyMode#OBJECT}, you will only be able to access data at
     * routes containing only keys parsed as strings (no integer, boolean... or <code>null</code> keys) by SnakeYAML
     * Engine. If such functionality is needed, use {@link #getBlockSafe(Route)} instead, please.
     * <p>
     * <b>Please note</b> that compatibility with Spigot/BungeeCord API is not maintained regarding empty string keys,
     * where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>This is one of the fundamental methods, upon which the functionality of other methods in this class is
     * built.</b>
     *
     * @param route the string route to get the block at
     * @return block at the given route encapsulated in an optional
     */
    public Optional<Block<?>> getBlockSafe(@NotNull String route) {
        return route.indexOf(root.getGeneralSettings().getSeparator()) != -1 ? getSafeInternalString(route, false) : getDirectBlockSafe(route);
    }

    /**
     * Returns the block encapsulated in the result of {@link #getBlockSafe(Route)}.
     * <p>
     * If it's an empty {@link Optional}, returns <code>null</code>.
     *
     * @param route the string route to get the block at
     * @return block at the given route, or <code>null</code> if it doesn't exist
     * @see #getBlockSafe(Route)
     */
    public Block<?> getBlock(@NotNull Route route) {
        return getBlockSafe(route).orElse(null);
    }

    /**
     * Returns the block encapsulated in the result of {@link #getBlockSafe(String)}.
     * <p>
     * If it's an empty {@link Optional}, returns <code>null</code>.
     *
     * @param route the string route to get the block at
     * @return block at the given route, or <code>null</code> if it doesn't exist
     * @see #getBlockSafe(String)
     */
    public Block<?> getBlock(@NotNull String route) {
        return getBlockSafe(route).orElse(null);
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
     * Internal method which returns a block, encapsulated in an instance of {@link Optional}, at the given string route
     * in this section. If there is no block present, returns an empty optional.
     * <p>
     * This method does not interact with any others defined in this class.
     *
     * @param route  the route to get the block at
     * @param parent if searching for the parent section of the given route
     * @return block at the given route encapsulated in an optional
     */
    private Optional<Block<?>> getSafeInternalString(@NotNull String route, boolean parent) {
        //Index of the last separator + 1
        int lastSeparator = 0;
        //Section
        Section section = this;

        //While true (control statements are inside)
        while (true) {
            //Next separator
            int nextSeparator = route.indexOf(root.getGeneralSettings().getSeparator(), lastSeparator);
            //If not found
            if (nextSeparator == -1)
                break;

            //The block at the key
            Block<?> block = section.getStoredValue().getOrDefault(route.substring(lastSeparator, nextSeparator), null);
            //If not a section
            if (!(block instanceof Section))
                return Optional.empty();
            //Set next section
            section = (Section) block;
            //Set
            lastSeparator = nextSeparator + 1;
        }

        //Return
        return Optional.ofNullable(parent ? section : section.getStoredValue().get(route.substring(lastSeparator)));
    }

    /**
     * Internal method which returns a block, encapsulated in an instance of {@link Optional}, at the given string route
     * in this section. If there is no block present, returns an empty optional.
     * <p>
     * This method does not interact with any others defined in this class.
     *
     * @param route  the route to get the block at
     * @param parent if searching for the parent section of the given route
     * @return block at the given route encapsulated in an optional
     */
    private Optional<Block<?>> getSafeInternal(@NotNull Route route, boolean parent) {
        //Starting index
        int i = -1;
        //Section
        Section section = this;

        //While not at the parent section
        while (++i < route.length() - 1) {
            //The block at the key
            Block<?> block = section.getStoredValue().getOrDefault(adaptKey(route.get(i)), null);
            //If not a section
            if (!(block instanceof Section))
                return Optional.empty();
            //Set next section
            section = (Section) block;
        }

        //Return
        return Optional.ofNullable(parent ? section : section.getStoredValue().get(adaptKey(route.get(i))));
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
     * Returns section at the parent route of the given one, encapsulated in an instance of {@link Optional}. Said
     * differently, the returned section is the result of {@link #getSection(Route)} called for the same route as given
     * here, with the last route element removed.
     * <p>
     * That means, this method ignores if the given route represents an existing block. If block at that parent route is
     * not a section (is an entry), returns an empty optional.
     *
     * @param route the route to get the parent section from
     * @return section at the parent route from the given one encapsulated in an optional
     */
    public Optional<Section> getParent(@NotNull Route route) {
        return getSafeInternal(route, true).map(block -> block instanceof Section ? (Section) block : null);
    }

    /**
     * Returns section at the parent route of the given one, encapsulated in an instance of {@link Optional}. Said
     * differently, the returned section is effectively the result of {@link #getSection(Route)} called for the same
     * route as given here, with the last route element removed ({@link Route#parent()}).
     * <p>
     * That means, this method ignores if the given route represents an existing block. If block at that parent route is
     * not a section (is an entry), returns an empty optional.
     *
     * @param route the route to get the parent section from
     * @return section at the parent route from the given one encapsulated in an optional
     */
    public Optional<Section> getParent(@NotNull String route) {
        return getSafeInternalString(route, true).map(block -> block instanceof Section ? (Section) block : null);
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
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance; encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getBlockSafe(Route)}. If the returned optional is not empty and does
     * not contain an instance of {@link Section}, returns the encapsulated value returned by {@link
     * Block#getStoredValue()} - the actual value (list, integer...).
     *
     * @param route the route to get the value at
     * @return the value, or section at the given route
     */
    public Optional<Object> getSafe(@NotNull Route route) {
        return getBlockSafe(route).map(block -> block instanceof Section ? block : block.getStoredValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance; encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getBlockSafe(String)}. If the returned optional is not empty and
     * does not contain an instance of {@link Section}, returns the encapsulated value returned by {@link
     * Block#getStoredValue()} - the actual value (list, integer...).
     *
     * @param route the route to get the value at
     * @return the value, or section at the given route
     */
    public Optional<Object> getSafe(@NotNull String route) {
        return getBlockSafe(route).map(block -> block instanceof Section ? block : block.getStoredValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns default value
     * defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     *
     * @param route the route to get the value at
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull Route route) {
        return getSafe(route).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns default value
     * defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     *
     * @param route the route to get the value at
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull String route) {
        return getSafe(route).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns the provided
     * default.
     *
     * @param route the route to get the value at
     * @param def   the default value
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull Route route, @Nullable Object def) {
        return getSafe(route).orElse(def);
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns the provided
     * default.
     *
     * @param route the route to get the value at
     * @param def   the default value
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull String route, @Nullable Object def) {
        return getSafe(route).orElse(def);
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
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class; encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getSafe(Route)} cast to the given class if not empty (or an empty
     * optional if the returned is empty, or types are incompatible).
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(@NotNull Route route, @NotNull Class<T> clazz) {
        return getSafe(route).map((object) -> clazz.isInstance(object) ? (T) object :
                isNumber(object.getClass()) && isNumber(clazz) ? (T) convertNumber(object, clazz) :
                        NON_NUMERICAL_CONVERSIONS.containsKey(object.getClass()) && NON_NUMERICAL_CONVERSIONS.containsKey(clazz) ? (T) object : null);
    }


    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class; encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns an empty optional.
     * <p>
     * More formally, returns the result of {@link #getSafe(String)} cast to the given class if not empty (or an empty
     * optional if the returned is empty, or types are incompatible).
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsSafe(@NotNull String route, @NotNull Class<T> clazz) {
        return getSafe(route).map((object) -> clazz.isInstance(object) ? (T) object :
                isNumber(object.getClass()) && isNumber(clazz) ? (T) convertNumber(object, clazz) :
                        NON_NUMERICAL_CONVERSIONS.containsKey(object.getClass()) && NON_NUMERICAL_CONVERSIONS.containsKey(clazz) ? (T) object : null);
    }

    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns <code>null</code>.
     * <p>
     * More formally, returns the result of {@link #getAs(Route, Class, Object)} with <code>null</code> default.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type, or <code>null</code> according to the documentation above
     */
    public <T> T getAs(@NotNull Route route, @NotNull Class<T> clazz) {
        return getAs(route, clazz, null);
    }

    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns <code>null</code>.
     * <p>
     * More formally, returns the result of {@link #getAs(String, Class, Object)} with <code>null</code> default.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type, or <code>null</code> according to the documentation above
     */
    public <T> T getAs(@NotNull String route, @NotNull Class<T> clazz) {
        return getAs(route, clazz, null);
    }

    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns the provided default.
     * <p>
     * More formally, returns the result of {@link #getAsSafe(Route, Class)} or the provided default if the returned
     * optional is empty.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param def   the default value
     * @param <T>   the target type
     * @return the value cast to the given type, or default according to the documentation above
     */
    public <T> T getAs(@NotNull Route route, @NotNull Class<T> clazz, @Nullable T def) {
        return getAsSafe(route, clazz).orElse(def);
    }

    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns the provided default.
     * <p>
     * More formally, returns the result of {@link #getAsSafe(String, Class)} or the provided default if the returned
     * optional is empty.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param def   the default value
     * @param <T>   the target type
     * @return the value cast to the given type, or default according to the documentation above
     */
    public <T> T getAs(@NotNull String route, @NotNull Class<T> clazz, @Nullable T def) {
        return getAsSafe(route, clazz).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only value (list, integer... or {@link Section}) at the given route exists, and
     * it is an instance of the given class.
     * <p>
     * More formally, returns {@link Optional#isPresent()} called on the result of {@link #getAsSafe(Route, Class)}.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to check the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return if a value exists at the given route, and it is an instance of the given class
     */
    public <T> boolean is(@NotNull Route route, @NotNull Class<T> clazz) {
        return getAsSafe(route, clazz).isPresent();
    }

    /**
     * Returns <code>true</code> if and only value (list, integer... or {@link Section}) at the given route exists, and
     * it is an instance of the given class.
     * <p>
     * More formally, returns {@link Optional#isPresent()} called on the result of {@link #getAsSafe(String, Class)}.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     *
     * @param route the route to check the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return if a value exists at the given route, and it is an instance of the given class
     */
    public <T> boolean is(@NotNull String route, @NotNull Class<T> clazz) {
        return getAsSafe(route, clazz).isPresent();
    }

    // END OF BASE METHODS, DEPENDENT (DERIVED) METHODS FOLLOW
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
     * Returns section at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link Section}, returns an empty optional.
     *
     * @param route the route to get the section at
     * @return the section at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Section> getSectionSafe(@NotNull Route route) {
        return getAsSafe(route, Section.class);
    }

    /**
     * Returns section at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link Section}, returns an empty optional.
     *
     * @param route the route to get the section at
     * @return the section at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Section> getSectionSafe(@NotNull String route) {
        return getAsSafe(route, Section.class);
    }

    /**
     * Returns section at the given route. If nothing is present given route, or is not a {@link Section}, returns
     * <code>null</code>.
     *
     * @param route the route to get the section at
     * @return the section at the given route, or default according to the documentation above
     * @see #getSection(Route, Section)
     */
    public Section getSection(@NotNull Route route) {
        return getSection(route, null);
    }

    /**
     * Returns section at the given route. If nothing is present at the given route, or is not a {@link Section},
     * returns
     * <code>null</code>.
     *
     * @param route the route to get the section at
     * @return the section at the given route, or default according to the documentation above
     * @see #getSection(String, Section)
     */
    public Section getSection(@NotNull String route) {
        return getSection(route, null);
    }

    /**
     * Returns section at the given route. If nothing is present at the given route, or is not a {@link Section},
     * returns the provided default.
     *
     * @param route the route to get the section at
     * @param def   the default value
     * @return the section at the given route, or default according to the documentation above
     * @see #getSectionSafe(Route)
     */
    public Section getSection(@NotNull Route route, @Nullable Section def) {
        return getSectionSafe(route).orElse(def);
    }

    /**
     * Returns section at the given route. If nothing is present at the given route, or is not a {@link Section},
     * returns the provided default.
     *
     * @param route the route to get the section at
     * @param def   the default value
     * @return the section at the given route, or default according to the documentation above
     * @see #getSectionSafe(String)
     */
    public Section getSection(@NotNull String route, @Nullable Section def) {
        return getSectionSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Section}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a section
     * @see #getSectionSafe(Route)
     */
    public boolean isSection(@NotNull Route route) {
        return getSectionSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Section}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a section
     * @see #getSectionSafe(String)
     */
    public boolean isSection(@NotNull String route) {
        return getSectionSafe(route).isPresent();
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
     * Returns string at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or {@link
     * Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by converting
     * them to one using {@link Object#toString()}.
     *
     * @param route the route to get the string at
     * @return the string at the given route
     * @see #getSafe(Route)
     */
    public Optional<String> getStringSafe(@NotNull Route route) {
        return getSafe(route).map((object) -> object instanceof String || object instanceof Number || object instanceof Boolean ? object.toString() : null);
    }

    /**
     * Returns string at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or {@link
     * Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by converting
     * them to one using {@link Object#toString()}.
     *
     * @param route the route to get the string at
     * @return the string at the given route
     * @see #getSafe(String)
     */
    public Optional<String> getStringSafe(@NotNull String route) {
        return getSafe(route).map((object) -> object instanceof String || object instanceof Number || object instanceof Boolean ? object.toString() : null);
    }

    /**
     * Returns string at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultString()}.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or {@link
     * Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by converting
     * them to one using {@link Object#toString()}.
     *
     * @param route the route to get the string at
     * @return the string at the given route, or default according to the documentation above
     * @see #getString(Route, String)
     */
    public String getString(@NotNull Route route) {
        return getString(route, root.getGeneralSettings().getDefaultString());
    }

    /**
     * Returns string at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultString()}.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or {@link
     * Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by converting
     * them to one using {@link Object#toString()}.
     *
     * @param route the route to get the string at
     * @return the string at the given route, or default according to the documentation above
     * @see #getString(String, String)
     */
    public String getString(@NotNull String route) {
        return getString(route, root.getGeneralSettings().getDefaultString());
    }

    /**
     * Returns string at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or {@link
     * Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by converting
     * them to one using {@link Object#toString()}.
     *
     * @param route the route to get the string at
     * @param def   the default value
     * @return the string at the given route, or default according to the documentation above
     * @see #getStringSafe(Route)
     */
    public String getString(@NotNull Route route, @Nullable String def) {
        return getStringSafe(route).orElse(def);
    }

    /**
     * Returns string at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link String} instance is preferred. However, if there is an instance of {@link Number} or {@link
     * Boolean} (or their primitive variant) present instead, they are treated like if they were strings, by converting
     * them to one using {@link Object#toString()}.
     *
     * @param route the route to get the string at
     * @param def   the default value
     * @return the string at the given route, or default according to the documentation above
     * @see #getStringSafe(String)
     */
    public String getString(@NotNull String route, @Nullable String def) {
        return getStringSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link String}, or any other
     * compatible type. Please learn more at {@link #getStringSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a string, or any other compatible type according to the
     * documentation above
     * @see #getStringSafe(Route)
     */
    public boolean isString(@NotNull Route route) {
        return getStringSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link String}, or any other
     * compatible type. Please learn more at {@link #getStringSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a string, or any other compatible type according to the
     * documentation above
     * @see #getStringSafe(String)
     */
    public boolean isString(@NotNull String route) {
        return getStringSafe(route).isPresent();
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
     * Returns char at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param route the route to get the char at
     * @return the char at the given route
     * @see #getSafe(Route)
     */
    public Optional<Character> getCharSafe(@NotNull Route route) {
        return getSafe(route).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
    }

    /**
     * Returns char at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param route the route to get the char at
     * @return the char at the given route
     * @see #getSafe(String)
     */
    public Optional<Character> getCharSafe(@NotNull String route) {
        return getSafe(route).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
    }

    /**
     * Returns char at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultChar()}.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param route the route to get the char at
     * @return the char at the given route, or default according to the documentation above
     * @see #getChar(Route, Character)
     */
    public Character getChar(@NotNull Route route) {
        return getChar(route, root.getGeneralSettings().getDefaultChar());
    }

    /**
     * Returns char at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultChar()}.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param route the route to get the char at
     * @return the char at the given route, or default according to the documentation above
     * @see #getChar(String, Character)
     */
    public Character getChar(@NotNull String route) {
        return getChar(route, root.getGeneralSettings().getDefaultChar());
    }

    /**
     * Returns char at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param route the route to get the char at
     * @param def   the default value
     * @return the char at the given route, or default according to the documentation above
     * @see #getCharSafe(Route)
     */
    public Character getChar(@NotNull Route route, @Nullable Character def) {
        return getCharSafe(route).orElse(def);
    }

    /**
     * Returns char at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     *
     * @param route the route to get the char at
     * @param def   the default value
     * @return the char at the given route, or default according to the documentation above
     * @see #getCharSafe(String)
     */
    public Character getChar(@NotNull String route, @Nullable Character def) {
        return getCharSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Character}, or any
     * other compatible type. Please learn more at {@link #getCharSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a character, or any other compatible type according to the
     * documentation above
     * @see #getCharSafe(Route)
     */
    public boolean isChar(@NotNull Route route) {
        return getCharSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Character}, or any
     * other compatible type. Please learn more at {@link #getCharSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a character, or any other compatible type according to the
     * documentation above
     * @see #getCharSafe(String)
     */
    public boolean isChar(@NotNull String route) {
        return getCharSafe(route).isPresent();
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
     * Returns integer at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the integer at
     * @return the integer at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Integer> getIntSafe(@NotNull Route route) {
        return toInt(getAsSafe(route, Number.class));
    }

    /**
     * Returns integer at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the integer at
     * @return the integer at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Integer> getIntSafe(@NotNull String route) {
        return toInt(getAsSafe(route, Number.class));
    }

    /**
     * Returns integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Integer} as defined below).
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the integer at
     * @return the integer at the given route, or default according to the documentation above
     * @see #getInt(Route, Integer)
     */
    public Integer getInt(@NotNull Route route) {
        return getInt(route, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    /**
     * Returns integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Integer} as defined below).
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the integer at
     * @return the integer at the given route, or default according to the documentation above
     * @see #getInt(String, Integer)
     */
    public Integer getInt(@NotNull String route) {
        return getInt(route, root.getGeneralSettings().getDefaultNumber().intValue());
    }

    /**
     * Returns integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the integer at
     * @param def   the default value
     * @return the integer at the given route, or default according to the documentation above
     * @see #getIntSafe(Route)
     */
    public Integer getInt(@NotNull Route route, @Nullable Integer def) {
        return getIntSafe(route).orElse(def);
    }

    /**
     * Returns integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the integer at
     * @param def   the default value
     * @return the integer at the given route, or default according to the documentation above
     * @see #getIntSafe(Route)
     */
    public Integer getInt(@NotNull String route, @Nullable Integer def) {
        return getIntSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is an {@link Integer}, or any
     * other compatible type. Please learn more at {@link #getIntSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getIntSafe(Route)
     */
    public boolean isInt(@NotNull Route route) {
        return getIntSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is an {@link Integer}, or any
     * other compatible type. Please learn more at {@link #getIntSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getIntSafe(String)
     */
    public boolean isInt(@NotNull String route) {
        return getIntSafe(route).isPresent();
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
     * Returns big integer at the given route encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     *
     * @param route the route to get the big integer at
     * @return the big integer at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<BigInteger> getBigIntSafe(@NotNull Route route) {
        return getAsSafe(route, Number.class).map(number -> number instanceof BigInteger ? (BigInteger) number : BigInteger.valueOf(number.longValue()));
    }

    /**
     * Returns big integer at the given route encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     *
     * @param route the route to get the big integer at
     * @return the big integer at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<BigInteger> getBigIntSafe(@NotNull String route) {
        return getAsSafe(route, Number.class).map(number -> number instanceof BigInteger ? (BigInteger) number : BigInteger.valueOf(number.longValue()));
    }

    /**
     * Returns big integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger} as defined below).
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     *
     * @param route the route to get the big integer at
     * @return the big integer at the given route
     * @see #getBigInt(Route, BigInteger)
     */
    public BigInteger getBigInt(@NotNull Route route) {
        return getBigInt(route, BigInteger.valueOf(root.getGeneralSettings().getDefaultNumber().longValue()));
    }

    /**
     * Returns big integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger} as defined below).
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     *
     * @param route the route to get the big integer at
     * @return the big integer at the given route
     * @see #getBigInt(Route, BigInteger)
     */
    public BigInteger getBigInt(@NotNull String route) {
        return getBigInt(route, BigInteger.valueOf(root.getGeneralSettings().getDefaultNumber().longValue()));
    }

    /**
     * Returns big integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     *
     * @param route the route to get the big integer at
     * @param def   the default value
     * @return the big integer at the given route
     * @see #getBigIntSafe(Route)
     */
    public BigInteger getBigInt(@NotNull Route route, @Nullable BigInteger def) {
        return getBigIntSafe(route).orElse(def);
    }

    /**
     * Returns big integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     *
     * @param route the route to get the big integer at
     * @param def   the default value
     * @return the big integer at the given route
     * @see #getBigIntSafe(String)
     */
    public BigInteger getBigInt(@NotNull String route, @Nullable BigInteger def) {
        return getBigIntSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link BigInteger}, or any
     * other compatible type. Please learn more at {@link #getBigIntSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getBigIntSafe(Route)
     */
    public boolean isBigInt(@NotNull Route route) {
        return getBigIntSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link BigInteger}, or any
     * other compatible type. Please learn more at {@link #getBigIntSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getBigIntSafe(Route)
     */
    public boolean isBigInt(@NotNull String route) {
        return getBigIntSafe(route).isPresent();
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
     * Returns boolean at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link Boolean} (or the primitive variant), returns an empty optional.
     *
     * @param route the route to get the boolean at
     * @return the boolean at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Boolean> getBooleanSafe(@NotNull Route route) {
        return getAsSafe(route, Boolean.class);
    }

    /**
     * Returns boolean at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link Boolean} (or the primitive variant), returns an empty optional.
     *
     * @param route the route to get the boolean at
     * @return the boolean at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Boolean> getBooleanSafe(@NotNull String route) {
        return getAsSafe(route, Boolean.class);
    }

    /**
     * Returns boolean at the given route. If nothing is present at the given route, or is not a {@link Boolean} (or the
     * primitive variant), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultBoolean()}.
     *
     * @param route the route to get the boolean at
     * @return the boolean at the given route, or default according to the documentation above
     * @see #getBoolean(Route, Boolean)
     */
    public Boolean getBoolean(@NotNull Route route) {
        return getBoolean(route, root.getGeneralSettings().getDefaultBoolean());
    }

    /**
     * Returns boolean at the given route. If nothing is present at the given route, or is not a {@link Boolean} (or the
     * primitive variant), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultBoolean()}.
     *
     * @param route the route to get the boolean at
     * @return the boolean at the given route, or default according to the documentation above
     * @see #getBoolean(String, Boolean)
     */
    public Boolean getBoolean(@NotNull String route) {
        return getBoolean(route, root.getGeneralSettings().getDefaultBoolean());
    }

    /**
     * Returns boolean at the given route. If nothing is present at the given route, or is not a {@link Boolean} (or the
     * primitive variant), returns the provided default.
     *
     * @param route the route to get the boolean at
     * @param def   the default value
     * @return the boolean at the given route, or default according to the documentation above
     * @see #getBooleanSafe(Route)
     */
    public Boolean getBoolean(@NotNull Route route, @Nullable Boolean def) {
        return getBooleanSafe(route).orElse(def);
    }

    /**
     * Returns boolean at the given route. If nothing is present at the given route, or is not a {@link Boolean} (or the
     * primitive variant), returns the provided default.
     *
     * @param route the route to get the boolean at
     * @param def   the default value
     * @return the boolean at the given route, or default according to the documentation above
     * @see #getBooleanSafe(String)
     */
    public Boolean getBoolean(@NotNull String route, @Nullable Boolean def) {
        return getBooleanSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Boolean} (or the
     * primitive variant).
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a boolean
     * @see #getBooleanSafe(Route)
     */
    public boolean isBoolean(@NotNull Route route) {
        return getBooleanSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Boolean} (or the
     * primitive variant).
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a boolean
     * @see #getBooleanSafe(String)
     */
    public boolean isBoolean(@NotNull String route) {
        return getBooleanSafe(route).isPresent();
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
     * Returns double at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the double at
     * @return the double at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Double> getDoubleSafe(@NotNull Route route) {
        return toDouble(getAsSafe(route, Number.class));
    }

    /**
     * Returns double at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the double at
     * @return the double at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Double> getDoubleSafe(@NotNull String route) {
        return toDouble(getAsSafe(route, Number.class));
    }

    /**
     * Returns double at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Double} as defined below).
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the double at
     * @return the double at the given route, or default according to the documentation above
     * @see #getDouble(Route, Double)
     */
    public Double getDouble(@NotNull Route route) {
        return getDouble(route, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    /**
     * Returns double at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Double} as defined below).
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the double at
     * @return the double at the given route, or default according to the documentation above
     * @see #getDouble(String, Double)
     */
    public Double getDouble(@NotNull String route) {
        return getDouble(route, root.getGeneralSettings().getDefaultNumber().doubleValue());
    }

    /**
     * Returns double at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the double at
     * @param def   the default value
     * @return the double at the given route, or default according to the documentation above
     * @see #getDoubleSafe(Route)
     */
    public Double getDouble(@NotNull Route route, @Nullable Double def) {
        return getDoubleSafe(route).orElse(def);
    }

    /**
     * Returns double at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the double at
     * @param def   the default value
     * @return the double at the given route, or default according to the documentation above
     * @see #getDoubleSafe(String)
     */
    public Double getDouble(@NotNull String route, @Nullable Double def) {
        return getDoubleSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Double}, or any other
     * compatible type. Please learn more at {@link #getDoubleSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a double, or any other compatible type according to the
     * documentation above
     * @see #getDoubleSafe(Route)
     */
    public boolean isDouble(@NotNull Route route) {
        return getDoubleSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Double}, or any other
     * compatible type. Please learn more at {@link #getDoubleSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a double, or any other compatible type according to the
     * documentation above
     * @see #getDoubleSafe(String)
     */
    public boolean isDouble(@NotNull String route) {
        return getDoubleSafe(route).isPresent();
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
     * Returns float at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the float at
     * @return the float at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Float> getFloatSafe(@NotNull Route route) {
        return toFloat(getAsSafe(route, Number.class));
    }

    /**
     * Returns float at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the float at
     * @return the float at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Float> getFloatSafe(@NotNull String route) {
        return toFloat(getAsSafe(route, Number.class));
    }

    /**
     * Returns float at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Float} as defined below).
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the float at
     * @return the float at the given route, or default according to the documentation above
     * @see #getFloat(Route, Float)
     */
    public Float getFloat(@NotNull Route route) {
        return getFloat(route, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    /**
     * Returns float at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Float} as defined below).
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the float at
     * @return the float at the given route, or default according to the documentation above
     * @see #getFloat(Route, Float)
     */
    public Float getFloat(@NotNull String route) {
        return getFloat(route, root.getGeneralSettings().getDefaultNumber().floatValue());
    }

    /**
     * Returns float at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the float at
     * @param def   the default value
     * @return the float at the given route, or default according to the documentation above
     * @see #getFloatSafe(Route)
     */
    public Float getFloat(@NotNull Route route, @Nullable Float def) {
        return getFloatSafe(route).orElse(def);
    }

    /**
     * Returns float at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the float at
     * @param def   the default value
     * @return the float at the given route, or default according to the documentation above
     * @see #getFloatSafe(String)
     */
    public Float getFloat(@NotNull String route, @Nullable Float def) {
        return getFloatSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Float}, or any other
     * compatible type. Please learn more at {@link #getFloatSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a float, or any other compatible type according to the
     * documentation above
     * @see #getFloatSafe(Route)
     */
    public boolean isFloat(@NotNull Route route) {
        return getFloatSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Float}, or any other
     * compatible type. Please learn more at {@link #getFloatSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a float, or any other compatible type according to the
     * documentation above
     * @see #getFloatSafe(String)
     */
    public boolean isFloat(@NotNull String route) {
        return getFloatSafe(route).isPresent();
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
     * Returns byte at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the byte at
     * @return the byte at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Byte> getByteSafe(@NotNull Route route) {
        return toByte(getAsSafe(route, Number.class));
    }

    /**
     * Returns byte at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the byte at
     * @return the byte at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Byte> getByteSafe(@NotNull String route) {
        return toByte(getAsSafe(route, Number.class));
    }

    /**
     * Returns byte at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Byte} as defined below).
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the byte at
     * @return the byte at the given route, or default according to the documentation above
     * @see #getByte(Route, Byte)
     */
    public Byte getByte(@NotNull Route route) {
        return getByte(route, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    /**
     * Returns byte at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Byte} as defined below).
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the byte at
     * @return the byte at the given route, or default according to the documentation above
     * @see #getByte(String, Byte)
     */
    public Byte getByte(@NotNull String route) {
        return getByte(route, root.getGeneralSettings().getDefaultNumber().byteValue());
    }

    /**
     * Returns byte at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the byte at
     * @return the byte at the given route, or default according to the documentation above
     * @see #getByteSafe(Route)
     */
    public Byte getByte(@NotNull Route route, @Nullable Byte def) {
        return getByteSafe(route).orElse(def);
    }

    /**
     * Returns byte at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the byte at
     * @return the byte at the given route, or default according to the documentation above
     * @see #getByteSafe(String)
     */
    public Byte getByte(@NotNull String route, @Nullable Byte def) {
        return getByteSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Byte}, or any other
     * compatible type. Please learn more at {@link #getByteSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a byte, or any other compatible type according to the
     * documentation above
     * @see #getByteSafe(Route)
     */
    public boolean isByte(@NotNull Route route) {
        return getByteSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Byte}, or any other
     * compatible type. Please learn more at {@link #getByteSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a byte, or any other compatible type according to the
     * documentation above
     * @see #getByteSafe(String)
     */
    public boolean isByte(@NotNull String route) {
        return getByteSafe(route).isPresent();
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
     * Returns long at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the long at
     * @return the long at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Long> getLongSafe(@NotNull Route route) {
        return toLong(getAsSafe(route, Number.class));
    }

    /**
     * Returns long at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the long at
     * @return the long at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Long> getLongSafe(String route) {
        return toLong(getAsSafe(route, Number.class));
    }

    /**
     * Returns long at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Long} as defined below).
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the long at
     * @return the long at the given route, or default according to the documentation above
     * @see #getLong(Route, Long)
     */
    public Long getLong(@NotNull Route route) {
        return getLong(route, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    /**
     * Returns long at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Long} as defined below).
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the long at
     * @return the long at the given route, or default according to the documentation above
     * @see #getLong(Route, Long)
     */
    public Long getLong(@NotNull String route) {
        return getLong(route, root.getGeneralSettings().getDefaultNumber().longValue());
    }

    /**
     * Returns long at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the long at
     * @return the long at the given route, or default according to the documentation above
     * @see #getLongSafe(Route)
     */
    public Long getLong(@NotNull Route route, @Nullable Long def) {
        return getLongSafe(route).orElse(def);
    }

    /**
     * Returns long at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the long at
     * @return the long at the given route, or default according to the documentation above
     * @see #getLongSafe(String)
     */
    public Long getLong(@NotNull String route, @Nullable Long def) {
        return getLongSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Long}, or any other
     * compatible type. Please learn more at {@link #getLongSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a long, or any other compatible type according to the
     * documentation above
     * @see #getLongSafe(Route)
     */
    public boolean isLong(@NotNull Route route) {
        return getLongSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Long}, or any other
     * compatible type. Please learn more at {@link #getLongSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a long, or any other compatible type according to the
     * documentation above
     * @see #getLongSafe(String)
     */
    public boolean isLong(@NotNull String route) {
        return getLongSafe(route).isPresent();
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
     * Returns short at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the short at
     * @return the short at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<Short> getShortSafe(@NotNull Route route) {
        return toShort(getAsSafe(route, Number.class));
    }

    /**
     * Returns short at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the short at
     * @return the short at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<Short> getShortSafe(@NotNull String route) {
        return toShort(getAsSafe(route, Number.class));
    }

    /**
     * Returns short at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Short} as defined below).
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the short at
     * @return the short at the given route, or default according to the documentation above
     * @see #getShort(Route, Short)
     */
    public Short getShort(@NotNull Route route) {
        return getShort(route, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    /**
     * Returns short at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Short} as defined below).
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the short at
     * @return the short at the given route, or default according to the documentation above
     * @see #getShort(String, Short)
     */
    public Short getShort(@NotNull String route) {
        return getShort(route, root.getGeneralSettings().getDefaultNumber().shortValue());
    }

    /**
     * Returns short at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the short at
     * @return the short at the given route, or default according to the documentation above
     * @see #getShortSafe(Route)
     */
    public Short getShort(@NotNull Route route, @Nullable Short def) {
        return getShortSafe(route).orElse(def);
    }

    /**
     * Returns short at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     *
     * @param route the route to get the short at
     * @return the short at the given route, or default according to the documentation above
     * @see #getShortSafe(Route)
     */
    public Short getShort(@NotNull String route, @Nullable Short def) {
        return getShortSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Short}, or any other
     * compatible type. Please learn more at {@link #getShortSafe(Route)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a short, or any other compatible type according to the
     * documentation above
     * @see #getShortSafe(Route)
     */
    public boolean isShort(@NotNull Route route) {
        return getShortSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Short}, or any other
     * compatible type. Please learn more at {@link #getShortSafe(String)}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a short, or any other compatible type according to the
     * documentation above
     * @see #getShortSafe(String)
     */
    public boolean isShort(@NotNull String route) {
        return getShortSafe(route).isPresent();
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
     * Returns list at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link List}, returns an empty optional.
     *
     * @param route the route to get the list at
     * @return the list at the given route
     * @see #getAsSafe(Route, Class)
     */
    public Optional<List<?>> getListSafe(@NotNull Route route) {
        return getAsSafe(route, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link List}, returns an empty optional.
     *
     * @param route the route to get the list at
     * @return the list at the given route
     * @see #getAsSafe(String, Class)
     */
    public Optional<List<?>> getListSafe(@NotNull String route) {
        return getAsSafe(route, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given route. If nothing is present at the given route, or is not a {@link List}, returns
     * default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     *
     * @param route the route to get the list at
     * @return the list at the given route, or default according to the documentation above
     * @see #getList(Route, List)
     */
    public List<?> getList(@NotNull Route route) {
        return getList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list at the given route. If nothing is present at the given route, or is not a {@link List}, returns
     * default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     *
     * @param route the route to get the list at
     * @return the list at the given route, or default according to the documentation above
     * @see #getList(String, List)
     */
    public List<?> getList(@NotNull String route) {
        return getList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list at the given route. If nothing is present at the given route, or is not a {@link List}, returns the
     * provided default.
     *
     * @param route the route to get the list at
     * @param def   the default value
     * @return the list at the given route, or default according to the documentation above
     * @see #getListSafe(Route)
     */
    public List<?> getList(@NotNull Route route, @Nullable List<?> def) {
        return getListSafe(route).orElse(def);
    }

    /**
     * Returns list at the given route. If nothing is present at the given route, or is not a {@link List}, returns the
     * provided default.
     *
     * @param route the route to get the list at
     * @param def   the default value
     * @return the list at the given route, or default according to the documentation above
     * @see #getListSafe(String)
     */
    public List<?> getList(@NotNull String route, @Nullable List<?> def) {
        return getListSafe(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link List}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a list
     * @see #getListSafe(Route)
     */
    public boolean isList(@NotNull Route route) {
        return getListSafe(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link List}.
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a list
     * @see #getListSafe(String)
     */
    public boolean isList(@NotNull String route) {
        return getListSafe(route).isPresent();
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
     * Returns list of strings at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getStringSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the string list at
     * @return the string list at the given route
     * @see #getListSafe(Route)
     * @see #getStringSafe(Route)
     */
    public Optional<List<String>> getStringListSafe(@NotNull Route route) {
        return toStringList(getListSafe(route));
    }

    /**
     * Returns list of strings at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getStringSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the string list at
     * @return the string list at the given route
     * @see #getListSafe(String)
     * @see #getStringSafe(String)
     */
    public Optional<List<String>> getStringListSafe(@NotNull String route) {
        return toStringList(getListSafe(route));
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getStringSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the string list at
     * @param def   the default value
     * @return the string list at the given route, or default according to the documentation above
     * @see #getStringListSafe(Route)
     * @see #getStringSafe(Route)
     */
    public List<String> getStringList(@NotNull Route route, @Nullable List<String> def) {
        return getStringListSafe(route).orElse(def);
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getStringSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the string list at
     * @param def   the default value
     * @return the string list at the given route, or default according to the documentation above
     * @see #getStringListSafe(String)
     * @see #getStringSafe(String)
     */
    public List<String> getStringList(@NotNull String route, @Nullable List<String> def) {
        return getStringListSafe(route).orElse(def);
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getStringSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the string list at
     * @return the string list at the given route, or default according to the documentation above
     * @see #getStringList(Route, List)
     * @see #getStringSafe(Route)
     */
    public List<String> getStringList(@NotNull Route route) {
        return getStringList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getStringSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the string list at
     * @return the string list at the given route, or default according to the documentation above
     * @see #getStringList(String, List)
     * @see #getStringSafe(String)
     */
    public List<String> getStringList(@NotNull String route) {
        return getStringList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of integers at the given route encapsulated in an instance of {@link Optional}. If nothing is
     * present at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getIntSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route
     * @see #getListSafe(Route)
     * @see #getIntSafe(Route)
     */
    public Optional<List<Integer>> getIntListSafe(@NotNull Route route) {
        return toIntList(getListSafe(route));
    }

    /**
     * Returns list of integers at the given route encapsulated in an instance of {@link Optional}. If nothing is
     * present at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getIntSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route
     * @see #getListSafe(String)
     * @see #getIntSafe(String)
     */
    public Optional<List<Integer>> getIntListSafe(@NotNull String route) {
        return toIntList(getListSafe(route));
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getIntSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the integer list at
     * @param def   the default value
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getIntListSafe(Route)
     * @see #getIntSafe(Route)
     */
    public List<Integer> getIntList(@NotNull Route route, @Nullable List<Integer> def) {
        return getIntListSafe(route).orElse(def);
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getIntSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the integer list at
     * @param def   the default value
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getIntListSafe(String)
     * @see #getIntSafe(String)
     */
    public List<Integer> getIntList(@NotNull String route, @Nullable List<Integer> def) {
        return getIntListSafe(route).orElse(def);
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getIntSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getIntList(Route, List)
     * @see #getIntSafe(Route)
     */
    public List<Integer> getIntList(@NotNull Route route) {
        return getIntList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getIntSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getIntList(String, List)
     * @see #getIntSafe(String)
     */
    public List<Integer> getIntList(@NotNull String route) {
        return getIntList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of big integers at the given route encapsulated in an instance of {@link Optional}. If nothing is
     * present at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getBigIntSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route
     * @see #getListSafe(Route)
     * @see #getBigIntSafe(Route)
     */
    public Optional<List<BigInteger>> getBigIntListSafe(@NotNull Route route) {
        return toBigIntList(getListSafe(route));
    }

    /**
     * Returns list of big integers at the given route encapsulated in an instance of {@link Optional}. If nothing is
     * present at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getBigIntSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route
     * @see #getListSafe(String)
     * @see #getBigIntSafe(String)
     */
    public Optional<List<BigInteger>> getBigIntListSafe(@NotNull String route) {
        return toBigIntList(getListSafe(route));
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getBigIntSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the big integer list at
     * @param def   the default value
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getBigIntListSafe(Route)
     * @see #getBigIntSafe(Route)
     */
    public List<BigInteger> getBigIntList(@NotNull Route route, @Nullable List<BigInteger> def) {
        return getBigIntListSafe(route).orElse(def);
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getBigIntSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the big integer list at
     * @param def   the default value
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getBigIntListSafe(String)
     * @see #getBigIntSafe(String)
     */
    public List<BigInteger> getBigIntList(@NotNull String route, @Nullable List<BigInteger> def) {
        return getBigIntListSafe(route).orElse(def);
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getBigIntSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getBigIntList(Route, List)
     * @see #getBigIntSafe(Route)
     */
    public List<BigInteger> getBigIntList(@NotNull Route route) {
        return getBigIntList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getBigIntSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getBigIntList(String, List)
     * @see #getBigIntSafe(String)
     */
    public List<BigInteger> getBigIntList(@NotNull String route) {
        return getBigIntList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of bytes at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getByteSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route
     * @see #getListSafe(Route)
     * @see #getByteSafe(Route)
     */
    public Optional<List<Byte>> getByteListSafe(@NotNull Route route) {
        return toByteList(getListSafe(route));
    }

    /**
     * Returns list of bytes at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getByteSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route
     * @see #getListSafe(String)
     * @see #getByteSafe(String)
     */
    public Optional<List<Byte>> getByteListSafe(@NotNull String route) {
        return toByteList(getListSafe(route));
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getByteSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the byte list at
     * @param def   the default value
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getByteListSafe(Route)
     * @see #getByteSafe(Route)
     */
    public List<Byte> getByteList(@NotNull Route route, @Nullable List<Byte> def) {
        return getByteListSafe(route).orElse(def);
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getByteSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the byte list at
     * @param def   the default value
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getByteListSafe(String)
     * @see #getByteSafe(String)
     */
    public List<Byte> getByteList(@NotNull String route, @Nullable List<Byte> def) {
        return getByteListSafe(route).orElse(def);
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getByteSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getByteList(Route, List)
     * @see #getByteSafe(Route)
     */
    public List<Byte> getByteList(@NotNull Route route) {
        return getByteList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getByteSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getByteList(String, List)
     * @see #getByteSafe(String)
     */
    public List<Byte> getByteList(@NotNull String route) {
        return getByteList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of longs at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getLongSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the long list at
     * @return the long list at the given route
     * @see #getListSafe(Route)
     * @see #getLongSafe(Route)
     */
    public Optional<List<Long>> getLongListSafe(@NotNull Route route) {
        return toLongList(getListSafe(route));
    }

    /**
     * Returns list of longs at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getLongSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the long list at
     * @return the long list at the given route
     * @see #getListSafe(String)
     * @see #getLongSafe(String)
     */
    public Optional<List<Long>> getLongListSafe(@NotNull String route) {
        return toLongList(getListSafe(route));
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getLongSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the long list at
     * @param def   the default value
     * @return the long list at the given route, or default according to the documentation above
     * @see #getLongListSafe(Route)
     * @see #getLongSafe(Route)
     */
    public List<Long> getLongList(@NotNull Route route, @Nullable List<Long> def) {
        return getLongListSafe(route).orElse(def);
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getLongSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the long list at
     * @param def   the default value
     * @return the long list at the given route, or default according to the documentation above
     * @see #getLongListSafe(String)
     * @see #getLongSafe(String)
     */
    public List<Long> getLongList(@NotNull String route, @Nullable List<Long> def) {
        return getLongListSafe(route).orElse(def);
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getLongSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the long list at
     * @return the long list at the given route, or default according to the documentation above
     * @see #getLongList(Route, List)
     * @see #getLongSafe(Route)
     */
    public List<Long> getLongList(@NotNull Route route) {
        return getLongList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getLongSafe(String)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the long list at
     * @return the long list at the given route, or default according to the documentation above
     * @see #getLongList(String, List)
     * @see #getLongSafe(String)
     */
    public List<Long> getLongList(@NotNull String route) {
        return getLongList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of doubles at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getDoubleSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the double list at
     * @return the double list at the given route
     * @see #getListSafe(Route)
     * @see #getDoubleSafe(Route)
     */
    public Optional<List<Double>> getDoubleListSafe(@NotNull Route route) {
        return toDoubleList(getListSafe(route));
    }

    /**
     * Returns list of doubles at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getDoubleSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the double list at
     * @return the double list at the given route
     * @see #getListSafe(String)
     * @see #getDoubleSafe(String)
     */
    public Optional<List<Double>> getDoubleListSafe(@NotNull String route) {
        return toDoubleList(getListSafe(route));
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getDoubleSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the double list at
     * @param def   the default value
     * @return the double list at the given route, or default according to the documentation above
     * @see #getDoubleListSafe(Route)
     * @see #getDoubleSafe(Route)
     */
    public List<Double> getDoubleList(@NotNull Route route, @Nullable List<Double> def) {
        return getDoubleListSafe(route).orElse(def);
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getDoubleSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the double list at
     * @param def   the default value
     * @return the double list at the given route, or default according to the documentation above
     * @see #getDoubleListSafe(String)
     * @see #getDoubleSafe(String)
     */
    public List<Double> getDoubleList(@NotNull String route, @Nullable List<Double> def) {
        return getDoubleListSafe(route).orElse(def);
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getDoubleSafe(Route)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the double list at
     * @return the double list at the given route, or default according to the documentation above
     * @see #getDoubleList(Route, List)
     * @see #getDoubleSafe(Route)
     */
    public List<Double> getDoubleList(@NotNull Route route) {
        return getDoubleList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getDoubleSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the double list at
     * @return the double list at the given route, or default according to the documentation above
     * @see #getDoubleList(String, List)
     * @see #getDoubleSafe(String)
     */
    public List<Double> getDoubleList(@NotNull String route) {
        return getDoubleList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of floats at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getFloatSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the float list at
     * @return the float list at the given route
     * @see #getListSafe(Route)
     * @see #getFloatSafe(Route)
     */
    public Optional<List<Float>> getFloatListSafe(@NotNull Route route) {
        return toFloatList(getListSafe(route));
    }

    /**
     * Returns list of floats at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getFloatSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the float list at
     * @return the float list at the given route
     * @see #getListSafe(String)
     * @see #getFloatSafe(String)
     */
    public Optional<List<Float>> getFloatListSafe(@NotNull String route) {
        return toFloatList(getListSafe(route));
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getFloatSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the float list at
     * @param def   the default value
     * @return the float list at the given route, or default according to the documentation above
     * @see #getFloatListSafe(Route)
     * @see #getFloatSafe(Route)
     */
    public List<Float> getFloatList(@NotNull Route route, @Nullable List<Float> def) {
        return getFloatListSafe(route).orElse(def);
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getFloatSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the float list at
     * @param def   the default value
     * @return the float list at the given route, or default according to the documentation above
     * @see #getFloatListSafe(String)
     * @see #getFloatSafe(String)
     */
    public List<Float> getFloatList(@NotNull String route, @Nullable List<Float> def) {
        return getFloatListSafe(route).orElse(def);
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getFloatSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the float list at
     * @return the float list at the given route, or default according to the documentation above
     * @see #getFloatList(Route, List)
     * @see #getFloatSafe(Route)
     */
    public List<Float> getFloatList(@NotNull Route route) {
        return getFloatList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getFloatSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the float list at
     * @return the float list at the given route, or default according to the documentation above
     * @see #getFloatList(String, List)
     * @see #getFloatSafe(String)
     */
    public List<Float> getFloatList(@NotNull String route) {
        return getFloatList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of shorts at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getShortSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the short list at
     * @return the short list at the given route
     * @see #getListSafe(Route)
     * @see #getShortSafe(Route)
     */
    public Optional<List<Short>> getShortListSafe(@NotNull Route route) {
        return toShortList(getListSafe(route));
    }

    /**
     * Returns list of shorts at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getShortSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the short list at
     * @return the short list at the given route
     * @see #getListSafe(String)
     * @see #getShortSafe(String)
     */
    public Optional<List<Short>> getShortListSafe(@NotNull String route) {
        return toShortList(getListSafe(route));
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getShortSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the short list at
     * @param def   the default value
     * @return the short list at the given route, or default according to the documentation above
     * @see #getShortListSafe(Route)
     * @see #getShortSafe(Route)
     */
    public List<Short> getShortList(@NotNull Route route, @Nullable List<Short> def) {
        return getShortListSafe(route).orElse(def);
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getShortSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the short list at
     * @param def   the default value
     * @return the short list at the given route, or default according to the documentation above
     * @see #getShortListSafe(String)
     * @see #getShortSafe(String)
     */
    public List<Short> getShortList(@NotNull String route, @Nullable List<Short> def) {
        return getShortListSafe(route).orElse(def);
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getShortSafe(Route)}, it is skipped and will not appear in
     * the returned list.
     *
     * @param route the route to get the short list at
     * @return the short list at the given route, or default according to the documentation above
     * @see #getShortList(Route, List)
     * @see #getShortSafe(Route)
     */
    public List<Short> getShortList(@NotNull Route route) {
        return getShortList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getShortSafe(String)}, it is skipped and will not appear
     * in the returned list.
     *
     * @param route the route to get the short list at
     * @return the short list at the given route, or default according to the documentation above
     * @see #getShortList(String, List)
     * @see #getShortSafe(String)
     */
    public List<Short> getShortList(@NotNull String route) {
        return getShortList(route, root.getGeneralSettings().getDefaultList());
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
     * Returns list of maps at the given route encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section. It is, however, if needed, still recommended to call {@link #set(Route, Object)} afterwards.
     *
     * @param route the route to get the map list at
     * @return the map list at the given route
     * @see #getListSafe(Route)
     */
    public Optional<List<Map<?, ?>>> getMapListSafe(@NotNull Route route) {
        return toMapList(getListSafe(route));
    }

    /**
     * Returns list of maps at the given route encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section. It is, however, if needed, still recommended to call {@link #set(String, Object)} afterwards.
     *
     * @param route the route to get the map list at
     * @return the map list at the given route
     * @see #getListSafe(String)
     */
    public Optional<List<Map<?, ?>>> getMapListSafe(@NotNull String route) {
        return toMapList(getListSafe(route));
    }

    /**
     * Returns list of maps at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call {@link
     * #set(Route, Object)} afterwards.
     *
     * @param route the route to get the map list at
     * @param def   the default value
     * @return the map list at the given route, or default according to the documentation above
     * @see #getMapListSafe(Route)
     */
    public List<Map<?, ?>> getMapList(@NotNull Route route, @Nullable List<Map<?, ?>> def) {
        return getMapListSafe(route).orElse(def);
    }

    /**
     * Returns list of maps at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call {@link
     * #set(String, Object)} afterwards.
     *
     * @param route the route to get the map list at
     * @param def   the default value
     * @return the map list at the given route, or default according to the documentation above
     * @see #getMapListSafe(String)
     */
    public List<Map<?, ?>> getMapList(@NotNull String route, @Nullable List<Map<?, ?>> def) {
        return getMapListSafe(route).orElse(def);
    }

    /**
     * Returns list of maps at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call {@link
     * #set(Route, Object)} afterwards.
     *
     * @param route the route to get the map list at
     * @return the map list at the given route, or default according to the documentation above
     * @see #getMapList(Route, List)
     */
    public List<Map<?, ?>> getMapList(@NotNull Route route) {
        return getMapList(route, root.getGeneralSettings().getDefaultList());
    }

    /**
     * Returns list of maps at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not an instance of {@link Map}, it is skipped and will not appear in the returned list.
     * <p>
     * <b>Please note</b> that this method does not clone the maps returned - mutating them affects the list stored in
     * the section (unless the default value is returned). It is, however, if needed, still recommended to call {@link
     * #set(String, Object)} afterwards.
     *
     * @param route the route to get the map list at
     * @return the map list at the given route, or default according to the documentation above
     * @see #getMapList(String, List)
     */
    public List<Map<?, ?>> getMapList(@NotNull String route) {
        return getMapList(route, root.getGeneralSettings().getDefaultList());
    }

}