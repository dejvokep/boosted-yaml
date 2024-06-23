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
import dev.dejvokep.boostedyaml.block.Comments;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendedRepresenterTest {

    @Test
    void represent() throws IOException {
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setStringStyle(ScalarStyle.FOLDED).build(), UpdaterSettings.DEFAULT);
        document.set("a", ScalarStyle.PLAIN);
        document.set("b", "string");
        assertEquals("\"a\": !!org.snakeyaml.engine.v2.common.ScalarStyle 'PLAIN'\n\"b\": >-\n  string\n", document.dump());
    }

    @Test
    void representBlock() throws IOException {
        // Assert safety (no exceptions)
        String expected = "#before value root (1)\n" +
                "#before value root (2)\n" +
                "\n" +
                "#inline value root (1)\n" +
                "#inline value root (2)\n" +
                "#before key map (1)\n" +
                "#before key map (2)\n" +
                "\n" +
                "map: #inline key map (1)\n" +
                "     #inline key map (2)\n" +
                "  #after key map (1)\n" +
                "  #after key map (2)\n" +
                "\n" +
                "    #before value map (1)\n" +
                "  #before value map (2)\n" +
                "\n" +
                "    #inline value map (1)\n" +
                "  #inline value map (2)\n" +
                "  #before key map.k (1)\n" +
                "  #before key map.k (2)\n" +
                "\n" +
                "  k: #inline key map.k (1)\n" +
                "     #inline key map.k (2)\n" +
                "    #after key map.k (1)\n" +
                "    #after key map.k (2)\n" +
                "\n" +
                "        #before value map.k (1)\n" +
                "    #before value map.k (2)\n" +
                "\n" +
                "    v #inline value map.k (1)\n" +
                "      #inline value map.k (2)\n" +
                "  #after value map.k (1)\n" +
                "  #after value map.k (2)\n" +
                "\n" + // Unwanted double blank line (does not occur with user-created YAML, further investigation needed)
                "  \n" +
                "#after value map (1)\n" +
                "#after value map (2)\n" +
                "\n" +
                "#before key list (1)\n" +
                "#before key list (2)\n" +
                "\n" +
                "list: #inline key list (1)\n" +
                "      #inline key list (2)\n" +
                "  #after key list (1)\n" +
                "  #after key list (2)\n" +
                "\n" +
                "    #before value list (1)\n" +
                "  #before value list (2)\n" +
                "\n" +
                "    #inline value list (1)\n" +
                "  #inline value list (2)\n" +
                "  - a\n" +
                "  - b\n" +
                "#after value list (1)\n" +
                "#after value list (2)\n" +
                "\n" +
                "#before key str (1)\n" +
                "#before key str (2)\n" +
                "\n" +
                "str: #inline key str (1)\n" +
                "     #inline key str (2)\n" +
                "  #after key str (1)\n" +
                "  #after key str (2)\n" +
                "\n" +
                "    #before value str (1)\n" +
                "  #before value str (2)\n" +
                "\n" +
                "  val #inline value str (1)\n" +
                "      #inline value str (2)\n" +
                "#after value str (1)\n" +
                "#after value str (2)\n" +
                "\n" +
                "#after value root (1)\n" +
                "#after value root (2)\n" +
                "\n";
        YamlDocument document = createDocument();
        DumperSettings settings = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).build();
        String dump = document.dump(settings);
        assertEquals(expected, dump);

        // Assert reproducibility
        String yaml = "#before value root (1)\n" +
                "#before value root (2)\n" +
                "\n" +
                "#inline value root (1)\n" +
                "#inline value root (2)\n" +
                "#before key map (1)\n" +
                "#before key map (2)\n" +
                "\n" +
                "map: #inline key map (1)\n" +
                "     #inline key map (2)\n" +
                "  #after key map (1)\n" +
                "  #after key map (2)\n" +
                "\n" +
                "  #before value map (1)\n" +
                "  #before value map (2)\n" +
                "\n" +
                "  #inline value map (1)\n" +
                "  #inline value map (2)\n" +
                "  #before key map.k (1)\n" +
                "  #before key map.k (2)\n" +
                "\n" +
                "  k: #inline key map.k (1)\n" +
                "     #inline key map.k (2)\n" +
                "    #after key map.k (1)\n" +
                "    #after key map.k (2)\n" +
                "\n" +
                "    #before value map.k (1)\n" +
                "    #before value map.k (2)\n" +
                "\n" +
                "    v #inline value map.k (1)\n" +
                "      #inline value map.k (2)\n" +
                "  #after value map.k (1)\n" +
                "  #after value map.k (2)\n" +
                "\n" +
                "#after value map (1)\n" +
                "#after value map (2)\n" +
                "\n" +
                "#before key list (1)\n" +
                "#before key list (2)\n" +
                "\n" +
                "list: #inline key list (1)\n" +
                "      #inline key list (2)\n" +
                "  #after key list (1)\n" +
                "  #after key list (2)\n" +
                "\n" +
                "  #before value list (1)\n" +
                "  #before value list (2)\n" +
                "\n" +
                "  #inline value list (1)\n" +
                "  #inline value list (2)\n" +
                "  - a\n" +
                "  - b\n" +
                "#after value list (1)\n" +
                "#after value list (2)\n" +
                "\n" +
                "#before key str (1)\n" +
                "#before key str (2)\n" +
                "\n" +
                "str: #inline key str (1)\n" +
                "     #inline key str (2)\n" +
                "  #after key str (1)\n" +
                "  #after key str (2)\n" +
                "\n" +
                "  #before value str (1)\n" +
                "  #before value str (2)\n" +
                "\n" +
                "  val #inline value str (1)\n" +
                "      #inline value str (2)\n" +
                "#after value str (1)\n" +
                "#after value str (2)\n" +
                "\n" +
                "#after value root (1)\n" +
                "#after value root (2)\n" +
                "\n";
        expected = "#before value root (1)\n" +
                "#before value root (2)\n" +
                "\n" +
                "#inline value root (1)\n" +
                "#inline value root (2)\n" +
                "#before key map (1)\n" +
                "#before key map (2)\n" +
                "\n" +
                "map: #inline key map (1)\n" +
                "     #inline key map (2)\n" +
                "  #after key map (1)\n" +
                "  #after key map (2)\n" +
                "\n" +
                "    #before value map (1)\n" +
                "  #before value map (2)\n" +
                "\n" +
                "    #inline value map (1)\n" +
                "  #inline value map (2)\n" +
                "  #before key map.k (1)\n" +
                "  #before key map.k (2)\n" +
                "\n" +
                "  k: #inline key map.k (1)\n" +
                "     #inline key map.k (2)\n" +
                "    #after key map.k (1)\n" +
                "    #after key map.k (2)\n" +
                "\n" +
                "        #before value map.k (1)\n" +
                "    #before value map.k (2)\n" +
                "\n" +
                "    v #inline value map.k (1)\n" +
                "      #inline value map.k (2)\n" +
                "#after value map.k (1)\n" + // AFTER comments are loaded as BEFORE comments for the next node
                "#after value map.k (2)\n" +
                "\n" +
                "#after value map (1)\n" +
                "#after value map (2)\n" +
                "\n" +
                "#before key list (1)\n" +
                "#before key list (2)\n" +
                "\n" +
                "list: #inline key list (1)\n" +
                "      #inline key list (2)\n" +
                "  #after key list (1)\n" +
                "  #after key list (2)\n" +
                "\n" +
                "    #before value list (1)\n" +
                "  #before value list (2)\n" +
                "\n" +
                "    #inline value list (1)\n" +
                "  #inline value list (2)\n" +
                "  - a\n" +
                "  - b\n" +
                "#after value list (1)\n" +
                "#after value list (2)\n" +
                "\n" +
                "#before key str (1)\n" +
                "#before key str (2)\n" +
                "\n" +
                "str: #inline key str (1)\n" +
                "     #inline key str (2)\n" +
                "  #after key str (1)\n" +
                "  #after key str (2)\n" +
                "\n" +
                "    #before value str (1)\n" +
                "  #before value str (2)\n" +
                "\n" +
                "  val #inline value str (1)\n" +
                "      #inline value str (2)\n" +
                "#after value str (1)\n" +
                "#after value str (2)\n" +
                "\n" +
                "#after value root (1)\n" +
                "#after value root (2)\n" +
                "\n";
        assertEquals(expected, YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(settings));
        // Empty line comments add same level indentation to the element after the node (needs a PR to the engine)
    }

    @Test
    void representFlow() throws IOException {
        YamlDocument document = createDocument();
        DumperSettings settings = DumperSettings.builder().setFlowStyle(FlowStyle.FLOW).build();
        String dump = document.dump(settings);
        assertEquals("#before value root (1)\n" +
                "#before value root (2)\n" +
                "\n" +
                "{map: {k: v} #inline value map (1)\n" +
                "             #inline value map (2)\n" +
                ", list: [a, b] #inline value list (1)\n" +
                "               #inline value list (2)\n" +
                ", str: val} #inline value root (1)\n" +
                "            #inline value root (2)\n" +
                "#after value root (1)\n" +
                "#after value root (2)\n" +
                "\n", dump);
        assertEquals(dump, document.dump(settings));
        assertEquals("#before value root (1)\n" +
                "#before value root (2)\n" +
                "\n" +
                "{map: {k: v} #inline value map (1)\n" +
                "             #inline value map (2)\n" +
                ", list: [a, b] #inline value list (1)\n" +
                "               #inline value list (2)\n" +
                ", str: val} #inline value root (1)\n" +
                "            #inline value root (2)\n" +
                "#after value root (1)\n" +
                "#after value root (2)\n" +
                "\n", YamlDocument.create(new ByteArrayInputStream(dump.getBytes(StandardCharsets.UTF_8))).dump(settings));
    }

    @Test
    void representMappingEntry() throws IOException {
        NodeTuple tuple = new ExtendedRepresenter(GeneralSettings.DEFAULT, DumperSettings.DEFAULT).representMappingEntry(createDocument().getStoredValue().entrySet().iterator().next());
        assertEquals("map", ((ScalarNode) tuple.getKeyNode()).getValue());
        NodeTuple sub = ((MappingNode) tuple.getValueNode()).getValue().get(0);
        assertEquals("k", ((ScalarNode) sub.getKeyNode()).getValue());
        assertEquals("v", ((ScalarNode) sub.getValueNode()).getValue());
    }

    private YamlDocument createDocument() throws IOException {
        YamlDocument root = YamlDocument.create(new ByteArrayInputStream("map:\n  k: v\nlist:\n- a\n- b\nstr: val".getBytes(StandardCharsets.UTF_8)));

        addComments(root, "root");
        addComments(root.getBlock("map"), "map");
        addComments(root.getBlock("map.k"), "map.k");
        addComments(root.getBlock("list"), "list");
        addComments(root.getBlock("str"), "str");

        return root;
    }

    private void addComments(Block<?> block, String uid) {
        Comments.set(block, NodeRole.KEY, Comments.Position.BEFORE, createComments("before key " + uid, CommentType.BLOCK));
        Comments.set(block, NodeRole.KEY, Comments.Position.INLINE, createComments("inline key " + uid, CommentType.IN_LINE));
        Comments.set(block, NodeRole.KEY, Comments.Position.AFTER, createComments("after key " + uid, CommentType.BLOCK));
        Comments.set(block, NodeRole.VALUE, Comments.Position.BEFORE, createComments("before value " + uid, CommentType.BLOCK));
        Comments.set(block, NodeRole.VALUE, Comments.Position.INLINE, createComments("inline value " + uid, CommentType.IN_LINE));
        Comments.set(block, NodeRole.VALUE, Comments.Position.AFTER, createComments("after value " + uid, CommentType.BLOCK));
    }

    private List<CommentLine> createComments(String uid, CommentType type) {
        List<CommentLine> comments = new ArrayList<>(3);

        comments.add(new CommentLine(Optional.empty(), Optional.empty(), String.format("%s (%d)", uid, 1), type));
        comments.add(new CommentLine(Optional.empty(), Optional.empty(), String.format("%s (%d)", uid, 2), type));

        if (type == CommentType.BLOCK)
            comments.add(new CommentLine(Optional.empty(), Optional.empty(), "", CommentType.BLANK_LINE));

        return comments;
    }

}