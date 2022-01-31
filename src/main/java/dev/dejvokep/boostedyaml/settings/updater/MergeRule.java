/*
 * Copyright 2021 https://dejvokep.dev/
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

/**
 * Enum representing all situations during merging, used to specify rules.
 */
public enum MergeRule {

    /**
     * Represents a situation where block at a certain route in:
     * <ul>
     *     <li>document is a section</li>
     *     <li>defaults is a mapping</li>
     * </ul>
     * This situation is, during merging, also referred to as <i>section at mapping</i>.
     */
    SECTION_AT_MAPPING,

    /**
     * Represents a situation where block at a certain route in:
     * <ul>
     *     <li>document is a mapping</li>
     *     <li>defaults is a section</li>
     * </ul>
     * This situation is, during merging, also referred to as <i>mapping at section</i>.
     */
    MAPPING_AT_SECTION,

    /**
     * Represents a situation where block at a certain route is a mapping in both the document and defaults.
     * <p>
     * This situation is, during merging, also referred to as <i>mapping at mapping</i>.
     */
    MAPPINGS;

    /**
     * Returns merge rule representing the given information.
     *
     * @param documentBlockIsSection if the document block is a section
     * @param defaultBlockIsSection  if the default block is a section
     * @return the merge rule
     */
    public static MergeRule getFor(boolean documentBlockIsSection, boolean defaultBlockIsSection) {
        return documentBlockIsSection ? defaultBlockIsSection ? null : SECTION_AT_MAPPING : defaultBlockIsSection ? MAPPING_AT_SECTION : MAPPINGS;
    }

}