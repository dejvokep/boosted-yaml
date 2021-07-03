package com.davidcubesvk.yamlUpdater.core.utils;

/**
 * Class which encapsulates a primitive object. This is generally useful if you want to, for example, pass an integer to
 * a function and modify it inside. After the function has finished, the changes are not reflected in the original
 * object. This way, you get pass modifiable primitives whenever you want.
 * @param <T> the type of the encapsulated object
 */
public class Primitive<T> {

    //The primitive value
    private T value;

    /**
     * Initializes the instance with the given starting value.
     * @param value the starting value
     */
    public Primitive(T value) {
        this.value = value;
    }

    /**
     * Sets the stored value to the given one.
     * @param value the new value
     */
    public void set(T value) {
        this.value = value;
    }

    /**
     * Returns the stored value.
     * @return the stored value
     */
    public T get() {
        return value;
    }

}