package com.davidcubesvk.yamlUpdater;

public interface FileProvider<T> {

    T convert(UpdatedFile file);

}