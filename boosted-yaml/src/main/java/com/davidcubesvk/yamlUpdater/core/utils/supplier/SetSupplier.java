package com.davidcubesvk.yamlUpdater.core.utils.supplier;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Supplier used to supply sets of any type.
 */
public interface SetSupplier {

    /**
     * Supplies set of the given type and (initial) size.
     *
     * @param size the (initial) size of the returned set, if supported by the set implementation returned
     * @param <T>  the type of the list
     * @return the set of the given size
     */
    @NotNull
    <T> Set<T> supply(int size);

}