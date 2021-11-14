package com.davidcubesvk.yamlUpdater.core.settings.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.block.Section;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.dumper.DumperSettings;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.AutomaticVersioning;
import com.davidcubesvk.yamlUpdater.core.versioning.wrapper.ManualVersioning;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
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
                .setKeep(new HashMap<String, Set<Path>>() {{
                    put("1.2", new HashSet<Path>() {{
                        add(Path.fromSingleKey("a"));
                    }});
                    put("1.3", new HashSet<Path>() {{
                        add(Path.fromSingleKey("c"));
                    }});
                }})
                .setStrKeep(new HashMap<String, Set<String>>() {{
                    put("1.2", new HashSet<String>() {{
                        add("b");
                    }});
                    put("1.4", new HashSet<String>() {{
                        add("d");
                    }});
                }})
                .setKeep("1.5", new HashSet<Path>() {{
                    add(Path.fromSingleKey("e"));
                }})
                .setStrKeep("1.5", new HashSet<String>() {{
                    add("f");
                }}).build();
        // Map
        Map<String, Set<Path>> paths = settings.getKeep('.');
        // Assert
        assertEquals(new HashSet<Path>() {{
            add(Path.fromSingleKey("a"));
            add(Path.fromSingleKey("b"));
        }}, paths.get("1.2"));
        assertEquals(new HashSet<Path>() {{
            add(Path.fromSingleKey("c"));
        }}, paths.get("1.3"));
        assertEquals(new HashSet<Path>() {{
            add(Path.fromSingleKey("d"));
        }}, paths.get("1.4"));
        assertEquals(new HashSet<Path>() {{
            add(Path.fromSingleKey("e"));
            add(Path.fromSingleKey("f"));
        }}, paths.get("1.5"));
        assertEquals(4, paths.keySet().size());
    }

    @Test
    void getRelocations() {
        // Build
        UpdaterSettings settings = UpdaterSettings.builder()
                .setRelocations(new HashMap<String, Map<Path, Path>>() {{
                    put("1.2", new HashMap<Path, Path>() {{
                        put(Path.fromSingleKey("a"), Path.fromSingleKey("g"));
                    }});
                    put("1.3", new HashMap<Path, Path>() {{
                        put(Path.fromSingleKey("c"), Path.fromSingleKey("i"));
                    }});
                }})
                .setStrRelocations(new HashMap<String, Map<String, String>>() {{
                    put("1.2", new HashMap<String, String>() {{
                        put("b", "h");
                    }});
                    put("1.4", new HashMap<String, String>() {{
                        put("d", "j");
                    }});
                }})
                .setRelocations("1.5", new HashMap<Path, Path>() {{
                    put(Path.fromSingleKey("e"), Path.fromSingleKey("k"));
                }})
                .setStrRelocations("1.5", new HashMap<String, String>() {{
                    put("f", "l");
                }}).build();
        // Map
        Map<String, Map<Path, Path>> relocations = settings.getRelocations('.');
        // Assert
        assertEquals(new HashMap<Path, Path>() {{
            put(Path.fromSingleKey("a"), Path.fromSingleKey("g"));
            put(Path.fromSingleKey("b"), Path.fromSingleKey("h"));
        }}, relocations.get("1.2"));
        assertEquals(new HashMap<Path, Path>() {{
            put(Path.fromSingleKey("c"), Path.fromSingleKey("i"));
        }}, relocations.get("1.3"));
        assertEquals(new HashMap<Path, Path>() {{
            put(Path.fromSingleKey("d"), Path.fromSingleKey("j"));
        }}, relocations.get("1.4"));
        assertEquals(new HashMap<Path, Path>() {{
            put(Path.fromSingleKey("e"), Path.fromSingleKey("k"));
            put(Path.fromSingleKey("f"), Path.fromSingleKey("l"));
        }}, relocations.get("1.5"));
        assertEquals(4, relocations.keySet().size());
    }

    @Test
    void getVersioning() {
        // Pattern
        Pattern pattern = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));
        // File
        YamlFile file = new YamlFile(
                new ByteArrayInputStream("a: 1.2".getBytes(StandardCharsets.UTF_8)), new ByteArrayInputStream("a: 1.3".getBytes(StandardCharsets.UTF_8)),
                GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
        // Target version
        Version version = pattern.getVersion("1.2");
        // Assert
        assertEquals(version, UpdaterSettings.builder().setVersioning(new ManualVersioning(pattern, "1.2", "1.3")).build().getVersioning().getUserSectionVersion(file));
        assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, "1.2", "1.3").build().getVersioning().getUserSectionVersion(file));
        assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, Path.fromSingleKey("a")).build().getVersioning().getUserSectionVersion(file));
        assertEquals(version, UpdaterSettings.builder().setVersioning(pattern, "a").build().getVersioning().getUserSectionVersion(file));
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