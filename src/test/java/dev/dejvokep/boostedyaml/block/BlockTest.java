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
package dev.dejvokep.boostedyaml.block;

import dev.dejvokep.boostedyaml.block.implementation.TerminatedBlock;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void init() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node keyNode = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN), valueNode = new ScalarNode(Tag.STR, "abc", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> keyComments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "abc", CommentType.BLOCK));
            add(new CommentLine(Optional.empty(), Optional.empty(), "def", CommentType.BLOCK));
        }}, valueComments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "123", CommentType.IN_LINE));
            add(new CommentLine(Optional.empty(), Optional.empty(), "ghi", CommentType.IN_LINE));
        }};
        // Set comments
        keyNode.setBlockComments(new ArrayList<>(keyComments));
        valueNode.setEndComments(new ArrayList<>(valueComments));
        // Init
        block.init(keyNode, valueNode);
        // Assert
        List<CommentLine> result = new ArrayList<>(keyComments);
        result.addAll(valueComments);
        assertEquals(4, block.beforeKeyComments.size());
        for (int i = 0; i < 4; i++) {
            assertEquals(CommentType.BLOCK, block.beforeKeyComments.get(i).getCommentType());
            assertEquals(result.get(i).getValue(), block.beforeKeyComments.get(i).getValue());
        }
    }

    @Test
    void getKeyBlockComments() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node node = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> comments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "abc1", CommentType.BLOCK));
        }};
        // Set comments
        node.setBlockComments(comments);
        // Init
        block.init(node, null);
        // Assert
        assertEquals(comments, block.beforeKeyComments);
    }

    @Test
    void getKeyInlineComments() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node node = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> comments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "abc2", CommentType.IN_LINE));
        }};
        // Set comments
        node.setInLineComments(comments);
        // Init
        block.init(node, null);
        // Assert
        assertEquals(1, block.beforeKeyComments.size());
        assertEquals(CommentType.BLOCK, block.beforeKeyComments.get(0).getCommentType());
        assertEquals("abc2", block.beforeKeyComments.get(0).getValue());
    }

    @Test
    void getKeyEndComments() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node node = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> comments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "abc3", CommentType.BLANK_LINE));
        }};
        // Set comments
        node.setEndComments(comments);
        // Init
        block.init(node, null);
        // Assert
        assertEquals(comments, block.beforeKeyComments);
    }

    @Test
    void getValueBlockComments() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node node = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> comments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "def1", CommentType.BLOCK));
        }};
        // Set comments
        node.setBlockComments(comments);
        // Init
        block.init(null, node);
        // Assert
        assertEquals(comments, block.beforeValueComments);
    }

    @Test
    void getValueInlineComments() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node node = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> comments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "def2", CommentType.IN_LINE));
        }};
        // Set comments
        node.setInLineComments(comments);
        // Init
        block.init(null, node);
        // Assert
        assertEquals(1, block.beforeKeyComments.size());
        assertEquals(CommentType.BLOCK, block.beforeKeyComments.get(0).getCommentType());
        assertEquals("def2", block.beforeKeyComments.get(0).getValue());
    }

    @Test
    void getValueEndComments() {
        // Block
        Block<?> block = new TerminatedBlock(null, null);
        // Nodes
        Node node = new ScalarNode(Tag.INT, "7", ScalarStyle.PLAIN);
        // Comments
        List<CommentLine> comments = new ArrayList<CommentLine>(){{
            add(new CommentLine(Optional.empty(), Optional.empty(), "def3", CommentType.BLANK_LINE));
        }};
        // Set comments
        node.setEndComments(comments);
        // Init
        block.init(null, node);
        // Assert
        assertEquals(comments, block.beforeKeyComments);
    }

    @Test
    void getValue() {
        assertEquals(5, new TerminatedBlock(null, 5).getStoredValue());
    }
}