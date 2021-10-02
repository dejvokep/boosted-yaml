package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.engine.AccessibleConstructor;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.snakeyaml.engine.v2.nodes.*;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import static com.davidcubesvk.yamlUpdater.core.utils.conversion.NumericConversions.*;
import static com.davidcubesvk.yamlUpdater.core.utils.conversion.ListConversions.*;

/**
 * An extension of the {@link } class used to represent a section.
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
     * While loading.
     */
    public Section(YamlFile root, Section parent, Object name, Path path, Node keyNode, MappingNode valueNode, AccessibleConstructor constructor) {
        //Call superclass
        super(keyNode, null, root.getGeneralSettings().getDefaultMap());
        //Set
        this.root = root;
        this.parent = parent;
        this.name = adaptKey(name);
        this.path = path;
        //Init
        init(root, constructor, keyNode, valueNode);
    }

    /**
     * When creating a new section manually -> mappings represent raw parsed YAML map!
     */
    public Section(YamlFile root, Section parent, Object name, Path path, Block<?> previous, Map<?, ?> mappings) {
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
     * Call #init afterwards!
     */
    protected Section(Map<Object, Block<?>> defaultMap) {
        //Call superclass
        super(defaultMap);
        //Set
        this.root = null;
        this.parent = null;
        this.name = null;
        this.path = new Path();
    }

    protected void init(YamlFile root, AccessibleConstructor constructor, Node keyNode, MappingNode valueNode) {
        //Call superclass
        super.init(keyNode, null);
        //Set
        this.root = root;
        boolean mainCommentsAssigned = false;
        //Loop through all mappings
        for (NodeTuple tuple : valueNode.getValue()) {
            //Key and value
            //System.out.println(constructor.getConstructed());
            Object key = adaptKey(constructor.getConstructed(tuple.getKeyNode())), value = constructor.getConstructed(tuple.getValueNode());
            System.out.println("ADDING KEY: " + key + ", VAL:" + value);
            //System.out.println("KEY: " + key + "VALUE: " + value);
            //System.out.println(tuple.getKeyNode().getBlockComments() + " " + tuple.getKeyNode().getInLineComments() + " " + tuple.getKeyNode().getEndComments());
            //System.out.println(tuple.getValueNode().getBlockComments() + " " + tuple.getValueNode().getInLineComments() + " " + tuple.getValueNode().getEndComments());
            //Add
            getValue().put(key, value instanceof Map ?
                    new Section(root, this, key, path.add(key), mainCommentsAssigned ? tuple.getKeyNode() : valueNode, (MappingNode) tuple.getValueNode(), constructor) :
                    new Mapping(mainCommentsAssigned ? tuple.getKeyNode() : valueNode, tuple.getValueNode(), value));
            if (!mainCommentsAssigned) {
                mainCommentsAssigned = true;
                /*valueNode.setBlockComments(new ArrayList<>());
                valueNode.setInLineComments(new ArrayList<>());
                valueNode.setEndComments(null);*/ // TODO: 2. 10. 2021 Is this needed or can be deleted? Check repeating nodes.
            }
        }
    }

    public boolean isEmpty(boolean deep) {
        //If no values are present
        if (getValue().size() == 0)
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

    public Object adaptKey(Object key) {
        return root.getGeneralSettings().getPathMode() == GeneralSettings.PathMode.OBJECT_BASED ? key : key.toString();
    }

    public Set<Path> getKeys(boolean deep) {
        //Create set
        Set<Path> keys = new HashSet<>();
        //Add
        addData((entry) -> keys.add(path.add(entry.getKey())), deep);
        //Return
        return keys;
    }

    public Set<Object> getKeys() {
        return new HashSet<>(getValue().keySet());
    }

    public Map<Path, Object> getValues(boolean deep) {
        //Create map
        Map<Path, Object> values = new HashMap<>();
        //Add
        addData((entry) -> values.put(path.add(entry.getKey()), entry.getValue().getValue()), deep);
        //Return
        return values;
    }

    public Map<Path, Block<?>> getBlocks(boolean deep) {
        //Create map
        Map<Path, Block<?>> blocks = new HashMap<>();
        //Add
        addData((entry) -> blocks.put(path.add(entry.getKey()), entry.getValue()), deep);
        //Return
        return blocks;
    }

    private void addData(Consumer<Map.Entry<?, Block<?>>> consumer, boolean deep) {
        //All keys
        for (Map.Entry<?, Block<?>> entry : getValue().entrySet()) {
            //Call
            consumer.accept(entry);
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addData(consumer, true);
        }
    }

    public boolean isRoot() {
        return false;
    }

    public boolean contains(Path path) {
        return getSafe(path).isPresent();
    }

    public boolean contains(Object key) {
        return getSafe(key).isPresent();
    }

    private void adapt(YamlFile root, Section parent, Object name, Path path) {
        this.root = root;
        this.parent = parent;
        this.name = name;
        this.path = path;
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