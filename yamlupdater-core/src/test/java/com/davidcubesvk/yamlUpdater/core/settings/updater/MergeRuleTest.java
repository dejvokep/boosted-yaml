package com.davidcubesvk.yamlUpdater.core.settings.updater;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeRuleTest {

    @Test
    void getFor() {
        assertEquals(MergeRule.getFor(false, false), MergeRule.MAPPINGS);
        assertEquals(MergeRule.getFor(true, false), MergeRule.MAPPING_AT_SECTION);
        assertEquals(MergeRule.getFor(false, true), MergeRule.SECTION_AT_MAPPING);
        assertNull(MergeRule.getFor(true, true));
    }
}