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
package dev.dejvokep.boostedyaml.block;

import dev.dejvokep.boostedyaml.block.implementation.TerminalBlock;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommentsTest {

    @Test
    void get() {
        //Create block
        Block<?> block = new TerminalBlock(null, null);
        //Add
        Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, Comments.create("comment", Comments.Position.BEFORE));
        //Assert
        List<CommentLine> comments = Comments.get(block, Comments.NodeType.KEY, Comments.Position.BEFORE);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("comment", comments.get(0).getValue());
    }

    @Test
    void set() {
        //Create block
        Block<?> block = new TerminalBlock(null, null);
        //Set
        Comments.set(block, Comments.NodeType.KEY, Comments.Position.BEFORE, new ArrayList<CommentLine>(){{
            add(Comments.create("comment", Comments.Position.BEFORE));
        }});
        //Assert
        List<CommentLine> comments = Comments.get(block, Comments.NodeType.KEY, Comments.Position.BEFORE);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("comment", comments.get(0).getValue());
    }

    @Test
    void remove() {
        //Create block
        Block<?> block = new TerminalBlock(null, null);
        //Add
        Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, Comments.create("comment", Comments.Position.BEFORE));
        //Remove
        Comments.remove(block, Comments.NodeType.KEY, Comments.Position.BEFORE);
        //Assert
        assertNull(Comments.get(block, Comments.NodeType.KEY, Comments.Position.BEFORE));
    }

    @Test
    void add() {
        //Create block
        Block<?> block = new TerminalBlock(null, null);
        //Add
        Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, new ArrayList<CommentLine>(){{
            add(Comments.create("comment1", Comments.Position.BEFORE));
            add(Comments.create("comment2", Comments.Position.BEFORE));
        }});
        Comments.add(block, Comments.NodeType.KEY, Comments.Position.BEFORE, Comments.create("comment3", Comments.Position.BEFORE));
        //Assert
        List<CommentLine> comments = Comments.get(block, Comments.NodeType.KEY, Comments.Position.BEFORE);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        assertEquals("comment1", comments.get(0).getValue());
        assertEquals("comment2", comments.get(1).getValue());
        assertEquals("comment3", comments.get(2).getValue());
    }

    @Test
    void create() {
        assertComment(new CommentLine(Optional.empty(), Optional.empty(), "comment", CommentType.BLOCK), Comments.create("comment", Comments.Position.BEFORE));
        assertComment(new CommentLine(Optional.empty(), Optional.empty(), "comment", CommentType.BLOCK), Comments.create("comment", Comments.Position.AFTER));
        assertComment(new CommentLine(Optional.empty(), Optional.empty(), "comment", CommentType.IN_LINE), Comments.create("comment", Comments.Position.INLINE));
    }

    private void assertComment(CommentLine expected, CommentLine actual) {
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getCommentType(), actual.getCommentType());
    }
}