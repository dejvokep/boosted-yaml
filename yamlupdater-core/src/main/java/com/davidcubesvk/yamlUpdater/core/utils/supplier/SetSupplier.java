package com.davidcubesvk.yamlUpdater.core.utils.supplier;

import java.util.Set;

/**
 * Supplier used to supply sets of any type.
 */
public interface SetSupplier {

    /**
     * Supplies set of the given type and (initial) size.
     *
     * @param size the (initial) size of the returned set
     * @param <T>  the type of the list
     * @return the set of the given size
     */
    <T> Set<T> supply(int size);

}