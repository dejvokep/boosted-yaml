package com.davidcubesvk.yamlUpdater.core.updater;

import com.davidcubesvk.yamlUpdater.core.YamlFile;
import com.davidcubesvk.yamlUpdater.core.fvs.segment.Segment;
import com.davidcubesvk.yamlUpdater.core.route.Route;
import com.davidcubesvk.yamlUpdater.core.fvs.Pattern;
import com.davidcubesvk.yamlUpdater.core.fvs.Version;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RelocatorTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE), Segment.literal("."), Segment.range(0, 10));
    // Versions
    private static final Version VERSION_USER = Objects.requireNonNull(PATTERN.getVersion("1.2")), VERSION_DEFAULT = Objects.requireNonNull(PATTERN.getVersion("2.3"));
    // Relocations
    private static final Map<String, Map<Route, Route>> RELOCATIONS = new HashMap<String, Map<Route, Route>>(){{
        put("1.0", new HashMap<Route, Route>(){{
            put(Route.from("d"), Route.from("e"));
        }});
        put("1.2", new HashMap<Route, Route>(){{
            put(Route.from("x"), Route.from("f"));
        }});
        put("1.3", new HashMap<Route, Route>(){{
            put(Route.from("x"), Route.from("g"));
            put(Route.from("y"), Route.from("x"));
            put(Route.from("j"), Route.from("k"));
        }});
        put("2.3", new HashMap<Route, Route>(){{
            put(Route.from("g"), Route.from("h"));
            put(Route.from("z"), Route.from("i"));
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