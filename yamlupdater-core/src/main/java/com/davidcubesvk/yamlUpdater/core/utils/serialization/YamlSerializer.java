package com.davidcubesvk.yamlUpdater.core.utils.serialization;

import com.davidcubesvk.yamlUpdater.core.utils.supplier.MapSupplier;

import java.util.Map;

public interface YamlSerializer {


    Object deserialize(Map<Object, Object> map);
    Map<Object, Object> serialize(Object object, MapSupplier supplier);
    Class<?> getSerializableClass();
    Object getClassKey();

}
