package dev.dejvokep.boostedyaml.settings;

/**
 * A marker interface implemented by all settings defined by the library.
 * <p>
 * Should you create custom settings implementations, always extend existing implementations of this class defined by
 * the library, as objects not implementing any of these will be rejected by
 * {@link dev.dejvokep.boostedyaml.YamlDocument} methods.
 */
public interface Settings {
}