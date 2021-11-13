package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelocatorTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));
    // Versions
    private static final Version VERSION_USER = PATTERN.getVersion("1.2"), VERSION_DEFAULT = PATTERN.getVersion("2.3");
    // Relocations
    private static final Map<String, Map<Path, Path>> RELOCATIONS = new HashMap<String, Map<Path, Path>>(){{
        put("1.0", new HashMap<Path, Path>(){{
            put(Path.fromSingleKey("d"), Path.fromSingleKey("e"));
        }});
        put("1.2", new HashMap<Path, Path>(){{
            put(Path.fromSingleKey("x"), Path.fromSingleKey("f"));
        }});
        put("1.3", new HashMap<Path, Path>(){{
            put(Path.fromSingleKey("x"), Path.fromSingleKey("g"));
            put(Path.fromSingleKey("y"), Path.fromSingleKey("x"));
            put(Path.fromSingleKey("j"), Path.fromSingleKey("k"));
        }});
        put("2.3", new HashMap<Path, Path>(){{
            put(Path.fromSingleKey("g"), Path.fromSingleKey("h"));
            put(Path.fromSingleKey("z"), Path.fromSingleKey("i"));
        }});
    }};

    @Test
    void apply() {
        // File
        YamlFile file = new YamlFile(new ByteArrayInputStream("x: a\ny: b\nz:\n  a: 1\n  b: 10".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT);
        // Create relocator
        Relocator relocator = new Relocator(file, VERSION_USER, VERSION_DEFAULT);
        // Apply
        relocator.apply(RELOCATIONS);
        // Assert
        assertEquals(file.get("h", null), "a");
        assertEquals(file.get("x", null), "b");
        assertEquals(file.get("i.a", null), 1);
        assertEquals(file.get("i.b", null), 10);
        assertEquals(file.getKeys().size(), 3);
        assertEquals(file.getSection("i").getKeys().size(), 2);
    }
}