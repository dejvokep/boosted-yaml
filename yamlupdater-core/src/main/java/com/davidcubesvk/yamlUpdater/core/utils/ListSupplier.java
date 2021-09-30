package com.davidcubesvk.yamlUpdater.core.utils;

import java.util.List;

public interface ListSupplier {

    <T> List<T> supply(int size);

}