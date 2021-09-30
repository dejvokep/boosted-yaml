package com.davidcubesvk.yamlUpdater.core.utils;

import java.util.Map;

public interface MapSupplier {

    <K, V> Map<K, V> supply(int size);

}