package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.files.Document;
import com.davidcubesvk.yamlUpdater.core.files.Path;
import org.snakeyaml.engine.v2.nodes.Node;

import java.math.BigInteger;
import java.util.*;

import static com.davidcubesvk.yamlUpdater.core.utils.NumericConversions.*;
import static com.davidcubesvk.yamlUpdater.core.utils.ListConversions.*;

/**
 * An extension of the {@link DocumentBlock} class used to represent a section.
 */
public class Section extends Block {

    //Mappings
    private final Map<?, ?> mappings;
    //Root document
    private Document root;
    //Parent section
    private Section parent;
    //Key to the section
    private Object name;
    //Full key
    private Path fullKey;

    /**
     * Initializes this section using the given block's data and the given sub-mappings.
     *
     * @param block    the block represented
     * @param mappings the sub-mappings of the section
     * @see #Section(String, Key, StringBuilder, Map, int) the detailed constructor
     */
    public Section(Node node, Map<?, ?> mappings) {
        super(node);
        this.mappings = mappings;
    }

    public Set<Path> getKeys(boolean deep) {
        //Create set
        Set<Path> keys = new HashSet<>();
        //Add
        addKeys(keys, new ArrayList<>(), deep);
        //Return
        return keys;
    }
    public Map<Path, Object> getValues(boolean deep) {
        //Create map
        Map<Path, Object> values = new HashMap<>();
        //Add
        addValues(values, new ArrayList<>(), deep);
        //Return
        return values;
    }

