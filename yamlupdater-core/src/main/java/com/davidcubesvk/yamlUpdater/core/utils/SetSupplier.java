package com.davidcubesvk.yamlUpdater.core.utils;

import java.util.List;
import java.util.Set;

public interface SetSupplier {

    <T> Set<T> supply(int size);

}