package com.davidcubesvk.yamlUpdater.reader;

public class Component<T> {

    private int line, index;
    private T value;

    public Component(int line, int index, T value) {
        this.line = line;
        this.index = index;
        this.value = value;
    }


    public T getComponent() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }
}