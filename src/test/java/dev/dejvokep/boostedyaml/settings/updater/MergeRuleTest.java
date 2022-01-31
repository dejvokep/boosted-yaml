/*
 * Copyright 2022 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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