package com.davidcubesvk.yamlUpdater.core.path;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PathFactoryTest {

    @Test
    void create() {
        assertEquals(Path.from("a", "b"), new PathFactory('.').create("a.b"));
    }

    @Test
    void getSeparator() {
        assertEquals(',', new PathFactory(',').getSeparator());
    }

    @Test
    void getEscapedSeparator() {
        assertEquals(Pattern.quote(","), new PathFactory(',').getEscapedSeparator());
    }
}