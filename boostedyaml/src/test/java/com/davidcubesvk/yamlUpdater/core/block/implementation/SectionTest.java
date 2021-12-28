package com.davidcubesvk.yamlUpdater.core.block.implementation;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void isEmpty() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertFalse(file.isEmpty(false));
        assertFalse(file.getSection("y").isEmpty(false));
        assertFalse(file.createSection("z.c").getParent().isEmpty(false));
        assertTrue(file.getSection("z").isEmpty(true));
    }

    @Test
    void isRoot() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertTrue(file.isRoot());
        assertFalse(file.getSection("y").isRoot());
    }

    @Test
    void getRoot() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(file, file.getSection("y").getRoot());
        assertEquals(file, file.getRoot());
    }

    @Test
    void getParent() throws IOException {
        // Create file
        YamlFile file = YamlFile.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n  c:\n    d: false".getBytes(StandardCharsets.UTF_8)));
        // Assert
        assertEquals(file, file.getSection("y").getParent());
        assertEquals(file.getSection("y"), file.getSection("y.c").getParent());
        assertNull(file.getParent());
    }

    @Test
    void getName() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals("y", file.getSection("y").getName());
        assertNull(file.getName());
    }

    @Test
    void getPath() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(Route.from("y"), file.getSection("y").getRoute());
        assertNull(file.getRoute());
    }

    @Test
    void getSubPath() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(Route.from(true), file.getSubRoute(true));
        assertEquals(Route.from("y", 5), file.getSection("y").getSubRoute(5));
    }

    @Test
    void adaptKey() throws IOException {
        assertEquals(7, createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build()).adaptKey(7));
        assertEquals("true", createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.STRING).build()).adaptKey(true));
    }

    @Test
    void getPaths() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashSet<Route>() {{
            add(Route.from("x"));
            add(Route.from("y"));
            add(Route.from(7));
        }}, file.getRoutes(false));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("x"));
            add(Route.from("y"));
            add(Route.from("y", "a"));
            add(Route.from("y", "b"));
            add(Route.from(7));
        }}, file.getRoutes(true));
    }

    @Test
    void getStrPaths() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashSet<String>() {{
            add("x");
            add("y");
            add("7");
        }}, file.getRoutesAsStrings(false));
        assertEquals(new HashSet<String>() {{
            add("x");
            add("y");
            add("y.a");
            add("y.b");
            add("7");
        }}, file.getRoutesAsStrings(true));
    }

    @Test
    void getKeys() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashSet<Object>() {{
            add("x");
            add("y");
            add(7);
        }}, file.getKeys());
        assertEquals(new HashSet<Object>() {{
            add("a");
            add("b");
        }}, file.getSection("y").getKeys());

        // Reset
        file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashSet<Object>() {{
            add("x");
            add("y");
            add("7");
        }}, file.getKeys());
        assertEquals(new HashSet<Object>() {{
            add("a");
            add("b");
        }}, file.getSection("y").getKeys());
    }

    @Test
    void getValues() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashMap<Route, Object>() {{
            put(Route.from("x"), 5);
            put(Route.from("y"), file.getSection("y"));
            put(Route.from(7), false);
        }}, file.getRouteMappedValues(false));
        assertEquals(new HashMap<Route, Object>() {{
            put(Route.from("x"), 5);
            put(Route.from("y"), file.getSection("y"));
            put(Route.from("y", "a"), true);
            put(Route.from("y", "b"), "abc");
            put(Route.from(7), false);
        }}, file.getRouteMappedValues(true));
    }

    @Test
    void getStrPathValues() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashMap<String, Object>() {{
            put("x", 5);
            put("y", file.getSection("y"));
            put("7", false);
        }}, file.getStringMappedValues(false));
        assertEquals(new HashMap<String, Object>() {{
            put("x", 5);
            put("y", file.getSection("y"));
            put("y.a", true);
            put("y.b", "abc");
            put("7", false);
        }}, file.getStringMappedValues(true));
    }

    @Test
    void getBlocks() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(new HashMap<Route, Block<?>>() {{
            put(Route.from("x"), file.getStoredValue().get("x"));
            put(Route.from("y"), file.getSection("y"));
            put(Route.from(7), file.getStoredValue().get(7));
        }}, file.getRouteMappedBlocks(false));
        assertEquals(new HashMap<Route, Block<?>>() {{
            put(Route.from("x"), file.getStoredValue().get("x"));
            put(Route.from("y"), file.getSection("y"));
            put(Route.from("y", "a"), file.getSection("y").getStoredValue().get("a"));
            put(Route.from("y", "b"), file.getSection("y").getStoredValue().get("b"));
            put(Route.from(7), file.getStoredValue().get(7));
        }}, file.getRouteMappedBlocks(true));
    }

    @Test
    void getStrPathBlocks() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashMap<String, Block<?>>() {{
            put("x", file.getStoredValue().get("x"));
            put("y", file.getSection("y"));
            put("7", file.getStoredValue().get("7"));
        }}, file.getStringMappedBlocks(false));
        assertEquals(new HashMap<String, Block<?>>() {{
            put("x", file.getStoredValue().get("x"));
            put("y", file.getSection("y"));
            put("y.a", file.getSection("y").getStoredValue().get("a"));
            put("y.b", file.getSection("y").getStoredValue().get("b"));
            put("7", file.getStoredValue().get("7"));
        }}, file.getStringMappedBlocks(true));
    }

    @Test
    void contains() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertTrue(file.contains("x"));
        assertTrue(file.contains("y.b"));
        assertTrue(file.contains(Route.from("x")));
        assertTrue(file.contains(Route.from("y", "b")));
        assertFalse(file.contains("z"));
        assertFalse(file.contains("z.c"));
        assertFalse(file.contains(Route.from("z")));
        assertFalse(file.contains(Route.from("z", "c")));
    }

    @Test
    void createSection() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Create sections
        Section s1 = file.createSection("z.c"), s2 = file.createSection(Route.from(true, "d"));
        // Assert
        assertTrue(file.contains("z"));
        assertTrue(file.contains("z.c"));
        assertTrue(file.contains(Route.from(true)));
        assertTrue(file.contains(Route.from(true, "d")));
        assertEquals(s1, file.getSection("z.c"));
        assertEquals(s2, file.getSection(Route.from(true, "d")));
    }

    @Test
    void set() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Set
        file.set("z.c", true);
        file.set(Route.from(4, 6), 9);
        // Assert
        assertTrue(file.contains("z.c"));
        assertTrue(file.contains(Route.from(4, 6)));
        assertTrue(file.getBoolean("z.c"));
        assertEquals(9, file.getInt(Route.from(4, 6)));
    }

    @Test
    void remove() throws IOException {
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
    void getBlockSafe() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(file.getStoredValue().get("x"), file.getOptionalBlock("x").orElse(null));
        assertEquals(file.getStoredValue().get("y"), file.getOptionalBlock("y").orElse(null));
        assertEquals(file.getSection("y").getStoredValue().get("a"), file.getOptionalBlock("y.a").orElse(null));
        assertEquals(file.getStoredValue().get(7), file.getOptionalBlock(Route.from(7)).orElse(null));
        assertFalse(file.getOptionalBlock("z").isPresent());
    }

    @Test
    void getParentOfPath() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(file.getSection("y"), file.getParent("y.a").orElse(null));
        assertEquals(file, file.getParent(Route.from(7)).orElse(null));
    }

    @Test
    void getSafe() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(5, file.getOptional("x").orElse(null));
        assertEquals(file.getStoredValue().get("y"), file.getOptional("y").orElse(null));
        assertEquals(true, file.getOptional("y.a").orElse(null));
        assertEquals(false, file.getOptional(Route.from(7)).orElse(null));
        assertFalse(file.getOptional("z").isPresent());
    }

    @Test
    void get() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(5, file.get("x"));
        assertEquals(file.get("y"), file.getStoredValue().get("y"));
        assertEquals(true, file.get("y.a"));
        assertEquals(GeneralSettings.DEFAULT_OBJECT, file.get(Route.from("a", "c")));
        assertEquals(false, file.get(Route.from(7)));
        assertNull(file.get("z", null));
    }

    @Test
    void getAsSafe() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(5D, file.getAsOptional("x", double.class).orElse(null));
        assertEquals(file.getStoredValue().get("y"), file.getAsOptional("y", Section.class).orElse(null));
        assertEquals(true, file.getAsOptional("y.a", boolean.class).orElse(null));
        assertEquals(false, file.getAsOptional(Route.from(7), boolean.class).orElse(null));
        assertFalse(file.getAsOptional(Route.from("a", "c"), int.class).isPresent());
        assertFalse(file.getAsOptional("z", double.class).isPresent());
    }

    @Test
    void getAs() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertEquals(5D, file.getAs("x", double.class));
        assertEquals(file.getStoredValue().get("y"), file.getAs("y", Block.class));
        assertEquals(true, file.getAs("y.a", boolean.class));
        assertEquals(false, file.getAs(Route.from(7), boolean.class));
        assertNull(file.getAs(Route.from("a", "c"), int.class));
        assertNull(file.getAs("z", double.class));
    }

    @Test
    void is() throws IOException {
        // Create file
        YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
        // Assert
        assertTrue(file.is("x", double.class));
        assertTrue(file.is("x", Double.class));
        assertTrue(file.is("y", Block.class));
        assertTrue(file.is("y.a", boolean.class));
        assertTrue(file.is(Route.from(7), boolean.class));
        assertFalse(file.is("x", boolean.class));
        assertFalse(file.is(Route.from("a", "c"), int.class));
        assertFalse(file.is("z", double.class));
    }

    private YamlFile createFile(GeneralSettings settings) throws IOException {
        return YamlFile.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false".getBytes(StandardCharsets.UTF_8)), settings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }
}