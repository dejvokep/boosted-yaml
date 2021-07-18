package com.davidcubesvk.yamlUpdater.core.reader;

/**
 * A class storing position of a character in a file (list of lines), if not noted otherwise - depends on
 * implementation.
 */
class Position {

    //Indexes
    private int line, index;

    /**
     * Initializes the object with the given indexes.
     * @param line the line index
     * @param index the character index in the line specified by the given index
     */
    Position(int line, int index) {
        this.line = line;
        this.index = index;
    }

    /**
     * Returns the line index, if not noted otherwise - depends on implementation.
     * @return the line index
     */
    int getLine() {
        return line;
    }

    /**
     * Returns the character index in the line at index {@link #getLine()}, if not noted otherwise - depends on
     * implementation.
     * @return the character index
     */
    int getIndex() {
        return index;
    }
}