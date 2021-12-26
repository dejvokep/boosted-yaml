package com.davidcubesvk.yamlUpdater.core.block.implementation;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Block;
import com.davidcubesvk.yamlUpdater.core.block.implementation.Section;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void isEmpty() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.DEFAULT);
            // Assert
            assertFalse(file.isEmpty(false));
            assertFalse(file.getSection("y").isEmpty(false));
            assertFalse(file.createSection("z.c").getParent().isEmpty(false));
            assertTrue(file.getSection("z").isEmpty(true));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void isRoot() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.DEFAULT);
            // Assert
            assertTrue(file.isRoot());
            assertFalse(file.getSection("y").isRoot());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getRoot() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.DEFAULT);
            // Assert
            assertEquals(file, file.getSection("y").getRoot());
            assertEquals(file, file.getRoot());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getParent() {
        try {
            // Create file
            YamlFile file = YamlFile.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n  c:\n    d: false".getBytes(StandardCharsets.UTF_8)));
            // Assert
            assertEquals(file, file.getSection("y").getParent());
            assertEquals(file.getSection("y"), file.getSection("y.c").getParent());
            assertNull(file.getParent());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getName() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.DEFAULT);
            // Assert
            assertEquals("y", file.getSection("y").getName());
            assertNull(file.getName());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getPath() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.DEFAULT);
            // Assert
            assertEquals(Route.from("y"), file.getSection("y").getRoute());
            assertNull(file.getRoute());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getSubPath() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.DEFAULT);
            // Assert
            assertEquals(Route.from(true), file.getSubRoute(true));
            assertEquals(Route.from("y", 5), file.getSection("y").getSubRoute(5));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void adaptKey() {
        try {
            assertEquals(7, createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build()).adaptKey(7));
            assertEquals("true", createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.STRING).build()).adaptKey(true));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getPaths() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getStrPaths() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getKeys() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getValues() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getStrPathValues() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getBlocks() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getStrPathBlocks() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void contains() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void createSection() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void set() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void remove() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getBlockSafe() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
            // Assert
            assertEquals(file.getStoredValue().get("x"), file.getBlockSafe("x").orElse(null));
            assertEquals(file.getStoredValue().get("y"), file.getBlockSafe("y").orElse(null));
            assertEquals(file.getSection("y").getStoredValue().get("a"), file.getBlockSafe("y.a").orElse(null));
            assertEquals(file.getStoredValue().get(7), file.getBlockSafe(Route.from(7)).orElse(null));
            assertFalse(file.getBlockSafe("z").isPresent());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getParentOfPath() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
            // Assert
            assertEquals(file.getSection("y"), file.getParent("y.a").orElse(null));
            assertEquals(file, file.getParent(Route.from(7)).orElse(null));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getSafe() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
            // Assert
            assertEquals(5, file.getSafe("x").orElse(null));
            assertEquals(file.getStoredValue().get("y"), file.getSafe("y").orElse(null));
            assertEquals(true, file.getSafe("y.a").orElse(null));
            assertEquals(false, file.getSafe(Route.from(7)).orElse(null));
            assertFalse(file.getSafe("z").isPresent());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void get() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
            // Assert
            assertEquals(5, file.get("x"));
            assertEquals(file.get("y"), file.getStoredValue().get("y"));
            assertEquals(true, file.get("y.a"));
            assertEquals(GeneralSettings.DEFAULT_OBJECT, file.get(Route.from("a", "c")));
            assertEquals(false, file.get(Route.from(7)));
            assertNull(file.get("z", null));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getAsSafe() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
            // Assert
            assertEquals(5D, file.getAsSafe("x", double.class).orElse(null));
            assertEquals(file.getStoredValue().get("y"), file.getAsSafe("y", Section.class).orElse(null));
            assertEquals(true, file.getAsSafe("y.a", boolean.class).orElse(null));
            assertEquals(false, file.getAsSafe(Route.from(7), boolean.class).orElse(null));
            assertFalse(file.getAsSafe(Route.from("a", "c"), int.class).isPresent());
            assertFalse(file.getAsSafe("z", double.class).isPresent());
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void getAs() {
        try {
            // Create file
            YamlFile file = createFile(GeneralSettings.builder().setKeyMode(GeneralSettings.KeyMode.OBJECT).build());
            // Assert
            assertEquals(5D, file.getAs("x", double.class));
            assertEquals(file.getStoredValue().get("y"), file.getAs("y", Block.class));
            assertEquals(true, file.getAs("y.a", boolean.class));
            assertEquals(false, file.getAs(Route.from(7), boolean.class));
            assertNull(file.getAs(Route.from("a", "c"), int.class));
            assertNull(file.getAs("z", double.class));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void is() {
        try {
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
        } catch (IOException ex) {
            fail(ex);
        }
    }

    private YamlFile createFile(GeneralSettings settings) throws IOException {
        return new YamlFile(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false".getBytes(StandardCharsets.UTF_8)), settings, LoaderSettings.DEFAULT);
    }
}