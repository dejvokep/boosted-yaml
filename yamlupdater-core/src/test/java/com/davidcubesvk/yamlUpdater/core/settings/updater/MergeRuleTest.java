package com.davidcubesvk.yamlUpdater.core.settings.updater;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeRuleTest {

    @Test
    void getFor() {
        assertEquals(MergeRule.MAPPINGS, MergeRule.getFor(false, false));
        assertEquals(MergeRule.MAPPING_AT_SECTION, MergeRule.getFor(true, false));
        assertEquals(MergeRule.SECTION_AT_MAPPING, MergeRule.getFor(false, true));
        assertNull(MergeRule.getFor(true, true));
    }
}