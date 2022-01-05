package dev.dejvokep.boostedyaml.settings.updater;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeRuleTest {

    @Test
    void getFor() {
        assertEquals(MergeRule.MAPPINGS, MergeRule.getFor(false, false));
        assertEquals(MergeRule.MAPPING_AT_SECTION, MergeRule.getFor(false, true));
        assertEquals(MergeRule.SECTION_AT_MAPPING, MergeRule.getFor(true, false));
        assertNull(MergeRule.getFor(true, true));
    }
}