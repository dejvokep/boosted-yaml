package com.davidcubesvk.yamlUpdater.core.reader;

class Position {

    private int line, index;

    Position(int line, int index) {
        this.line = line;
        this.index = index;
    }

    int getLine() {
        return line;
    }

    int getIndex() {
        return index;
    }
}