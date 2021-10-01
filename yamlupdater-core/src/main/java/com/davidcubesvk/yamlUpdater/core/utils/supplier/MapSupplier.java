package com.davidcubesvk.yamlUpdater.core.utils.supplier;

import java.util.Map;

public interface MapSupplier {

    <K, V> Map<K, V> supply(int size);

}