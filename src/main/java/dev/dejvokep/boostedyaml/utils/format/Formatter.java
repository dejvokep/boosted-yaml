package dev.dejvokep.boostedyaml.utils.format;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.nodes.Tag;

public interface Formatter<S, V> {

    @NotNull
    S format(@NotNull Tag tag, @NotNull V value, @NotNull NodeRole role, @NotNull S previous);

    @NotNull
    static <S, V> Formatter<S, V> identity() {
        return (tag, value, role, previous) -> previous;
    }

}
