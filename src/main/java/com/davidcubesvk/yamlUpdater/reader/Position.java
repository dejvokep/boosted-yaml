package com.davidcubesvk.yamlUpdater.reader;

class Position {

    private int line, index;

    Position(int line, int index) {
        this.line = line;
        this.index = index;
    }

    public int getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }
}