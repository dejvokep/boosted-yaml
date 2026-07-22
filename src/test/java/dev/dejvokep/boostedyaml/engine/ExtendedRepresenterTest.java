/*
 * Copyright 2026 https://dejvokep.dev/
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
import dev.dejvokep.boostedyaml.utils.format.Formatter;
import dev.dejvokep.boostedyaml.utils.format.NodeRole;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtendedRepresenterTest {

    @Test
    void represent() throws IOException {
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setScalarFormatter(new Formatter<ScalarStyle, String>() {
            @NotNull
            @Override
            public ScalarStyle format(@NotNull Tag tag, @NotNull String value, @NotNull NodeRole role, @NotNull ScalarStyle def) {
                return tag == Tag.STR ? ScalarStyle.FOLDED : def;
            }
        }).build(), UpdaterSettings.DEFAULT);

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

    @Test
    void representScalar() throws IOException {
        DumperSettings settings = DumperSettings.builder().setScalarStyle(ScalarStyle.PLAIN).setScalarFormatter(((tag, value, role, def) -> tag == Tag.INT ? ScalarStyle.SINGLE_QUOTED : tag == Tag.STR && (value.equals("quoted") || (role == NodeRole.KEY && value.equals("k4"))) ? ScalarStyle.DOUBLE_QUOTED : def)).build();
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("k1: quoted\nk2: plain\nk3: 1\na:\n  k4: k4".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("k1: \"quoted\"\nk2: plain\nk3: !!int '1'\na:\n  \"k4\": k4\n", document.dump());
    }

    @Test
    void representScalarPreserveStyle() throws IOException {
        String yaml = "k1: \"double\"\nk2: 'single'\nk3: plain\n\"k4\": v\n";

        // On: keep the original styles even though string style says single quote everything
        DumperSettings preserve = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setStringStyle(ScalarStyle.SINGLE_QUOTED).setPreserveScalarStyle(true).build();
        assertEquals(yaml, YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(preserve));

        // Off: string style takes over
        DumperSettings off = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setStringStyle(ScalarStyle.SINGLE_QUOTED).setPreserveScalarStyle(false).build();
        assertEquals("'k1': 'double'\n'k2': 'single'\n'k3': 'plain'\n'k4': 'v'\n", YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(off));

        // A formatter still has the last word over the preserved style
        DumperSettings override = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setPreserveScalarStyle(true).setScalarFormatter((tag, value, role, def) -> ScalarStyle.PLAIN).build();
        assertEquals("k1: double\nk2: single\nk3: plain\nk4: v\n", YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(override));
    }

    @Test
    void representPreserveStyleAfterSet() throws IOException {
        DumperSettings preserve = DumperSettings.builder().setPreserveScalarStyle(true).build();
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("\"key\": \"old\"\n".getBytes(StandardCharsets.UTF_8)), preserve);
        document.set("key", "new");
        // A value replaced via a set keeps both the key's and the previous value's style
        assertEquals("\"key\": \"new\"\n", document.dump());
    }

    @Test
    void representPreserveListStyleAfterSet() throws IOException {
        String yaml = "items:\n- 'a'\n- \"b\"\n";
        DumperSettings preserve = DumperSettings.builder().setPreserveScalarStyle(true).setPreserveFlowStyle(true).build();

        // Same size: each element keeps the style of the element it replaced, positionally
        YamlDocument same = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), preserve);
        same.set("items", Arrays.asList("x", "y"));
        assertEquals("items:\n- 'x'\n- \"y\"\n", same.dump());

        // Different size: cannot align, so the whole list falls back to the configured style
        YamlDocument diff = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), preserve);
        diff.set("items", Arrays.asList("x", "y", "z"));
        assertEquals("items:\n- x\n- y\n- z\n", diff.dump());
    }

    @Test
    void representPreserveFlowStyle() throws IOException {
        String yaml = "flowList: [a, b]\nblockList:\n- c\n- d\nflowMap: {x: 1}\nblockMap:\n  y: 2\n";

        // On: each collection keeps the style it was loaded with, even though the configured style is BLOCK
        DumperSettings preserve = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setPreserveFlowStyle(true).build();
        assertEquals(yaml, YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(preserve));

        // Off: everything drops to the configured block style
        DumperSettings off = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setPreserveFlowStyle(false).build();
        assertEquals("flowList:\n- a\n- b\nblockList:\n- c\n- d\nflowMap:\n  x: 1\nblockMap:\n  y: 2\n", YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(off));

        // A formatter still has the last word over the preserved style
        DumperSettings override = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setPreserveFlowStyle(true).setSequenceFormatter((tag, value, role, def) -> FlowStyle.BLOCK).build();
        assertEquals("flowList:\n- a\n- b\nblockList:\n- c\n- d\nflowMap: {x: 1}\nblockMap:\n  y: 2\n", YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).dump(override));
    }

    @Test
    void representPreserveNestedStyle() throws IOException {
        // Styles nested inside sequences and mappings (not stored as blocks of their own) are preserved too
        String yaml = "name: \"double quoted\"\n" +
                "nick: 'single quoted'\n" +
                "plain: value\n" +
                "count: 5\n" +
                "messages:\n" +
                "- \"first\"\n" +
                "- \"second\"\n" +
                "- 'third'\n" +
                "tags: [alpha, beta]\n" +
                "nested:\n" +
                "  key: \"quoted\"\n" +
                "  flow: {a: 1, b: 2}\n" +
                "matrix:\n" +
                "- [1, 2]\n" +
                "- [3, 4]\n";

        DumperSettings preserve = DumperSettings.builder().setPreserveScalarStyle(true).setPreserveFlowStyle(true).build();
        assertEquals(yaml, YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)), preserve).dump());
    }

    @Test
    void representSequence() throws IOException {
        DumperSettings settings = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setSequenceFormatter(((tag, value, role, def) -> value.iterator().next().toString().equals("a") ? FlowStyle.FLOW : def)).build();
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("flow:\n- a\n- b\n- c\nblock: [d, e, f]".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("flow: [a, b, c]\nblock:\n- d\n- e\n- f\n", document.dump());

        document = YamlDocument.create(new ByteArrayInputStream("flow:\n- a\n- b\n- c\n- [d, e, f]".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("flow: [a, b, c, [d, e, f]]\n", document.dump());
        document = YamlDocument.create(new ByteArrayInputStream("block:\n- d\n- e\n- f\n- [a, b, c]".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("block:\n- d\n- e\n- f\n- [a, b, c]\n", document.dump());
    }

    @Test
    void representMapping() throws IOException {
        DumperSettings settings = DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).setMappingFormatter(((tag, value, role, def) -> value.containsKey("flow") ? FlowStyle.FLOW : def)).build();
        YamlDocument document = YamlDocument.create(new ByteArrayInputStream("k1:\n  flow: a\nk2: {block: b}".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("k1: {flow: a}\nk2:\n  block: b\n", document.dump());

        document = YamlDocument.create(new ByteArrayInputStream("flow: {block: a}".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("{flow: {block: a}}\n", document.dump());
        document = YamlDocument.create(new ByteArrayInputStream("block: {flow: a}".getBytes(StandardCharsets.UTF_8)), settings);
        assertEquals("block: {flow: a}\n", document.dump());
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