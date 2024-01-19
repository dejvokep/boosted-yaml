/*
 * Copyright 2024 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.engine;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.Block;
import dev.dejvokep.boostedyaml.block.implementation.TerminatedBlock;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExtendedRepresenterTest {

    @Test
    void represent() throws IOException {
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setStringStyle(ScalarStyle.FOLDED).build(), UpdaterSettings.DEFAULT);
        document.set("a", ScalarStyle.PLAIN);
        document.set("b", "string");
        assertEquals("\"a\": !!org.snakeyaml.engine.v2.common.ScalarStyle 'PLAIN'\n\"b\": >-\n  string\n", document.dump());
    }

    @Test
    void applyKeyComments() {
        Node represented = new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN);
        assertEquals(represented, buildRepresenter().applyKeyComments(buildCommentedBlock(), represented));
        assertEquals("x", represented.getBlockComments().get(0).getValue());
        assertEquals("y", represented.getBlockComments().get(1).getValue());
        assertEquals("z", represented.getBlockComments().get(2).getValue());
        assertEquals("b", represented.getBlockComments().get(3).getValue());
        assertEquals("c", represented.getBlockComments().get(4).getValue());
        assertEquals(5, represented.getBlockComments().size());
        assertNull(represented.getInLineComments());
        assertNull(represented.getEndComments());
    }

    @Test
    void applyValueComments() {
        Node represented = new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN);
        assertEquals(represented, buildRepresenter().applyValueComments(buildCommentedBlock(), represented));
        assertEquals("a", represented.getBlockComments().get(0).getValue());
        assertEquals(1, represented.getBlockComments().size());
        assertNull(represented.getInLineComments());
        assertNull(represented.getEndComments());
    }

    @Test
    void representMappingEntry() {
        NodeTuple tuple = buildRepresenter().representMappingEntry(Collections.singletonMap("abc", 123).entrySet().iterator().next());
        assertEquals("abc", ((ScalarNode) tuple.getKeyNode()).getValue());
        assertEquals("123", ((ScalarNode) tuple.getValueNode()).getValue());
    }

    private ExtendedRepresenter buildRepresenter() {
        return new ExtendedRepresenter(GeneralSettings.DEFAULT, DumperSettings.DEFAULT);
    }

    private Block<Object> buildCommentedBlock() {
        Node keyNode = new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN);
        keyNode.setBlockComments(createCommentList("x", CommentType.BLOCK));
        keyNode.setInLineComments(createCommentList("y", CommentType.IN_LINE));
        keyNode.setEndComments(createCommentList("z", CommentType.BLOCK));

        Node valueNode = new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN);
        valueNode.setBlockComments(createCommentList("a", CommentType.BLOCK));
        valueNode.setInLineComments(createCommentList("b", CommentType.IN_LINE));
        valueNode.setEndComments(createCommentList("c", CommentType.BLOCK));

        return new TerminatedBlock(keyNode, valueNode, "");
    }

    private List<CommentLine> createCommentList(String value, CommentType commentType) {
        return new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), value, commentType));
        }};
    }

}