package com.davidcubesvk.yamlUpdater.core.settings.dumper;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;
import org.snakeyaml.engine.v2.serializer.NumberAnchorGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class DumperSettingsTest {

    @Test
    void buildEngineSettings() {
        Supplier<AnchorGenerator> generatorSupplier = () -> new NumberAnchorGenerator(0);
        assertTrue(DumperSettings.builder().setAnchorGenerator(generatorSupplier).build().buildEngineSettings().getAnchorGenerator() instanceof NumberAnchorGenerator);
        // Canonical form
        assertTrue(DumperSettings.builder().setCanonicalForm(true).build().buildEngineSettings().isCanonical());
        assertFalse(DumperSettings.builder().setCanonicalForm(false).build().buildEngineSettings().isCanonical());
        // Start marker
        assertTrue(DumperSettings.builder().setStartMarker(true).build().buildEngineSettings().isExplicitStart());
        assertFalse(DumperSettings.builder().setStartMarker(false).build().buildEngineSettings().isExplicitStart());
        // End marker
        assertTrue(DumperSettings.builder().setEndMarker(true).build().buildEngineSettings().isExplicitEnd());
        assertFalse(DumperSettings.builder().setEndMarker(false).build().buildEngineSettings().isExplicitEnd());
        // Multiline style
        assertTrue(DumperSettings.builder().setMultilineStyle(true).build().buildEngineSettings().isMultiLineFlow());
        assertFalse(DumperSettings.builder().setMultilineStyle(false).build().buildEngineSettings().isMultiLineFlow());
        // Escape unprintable
        assertEquals(DumperSettings.builder().setEscapeUnprintable(true).build().buildEngineSettings().getNonPrintableStyle(), NonPrintableStyle.ESCAPE);
        assertEquals(DumperSettings.builder().setEscapeUnprintable(false).build().buildEngineSettings().getNonPrintableStyle(), NonPrintableStyle.BINARY);
        // Unprintable style
        assertEquals(DumperSettings.builder().setUnprintableStyle(NonPrintableStyle.ESCAPE).build().buildEngineSettings().getNonPrintableStyle(), NonPrintableStyle.ESCAPE);
        assertEquals(DumperSettings.builder().setUnprintableStyle(NonPrintableStyle.BINARY).build().buildEngineSettings().getNonPrintableStyle(), NonPrintableStyle.BINARY);
        // Encoding
        assertTrue(DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build().buildEngineSettings().isUseUnicodeEncoding());
        assertFalse(DumperSettings.builder().setEncoding(DumperSettings.Encoding.ASCII).build().buildEngineSettings().isUseUnicodeEncoding());
        // Flow style
        assertEquals(DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).build().buildEngineSettings().getDefaultFlowStyle(), FlowStyle.BLOCK);
        assertEquals(DumperSettings.builder().setFlowStyle(FlowStyle.FLOW).build().buildEngineSettings().getDefaultFlowStyle(), FlowStyle.FLOW);
        // Scalar style
        assertEquals(DumperSettings.builder().setScalarStyle(ScalarStyle.LITERAL).build().buildEngineSettings().getDefaultScalarStyle(), ScalarStyle.LITERAL);
        assertEquals(DumperSettings.builder().setScalarStyle(ScalarStyle.DOUBLE_QUOTED).build().buildEngineSettings().getDefaultScalarStyle(), ScalarStyle.DOUBLE_QUOTED);
        // Scalar resolver
        ScalarResolver scalarResolver = new JsonScalarResolver();
        assertEquals(DumperSettings.builder().setScalarResolver(scalarResolver).build().buildEngineSettings().getScalarResolver(), scalarResolver);
        // Line break
        assertEquals(DumperSettings.builder().setLineBreak("\r\n").build().buildEngineSettings().getBestLineBreak(), "\r\n");
        // Indentation
        assertEquals(DumperSettings.builder().setIndentation(5).build().buildEngineSettings().getIndent(), 5);
        assertEquals(DumperSettings.builder().setIndentation(7).build().buildEngineSettings().getIndent(), 7);
        // Indicator indentation
        DumpSettings dumpSettings = DumperSettings.builder().setIndicatorIndentation(5).build().buildEngineSettings();
        assertEquals(dumpSettings.getIndicatorIndent(), 5);
        assertTrue(dumpSettings.getIndentWithIndicator());
        dumpSettings = DumperSettings.builder().setIndicatorIndentation(0).build().buildEngineSettings();
        assertEquals(dumpSettings.getIndicatorIndent(), 0);
        assertFalse(dumpSettings.getIndentWithIndicator());
        // Line width
        assertEquals(DumperSettings.builder().setLineWidth(5).build().buildEngineSettings().getWidth(), 5);
        assertEquals(DumperSettings.builder().setLineWidth(0).build().buildEngineSettings().getWidth(), Integer.MAX_VALUE);
        // Simple key length
        assertEquals(DumperSettings.builder().setMaxSimpleKeyLength(5).build().buildEngineSettings().getMaxSimpleKeyLength(), 5+6);
        assertEquals(DumperSettings.builder().setMaxSimpleKeyLength(0).build().buildEngineSettings().getMaxSimpleKeyLength(), 1024);
        // Root tag
        assertEquals(DumperSettings.builder().setRootTag(Tag.INT).build().buildEngineSettings().getExplicitRootTag(), Optional.of(Tag.INT));
        assertEquals(DumperSettings.builder().setRootTag(null).build().buildEngineSettings().getExplicitRootTag(), Optional.empty());
        // Yaml directive
        SpecVersion yamlDirective = new SpecVersion(1, 1);
        assertEquals(DumperSettings.builder().setYamlDirective(yamlDirective).build().buildEngineSettings().getYamlDirective(), Optional.of(yamlDirective));
        assertEquals(DumperSettings.builder().setYamlDirective(null).build().buildEngineSettings().getYamlDirective(), Optional.empty());
        // Tag directive
        Map<String, String> tagDirectives = new HashMap<String, String>(){{
            put("!handle!", "prefix");
        }};
        assertEquals(DumperSettings.builder().setTagDirectives(tagDirectives).build().buildEngineSettings().getTagDirective(), tagDirectives);
    }
}