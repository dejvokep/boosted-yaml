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
        assertEquals(NonPrintableStyle.ESCAPE, DumperSettings.builder().setEscapeUnprintable(true).build().buildEngineSettings().getNonPrintableStyle());
        assertEquals(NonPrintableStyle.BINARY, DumperSettings.builder().setEscapeUnprintable(false).build().buildEngineSettings().getNonPrintableStyle());
        // Unprintable style
        assertEquals(NonPrintableStyle.ESCAPE, DumperSettings.builder().setUnprintableStyle(NonPrintableStyle.ESCAPE).build().buildEngineSettings().getNonPrintableStyle());
        assertEquals(NonPrintableStyle.BINARY, DumperSettings.builder().setUnprintableStyle(NonPrintableStyle.BINARY).build().buildEngineSettings().getNonPrintableStyle());
        // Encoding
        assertTrue(DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build().buildEngineSettings().isUseUnicodeEncoding());
        assertFalse(DumperSettings.builder().setEncoding(DumperSettings.Encoding.ASCII).build().buildEngineSettings().isUseUnicodeEncoding());
        // Flow style
        assertEquals(FlowStyle.BLOCK, DumperSettings.builder().setFlowStyle(FlowStyle.BLOCK).build().buildEngineSettings().getDefaultFlowStyle());
        assertEquals(FlowStyle.FLOW, DumperSettings.builder().setFlowStyle(FlowStyle.FLOW).build().buildEngineSettings().getDefaultFlowStyle());
        // Scalar style
        assertEquals(ScalarStyle.LITERAL, DumperSettings.builder().setScalarStyle(ScalarStyle.LITERAL).build().buildEngineSettings().getDefaultScalarStyle());
        assertEquals(ScalarStyle.DOUBLE_QUOTED, DumperSettings.builder().setScalarStyle(ScalarStyle.DOUBLE_QUOTED).build().buildEngineSettings().getDefaultScalarStyle());
        // Scalar resolver
        ScalarResolver scalarResolver = new JsonScalarResolver();
        assertEquals(scalarResolver, DumperSettings.builder().setScalarResolver(scalarResolver).build().buildEngineSettings().getScalarResolver());
        // Line break
        assertEquals("\r\n", DumperSettings.builder().setLineBreak("\r\n").build().buildEngineSettings().getBestLineBreak());
        // Indentation
        assertEquals(5, DumperSettings.builder().setIndentation(5).build().buildEngineSettings().getIndent());
        assertEquals(7, DumperSettings.builder().setIndentation(7).build().buildEngineSettings().getIndent());
        // Indicator indentation
        DumpSettings dumpSettings = DumperSettings.builder().setIndicatorIndentation(5).build().buildEngineSettings();
        assertEquals(5, dumpSettings.getIndicatorIndent());
        assertTrue(dumpSettings.getIndentWithIndicator());
        dumpSettings = DumperSettings.builder().setIndicatorIndentation(-1).build().buildEngineSettings();
        assertEquals(0, dumpSettings.getIndicatorIndent());
        assertFalse(dumpSettings.getIndentWithIndicator());
        // Line width
        assertEquals(5, DumperSettings.builder().setLineWidth(5).build().buildEngineSettings().getWidth());
        assertEquals(Integer.MAX_VALUE, DumperSettings.builder().setLineWidth(-1).build().buildEngineSettings().getWidth());
        // Simple key length
        assertEquals(5+6, DumperSettings.builder().setMaxSimpleKeyLength(5).build().buildEngineSettings().getMaxSimpleKeyLength());
        assertEquals(1024, DumperSettings.builder().setMaxSimpleKeyLength(-1).build().buildEngineSettings().getMaxSimpleKeyLength());
        // Root tag
        assertEquals(Optional.of(Tag.INT), DumperSettings.builder().setRootTag(Tag.INT).build().buildEngineSettings().getExplicitRootTag());
        assertEquals(Optional.empty(), DumperSettings.builder().setRootTag(null).build().buildEngineSettings().getExplicitRootTag());
        // Yaml directive
        SpecVersion yamlDirective = new SpecVersion(1, 1);
        assertEquals(Optional.of(yamlDirective), DumperSettings.builder().setYamlDirective(yamlDirective).build().buildEngineSettings().getYamlDirective());
        assertEquals(Optional.empty(), DumperSettings.builder().setYamlDirective(null).build().buildEngineSettings().getYamlDirective());
        // Tag directive
        Map<String, String> tagDirectives = new HashMap<String, String>(){{
            put("!handle!", "prefix");
        }};
        assertEquals(tagDirectives, DumperSettings.builder().setTagDirectives(tagDirectives).build().buildEngineSettings().getTagDirective());
    }
}