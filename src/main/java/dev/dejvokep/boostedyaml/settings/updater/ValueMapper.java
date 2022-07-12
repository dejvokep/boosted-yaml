package dev.dejvokep.boostedyaml.settings.updater;

import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ValueMapper {

    @Nullable
    default Object map(@NotNull Section section, @NotNull Route key) {
        return map(section.get(key));
    }

    @Nullable
    default Object map(@NotNull Block<?> block) {
        return map(block.getStoredValue());
    }

    @Nullable
    default Object map(@Nullable Object value) {
        return value;
    }

    static ValueMapper section(BiFunction<Section, Route, Object> mapper) {
        return new ValueMapper() {
            @Override
            public Object map(@NotNull Section section, @NotNull Route key) {
                return mapper.apply(section, key);
            }
        };
    }

    static ValueMapper block(Function<Block<?>, Object> mapper) {
        return new ValueMapper() {
            @Override
            public Object map(@NotNull Block<?> block) {
                return mapper.apply(block);
            }
        };
    }

    static ValueMapper value(Function<Object, Object> mapper) {
        return new ValueMapper() {
            @Override
            public Object map(@Nullable Object value) {
                return mapper.apply(value);
            }
        };
    }

}