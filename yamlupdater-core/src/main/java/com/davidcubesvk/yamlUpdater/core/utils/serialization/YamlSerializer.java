package com.davidcubesvk.yamlUpdater.core.utils.serialization;

import java.util.Map;

public interface YamlSerializer {


    Object deserialize(Map<Object, Object> map);
    Map<Object, Object> serialize(Object object);

}
