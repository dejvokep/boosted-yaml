package dev.dejvokep.boostedyaml.updater.operators;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.ValueMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void apply() {
        try {
            YamlDocument document = YamlDocument.create(new ByteArrayInputStream("x: true\ny: abc\nz:\n  a: 1\n  b: 10".getBytes(StandardCharsets.UTF_8)));
            Mapper.apply(document, new HashMap<Route, ValueMapper>(){{
                put(Route.from("x"), ValueMapper.section((section, route) -> section.getOptionalBoolean(route).map(bool -> bool ? "yes" : "no").orElse("no")));
                put(Route.from("y"), ValueMapper.block(block -> block.getStoredValue().toString().length()));
                put(Route.from("z", "a"), ValueMapper.value(value -> value instanceof Integer ? ((Integer) value) + 1 : -1));
            }});
            assertEquals("yes", document.get("x", null));
            assertEquals(3, document.get("y", null));
            assertEquals(2, document.get("z.a", null));
            assertEquals(10, document.get("z.b", null));
            assertEquals(3, document.getKeys().size());
            assertEquals(2, document.getSection("z").getKeys().size());
        } catch (IOException ex) {
            fail(ex);
        }
    }

}