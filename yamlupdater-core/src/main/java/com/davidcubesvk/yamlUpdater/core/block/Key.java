package com.davidcubesvk.yamlUpdater.core.block;

public class Key {

    private String raw, formatted;
    private int indents;

    public Key(String key, int indents) {
        this.formatted = this.raw = key;
        this.indents = indents;
    }
    public Key(String raw, String formatted, int indents) {
        this.raw = raw;
        this.formatted = formatted;
        this.indents = indents;
    }

    public String getRaw() {
        return raw;
    }

    public String getFormatted() {
        return formatted;
    }

    public int getIndents() {
        return indents;
    }
}