    private void addKeys(Set<Path> keys, List<Object> keyArray, boolean deep) {
        //All keys
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            //Add
            keyArray.add(entry.getKey());
            //Add
            keys.add(Path.from(keyArray.toArray()));
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addKeys(keys, keyArray, true);
            //Remove
            keyArray.remove(keyArray.size() - 1);
        }
    }
    private void addValues(Map<Path, Object> values, List<Object> keyArray, boolean deep) {
        //All keys
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            //Add
            keyArray.add(entry.getKey());
            //Add
            values.put(Path.from(keyArray.toArray()), entry.getValue());
            //If a section and deep is enabled
            if (deep && entry.getValue() instanceof Section)
                ((Section) entry.getValue()).addValues(values, keyArray, true);
            //Remove
            keyArray.remove(keyArray.size() - 1);
        }
    }

    public boolean contains(Path path) {
        return getSafe(path).isPresent();
    }
    public boolean contains(Object key) {
        return getSafe(key).isPresent();
    }

    public Document getRoot() {
        return root;
    }

    public Section getParent() {
        return parent;
    }

    public Object getName() {
        return name;
    }
    public Path getFullKey() {
        return fullKey;
    }

    private Optional<Object> getSafeInternal(Path path, int i) {
        //If at last index
        if (i + 1 >= path.getLength())
            return getSafe(path.getKey(i));

        //Section
        Optional<Object> section = getSafe(path.getKey(i));
        //If not present
        if (!section.isPresent())
            return section;
        //If not a section
        if (!(section.get() instanceof Section))
            return Optional.empty();
        //Return
        return ((Section) section.get()).getSafeInternal(path, i + 1);
    }

    public Optional<Object> getSafe(Path path) {
        return getSafeInternal(path, 0);
    }

    public Optional<Object> getSafe(Object key) {
        //If does not contain
        if (!mappings.containsKey(key))
            return Optional.empty();

        //Return
        return Optional.of(mappings.get(key));
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
        return getSafe(path).orElseThrow(() -> {throw new NoSuchElementException("Object at path " + path.toString() + " not found!");});
    }
    public Object get(Object key) {
        return getSafe(key).orElseThrow(() -> {throw new NoSuchElementException("Object at path " + key.toString() + " not found!");});
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Path path, Class<T> clazz) {
        //The value
        Optional<?> value = getSafe(path);
        //If empty
        if (!value.isPresent())
            throw new NoSuchElementException("Object at path " + path.toString() + " not found!");
        //If not an instance of the target type
        if (!clazz.isInstance(value.get()))
            throw new ClassCastException("Object at path " + path.toString() + " of type " + value.get().getClass() + " is not an instance of type " + clazz + "!");

        //Return
        return (T) value.get();
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Object key, Class<T> clazz) {
        //The value
        Optional<?> value = getSafe(key);
        //If empty
        if (!value.isPresent())
            throw new NoSuchElementException("Object at path " + key.toString() + " not found!");
        //If not an instance of the target type
        if (!clazz.isInstance(value.get()))
            throw new ClassCastException("Object at path " + key.toString() + " of type " + value.get().getClass() + " is not an instance of type " + clazz + "!");

        //Return
        return (T) value.get();
    }

    public Object getOrDefault(Path path, Object def) {
        return getSafe(path).orElse(def);
    }
    public Object getOrDefault(Object key, Object def) {
        return getSafe(key).orElse(def);
    }
    public <T> T getAsOrDefault(Path path, Class<T> clazz, T def) {
        return getAsSafe(path, clazz).orElse(def);
    }
    public <T> T getAsOrDefault(Object key, Class<T> clazz, T def) {
        return getAsSafe(key, clazz).orElse(def);
    }

    public Optional<Section> getSectionSafe(Path path) {
        return getAsSafe(path, Section.class);
    }
    public Optional<Section> getSectionSafe(Object key) {
        return getAsSafe(key, Section.class);
    }
    public Section getSection(Path path) {
        return getAs(path, Section.class);
    }
    public Section getSection(Object key) {
        return getAs(key, Section.class);
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
        return getAs(path, String.class);
    }
    public String getString(Object key) {
        return getAs(key, String.class);
    }
    public boolean isString(Path path) {
        return getStringSafe(path).isPresent();
    }
    public boolean isString(Object key) {
        return getStringSafe(key).isPresent();
    }

    public Optional<Character> getCharSafe(Path path) {
        //The value
        Optional<String> value = getStringSafe(path);
        //If empty or the string is longer
        if (!value.isPresent() || value.get().length() != 1)
            return Optional.empty();
        //Return
        return Optional.of(value.get().charAt(0));
    }
    public Optional<Character> getCharSafe(Object key) {
        //The value
        Optional<String> value = getStringSafe(key);
        //If empty or the string is longer
        if (!value.isPresent() || value.get().length() != 1)
            return Optional.empty();
        //Return
        return Optional.of(value.get().charAt(0));
    }
    public Character getChar(Path path) {
        //The value
        String value = getAs(path, String.class);
        //If longer
        if (value.length() != 1)
            throw new NoSuchElementException("String at path " + path.toString() + " is not exactly 1 character long!");
        //Return
        return value.charAt(0);
    }
    public Character getChar(Object key) {
        //The value
        String value = getAs(key, String.class);
        //If longer
        if (value.length() != 1)
            throw new NoSuchElementException("String at path " + key.toString() + " is not exactly 1 character long!");
        //Return
        return value.charAt(0);
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
    public Integer getInt(Path path) {
        return getAs(path, Number.class).intValue();
    }
    public Integer getInt(Object key) {
        return getAs(key, Number.class).intValue();
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
    public Boolean getBoolean(Path path) {
        return getAs(path, Boolean.class);
    }
    public Boolean getBoolean(Object key) {
        return getAs(key, Boolean.class);
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
    public Double getDouble(Path path) {
        return getAs(path, Number.class).doubleValue();
    }
    public Double getDouble(Object key) {
        return getAs(key, Number.class).doubleValue();
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
    public Float getFloat(Path path) {
        return getAs(path, Number.class).floatValue();
    }
    public Float getFloat(Object key) {
        return getAs(key, Number.class).floatValue();
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
    public Byte getByte(Path path) {
        return getAs(path, Number.class).byteValue();
    }
    public Byte getByte(Object key) {
        return getAs(key, Number.class).byteValue();
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
    public Long getLong(Path path) {
        return getAs(path, Number.class).longValue();
    }
    public Long getLong(Object key) {
        return getAs(key, Number.class).longValue();
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
    public Short getShort(Path path) {
        return getAs(path, Number.class).shortValue();
    }
    public Short getShort(Object key) {
        return getAs(key, Number.class).shortValue();
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
    public List<?> getList(Path path) {
        return getAs(path, List.class);
    }
    public List<?> getList(Object key) {
        return getAs(key, List.class);
    }
    public List<?> getListOrDefault(Path path, List<?> def) {
        return getAsOrDefault(path, List.class, def);
    }
    public List<?> getListOrDefault(Object key, List<?> def) {
        return getAsOrDefault(key, List.class, def);
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
    public List<String> getStringList(Path path) {
        return toStringList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<String> getStringList(Object key) {
        return toStringList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<String> getStringListOrDefault(Path path, List<String> def) {
        return toStringList(Optional.of(getListOrDefault(path, def))).orElse(null);
    }
    public List<String> getStringListOrDefault(Object key, List<String> def) {
        return toStringList(Optional.of(getListOrDefault(key, def))).orElse(null);
    }

    public Optional<List<Integer>> getIntListSafe(Path path) {
        return toIntList(getAsSafe(path, List.class));
    }
    public Optional<List<Integer>> getIntListSafe(Object key) {
        return toIntList(getAsSafe(key, List.class));
    }
    public List<Integer> getIntList(Path path) {
        return toIntList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Integer> getIntList(Object key) {
        return toIntList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Integer> getIntListOrDefault(Path path, List<Integer> def) {
        return getIntListSafe(path).orElse(def);
    }
    public List<Integer> getIntListOrDefault(Object key, List<Integer> def) {
        return getIntListSafe(key).orElse(def);
    }

    public Optional<List<BigInteger>> getBigIntListSafe(Path path) {
        return toBigIntList(getAsSafe(path, List.class));
    }
    public Optional<List<BigInteger>> getBigIntListSafe(Object key) {
        return toBigIntList(getAsSafe(key, List.class));
    }
    public List<BigInteger> getBigIntList(Path path) {
        return toBigIntList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<BigInteger> getBigIntList(Object key) {
        return toBigIntList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<BigInteger> getBigIntListOrDefault(Path path, List<BigInteger> def) {
        return getBigIntListSafe(path).orElse(def);
    }
    public List<BigInteger> getBigIntListOrDefault(Object key, List<BigInteger> def) {
        return getBigIntListSafe(key).orElse(def);
    }

    public Optional<List<Byte>> getByteListSafe(Path path) {
        return toByteList(getAsSafe(path, List.class));
    }
    public Optional<List<Byte>> getByteListSafe(Object key) {
        return toByteList(getAsSafe(key, List.class));
    }
    public List<Byte> getByteList(Path path) {
        return toByteList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Byte> getByteList(Object key) {
        return toByteList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Byte> getByteListOrDefault(Path path, List<Byte> def) {
        return getByteListSafe(path).orElse(def);
    }
    public List<Byte> getByteListOrDefault(Object key, List<Byte> def) {
        return getByteListSafe(key).orElse(def);
    }

    public Optional<List<Long>> getLongListSafe(Path path) {
        return toLongList(getAsSafe(path, List.class));
    }
    public Optional<List<Long>> getLongListSafe(Object key) {
        return toLongList(getAsSafe(key, List.class));
    }
    public List<Long> getLongList(Path path) {
        return toLongList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Long> getLongList(Object key) {
        return toLongList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Long> getLongListOrDefault(Path path, List<Long> def) {
        return getLongListSafe(path).orElse(def);
    }
    public List<Long> getLongListOrDefault(Object key, List<Long> def) {
        return getLongListSafe(key).orElse(def);
    }

    public Optional<List<Double>> getDoubleListSafe(Path path) {
        return toDoubleList(getAsSafe(path, List.class));
    }
    public Optional<List<Double>> getDoubleListSafe(Object key) {
        return toDoubleList(getAsSafe(key, List.class));
    }
    public List<Double> getDoubleList(Path path) {
        return toDoubleList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Double> getDoubleList(Object key) {
        return toDoubleList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Double> getDoubleListOrDefault(Path path, List<Double> def) {
        return getDoubleListSafe(path).orElse(def);
    }
    public List<Double> getDoubleListOrDefault(Object key, List<Double> def) {
        return getDoubleListSafe(key).orElse(def);
    }

    public Optional<List<Float>> getFloatListSafe(Path path) {
        return toFloatList(getAsSafe(path, List.class));
    }
    public Optional<List<Float>> getFloatListSafe(Object key) {
        return toFloatList(getAsSafe(key, List.class));
    }
    public List<Float> getFloatList(Path path) {
        return toFloatList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Float> getFloatList(Object key) {
        return toFloatList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Float> getFloatListOrDefault(Path path, List<Float> def) {
        return getFloatListSafe(path).orElse(def);
    }
    public List<Float> getFloatListOrDefault(Object key, List<Float> def) {
        return getFloatListSafe(key).orElse(def);
    }

    public Optional<List<Short>> getShortListSafe(Path path) {
        return toShortList(getAsSafe(path, List.class));
    }
    public Optional<List<Short>> getShortListSafe(Object key) {
        return toShortList(getAsSafe(key, List.class));
    }
    public List<Short> getShortList(Path path) {
        return toShortList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Short> getShortList(Object key) {
        return toShortList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Short> getShortListOrDefault(Path path, List<Short> def) {
        return getShortListSafe(path).orElse(def);
    }
    public List<Short> getShortListOrDefault(Object key, List<Short> def) {
        return getShortListSafe(key).orElse(def);
    }

    public Optional<List<Map<?, ?>>> getMapListSafe(Path path) {
        return toMapList(getAsSafe(path, List.class));
    }
    public Optional<List<Map<?, ?>>> getMapListSafe(Object key) {
        return toMapList(getAsSafe(key, List.class));
    }
    public List<Map<?, ?>> getMapList(Path path) {
        return toMapList(Optional.of(getAs(path, List.class))).orElse(null);
    }
    public List<Map<?, ?>> getMapList(Object key) {
        return toMapList(Optional.of(getAs(key, List.class))).orElse(null);
    }
    public List<Map<?, ?>> getMapListOrDefault(Path path, List<Map<?, ?>> def) {
        return getMapListSafe(path).orElse(def);
    }
    public List<Map<?, ?>> getMapListOrDefault(Object key, List<Map<?, ?>> def) {
        return getMapListSafe(key).orElse(def);
    }

    public void set(String key, Object value) {
        //If null (delete)
        if (value == null)
            remove(key);

        //If a section
        if (value instanceof Section) {
            //Set
            mappings.put(key, (Section) value);
            return;
        }

        //If a map
        if (value instanceof Map) {
            // TODO: 13. 9. 2021 Load using FileLoader
            return;
        }

        //Block at the path
        DocumentBlock block = mappings.get(key);
        //If exists
        if (block != null) {
        }
    }

    public boolean remove(String key) {
        return mappings.remove(key) != null;
    }
}