package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.path.Path;
import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.settings.loader.LoaderSettings;
import com.davidcubesvk.yamlUpdater.core.versioning.Pattern;
import com.davidcubesvk.yamlUpdater.core.versioning.Version;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RelocatorTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(new Pattern.Part(1, 100), new Pattern.Part("."), new Pattern.Part(0, 10));
    // Versions
    private static final Version VERSION_USER = PATTERN.getVersion("1.2"), VERSION_DEFAULT = PATTERN.getVersion("2.3");
    // Relocations
    private static final Map<String, Map<Path, Path>> RELOCATIONS = new HashMap<String, Map<Path, Path>>(){{
        put("1.0", new HashMap<Path, Path>(){{
            put(Path.from("d"), Path.from("e"));
        }});
        put("1.2", new HashMap<Path, Path>(){{
            put(Path.from("x"), Path.from("f"));
        }});
        put("1.3", new HashMap<Path, Path>(){{
            put(Path.from("x"), Path.from("g"));
            put(Path.from("y"), Path.from("x"));
            put(Path.from("j"), Path.from("k"));
        }});
        put("2.3", new HashMap<Path, Path>(){{
            put(Path.from("g"), Path.from("h"));
            put(Path.from("z"), Path.from("i"));
        }});
    }};

    @Test
    void apply() {
        try {
            // File
            YamlFile file = YamlFile.create(new ByteArrayInputStream("x: a\ny: b\nz:\n  a: 1\n  b: 10".getBytes(StandardCharsets.UTF_8)));
            // Create relocator
            Relocator relocator = new Relocator(file, VERSION_USER, VERSION_DEFAULT);
            // Apply
            relocator.apply(RELOCATIONS);
            // Assert
            assertEquals("a", file.get("h", null));
            assertEquals("b", file.get("x", null));
            assertEquals(1, file.get("i.a", null));
            assertEquals(10, file.get("i.b", null));
            assertEquals(3, file.getKeys().size());
            assertEquals(2, file.getSection("i").getKeys().size());
        } catch (IOException ex) {
            fail(ex);
        }
    }
}