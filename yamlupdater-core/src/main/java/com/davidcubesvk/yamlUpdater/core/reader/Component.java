package com.davidcubesvk.yamlUpdater.core.reader;

/**
 * Represents a component, usually in reading stages.
 *
 * @param <T> type of the component's value (the actual component)
 */
public class Component<T> {

    //The indexes
    private int line, index;
    //The value
    private T value;

    /**
     * Initializes the component by the given identifiers.
     *
     * @param line  the index of the last line which contains the component, in the source list (if not noted otherwise)
     * @param index the index of the character just after the last character which contains the component (or the line's
     *              length if the last character is also the last in the line), more formally,
     *              <code>last character + 1</code> (if not noted otherwise)
     * @param value the actual value of the component
     */
    public Component(int line, int index, T value) {
        this.line = line;
        this.index = index;
        this.value = value;
    }

    /**
     * Returns the actual value, component represented.
     *
     * @return the actual component
     */
    public T getComponent() {
        return value;
    }

    /**
     * Returns the index of the last line which contains the component, in the source list. Please note that this is
     * only general description, this index can also represent other things - as noted in the description of the method
     * returning this object.
     *
     * @return the index of the last line of the component (if not noted otherwise)
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the index of the <code>last character + 1</code> which contains the component. Please note that this is
     * only general description, this index can also represent other things - as noted in the description of the method
     * returning this object.
     *
     * @return the index of the character just after the last character which contains the component (or the line's
     * length if the last character is also the last in the line), if not noted otherwise
     */
    public int getIndex() {
        return index;
    }
}