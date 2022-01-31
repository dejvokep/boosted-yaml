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
package dev.dejvokep.boostedyaml.fvs.versioning;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.fvs.Pattern;
import dev.dejvokep.boostedyaml.fvs.segment.Segment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ManualVersioningTest {

    // Pattern
    private static final Pattern PATTERN = new Pattern(Segment.range(1, Integer.MAX_VALUE), Segment.literal("."), Segment.range(0, 10));
    // Versioning
    private static final ManualVersioning VERSIONING = new ManualVersioning(PATTERN, "1.2", "1.4");

    @Test
    void getDefSectionVersion() throws IOException {
        assertEquals(PATTERN.getVersion("1.4"), VERSIONING.getDefaultsVersion(createFile()));
    }

    @Test
    void getUserSectionVersion() throws IOException {
        assertEquals(PATTERN.getVersion("1.2"), VERSIONING.getDocumentVersion(createFile()));
    }

    @Test
    void getOldest() {
        assertEquals(PATTERN.getFirstVersion(), VERSIONING.getFirstVersion());
    }

    private YamlDocument createFile() throws IOException {
        return YamlDocument.create(new ByteArrayInputStream("x: 1.2\ny: true".getBytes(StandardCharsets.UTF_8)));
    }

}