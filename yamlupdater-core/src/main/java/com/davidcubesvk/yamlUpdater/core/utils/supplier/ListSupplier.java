package com.davidcubesvk.yamlUpdater.core.utils.supplier;

import java.util.List;

public interface ListSupplier {

    <T> List<T> supply(int size);

}