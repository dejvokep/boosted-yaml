/*
 * Copyright 2021 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.block.implementation;

import dev.dejvokep.boostedyaml.YamlFile;
import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.engine.ExtendedConstructor;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings.KeyMode;
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
import java.util.function.Supplier;

import static dev.dejvokep.boostedyaml.utils.conversion.ListConversions.*;
import static dev.dejvokep.boostedyaml.utils.conversion.NumericConversions.*;

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
    private Section defaults = null;
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
        resetDefaults();
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
        resetDefaults();
        //Loop through all mappings
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            //Key and value
            Object key = adaptKey(entry.getKey()), value = entry.getValue();
            //Add
            getStoredValue().put(key, value instanceof Map ? new Section(root, this, route.add(key), null, (Map<?, ?>) value) : new TerminalBlock(null, value));
        }
    }

    /**
     * Creates a section using the given (not necessarily empty) instance of default map.
     * <p>
     * Sets the root file, parent section, name and route to <code>null</code>.
     * <p>
     * <b>This constructor is only used by {@link YamlFile the extending class}, where nodes are unknown at the time of
     * initialization. It is needed to call {@link #init(YamlFile, Node, MappingNode, ExtendedConstructor)}
     * afterwards.</b>
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
        this.defaults = null;
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
     * <p>
     * This method must only be called if {@link #isRoot()} returns <code>true</code>.
     *
     * @param root the root file
     */
    protected void initEmpty(@NotNull YamlFile root) {
        //Validate
        if (!root.isRoot())
            throw new IllegalStateException("Cannot init non-root section!");
        //Call superclass
        super.init(null, null);
        //Set
        this.root = root;
        resetDefaults();
    }

    /**
     * Initializes this section, and it's contents using the given parameters, while also initializing the superclass by
     * calling {@link Block#init(Node, Node)}.
     * <p>
     * This method must only be called if {@link #isRoot()} returns <code>true</code>.
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
        resetDefaults();
        //If comments of the super node were assigned
        boolean superNodeComments = false;
        //Loop through all mappings
        for (NodeTuple tuple : valueNode.getValue()) {
            //Key and value
            Object key = adaptKey(constructor.getConstructed(tuple.getKeyNode())), value = constructor.getConstructed(tuple.getValueNode());
            //Add
            getStoredValue().put(key, value instanceof Map ?
                    new Section(root, this, getSubRoute(key), superNodeComments ? tuple.getKeyNode() : valueNode, (MappingNode) tuple.getValueNode(), constructor) :
                    new TerminalBlock(superNodeComments ? tuple.getKeyNode() : valueNode, tuple.getValueNode(), value));
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
     * If <code>deep == true</code>, iterates through contents of this section. If any of the values is not a section,
     * returns <code>false</code>. If it is a section, runs this method recursively (with
     * <code>deep</code> set to <code>true</code>) and returns <code>false</code> if the result of that call is
     * <code>false</code>. If the iteration finished and none of the sub-calls returned otherwise, returns
     * <code>true</code>.
     * <p>
     * <b>Does not interact with defaults.</b>
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
            //If a terminal or non-empty section
            if (value instanceof TerminalBlock || (value instanceof Section && !((Section) value).isEmpty(true)))
                return false;
        }

        //Empty
        return true;
    }

    @Override
    public boolean isSection() {
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
        if (this.parent != null && this.parent.getStoredValue().get(name) == this)
            this.parent.removeInternal(this.parent, name);

        //Set
        this.name = route.get(route.length() - 1);
        //Set
        this.parent = parent;
        //Adapt
        adapt(root, route);
    }

    /**
     * Recursively adapts this section (including sub-sections) to the new relatives. This method should initially be
     * called after {@link #adapt(YamlFile, Section, Route)}.
     *
     * @param root  new root file
     * @param route new absolute route to this section (from the new root)
     */
    private void adapt(@NotNull YamlFile root, @NotNull Route route) {
        //Set
        this.root = root;
        this.route = route;
        resetDefaults();
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

    /**
     * Resets the default equivalent of this section.
     */
    private void resetDefaults() {
        this.defaults = isRoot() ? root.getDefaults() : parent == null || parent.defaults == null ? null : parent.defaults.getSection(Route.fromSingleKey(name), null);
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to check
     * @return if this section contains anything at the given route
     * @see #getOptionalBlock(Route)
     */
    public boolean contains(@NotNull Route route) {
        return getOptionalBlock(route).isPresent();
    }

    /**
     * Returns whether this section contains anything at the given route.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to check
     * @return if this section contains anything at the given route
     * @see #getOptionalBlock(String)
     */
    public boolean contains(@NotNull String route) {
        return getOptionalBlock(route).isPresent();
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
     * If there already is an entry already existing at the key, it is overwritten. If there is a section already, does
     * not overwrite anything and the already existing section is returned.
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

        return getOptionalSection(Route.from(adapted)).orElseGet(() -> {
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
     *     <li>any other {@link Block}: the given block will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Route, Block, Map)} for more information),</li>
     * </ul>
     * <p>
     * If there's any entry at the given route, it's comments are kept and assigned to the new entry (does not apply
     * when the value is an instance of {@link Block}, in which case comments from the block are preserved.
     * <p>
     * <b>Attempt to set an instance of {@link Section} whose call to {@link #isRoot()} returns <code>true</code> is
     * considered illegal and will result in an {@link IllegalArgumentException}.</b>
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
     *     <li><b>non-root</b> {@link Section}: the given section will be <i>pasted</i> here (including comments),</li>
     *     <li>any other {@link Block}: the given block will be <i>pasted</i> here (including comments),</li>
     *     <li>{@link Map}: a section will be created and initialized by the contents of the given map and comments of
     *     the previous block at that key (if any); where the map must only contain raw content (e.g. no {@link Block}
     *     instances; please see {@link #Section(YamlFile, Section, Route, Block, Map)} for more information),</li>
     * </ul>
     * <p>
     * If there's any entry at the given route, it's comments are kept and assigned to the new entry (does not apply
     * when the value is an instance of {@link Block}, in which case comments from the block are preserved.
     * <p>
     * <b>Attempt to set an instance of {@link Section} whose call to {@link #isRoot()} returns <code>true</code> is
     * considered illegal and will result in an {@link IllegalArgumentException}.</b>
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
            //If is the root
            if (section.isRoot())
                throw new IllegalArgumentException("Cannot set root section as the value!");
            //Set
            getStoredValue().put(key, section);

            //Adapt
            section.adapt(root, this, getSubRoute(key));
            return;
        } else if (value instanceof TerminalBlock) {
            //Set
            getStoredValue().put(key, (TerminalBlock) value);
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
            getStoredValue().put(key, new TerminalBlock(null, null, value));
            return;
        }

        //Add with existing block's comments
        getStoredValue().put(key, new TerminalBlock(previous, value));
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
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to remove the entry at
     * @return if anything was removed
     */
    public boolean remove(@NotNull Route route) {
        return removeInternal(getParent(route).orElse(null), adaptKey(route.get(route.length() - 1)));
    }

    /**
     * Removes entry at the given route (if any); returns <code>true</code> if successfully removed. The method returns
     * <code>false</code> if and only no entry exists at the route (nothing was removed).
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to remove the entry at
     * @return if anything was removed
     */
    public boolean remove(@NotNull String route) {
        return removeInternal(getParent(route).orElse(null), route.substring(route.lastIndexOf(root.getGeneralSettings().getSeparator()) + 1));
    }

    /**
     * An internal method used to actually remove an entry. Created to extract common parts from both removal-oriented
     * methods.
     * <p>
     * Returns <code>false</code> if the parent section is <code>null</code>, or if nothing is present at the key in the
     * given section. Returns <code>true</code> otherwise.
     *
     * @param parent the parent section, or <code>null</code> if it does not exist
     * @param key    the last key in the route, key to check in the given section (already adapted using {@link
     *               #adaptKey(Object)})
     * @return if the entry has been removed
     */
    private boolean removeInternal(@Nullable Section parent, @Nullable Object key) {
        //If the parent is null
        if (parent == null)
            return false;
        //Remove
        return parent.getStoredValue().remove(key) != null;
    }

    /**
     * Clears content within this section.
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * Each value is encapsulated in a {@link Block}: {@link Section} or {@link TerminalBlock} instances. See the {wiki}
     * for more information.
     * <p>
     * <b>Functionality notes:</b> When individual elements (keys) of the given route are traversed, they are (without
     * modifying the route object given - it is immutable) adapted to the current key mode setting (see {@link
     * #adaptKey(Object)}).
     * <p>
     * <b>Does not interact with defaults.</b>
     * <p>
     * <b>This is one of the fundamental methods, upon which the functionality of other methods in this class is
     * built.</b>
     *
     * @param route the route to get the block at
     * @return block at the given route encapsulated in an optional
     */
    public Optional<Block<?>> getOptionalBlock(@NotNull Route route) {
        return getSafeInternal(route, false);
    }

    /**
     * Returns block at the given direct key encapsulated in an instance of {@link Optional}. If there is no block
     * present (no value) at the given key, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Section} or {@link TerminalBlock} instances. See the {wiki}
     * for more information.
     * <p>
     * <b>A direct key</b> means the key is referring to object in this section directly (e.g. does not work like
     * route, which might - if consisting of multiple keys - refer to subsections) - similar to {@link
     * Map#get(Object)}.
     * <p>
     * <b>Please note</b> that this class also supports <code>null</code> keys, or empty string keys (<code>""</code>)
     * as allowed by YAML 1.2 spec. This also means that compatibility with Spigot/BungeeCord API is not maintained
     * regarding empty string keys, where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>Does not interact with defaults.</b>
     * <p>
     * <b>This is one of the fundamental methods, upon which the functionality of other methods in this class is
     * built.</b>
     *
     * @param key the key to get the block at
     * @return block at the given route encapsulated in an optional
     */
    private Optional<Block<?>> getDirectOptionalBlock(@NotNull Object key) {
        return Optional.ofNullable(getStoredValue().get(adaptKey(key)));
    }

    /**
     * Returns block at the given string route encapsulated in an instance of {@link Optional}. If there is no block
     * present (no value) at the given route, returns an empty optional.
     * <p>
     * Each value is encapsulated in a {@link Block}: {@link Section} or {@link TerminalBlock} instances. See the {wiki}
     * for more information.
     * <p>
     * <b>Functionality notes:</b> The given route must contain individual keys separated using the separator character
     * configured using {@link GeneralSettings.Builder#setSeparator(char)}.
     * <p>
     * If the given string route does not contain the separator character (is only one key), the route refers to content
     * in this section.
     * <p>
     * Otherwise, traverses appropriate subsections determined by the keys contained, and returns the block at the last
     * key defined in the given route (just like paths to files...). For example, for route separator
     * <code>'.'</code> and route <code>a.b</code>, this method firstly attempts to get the section at key
     * <code>"a"</code> in <b>this</b> section, <b>then</b> block at <code>"b"</code> in <b>that</b> (keyed as
     * <code>"a"</code>) section.
     * <p>
     * We can also interpret this behaviour as a call to {@link #getOptionalBlock(Route)} with route created via
     * constructor {@link Route#fromString(String, char)} (which effectively splits the given string route into separate
     * string keys according to the separator).
     * <p>
     * This method works independently of the root's {@link GeneralSettings#getKeyMode()}. However, as the given route
     * contains individual <b>string</b> keys, if set to {@link KeyMode#OBJECT}, you will only be able to access data at
     * routes containing only keys parsed as strings (no integer, boolean... or <code>null</code> keys) by SnakeYAML
     * Engine. If such functionality is needed, use {@link #getOptionalBlock(Route)} instead.
     * <p>
     * <b>Please note</b> that compatibility with Spigot/BungeeCord API is not maintained regarding empty string keys,
     * where those APIs would return the instance of the current block - this section.
     * <p>
     * <b>Does not interact with defaults.</b>
     * <p>
     * <b>This is one of the fundamental methods, upon which the functionality of other methods in this class is
     * built.</b>
     *
     * @param route the string route to get the block at
     * @return block at the given route encapsulated in an optional
     */
    public Optional<Block<?>> getOptionalBlock(@NotNull String route) {
        return route.indexOf(root.getGeneralSettings().getSeparator()) != -1 ? getSafeInternalString(route, false) : getDirectOptionalBlock(route);
    }

    /**
     * Returns the block encapsulated in the result of {@link #getOptionalBlock(Route)}.
     * <p>
     * If it's an empty {@link Optional}, returns <code>null</code>.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the string route to get the block at
     * @return block at the given route, or <code>null</code> if it doesn't exist
     * @see #getOptionalBlock(Route)
     */
    public Block<?> getBlock(@NotNull Route route) {
        return getOptionalBlock(route).orElse(null);
    }

    /**
     * Returns the block encapsulated in the result of {@link #getOptionalBlock(String)}.
     * <p>
     * If it's an empty {@link Optional}, returns <code>null</code>.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the string route to get the block at
     * @return block at the given route, or <code>null</code> if it doesn't exist
     * @see #getOptionalBlock(String)
     */
    public Block<?> getBlock(@NotNull String route) {
        return getOptionalBlock(route).orElse(null);
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * not a section, returns an empty optional.
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * not a section, returns an empty optional.
     * <p>
     * <b>Does not interact with defaults.</b>
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
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to get the value at
     * @return the value, or section at the given route
     */
    public Optional<Object> getOptional(@NotNull Route route) {
        return getOptionalBlock(route).map(block -> block instanceof Section ? block : block.getStoredValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance; encapsulated in an instance of {@link Optional}.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns an empty optional.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to get the value at
     * @return the value, or section at the given route
     */
    public Optional<Object> getOptional(@NotNull String route) {
        return getOptionalBlock(route).map(block -> block instanceof Section ? block : block.getStoredValue());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns default value
     * defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to get the value at
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull Route route) {
        return getOptional(route).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns default value
     * defined by root's general settings {@link GeneralSettings#getDefaultObject()}.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to get the value at
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull String route) {
        return getOptional(route).orElse(root.getGeneralSettings().getDefaultObject());
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns the provided
     * default.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to get the value at
     * @param def   the default value
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull Route route, @Nullable Object def) {
        return getOptional(route).orElse(def);
    }

    /**
     * Returns the value of the block (the actual value - list, integer...) at the given route, or if it is a section,
     * the corresponding {@link Section} instance.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), returns the provided
     * default.
     * <p>
     * <b>Does not interact with defaults.</b>
     *
     * @param route the route to get the value at
     * @param def   the default value
     * @return the value at the given route, or default according to the documentation above
     */
    public Object get(@NotNull String route, @Nullable Object def) {
        return getOptional(route).orElse(def);
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
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsOptional(@NotNull Route route, @NotNull Class<T> clazz) {
        Optional<T> value = getOptional(route).map((object) -> clazz.isInstance(object) ? (T) object :
                isNumber(object.getClass()) && isNumber(clazz) ? (T) convertNumber(object, clazz) :
                        NON_NUMERICAL_CONVERSIONS.containsKey(object.getClass()) && NON_NUMERICAL_CONVERSIONS.containsKey(clazz) ? (T) object : null);
        return value.isPresent() ? value : Optional.ofNullable(defaults == null ? null : defaults.getAs(route, clazz));
    }


    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class; encapsulated in an instance of
     * {@link Optional}.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns an empty optional.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return the value cast to the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAsOptional(@NotNull String route, @NotNull Class<T> clazz) {
        Optional<T> value = getOptional(route).map((object) -> clazz.isInstance(object) ? (T) object :
                isNumber(object.getClass()) && isNumber(clazz) ? (T) convertNumber(object, clazz) :
                        NON_NUMERICAL_CONVERSIONS.containsKey(object.getClass()) && NON_NUMERICAL_CONVERSIONS.containsKey(clazz) ? (T) object : null);
        return value.isPresent() ? value : Optional.ofNullable(defaults == null ? null : defaults.getAs(route, clazz));
    }

    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns <code>null</code>.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns <code>null</code>.</b>
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
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns <code>null</code>.</b>
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
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param def   the default value
     * @param <T>   the target type
     * @return the value cast to the given type, or default according to the documentation above
     */
    public <T> T getAs(@NotNull Route route, @NotNull Class<T> clazz, @Nullable T def) {
        return getAsOptional(route, clazz).orElse(def);
    }

    /**
     * Returns the value of the block (the actual value) at the given route, or if it is a section, the corresponding
     * {@link Section} instance, in both cases cast to instance of the given class.
     * <p>
     * If there is no block present at the given route (therefore no value can be returned), or the value (block's
     * actual value or {@link Section} instance) is not castable to the given type, returns the provided default.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the value at
     * @param clazz class of the target type
     * @param def   the default value
     * @param <T>   the target type
     * @return the value cast to the given type, or default according to the documentation above
     */
    public <T> T getAs(@NotNull String route, @NotNull Class<T> clazz, @Nullable T def) {
        return getAsOptional(route, clazz).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only value (list, integer... or {@link Section}) at the given route exists, and
     * it is an instance of the given class.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks the defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return if a value exists at the given route, and it is an instance of the given class
     */
    public <T> boolean is(@NotNull Route route, @NotNull Class<T> clazz) {
        return getAsOptional(route, clazz).isPresent();
    }

    /**
     * Returns <code>true</code> if and only value (list, integer... or {@link Section}) at the given route exists, and
     * it is an instance of the given class.
     * <p>
     * <b>This method supports</b> casting between two numeric primitives, two non-primitive numeric representations
     * and one of each kind. Casting between any primitive type, and it's non-primitive representation is also
     * supported.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @param clazz class of the target type
     * @param <T>   the target type
     * @return if a value exists at the given route, and it is an instance of the given class
     */
    public <T> boolean is(@NotNull String route, @NotNull Class<T> clazz) {
        return getAsOptional(route, clazz).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the section at
     * @return the section at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Section> getOptionalSection(@NotNull Route route) {
        return getAsOptional(route, Section.class);
    }

    /**
     * Returns section at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link Section}, returns an empty optional.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the section at
     * @return the section at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Section> getOptionalSection(@NotNull String route) {
        return getAsOptional(route, Section.class);
    }

    /**
     * Returns section at the given route. If nothing is present at the given route, or is not a {@link Section},
     * returns
     * <code>null</code>.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the section at
     * @param def   the default value
     * @return the section at the given route, or default according to the documentation above
     * @see #getOptionalSection(Route)
     */
    public Section getSection(@NotNull Route route, @Nullable Section def) {
        return getOptionalSection(route).orElse(def);
    }

    /**
     * Returns section at the given route. If nothing is present at the given route, or is not a {@link Section},
     * returns the provided default.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the section at
     * @param def   the default value
     * @return the section at the given route, or default according to the documentation above
     * @see #getOptionalSection(String)
     */
    public Section getSection(@NotNull String route, @Nullable Section def) {
        return getOptionalSection(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Section}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a section
     * @see #getOptionalSection(Route)
     */
    public boolean isSection(@NotNull Route route) {
        return getOptionalSection(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Section}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a section
     * @see #getOptionalSection(String)
     */
    public boolean isSection(@NotNull String route) {
        return getOptionalSection(route).isPresent();
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
     * Natively, {@link String} instance is preferred. Anything else is converted to one using {@link
     * Object#toString()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the string at
     * @return the string at the given route
     * @see #getOptional(Route)
     */
    public Optional<String> getOptionalString(@NotNull Route route) {
        Optional<String> value = getOptional(route).map(Object::toString);
        return value.isPresent() ? value : defaults == null ? Optional.empty() : defaults.getOptionalString(route);
    }

    /**
     * Returns string at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link String} instance is preferred. Anything else is converted to one using {@link
     * Object#toString()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the string at
     * @return the string at the given route
     * @see #getOptional(String)
     */
    public Optional<String> getOptionalString(@NotNull String route) {
        Optional<String> value = getOptional(route).map(Object::toString);
        return value.isPresent() ? value : defaults == null ? Optional.empty() : defaults.getOptionalString(route);
    }

    /**
     * Returns string at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultString()}.
     * <p>
     * Natively, {@link String} instance is preferred. Anything else is converted to one using {@link
     * Object#toString()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * Natively, {@link String} instance is preferred. Anything else is converted to one using {@link
     * Object#toString()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * Natively, {@link String} instance is preferred. Anything else is converted to one using {@link
     * Object#toString()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the string at
     * @param def   the default value
     * @return the string at the given route, or default according to the documentation above
     * @see #getOptionalString(Route)
     */
    public String getString(@NotNull Route route, @Nullable String def) {
        return getOptionalString(route).orElse(def);
    }

    /**
     * Returns string at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link String} instance is preferred. Anything else is converted to one using {@link
     * Object#toString()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the string at
     * @param def   the default value
     * @return the string at the given route, or default according to the documentation above
     * @see #getOptionalString(String)
     */
    public String getString(@NotNull String route, @Nullable String def) {
        return getOptionalString(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link String}, or any other
     * compatible type. Please learn more at {@link #getOptionalString(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a string, or any other compatible type according to the
     * documentation above
     * @see #getOptionalString(Route)
     */
    public boolean isString(@NotNull Route route) {
        return getOptionalString(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link String}, or any other
     * compatible type. Please learn more at {@link #getOptionalString(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a string, or any other compatible type according to the
     * documentation above
     * @see #getOptionalString(String)
     */
    public boolean isString(@NotNull String route) {
        return getOptionalString(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the char at
     * @return the char at the given route
     * @see #getOptional(Route)
     */
    public Optional<Character> getOptionalChar(@NotNull Route route) {
        Optional<Character> value = getOptional(route).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
        return value.isPresent() ? value : defaults == null ? Optional.empty() : defaults.getOptionalChar(route);
    }

    /**
     * Returns char at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the char at
     * @return the char at the given route
     * @see #getOptional(String)
     */
    public Optional<Character> getOptionalChar(@NotNull String route) {
        Optional<Character> value = getOptional(route).map((object) -> object instanceof String ? object.toString().length() != 1 ? null : object.toString().charAt(0) : object instanceof Integer ? (char) ((int) object) : null);
        return value.isPresent() ? value : defaults == null ? Optional.empty() : defaults.getOptionalChar(route);
    }

    /**
     * Returns char at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultChar()}.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the char at
     * @param def   the default value
     * @return the char at the given route, or default according to the documentation above
     * @see #getOptionalChar(Route)
     */
    public Character getChar(@NotNull Route route, @Nullable Character def) {
        return getOptionalChar(route).orElse(def);
    }

    /**
     * Returns char at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Character} instance is preferred. However, if there is an instance of {@link String} and it is
     * exactly 1 character in length, returns that character. If is an {@link Integer} (or primitive variant), it is
     * converted to a character (by casting, see the <a href="https://en.wikipedia.org/wiki/ASCII">ASCII table</a>).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the char at
     * @param def   the default value
     * @return the char at the given route, or default according to the documentation above
     * @see #getOptionalChar(String)
     */
    public Character getChar(@NotNull String route, @Nullable Character def) {
        return getOptionalChar(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Character}, or any
     * other compatible type. Please learn more at {@link #getOptionalChar(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a character, or any other compatible type according to the
     * documentation above
     * @see #getOptionalChar(Route)
     */
    public boolean isChar(@NotNull Route route) {
        return getOptionalChar(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Character}, or any
     * other compatible type. Please learn more at {@link #getOptionalChar(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a character, or any other compatible type according to the
     * documentation above
     * @see #getOptionalChar(String)
     */
    public boolean isChar(@NotNull String route) {
        return getOptionalChar(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the integer at
     * @return the integer at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Integer> getOptionalInt(@NotNull Route route) {
        return toInt(getAs(route, Number.class));
    }

    /**
     * Returns integer at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the integer at
     * @return the integer at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Integer> getOptionalInt(@NotNull String route) {
        return toInt(getAs(route, Number.class));
    }

    /**
     * Returns integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Integer} as defined below).
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the integer at
     * @param def   the default value
     * @return the integer at the given route, or default according to the documentation above
     * @see #getOptionalInt(Route)
     */
    public Integer getInt(@NotNull Route route, @Nullable Integer def) {
        return getOptionalInt(route).orElse(def);
    }

    /**
     * Returns integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Integer} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#intValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the integer at
     * @param def   the default value
     * @return the integer at the given route, or default according to the documentation above
     * @see #getOptionalInt(Route)
     */
    public Integer getInt(@NotNull String route, @Nullable Integer def) {
        return getOptionalInt(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is an {@link Integer}, or any
     * other compatible type. Please learn more at {@link #getOptionalInt(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getOptionalInt(Route)
     */
    public boolean isInt(@NotNull Route route) {
        return getOptionalInt(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is an {@link Integer}, or any
     * other compatible type. Please learn more at {@link #getOptionalInt(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getOptionalInt(String)
     */
    public boolean isInt(@NotNull String route) {
        return getOptionalInt(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the big integer at
     * @return the big integer at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<BigInteger> getOptionalBigInt(@NotNull Route route) {
        return toBigInt(getAs(route, Number.class));
    }

    /**
     * Returns big integer at the given route encapsulated in an instance of {@link Optional}. If nothing is present at
     * the given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the big integer at
     * @return the big integer at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<BigInteger> getOptionalBigInt(@NotNull String route) {
        return toBigInt(getAs(route, Number.class));
    }

    /**
     * Returns big integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link BigInteger} as defined below).
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the big integer at
     * @param def   the default value
     * @return the big integer at the given route
     * @see #getOptionalBigInt(Route)
     */
    public BigInteger getBigInt(@NotNull Route route, @Nullable BigInteger def) {
        return getOptionalBigInt(route).orElse(def);
    }

    /**
     * Returns big integer at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link BigInteger} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is big integer created from the result of {@link Number#longValue()} using {@link
     * BigInteger#valueOf(long)} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the big integer at
     * @param def   the default value
     * @return the big integer at the given route
     * @see #getOptionalBigInt(String)
     */
    public BigInteger getBigInt(@NotNull String route, @Nullable BigInteger def) {
        return getOptionalBigInt(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link BigInteger}, or any
     * other compatible type. Please learn more at {@link #getOptionalBigInt(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getOptionalBigInt(Route)
     */
    public boolean isBigInt(@NotNull Route route) {
        return getOptionalBigInt(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link BigInteger}, or any
     * other compatible type. Please learn more at {@link #getOptionalBigInt(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is an integer, or any other compatible type according to the
     * documentation above
     * @see #getOptionalBigInt(Route)
     */
    public boolean isBigInt(@NotNull String route) {
        return getOptionalBigInt(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the boolean at
     * @return the boolean at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Boolean> getOptionalBoolean(@NotNull Route route) {
        return getAsOptional(route, Boolean.class);
    }

    /**
     * Returns boolean at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link Boolean} (or the primitive variant), returns an empty optional.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the boolean at
     * @return the boolean at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Boolean> getOptionalBoolean(@NotNull String route) {
        return getAsOptional(route, Boolean.class);
    }

    /**
     * Returns boolean at the given route. If nothing is present at the given route, or is not a {@link Boolean} (or the
     * primitive variant), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultBoolean()}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the boolean at
     * @param def   the default value
     * @return the boolean at the given route, or default according to the documentation above
     * @see #getOptionalBoolean(Route)
     */
    public Boolean getBoolean(@NotNull Route route, @Nullable Boolean def) {
        return getOptionalBoolean(route).orElse(def);
    }

    /**
     * Returns boolean at the given route. If nothing is present at the given route, or is not a {@link Boolean} (or the
     * primitive variant), returns the provided default.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the boolean at
     * @param def   the default value
     * @return the boolean at the given route, or default according to the documentation above
     * @see #getOptionalBoolean(String)
     */
    public Boolean getBoolean(@NotNull String route, @Nullable Boolean def) {
        return getOptionalBoolean(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Boolean} (or the
     * primitive variant).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a boolean
     * @see #getOptionalBoolean(Route)
     */
    public boolean isBoolean(@NotNull Route route) {
        return getOptionalBoolean(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Boolean} (or the
     * primitive variant).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a boolean
     * @see #getOptionalBoolean(String)
     */
    public boolean isBoolean(@NotNull String route) {
        return getOptionalBoolean(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the double at
     * @return the double at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Double> getOptionalDouble(@NotNull Route route) {
        return toDouble(getAs(route, Number.class));
    }

    /**
     * Returns double at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the double at
     * @return the double at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Double> getOptionalDouble(@NotNull String route) {
        return toDouble(getAs(route, Number.class));
    }

    /**
     * Returns double at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Double} as defined below).
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the double at
     * @param def   the default value
     * @return the double at the given route, or default according to the documentation above
     * @see #getOptionalDouble(Route)
     */
    public Double getDouble(@NotNull Route route, @Nullable Double def) {
        return getOptionalDouble(route).orElse(def);
    }

    /**
     * Returns double at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Double} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#doubleValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the double at
     * @param def   the default value
     * @return the double at the given route, or default according to the documentation above
     * @see #getOptionalDouble(String)
     */
    public Double getDouble(@NotNull String route, @Nullable Double def) {
        return getOptionalDouble(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Double}, or any other
     * compatible type. Please learn more at {@link #getOptionalDouble(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a double, or any other compatible type according to the
     * documentation above
     * @see #getOptionalDouble(Route)
     */
    public boolean isDouble(@NotNull Route route) {
        return getOptionalDouble(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Double}, or any other
     * compatible type. Please learn more at {@link #getOptionalDouble(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a double, or any other compatible type according to the
     * documentation above
     * @see #getOptionalDouble(String)
     */
    public boolean isDouble(@NotNull String route) {
        return getOptionalDouble(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the float at
     * @return the float at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Float> getOptionalFloat(@NotNull Route route) {
        return toFloat(getAs(route, Number.class));
    }

    /**
     * Returns float at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the float at
     * @return the float at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Float> getOptionalFloat(@NotNull String route) {
        return toFloat(getAs(route, Number.class));
    }

    /**
     * Returns float at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Float} as defined below).
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the float at
     * @param def   the default value
     * @return the float at the given route, or default according to the documentation above
     * @see #getOptionalFloat(Route)
     */
    public Float getFloat(@NotNull Route route, @Nullable Float def) {
        return getOptionalFloat(route).orElse(def);
    }

    /**
     * Returns float at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Float} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#floatValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the float at
     * @param def   the default value
     * @return the float at the given route, or default according to the documentation above
     * @see #getOptionalFloat(String)
     */
    public Float getFloat(@NotNull String route, @Nullable Float def) {
        return getOptionalFloat(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Float}, or any other
     * compatible type. Please learn more at {@link #getOptionalFloat(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a float, or any other compatible type according to the
     * documentation above
     * @see #getOptionalFloat(Route)
     */
    public boolean isFloat(@NotNull Route route) {
        return getOptionalFloat(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Float}, or any other
     * compatible type. Please learn more at {@link #getOptionalFloat(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a float, or any other compatible type according to the
     * documentation above
     * @see #getOptionalFloat(String)
     */
    public boolean isFloat(@NotNull String route) {
        return getOptionalFloat(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the byte at
     * @return the byte at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Byte> getOptionalByte(@NotNull Route route) {
        return toByte(getAs(route, Number.class));
    }

    /**
     * Returns byte at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the byte at
     * @return the byte at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Byte> getOptionalByte(@NotNull String route) {
        return toByte(getAs(route, Number.class));
    }

    /**
     * Returns byte at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Byte} as defined below).
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the byte at
     * @return the byte at the given route, or default according to the documentation above
     * @see #getOptionalByte(Route)
     */
    public Byte getByte(@NotNull Route route, @Nullable Byte def) {
        return getOptionalByte(route).orElse(def);
    }

    /**
     * Returns byte at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Byte} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#byteValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the byte at
     * @return the byte at the given route, or default according to the documentation above
     * @see #getOptionalByte(String)
     */
    public Byte getByte(@NotNull String route, @Nullable Byte def) {
        return getOptionalByte(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Byte}, or any other
     * compatible type. Please learn more at {@link #getOptionalByte(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a byte, or any other compatible type according to the
     * documentation above
     * @see #getOptionalByte(Route)
     */
    public boolean isByte(@NotNull Route route) {
        return getOptionalByte(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Byte}, or any other
     * compatible type. Please learn more at {@link #getOptionalByte(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a byte, or any other compatible type according to the
     * documentation above
     * @see #getOptionalByte(String)
     */
    public boolean isByte(@NotNull String route) {
        return getOptionalByte(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the long at
     * @return the long at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Long> getOptionalLong(@NotNull Route route) {
        return toLong(getAs(route, Number.class));
    }

    /**
     * Returns long at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the long at
     * @return the long at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Long> getOptionalLong(String route) {
        return toLong(getAs(route, Number.class));
    }

    /**
     * Returns long at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Long} as defined below).
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the long at
     * @return the long at the given route, or default according to the documentation above
     * @see #getOptionalLong(Route)
     */
    public Long getLong(@NotNull Route route, @Nullable Long def) {
        return getOptionalLong(route).orElse(def);
    }

    /**
     * Returns long at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Long} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#longValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the long at
     * @return the long at the given route, or default according to the documentation above
     * @see #getOptionalLong(String)
     */
    public Long getLong(@NotNull String route, @Nullable Long def) {
        return getOptionalLong(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Long}, or any other
     * compatible type. Please learn more at {@link #getOptionalLong(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a long, or any other compatible type according to the
     * documentation above
     * @see #getOptionalLong(Route)
     */
    public boolean isLong(@NotNull Route route) {
        return getOptionalLong(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Long}, or any other
     * compatible type. Please learn more at {@link #getOptionalLong(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a long, or any other compatible type according to the
     * documentation above
     * @see #getOptionalLong(String)
     */
    public boolean isLong(@NotNull String route) {
        return getOptionalLong(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the short at
     * @return the short at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<Short> getOptionalShort(@NotNull Route route) {
        return toShort(getAs(route, Number.class));
    }

    /**
     * Returns short at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not an instance of any compatible type (see below), returns an empty optional.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, returns value from the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the short at
     * @return the short at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<Short> getOptionalShort(@NotNull String route) {
        return toShort(getAs(route, Number.class));
    }

    /**
     * Returns short at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns default value defined by root's general settings {@link
     * GeneralSettings#getDefaultNumber()} (converted to {@link Short} as defined below).
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the short at
     * @return the short at the given route, or default according to the documentation above
     * @see #getOptionalShort(Route)
     */
    public Short getShort(@NotNull Route route, @Nullable Short def) {
        return getOptionalShort(route).orElse(def);
    }

    /**
     * Returns short at the given route. If nothing is present at the given route, or is not an instance of any
     * compatible type (see below), returns the provided default.
     * <p>
     * Natively, {@link Short} instance is preferred. However, if there is an instance of {@link Number}, the value
     * returned is the result of {@link Number#shortValue()} (which might involve rounding or truncating).
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if couldn't find a valid value there returns the given default.</b>
     *
     * @param route the route to get the short at
     * @return the short at the given route, or default according to the documentation above
     * @see #getOptionalShort(Route)
     */
    public Short getShort(@NotNull String route, @Nullable Short def) {
        return getOptionalShort(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Short}, or any other
     * compatible type. Please learn more at {@link #getOptionalShort(Route)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a short, or any other compatible type according to the
     * documentation above
     * @see #getOptionalShort(Route)
     */
    public boolean isShort(@NotNull Route route) {
        return getOptionalShort(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link Short}, or any other
     * compatible type. Please learn more at {@link #getOptionalShort(String)}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a short, or any other compatible type according to the
     * documentation above
     * @see #getOptionalShort(String)
     */
    public boolean isShort(@NotNull String route) {
        return getOptionalShort(route).isPresent();
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
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the list at
     * @return the list at the given route
     * @see #getAsOptional(Route, Class)
     */
    public Optional<List<?>> getOptionalList(@NotNull Route route) {
        return getAsOptional(route, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given route encapsulated in an instance of {@link Optional}. If nothing is present at the
     * given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the list at
     * @return the list at the given route
     * @see #getAsOptional(String, Class)
     */
    public Optional<List<?>> getOptionalList(@NotNull String route) {
        return getAsOptional(route, List.class).map(list -> (List<?>) list);
    }

    /**
     * Returns list at the given route. If nothing is present at the given route, or is not a {@link List}, returns
     * default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if there is no list at the route, returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the given default.</b>
     *
     * @param route the route to get the list at
     * @param def   the default value
     * @return the list at the given route, or default according to the documentation above
     * @see #getOptionalList(Route)
     */
    public List<?> getList(@NotNull Route route, @Nullable List<?> def) {
        return getOptionalList(route).orElse(def);
    }

    /**
     * Returns list at the given route. If nothing is present at the given route, or is not a {@link List}, returns the
     * provided default.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the given default.</b>
     *
     * @param route the route to get the list at
     * @param def   the default value
     * @return the list at the given route, or default according to the documentation above
     * @see #getOptionalList(String)
     */
    public List<?> getList(@NotNull String route, @Nullable List<?> def) {
        return getOptionalList(route).orElse(def);
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link List}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a list
     * @see #getOptionalList(Route)
     */
    public boolean isList(@NotNull Route route) {
        return getOptionalList(route).isPresent();
    }

    /**
     * Returns <code>true</code> if and only a value at the given route exists, and it is a {@link List}.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, checks defaults under the same conditions as
     * above.</b>
     *
     * @param route the route to check the value at
     * @return if the value at the given route exists and is a list
     * @see #getOptionalList(String)
     */
    public boolean isList(@NotNull String route) {
        return getOptionalList(route).isPresent();
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
     * source list is not compatible as documented at {@link #getOptionalString(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the string list at
     * @return the string list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalString(Route)
     */
    public Optional<List<String>> getOptionalStringList(@NotNull Route route) {
        return toStringList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of strings at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalString(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the string list at
     * @return the string list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalString(String)
     */
    public Optional<List<String>> getOptionalStringList(@NotNull String route) {
        return toStringList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalString(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the string list at
     * @param def   the default value
     * @return the string list at the given route, or default according to the documentation above
     * @see #getOptionalStringList(Route)
     * @see #getOptionalString(Route)
     */
    public List<String> getStringList(@NotNull Route route, @Nullable List<String> def) {
        return getOptionalStringList(route).orElse(def);
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalString(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the string list at
     * @param def   the default value
     * @return the string list at the given route, or default according to the documentation above
     * @see #getOptionalStringList(String)
     * @see #getOptionalString(String)
     */
    public List<String> getStringList(@NotNull String route, @Nullable List<String> def) {
        return getOptionalStringList(route).orElse(def);
    }

    /**
     * Returns list of strings at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalString(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the string list at
     * @return the string list at the given route, or default according to the documentation above
     * @see #getStringList(Route, List)
     * @see #getOptionalString(Route)
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
     * source list is not compatible as documented at {@link #getOptionalString(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the string list at
     * @return the string list at the given route, or default according to the documentation above
     * @see #getStringList(String, List)
     * @see #getOptionalString(String)
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
     * source list is not compatible as documented at {@link #getOptionalInt(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalInt(Route)
     */
    public Optional<List<Integer>> getOptionalIntList(@NotNull Route route) {
        return toIntList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of integers at the given route encapsulated in an instance of {@link Optional}. If nothing is
     * present at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalInt(String)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalInt(String)
     */
    public Optional<List<Integer>> getOptionalIntList(@NotNull String route) {
        return toIntList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalInt(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the integer list at
     * @param def   the default value
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getOptionalIntList(Route)
     * @see #getOptionalInt(Route)
     */
    public List<Integer> getIntList(@NotNull Route route, @Nullable List<Integer> def) {
        return getOptionalIntList(route).orElse(def);
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalInt(String)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the integer list at
     * @param def   the default value
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getOptionalIntList(String)
     * @see #getOptionalInt(String)
     */
    public List<Integer> getIntList(@NotNull String route, @Nullable List<Integer> def) {
        return getOptionalIntList(route).orElse(def);
    }

    /**
     * Returns list of integers at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalInt(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getIntList(Route, List)
     * @see #getOptionalInt(Route)
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
     * source list is not compatible as documented at {@link #getOptionalInt(String)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the integer list at
     * @return the integer list at the given route, or default according to the documentation above
     * @see #getIntList(String, List)
     * @see #getOptionalInt(String)
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
     * source list is not compatible as documented at {@link #getOptionalBigInt(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalBigInt(Route)
     */
    public Optional<List<BigInteger>> getOptionalBigIntList(@NotNull Route route) {
        return toBigIntList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of big integers at the given route encapsulated in an instance of {@link Optional}. If nothing is
     * present at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalBigInt(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalBigInt(String)
     */
    public Optional<List<BigInteger>> getOptionalBigIntList(@NotNull String route) {
        return toBigIntList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalBigInt(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the big integer list at
     * @param def   the default value
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getOptionalBigIntList(Route)
     * @see #getOptionalBigInt(Route)
     */
    public List<BigInteger> getBigIntList(@NotNull Route route, @Nullable List<BigInteger> def) {
        return getOptionalBigIntList(route).orElse(def);
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalBigInt(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the big integer list at
     * @param def   the default value
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getOptionalBigIntList(String)
     * @see #getOptionalBigInt(String)
     */
    public List<BigInteger> getBigIntList(@NotNull String route, @Nullable List<BigInteger> def) {
        return getOptionalBigIntList(route).orElse(def);
    }

    /**
     * Returns list of big integers at the given route. If nothing is present at the given route, or is not a {@link
     * List}, returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalBigInt(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getBigIntList(Route, List)
     * @see #getOptionalBigInt(Route)
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
     * source list is not compatible as documented at {@link #getOptionalBigInt(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the big integer list at
     * @return the big integer list at the given route, or default according to the documentation above
     * @see #getBigIntList(String, List)
     * @see #getOptionalBigInt(String)
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
     * source list is not compatible as documented at {@link #getOptionalByte(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalByte(Route)
     */
    public Optional<List<Byte>> getOptionalByteList(@NotNull Route route) {
        return toByteList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of bytes at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalByte(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalByte(String)
     */
    public Optional<List<Byte>> getOptionalByteList(@NotNull String route) {
        return toByteList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalByte(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the byte list at
     * @param def   the default value
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getOptionalByteList(Route)
     * @see #getOptionalByte(Route)
     */
    public List<Byte> getByteList(@NotNull Route route, @Nullable List<Byte> def) {
        return getOptionalByteList(route).orElse(def);
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalByte(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the byte list at
     * @param def   the default value
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getOptionalByteList(String)
     * @see #getOptionalByte(String)
     */
    public List<Byte> getByteList(@NotNull String route, @Nullable List<Byte> def) {
        return getOptionalByteList(route).orElse(def);
    }

    /**
     * Returns list of bytes at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalByte(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getByteList(Route, List)
     * @see #getOptionalByte(Route)
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
     * source list is not compatible as documented at {@link #getOptionalByte(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the byte list at
     * @return the byte list at the given route, or default according to the documentation above
     * @see #getByteList(String, List)
     * @see #getOptionalByte(String)
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
     * source list is not compatible as documented at {@link #getOptionalLong(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the long list at
     * @return the long list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalLong(Route)
     */
    public Optional<List<Long>> getOptionalLongList(@NotNull Route route) {
        return toLongList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of longs at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalLong(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the long list at
     * @return the long list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalLong(String)
     */
    public Optional<List<Long>> getOptionalLongList(@NotNull String route) {
        return toLongList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalLong(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the long list at
     * @param def   the default value
     * @return the long list at the given route, or default according to the documentation above
     * @see #getOptionalLongList(Route)
     * @see #getOptionalLong(Route)
     */
    public List<Long> getLongList(@NotNull Route route, @Nullable List<Long> def) {
        return getOptionalLongList(route).orElse(def);
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalLong(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the long list at
     * @param def   the default value
     * @return the long list at the given route, or default according to the documentation above
     * @see #getOptionalLongList(String)
     * @see #getOptionalLong(String)
     */
    public List<Long> getLongList(@NotNull String route, @Nullable List<Long> def) {
        return getOptionalLongList(route).orElse(def);
    }

    /**
     * Returns list of longs at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalLong(Route)}, it is skipped and will not appear
     * in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the long list at
     * @return the long list at the given route, or default according to the documentation above
     * @see #getLongList(Route, List)
     * @see #getOptionalLong(Route)
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
     * source list is not compatible as documented at {@link #getOptionalLong(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the long list at
     * @return the long list at the given route, or default according to the documentation above
     * @see #getLongList(String, List)
     * @see #getOptionalLong(String)
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
     * source list is not compatible as documented at {@link #getOptionalDouble(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the double list at
     * @return the double list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalDouble(Route)
     */
    public Optional<List<Double>> getOptionalDoubleList(@NotNull Route route) {
        return toDoubleList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of doubles at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalDouble(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the double list at
     * @return the double list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalDouble(String)
     */
    public Optional<List<Double>> getOptionalDoubleList(@NotNull String route) {
        return toDoubleList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalDouble(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the double list at
     * @param def   the default value
     * @return the double list at the given route, or default according to the documentation above
     * @see #getOptionalDoubleList(Route)
     * @see #getOptionalDouble(Route)
     */
    public List<Double> getDoubleList(@NotNull Route route, @Nullable List<Double> def) {
        return getOptionalDoubleList(route).orElse(def);
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalDouble(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the double list at
     * @param def   the default value
     * @return the double list at the given route, or default according to the documentation above
     * @see #getOptionalDoubleList(String)
     * @see #getOptionalDouble(String)
     */
    public List<Double> getDoubleList(@NotNull String route, @Nullable List<Double> def) {
        return getOptionalDoubleList(route).orElse(def);
    }

    /**
     * Returns list of doubles at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalDouble(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the double list at
     * @return the double list at the given route, or default according to the documentation above
     * @see #getDoubleList(Route, List)
     * @see #getOptionalDouble(Route)
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
     * source list is not compatible as documented at {@link #getOptionalDouble(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the double list at
     * @return the double list at the given route, or default according to the documentation above
     * @see #getDoubleList(String, List)
     * @see #getOptionalDouble(String)
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
     * source list is not compatible as documented at {@link #getOptionalFloat(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the float list at
     * @return the float list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalFloat(Route)
     */
    public Optional<List<Float>> getOptionalFloatList(@NotNull Route route) {
        return toFloatList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of floats at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalFloat(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the float list at
     * @return the float list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalFloat(String)
     */
    public Optional<List<Float>> getOptionalFloatList(@NotNull String route) {
        return toFloatList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalFloat(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the float list at
     * @param def   the default value
     * @return the float list at the given route, or default according to the documentation above
     * @see #getOptionalFloatList(Route)
     * @see #getOptionalFloat(Route)
     */
    public List<Float> getFloatList(@NotNull Route route, @Nullable List<Float> def) {
        return getOptionalFloatList(route).orElse(def);
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalFloat(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the float list at
     * @param def   the default value
     * @return the float list at the given route, or default according to the documentation above
     * @see #getOptionalFloatList(String)
     * @see #getOptionalFloat(String)
     */
    public List<Float> getFloatList(@NotNull String route, @Nullable List<Float> def) {
        return getOptionalFloatList(route).orElse(def);
    }

    /**
     * Returns list of floats at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalFloat(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the float list at
     * @return the float list at the given route, or default according to the documentation above
     * @see #getFloatList(Route, List)
     * @see #getOptionalFloat(Route)
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
     * source list is not compatible as documented at {@link #getOptionalFloat(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the float list at
     * @return the float list at the given route, or default according to the documentation above
     * @see #getFloatList(String, List)
     * @see #getOptionalFloat(String)
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
     * source list is not compatible as documented at {@link #getOptionalShort(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the short list at
     * @return the short list at the given route
     * @see #getOptionalList(Route)
     * @see #getOptionalShort(Route)
     */
    public Optional<List<Short>> getOptionalShortList(@NotNull Route route) {
        return toShortList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of shorts at the given route encapsulated in an instance of {@link Optional}. If nothing is present
     * at the given route, or is not a {@link List}, returns an empty optional.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalShort(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the short list at
     * @return the short list at the given route
     * @see #getOptionalList(String)
     * @see #getOptionalShort(String)
     */
    public Optional<List<Short>> getOptionalShortList(@NotNull String route) {
        return toShortList(getList(route, defaults == null ? null : defaults.getList(route)));
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalShort(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the short list at
     * @param def   the default value
     * @return the short list at the given route, or default according to the documentation above
     * @see #getOptionalShortList(Route)
     * @see #getOptionalShort(Route)
     */
    public List<Short> getShortList(@NotNull Route route, @Nullable List<Short> def) {
        return getOptionalShortList(route).orElse(def);
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns the provided default.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalShort(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the short list at
     * @param def   the default value
     * @return the short list at the given route, or default according to the documentation above
     * @see #getOptionalShortList(String)
     * @see #getOptionalShort(String)
     */
    public List<Short> getShortList(@NotNull String route, @Nullable List<Short> def) {
        return getOptionalShortList(route).orElse(def);
    }

    /**
     * Returns list of shorts at the given route. If nothing is present at the given route, or is not a {@link List},
     * returns default value defined by root's general settings {@link GeneralSettings#getDefaultList()}.
     * <p>
     * This method creates and returns newly created list of instance defined by root's general settings {@link
     * GeneralSettings#getDefaultList()}, with the elements re-added (to the target/returned list) from the (source)
     * list at the given route one by one, in order determined by the list iterator. If any of the elements of the
     * source list is not compatible as documented at {@link #getOptionalShort(Route)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the short list at
     * @return the short list at the given route, or default according to the documentation above
     * @see #getShortList(Route, List)
     * @see #getOptionalShort(Route)
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
     * source list is not compatible as documented at {@link #getOptionalShort(String)}, it is skipped and will not
     * appear in the returned list.
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the short list at
     * @return the short list at the given route, or default according to the documentation above
     * @see #getShortList(String, List)
     * @see #getOptionalShort(String)
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
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the map list at
     * @return the map list at the given route
     * @see #getOptionalList(Route)
     */
    public Optional<List<Map<?, ?>>> getOptionalMapList(@NotNull Route route) {
        return toMapList(getList(route, defaults == null ? null : defaults.getList(route)));
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
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above.</b>
     *
     * @param route the route to get the map list at
     * @return the map list at the given route
     * @see #getOptionalList(String)
     */
    public Optional<List<Map<?, ?>>> getOptionalMapList(@NotNull String route) {
        return toMapList(getList(route, defaults == null ? null : defaults.getList(route)));
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
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the map list at
     * @param def   the default value
     * @return the map list at the given route, or default according to the documentation above
     * @see #getOptionalMapList(Route)
     */
    public List<Map<?, ?>> getMapList(@NotNull Route route, @Nullable List<Map<?, ?>> def) {
        return getOptionalMapList(route).orElse(def);
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
     * <p>
     * <b>If there are any defaults and if there is no list at the route, searches the defaults under the same
     * conditions as above, and if couldn't find a list there returns the given default.</b>
     *
     * @param route the route to get the map list at
     * @param def   the default value
     * @return the map list at the given route, or default according to the documentation above
     * @see #getOptionalMapList(String)
     */
    public List<Map<?, ?>> getMapList(@NotNull String route, @Nullable List<Map<?, ?>> def) {
        return getOptionalMapList(route).orElse(def);
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
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
     * <p>
     * <b>If there are any defaults and if couldn't find a valid value, searches the defaults under the same conditions
     * as above, and if there is no list at the route, returns the settings default.</b>
     *
     * @param route the route to get the map list at
     * @return the map list at the given route, or default according to the documentation above
     * @see #getMapList(String, List)
     */
    public List<Map<?, ?>> getMapList(@NotNull String route) {
        return getMapList(route, root.getGeneralSettings().getDefaultList());
    }


    private <T> T orSupply(@Nullable T o1, @NotNull Supplier<T> o2) {
        return o1 == null ? o2.get() : o1;
    }

    private <T> T or(@Nullable T o1, @Nullable T o2) {
        return o1 == null ? o2 : o1;
    }

}