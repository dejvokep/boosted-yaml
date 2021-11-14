package com.davidcubesvk.yamlUpdater.core.block;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void isEmpty() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertFalse(file.isEmpty(false));
        assertFalse(file.getSection("y").isEmpty(false));
        assertFalse(file.createSection("z.c").getParent().isEmpty(false));
        assertTrue(file.getSection("z").isEmpty(true));
    }

    @Test
    void isRoot() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertTrue(file.isRoot());
        assertFalse(file.getSection("y").isRoot());
    }

    @Test
    void getRoot() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(file, file.getSection("y").getRoot());
        assertEquals(file, file.getRoot());
    }

    @Test
    void getParent() {
        // Create file
        YamlFile file = new YamlFile(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n  c:\n    d: false".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT);
        // Assert
        assertEquals(file, file.getSection("y").getParent());
        assertEquals(file.getSection("y"), file.getSection("y.c").getParent());
        assertNull(file.getParent());
    }

    @Test
    void getName() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals("y", file.getSection("y").getName());
        assertNull(file.getName());
    }

    @Test
    void getPath() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(Path.fromSingleKey("y"), file.getSection("y").getPath());
        assertNull(file.getPath());
    }

    @Test
    void getSubPath() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(Path.fromSingleKey(true), file.getSubPath(true));
        assertEquals(Path.from("y", 5), file.getSection("y").getSubPath(5));
    }

    @Test
    void adaptKey() {
        assertEquals(7, createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build()).adaptKey(7));
        assertEquals("true", createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.STRING).build()).adaptKey(true));
    }

    @Test
    void getPaths() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashSet<Path>(){{
            add(Path.from("x"));
            add(Path.from("y"));
            add(Path.from(7));
        }}, file.getPaths(false));
        assertEquals(new HashSet<Path>(){{
            add(Path.from("x"));
            add(Path.from("y"));
            add(Path.from("y", "a"));
            add(Path.from("y", "b"));
            add(Path.from(7));
        }}, file.getPaths(true));
    }

    @Test
    void getStrPaths() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashSet<String>(){{
            add("x");
            add("y");
            add("7");
        }}, file.getStrPaths(false));
        assertEquals(new HashSet<String>(){{
            add("x");
            add("y");
            add("y.a");
            add("y.b");
            add("7");
        }}, file.getStrPaths(true));
    }

    @Test
    void getKeys() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashSet<Object>(){{
            add("x");
            add("y");
            add(7);
        }}, file.getKeys());
        assertEquals(new HashSet<Object>(){{
            add("a");
            add("b");
        }}, file.getSection("y").getKeys());

        // Reset
        file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashSet<Object>(){{
            add("x");
            add("y");
            add("7");
        }}, file.getKeys());
        assertEquals(new HashSet<Object>(){{
            add("a");
            add("b");
        }}, file.getSection("y").getKeys());
    }

    @Test
    void getValues() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashMap<Path, Object>(){{
            put(Path.from("x"), 5);
            put(Path.from("y"), file.getSection("y"));
            put(Path.from(7), false);
        }}, file.getValues(false));
        assertEquals(new HashMap<Path, Object>(){{
            put(Path.from("x"), 5);
            put(Path.from("y"), file.getSection("y"));
            put(Path.from("y", "a"), true);
            put(Path.from("y", "b"), "abc");
            put(Path.from(7), false);
        }}, file.getValues(true));
    }

    @Test
    void getStrPathValues() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashMap<String, Object>(){{
            put("x", 5);
            put("y", file.getSection("y"));
            put("7", false);
        }}, file.getStrPathValues(false));
        assertEquals(new HashMap<String, Object>(){{
            put("x", 5);
            put("y", file.getSection("y"));
            put("y.a", true);
            put("y.b", "abc");
            put("7", false);
        }}, file.getStrPathValues(true));
    }

    @Test
    void getBlocks() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashMap<Path, Block<?>>(){{
            put(Path.from("x"), file.getValue().get("x"));
            put(Path.from("y"), file.getSection("y"));
            put(Path.from(7), file.getValue().get(7));
        }}, file.getBlocks(false));
        assertEquals(new HashMap<Path, Block<?>>(){{
            put(Path.from("x"), file.getValue().get("x"));
            put(Path.from("y"), file.getSection("y"));
            put(Path.from("y", "a"), file.getSection("y").getValue().get("a"));
            put(Path.from("y", "b"), file.getSection("y").getValue().get("b"));
            put(Path.from(7), file.getValue().get(7));
        }}, file.getBlocks(true));
    }

    @Test
    void getStrPathBlocks() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashMap<String, Block<?>>(){{
            put("x", file.getValue().get("x"));
            put("y", file.getSection("y"));
            put("7", file.getValue().get("7"));
        }}, file.getStrPathBlocks(false));
        assertEquals(new HashMap<String, Block<?>>(){{
            put("x", file.getValue().get("x"));
            put("y", file.getSection("y"));
            put("y.a", file.getSection("y").getValue().get("a"));
            put("y.b", file.getSection("y").getValue().get("b"));
            put("7", file.getValue().get("7"));
        }}, file.getStrPathBlocks(true));
    }

    @Test
    void contains() {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertTrue(file.contains("x"));
        assertTrue(file.contains("y.b"));
        assertTrue(file.contains(Path.from("x")));
        assertTrue(file.contains(Path.from("y", "b")));
        assertFalse(file.contains("z"));
        assertFalse(file.contains("z.c"));
        assertFalse(file.contains(Path.from("z")));
        assertFalse(file.contains(Path.from("z", "c")));
    }

    @Test
    void createSection() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Create sections
        file.createSection("z.c");
        file.createSection(Path.from(true, "d"));
        // Assert
        assertTrue(file.contains("z"));
        assertTrue(file.contains("z.c"));
        assertTrue(file.contains(Path.from(true)));
        assertTrue(file.contains(Path.from(true, "d")));
    }

    @Test
    void set() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Set
        file.set("z.c", true);
        file.set(Path.from(4, 6), 9);
        // Assert
        assertTrue(file.contains("z.c"));
        assertTrue(file.contains(Path.from(4, 6)));
        assertTrue(file.getBoolean("z.c"));
        assertEquals(9, file.getInt(Path.from(4, 6)));
    }

    @Test
    void remove() {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Remove
        file.remove("y.b");
        // Assert
        assertFalse(file.contains("y.b"));
        // Remove
        file.remove("y");
        // Assert
        assertFalse(file.contains("y"));
    }

    @Test
    void getBlockSafe() {
    }

    @Test
    void getDirectBlockSafe() {
    }

    @Test
    void testGetBlockSafe() {
    }

    @Test
    void testGetParent() {
    }

    @Test
    void testGetParent1() {
    }

    @Test
    void getSafe() {
    }

    @Test
    void testGetSafe() {
    }

    @Test
    void get() {
    }

    @Test
    void testGet() {
    }

    @Test
    void testGet1() {
    }

    @Test
    void testGet2() {
    }

    @Test
    void getAsSafe() {
    }

    @Test
    void testGetAsSafe() {
    }

    @Test
    void getAs() {
    }

    @Test
    void testGetAs() {
    }

    @Test
    void testGetAs1() {
    }

    @Test
    void testGetAs2() {
    }

    @Test
    void is() {
    }

    @Test
    void testIs() {
    }

    private YamlFile createFile(GeneralSettings settings) {
        return new YamlFile(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false".getBytes(StandardCharsets.UTF_8)), settings, LoaderSettings.DEFAULT);
    }
}