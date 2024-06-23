package dev.dejvokep.boostedyaml.utils.format;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.nodes.Tag;

/**
 * An interface used to format (style) nodes.
 *
 * @param <S> the style type of the node
 * @param <V> the value type
 */
public interface Formatter<S, V> {

    /**
     * Returns format to use for the given node.
     *
     * @param tag   the actual datatype the node is representing
     * @param value value of the node (for scalars, this is a string-based representation)
     * @param role  role of the node
     * @param def   the default style (as configured by the respective setting)
     * @return the style to use for the node
     */
    @NotNull
    S format(@NotNull Tag tag, @NotNull V value, @NotNull NodeRole role, @NotNull S def);

    /**
     * Returns a formatter which returns the default style, as given to the method (as configured by the respective
     * setting).
     *
     * @param <S> the style type of the node
     * @param <V> the value type
     * @return the identity formatter
     */
    @NotNull
    static <S, V> Formatter<S, V> identity() {
        return (tag, value, role, def) -> def;
    }

}
