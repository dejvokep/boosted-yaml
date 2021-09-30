package com.davidcubesvk.yamlUpdater.core.utils;

import java.util.Map;

public interface Serializator {

    Map<Object, Object> serialize(Object object);
    Object deserialize(Map<Object, Object> map);

}