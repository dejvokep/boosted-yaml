/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.settings.updater;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.segment.Segment;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.Version;
import dev.dejvokep.boostedyaml.dvs.versioning.ManualVersioning;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class UpdaterSettingsTest {

    @Test
    void getMergeRules() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .setMergeRule(MergeRule.MAPPINGS, false)
                .setMergeRules(new HashMap<MergeRule, Boolean>() {{
                    put(MergeRule.MAPPING_AT_SECTION, true);
                    put(MergeRule.SECTION_AT_MAPPING, true);
                }}).build();
        // Assert
        assertEquals(false, settings.getMergeRules().get(MergeRule.MAPPINGS));
        assertEquals(true, settings.getMergeRules().get(MergeRule.MAPPING_AT_SECTION));
        assertEquals(true, settings.getMergeRules().get(MergeRule.SECTION_AT_MAPPING));
    }

    @Test
    void getIgnoredRoutes() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .addIgnoredRoutes(new HashMap<String, Set<Route>>() {{
                    put("1.2", new HashSet<Route>() {{
                        add(Route.from("a"));
                    }});
                    put("1.3", new HashSet<Route>() {{
                        add(Route.from("c"));
                    }});
                }})
                .addIgnoredRoutes(new HashMap<String, Set<String>>() {{
                    put("1.2", new HashSet<String>() {{
                        add("b");
                    }});
                    put("1.4", new HashSet<String>() {{
                        add("d");
                    }});
                }}, '.')
                .addIgnoredRoutes("1.5", new HashSet<Route>() {{
                    add(Route.from("e"));
                }})
                .addIgnoredRoutes("1.5", new HashSet<String>() {{
                    add("f");
                }}, '.').build();
        // Assert
        assertEquals(new HashSet<Route>() {{
            add(Route.from("a"));
            add(Route.from("b"));
        }}, settings.getIgnoredRoutes("1.2", '.'));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("c"));
        }}, settings.getIgnoredRoutes("1.3", '.'));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("d"));
        }}, settings.getIgnoredRoutes("1.4", '.'));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("e"));
            add(Route.from("f"));
        }}, settings.getIgnoredRoutes("1.5", '.'));
    }

    @Test
    void getRelocations() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .addRelocations(new HashMap<String, Map<Route, Route>>() {{
                    put("1.2", new HashMap<Route, Route>() {{
                        put(Route.from("a"), Route.from("g"));
                    }});
                    put("1.3", new HashMap<Route, Route>() {{
                        put(Route.from("c"), Route.from("i"));
                    }});
                }})
                .addRelocations(new HashMap<String, Map<String, String>>() {{
                    put("1.2", new HashMap<String, String>() {{
                        put("b", "h");
                    }});
                    put("1.4", new HashMap<String, String>() {{
                        put("d", "j");
                    }});
                }}, '.')
                .addRelocations("1.5", new HashMap<Route, Route>() {{
                    put(Route.from("e"), Route.from("k"));
                }})
                .addRelocations("1.5", new HashMap<String, String>() {{
                    put("f", "l");
                }}, '.').build();
        // Assert
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("a"), Route.from("g"));
            put(Route.from("b"), Route.from("h"));
        }}, settings.getRelocations("1.2", '.'));
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("c"), Route.from("i"));
        }}, settings.getRelocations("1.3", '.'));
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("d"), Route.from("j"));
        }}, settings.getRelocations("1.4", '.'));
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("e"), Route.from("k"));
            put(Route.from("f"), Route.from("l"));
        }}, settings.getRelocations("1.5", '.'));
    }

    @Test
    void getMappers() {
        // Mappers
        ValueMapper valueMapper = ValueMapper.value(object -> object), blockMapper = ValueMapper.block(block -> block.getStoredValue().toString());
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .addMappers(new HashMap<String, Map<Route, ValueMapper>>() {{
                    put("1.2", new HashMap<Route, ValueMapper>() {{
                        put(Route.from("a"), valueMapper);
                    }});
                    put("1.3", new HashMap<Route, ValueMapper>() {{
                        put(Route.from("c"), blockMapper);
                    }});
                }})
                .addMappers(new HashMap<String, Map<String, ValueMapper>>() {{
                    put("1.2", new HashMap<String, ValueMapper>() {{
                        put("b", valueMapper);
                    }});
                    put("1.4", new HashMap<String, ValueMapper>() {{
                        put("d", blockMapper);
                    }});
                }}, '.')
                .addMappers("1.5", new HashMap<Route, ValueMapper>() {{
                    put(Route.from("e"), valueMapper);
                }})
                .addMappers("1.5", new HashMap<String, ValueMapper>() {{
                    put("f", blockMapper);
                }}, '.').build();
        // Assert
        assertEquals(new HashMap<Route, ValueMapper>() {{
            put(Route.from("a"), valueMapper);
            put(Route.from("b"), valueMapper);
        }}, settings.getMappers("1.2", '.'));
        assertEquals(new HashMap<Route, ValueMapper>() {{
            put(Route.from("c"), blockMapper);
        }}, settings.getMappers("1.3", '.'));
        assertEquals(new HashMap<Route, ValueMapper>() {{
            put(Route.from("d"), blockMapper);
        }}, settings.getMappers("1.4", '.'));
        assertEquals(new HashMap<Route, ValueMapper>() {{
            put(Route.from("e"), valueMapper);
            put(Route.from("f"), blockMapper);
        }}, settings.getMappers("1.5", '.'));
    }

    @Test
    void getCustomLogic() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .addCustomLogic(new HashMap<String, List<Consumer<YamlDocument>>>() {{
                    put("1.2", createList(2));
                    put("1.3", createList(3));
                }})
                .addCustomLogic(new HashMap<String, List<Consumer<YamlDocument>>>() {{
                    put("1.2", createList(4));
                    put("1.4", createList(5));
                }})
                .addCustomLogic("1.5", createList(6))
                .addCustomLogic("1.5", yamlDocument -> {}).build();
        // Assert
        assertEquals(createList(6), settings.getCustomLogic("1.2"));
        assertEquals(createList(3), settings.getCustomLogic("1.3"));
        assertEquals(createList(5), settings.getCustomLogic("1.4"));
        assertEquals(7, settings.getCustomLogic("1.5").size());
    }

    private List<Consumer<YamlDocument>> createList(int size) {
        List<Consumer<YamlDocument>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            list.add(yamlDocument -> {});
        return list;
    }

    @Test
    void getVersioning() {
        try {
            // Pattern
            Pattern pattern = new Pattern(Segment.range(1, 100), Segment.literal("."), Segment.range(0, 10));
            // File
            YamlDocument file = YamlDocument.create(
                    new ByteArrayInputStream("a: 1.2".getBytes(StandardCharsets.UTF_8)), new ByteArrayInputStream("a: 1.3".getBytes(StandardCharsets.UTF_8)),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            // Target version
            Version version = pattern.getVersion("1.2");
            // Assert
            assertEquals(version, UpdaterSettings.builder().setVersioning(new ManualVersioning(pattern, "1.2", "1.3")).build().getVersioning().getDocumentVersion(file, false));
            assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, "1.2", "1.3").build().getVersioning().getDocumentVersion(file, false));
            assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, Route.from("a")).build().getVersioning().getDocumentVersion(file, false));
            assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, "a").build().getVersioning().getDocumentVersion(file, false));
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    void isEnableDowngrading() {
        assertTrue(UpdaterSettings.builder().setEnableDowngrading(true).build().isEnableDowngrading());
        assertFalse(UpdaterSettings.builder().setEnableDowngrading(false).build().isEnableDowngrading());
    }

    @Test
    void isKeepAll() {
        assertTrue(UpdaterSettings.builder().setKeepAll(true).build().isKeepAll());
        assertFalse(UpdaterSettings.builder().setKeepAll(false).build().isKeepAll());
    }

    @Test
    void isAutoSave() {
        assertTrue(UpdaterSettings.builder().setAutoSave(true).build().isAutoSave());
        assertFalse(UpdaterSettings.builder().setAutoSave(false).build().isAutoSave());
    }

    @Test
    void getOptionSorting() {
        assertEquals(UpdaterSettings.OptionSorting.NONE, UpdaterSettings.builder().setOptionSorting(UpdaterSettings.OptionSorting.NONE).build().getOptionSorting());
        assertEquals(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS, UpdaterSettings.builder().setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build().getOptionSorting());
    }
}