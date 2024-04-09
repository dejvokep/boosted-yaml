/*
 * Copyright 2024 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.block.implementation;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void isEmpty() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertFalse(file.isEmpty(false));
        assertFalse(file.getSection("y").isEmpty(false));
        assertFalse(file.createSection("z.c").getParent().isEmpty(false));
        assertTrue(file.getSection("z").isEmpty(true));
    }

    @Test
    void isRoot() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertTrue(file.isRoot());
        assertFalse(file.getSection("y").isRoot());
    }

    @Test
    void getRoot() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(file, file.getSection("y").getRoot());
        assertEquals(file, file.getRoot());
    }

    @Test
    void getParent() throws IOException {
        // Create file
        YamlDocument file = YamlDocument.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n  c:\n    d: false".getBytes(StandardCharsets.UTF_8)));
        // Assert
        assertEquals(file, file.getSection("y").getParent());
        assertEquals(file.getSection("y"), file.getSection("y.c").getParent());
        assertNull(file.getParent());
    }

    @Test
    void getName() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals("y", file.getSection("y").getName());
        assertNull(file.getName());
    }

    @Test
    void getRoute() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(Route.from("y"), file.getSection("y").getRoute());
        assertNull(file.getRoute());
    }

    @Test
    void getSubRoute() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(Route.from(true), file.getSubRoute(true));
        assertEquals(Route.from("y", 5), file.getSection("y").getSubRoute(5));
    }

    @Test
    void adaptKey() throws IOException {
        assertEquals(7, createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build()).adaptKey(7));
        assertEquals("true", createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.STRING).build()).adaptKey(true));
    }

    @Test
    void getRoutes() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertEquals(new HashSet<Route>() {{
            add(Route.from("x"));
            add(Route.from("y"));
            add(Route.from(7));
            add(Route.from("c"));
        }}, file.getRoutes(false));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("x"));
            add(Route.from("y"));
            add(Route.from("y", "a"));
            add(Route.from("y", "b"));
            add(Route.from(7));
            add(Route.from("c"));
        }}, file.getRoutes(true));
    }

    @Test
    void getRoutesAsStrings() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashSet<String>() {{
            add("x");
            add("y");
            add("7");
            add("c");
        }}, file.getRoutesAsStrings(false));
        assertEquals(new HashSet<String>() {{
            add("x");
            add("y");
            add("y.a");
            add("y.b");
            add("7");
            add("c");
        }}, file.getRoutesAsStrings(true));
    }

    @Test
    void getKeys() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertEquals(new HashSet<Object>() {{
            add("x");
            add("y");
            add(7);
            add("c");
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
            add("c");
        }}, file.getKeys());
        assertEquals(new HashSet<Object>() {{
            add("a");
            add("b");
        }}, file.getSection("y").getKeys());
    }

    @Test
    void getValues() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertEquals(new HashMap<Route, Object>() {{
            put(Route.from("x"), 5);
            put(Route.from("y"), file.getSection("y"));
            put(Route.from(7), false);
            put(Route.from("c"), "A");
        }}, file.getRouteMappedValues(false));
        assertEquals(new HashMap<Route, Object>() {{
            put(Route.from("x"), 5);
            put(Route.from("y"), file.getSection("y"));
            put(Route.from("y", "a"), true);
            put(Route.from("y", "b"), "abc");
            put(Route.from(7), false);
            put(Route.from("c"), "A");
        }}, file.getRouteMappedValues(true));
    }

    @Test
    void getStringRouteMappedValues() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashMap<String, Object>() {{
            put("x", 5);
            put("y", file.getSection("y"));
            put("7", false);
            put("c", "A");
        }}, file.getStringRouteMappedValues(false));
        assertEquals(new HashMap<String, Object>() {{
            put("x", 5);
            put("y", file.getSection("y"));
            put("y.a", true);
            put("y.b", "abc");
            put("7", false);
            put("c", "A");
        }}, file.getStringRouteMappedValues(true));
    }

    @Test
    void getBlocks() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertEquals(new HashMap<Route, Block<?>>() {{
            put(Route.from("x"), file.getStoredValue().get("x"));
            put(Route.from("y"), file.getSection("y"));
            put(Route.from(7), file.getStoredValue().get(7));
            put(Route.from("c"), file.getStoredValue().get("c"));
        }}, file.getRouteMappedBlocks(false));
        assertEquals(new HashMap<Route, Block<?>>() {{
            put(Route.from("x"), file.getStoredValue().get("x"));
            put(Route.from("y"), file.getSection("y"));
            put(Route.from("y", "a"), file.getSection("y").getStoredValue().get("a"));
            put(Route.from("y", "b"), file.getSection("y").getStoredValue().get("b"));
            put(Route.from(7), file.getStoredValue().get(7));
            put(Route.from("c"), file.getStoredValue().get("c"));
        }}, file.getRouteMappedBlocks(true));
    }

    @Test
    void getStringRouteMappedBlocks() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
        // Assert
        assertEquals(new HashMap<String, Block<?>>() {{
            put("x", file.getStoredValue().get("x"));
            put("y", file.getSection("y"));
            put("7", file.getStoredValue().get("7"));
            put("c", file.getStoredValue().get("c"));
        }}, file.getStringRouteMappedBlocks(false));
        assertEquals(new HashMap<String, Block<?>>() {{
            put("x", file.getStoredValue().get("x"));
            put("y", file.getSection("y"));
            put("y.a", file.getSection("y").getStoredValue().get("a"));
            put("y.b", file.getSection("y").getStoredValue().get("b"));
            put("7", file.getStoredValue().get("7"));
            put("c", file.getStoredValue().get("c"));
        }}, file.getStringRouteMappedBlocks(true));
    }

    @Test
    void contains() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.DEFAULT);
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Set
        file.set("a.a.a", true);
        file.set("a.a.b", "abc");
        file.set(Route.from(4, 6), 9);
        file.set("c", Alphabet.B);
        // Assert
        assertTrue(file.contains("a.a.a"));
        assertTrue(file.contains("a.a.b"));
        assertTrue(file.contains(Route.from(4, 6)));
        assertTrue(file.getBoolean("a.a.a"));
        assertEquals("abc", file.getString("a.a.b"));
        assertEquals(Alphabet.B, file.getEnum("c", Alphabet.class));
        assertEquals(9, file.getInt(Route.from(4, 6)));
    }

    @Test
    void remove() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertEquals(file.getSection("y"), file.getParent("y.a").orElse(null));
        assertEquals(file, file.getParent(Route.from(7)).orElse(null));
    }

    @Test
    void getSafe() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertEquals(5, file.get("x"));
        assertEquals(file.get("y"), file.getStoredValue().get("y"));
        assertEquals(true, file.get("y.a"));
        assertEquals(GeneralSettings.DEFAULT_OBJECT, file.get(Route.from("a", "c")));
        assertEquals(false, file.get(Route.from(7)));
        assertEquals(Alphabet.A, file.getEnum("c", Alphabet.class));
        assertNull(file.get("z", null));
    }

    @Test
    void getAsSafe() throws IOException {
        // Create file
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
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
        YamlDocument file = createFile(GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());
        // Assert
        assertTrue(file.is("x", int.class));
        assertTrue(file.is("x", Integer.class));
        assertTrue(file.is("y", Block.class));
        assertTrue(file.is("y.a", boolean.class));
        assertTrue(file.is(Route.from(7), boolean.class));
        assertFalse(file.is("x", boolean.class));
        assertFalse(file.is(Route.from("a", "c"), int.class));
        assertFalse(file.is("z", double.class));
    }

    @Test
    void getList() throws IOException {
        // Create file
        YamlDocument file = YamlDocument.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false\nz:\n- \"a\"\n- \"b\"\n- 4".getBytes(StandardCharsets.UTF_8)));
        List<?> list = file.getList("z");
        // Assert
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals(4, list.get(2));
    }

    @Test
    void defaults() throws IOException {
        // Create file
        YamlDocument file = YamlDocument.create(new ByteArrayInputStream("x: \"y\"\ny:\n  a: true\n  b: false\n7: f".getBytes(StandardCharsets.UTF_8)), new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: def\n7: true".getBytes(StandardCharsets.UTF_8)));
        // Assert
        assertEquals(5, file.getInt("x"));
        assertEquals(true, file.getBoolean("y.a"));
        assertEquals(true, file.getBoolean("7"));
        assertEquals(false, file.getBoolean("y.b"));
    }

    private YamlDocument createFile(GeneralSettings settings) throws IOException {
        return YamlDocument.create(new ByteArrayInputStream("x: 5\ny:\n  a: true\n  b: abc\n7: false\nc: A".getBytes(StandardCharsets.UTF_8)), settings, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
    }

    @Test
    void move() throws IOException {
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("x: \"y\"\ny:\n  a: true\n  b: false\n7: f".getBytes(StandardCharsets.UTF_8)), GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build());

        Block moved = document.getBlock(Route.from(7));
        assertEquals(moved, document.move(Route.from(7), Route.from("z", true)));
        assertEquals(moved, document.getBlock(Route.from("z", true)));
        assertNull(document.getBlock(Route.from(7)));

        moved = document.getBlock("x.a");
        assertEquals(moved, document.move("x.a", "d"));
        assertEquals(moved, document.getBlock("d"));
        assertNull(document.getBlock("x.a"));
    }

    private enum Alphabet {
        A, B, C
    }
}