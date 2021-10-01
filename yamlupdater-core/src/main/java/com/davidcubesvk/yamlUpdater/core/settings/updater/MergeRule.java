package com.davidcubesvk.yamlUpdater.core.settings.updater;

public enum MergeRule {

    SECTION_AT_MAPPING, MAPPING_AT_SECTION, MAPPINGS;

    public static MergeRule getFor(boolean userValueIsSection, boolean defaultValueIsSection) {
        //If user value is section
        if (userValueIsSection)
            return defaultValueIsSection ? null : SECTION_AT_MAPPING;
        else
            return defaultValueIsSection ? MAPPING_AT_SECTION : MAPPINGS;
    }

}