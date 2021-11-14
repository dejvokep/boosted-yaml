package com.davidcubesvk.yamlUpdater.core.utils.supplier;

import java.util.Map;

/**
 * Supplier used to supply maps of any type.
 */
public interface MapSupplier {

    /**
     * Supplies map of the given key and value types and (initial) size.
     *
     * @param size the (initial) size of the returned map, if supported by the map implementation returned
     * @param <K>  key type
     * @param <V>  value type
     * @return the map of the given size
     */
    <K, V> Map<K, V> supply(int size);

}