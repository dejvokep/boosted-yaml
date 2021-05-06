package com.davidcubesvk.yamlUpdater.block;

public class Value {

    private boolean section;
    private StringBuilder value;

    public Value(StringBuilder value) {
        this.value = value;
    }

    public Value(StringBuilder value, boolean section) {
        this.value = value;
        this.section = section;
    }

    public StringBuilder getValue() {
        return value;
    }

    public boolean isSection() {
        return section;
    }

}