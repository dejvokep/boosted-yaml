package com.davidcubesvk.yamlUpdater.core.files;

/**
 * Class used to convert YAML-style mappings into configuration objects used by plugins.
 * @param <T> the type of the object returned by the {@link #convert(UpdatedFile)} function
 */
public interface FileProvider<T> {

    /**
     * Converts the given updated file's data into standard configuration object. This invokes several reflective
     * operations.
     * @param file the file to convert
     * @return the converted file
     * @throws ReflectiveOperationException if anything goes wrong during accessing normally not accessible fields
     * @throws ClassCastException if failed to cast objects returned by reflective operations
     */
    T convert(UpdatedFile<T> file) throws ReflectiveOperationException, ClassCastException;

}