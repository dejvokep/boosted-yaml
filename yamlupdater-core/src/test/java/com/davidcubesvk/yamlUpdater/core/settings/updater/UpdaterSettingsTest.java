package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.fvs.segment.Segment;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.fvs.Pattern;
import com.davidcubesvk.yamlUpdater.core.fvs.Version;
import com.davidcubesvk.yamlUpdater.core.fvs.versioning.ManualVersioning;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    void getKeep() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .setKeepRoutes(new HashMap<String, Set<Route>>() {{
                    put("1.2", new HashSet<Route>() {{
                        add(Route.from("a"));
                    }});
                    put("1.3", new HashSet<Route>() {{
                        add(Route.from("c"));
                    }});
                }})
                .setStringKeepRoutes(new HashMap<String, Set<String>>() {{
                    put("1.2", new HashSet<String>() {{
                        add("b");
                    }});
                    put("1.4", new HashSet<String>() {{
                        add("d");
                    }});
                }})
                .setKeepRoutes("1.5", new HashSet<Route>() {{
                    add(Route.from("e"));
                }})
                .setStringKeepRoutes("1.5", new HashSet<String>() {{
                    add("f");
                }}).build();
        // Map
        Map<String, Set<Route>> routes = settings.getKeep('.');
        // Assert
        assertEquals(new HashSet<Route>() {{
            add(Route.from("a"));
            add(Route.from("b"));
        }}, routes.get("1.2"));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("c"));
        }}, routes.get("1.3"));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("d"));
        }}, routes.get("1.4"));
        assertEquals(new HashSet<Route>() {{
            add(Route.from("e"));
            add(Route.from("f"));
        }}, routes.get("1.5"));
        assertEquals(4, routes.keySet().size());
    }

    @Test
    void getRelocations() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .setRelocations(new HashMap<String, Map<Route, Route>>() {{
                    put("1.2", new HashMap<Route, Route>() {{
                        put(Route.from("a"), Route.from("g"));
                    }});
                    put("1.3", new HashMap<Route, Route>() {{
                        put(Route.from("c"), Route.from("i"));
                    }});
                }})
                .setStringRelocations(new HashMap<String, Map<String, String>>() {{
                    put("1.2", new HashMap<String, String>() {{
                        put("b", "h");
                    }});
                    put("1.4", new HashMap<String, String>() {{
                        put("d", "j");
                    }});
                }})
                .setRelocations("1.5", new HashMap<Route, Route>() {{
                    put(Route.from("e"), Route.from("k"));
                }})
                .setStringRelocations("1.5", new HashMap<String, String>() {{
                    put("f", "l");
                }}).build();
        // Map
        Map<String, Map<Route, Route>> relocations = settings.getRelocations('.');
        // Assert
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("a"), Route.from("g"));
            put(Route.from("b"), Route.from("h"));
        }}, relocations.get("1.2"));
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("c"), Route.from("i"));
        }}, relocations.get("1.3"));
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("d"), Route.from("j"));
        }}, relocations.get("1.4"));
        assertEquals(new HashMap<Route, Route>() {{
            put(Route.from("e"), Route.from("k"));
            put(Route.from("f"), Route.from("l"));
        }}, relocations.get("1.5"));
        assertEquals(4, relocations.keySet().size());
    }

    @Test
    void getVersioning() {
        try {
            // Pattern
            Pattern pattern = new Pattern(Segment.range(1, 100), Segment.literal("."), Segment.range(0, 10));
            // File
            YamlFile file = YamlFile.create(
                    new ByteArrayInputStream("a: 1.2".getBytes(StandardCharsets.UTF_8)), new ByteArrayInputStream("a: 1.3".getBytes(StandardCharsets.UTF_8)),
                    GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
            // Target version
            Version version = pattern.getVersion("1.2");
            // Assert
            assertEquals(version, UpdaterSettings.builder().setVersioning(new ManualVersioning(pattern, "1.2", "1.3")).build().getVersioning().getUserSectionVersion(file));
            assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, "1.2", "1.3").build().getVersioning().getUserSectionVersion(file));
            assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, Route.from("a")).build().getVersioning().getUserSectionVersion(file));
            assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, "a").build().getVersioning().getUserSectionVersion(file));
        } catch (
                IOException ex) {
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

}