package com.davidcubesvk.yamlUpdater.core.files;

public interface FileProvider<T> {

    T convert(UpdatedFile<T> file) throws ReflectiveOperationException, ClassCastException;

}