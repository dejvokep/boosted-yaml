package com.davidcubesvk.yamlUpdater.core.utils.supplier;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Supplier used to supply lists of any type.
 */
public interface ListSupplier {

    /**
     * Supplies list of the given type and (initial) size.
     *
     * @param size the (initial) size of the returned list, if supported by the list implementation returned
     * @param <T>  the type of the list
     * @return the list of the given size
     */
    @NotNull
    <T> List<T> supply(int size);

}