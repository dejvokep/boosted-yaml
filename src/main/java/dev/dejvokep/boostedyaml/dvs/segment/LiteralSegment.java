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
package dev.dejvokep.boostedyaml.dvs.segment;

/**
 * Represents an immutable segment constructed directly from an array of elements.
 */
public class LiteralSegment implements Segment {

    //The elements
    private final String[] elements;

    /**
     * Creates a segment with the given elements.
     *
     * @param elements the elements
     */
    public LiteralSegment(String... elements) {
        this.elements = elements;
    }

    @Override
    public int parse(String versionId, int index) {
        //Go through all indexes
        for (int i = 0; i < elements.length; i++) {
            //If the same
            if (versionId.startsWith(elements[i], index))
                //Set
                return i;
        }

        //Cannot parse
        return -1;
    }

    @Override
    public String getElement(int index) {
        return elements[index];
    }

    @Override
    public int getElementLength(int index) {
        return elements[index].length();
    }

    @Override
    public int length() {
        return elements.length;
    }
}