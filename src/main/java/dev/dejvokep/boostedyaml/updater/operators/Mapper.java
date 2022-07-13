package dev.dejvokep.boostedyaml.updater.operators;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.ValueMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Mapper {

    public static void apply(@NotNull Section section, @NotNull Map<Route, ValueMapper> mappers) {
        mappers.forEach(((route, mapper) -> section.getParent(route).ifPresent(parent -> {
            // Single key route
            Route key = Route.fromSingleKey(route.get(route.length() - 1));
            // There is no value
            if (!parent.getStoredValue().containsKey(key.get(0)))
                return;
            // Replace
            parent.set(key, mapper.map(parent, key));
        })));
    }

}