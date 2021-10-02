package com.davidcubesvk.yamlUpdater.core.serialization;

import java.util.Map;

public interface Serializable {

    Object deserialize(Map<Object, Object> map);
    Map<Object, Object> serialize();

}