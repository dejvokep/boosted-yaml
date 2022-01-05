package dev.dejvokep.boostedyaml.settings.updater;

/**
 * Enum representing all situations during merging, used to specify rules.
 */
public enum MergeRule {

    /**
     * Represents a situation where block at a certain route in:
     * <ul>
     *     <li>user file is a section</li>
     *     <li>default file is a mapping</li>
     * </ul>
     * This situation is, during merging, also referred to as <i>section at mapping</i>.
     */
    SECTION_AT_MAPPING,

    /**
     * Represents a situation where block at a certain route in:
     * <ul>
     *     <li>user file is a mapping</li>
     *     <li>default file is a section</li>
     * </ul>
     * This situation is, during merging, also referred to as <i>mapping at section</i>.
     */
    MAPPING_AT_SECTION,

    /**
     * Represents a situation where block at a certain route is a mapping in both files.
     * <p>
     * This situation is, during merging, also referred to as <i>mapping at mapping</i>.
     */
    MAPPINGS;

    /**
     * Returns merge rule representing the given information.
     *
     * @param userValueIsSection    if the user value is a section (not a mapping)
     * @param defaultValueIsSection if the default value is a section (not a mapping)
     * @return the merge rule
     */
    public static MergeRule getFor(boolean userValueIsSection, boolean defaultValueIsSection) {
        return userValueIsSection ? defaultValueIsSection ? null : SECTION_AT_MAPPING : defaultValueIsSection ? MAPPING_AT_SECTION : MAPPINGS;
    }

}