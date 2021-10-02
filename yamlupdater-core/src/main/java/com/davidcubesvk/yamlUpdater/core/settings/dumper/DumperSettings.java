package com.davidcubesvk.yamlUpdater.core.settings.dumper;

import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class DumperSettings {

    public enum Preset {
        YAML, JSON
    }

    public static final DumperSettings DEFAULT = builder().build();

    private final DumpSettings settings;
    private final DumpSettingsBuilder builder;
    private final Supplier<AnchorGenerator> generatorSupplier;

    private DumperSettings(Builder builder) {
        //If resetting enabled
        if (builder.resetAnchorGenerator) {
            this.settings = null;
            this.builder = builder.builder;
            this.generatorSupplier = builder.anchorGeneratorSupplier;
            return;
        }

        //Set
        this.settings = builder.builder.build();
        this.builder = null;
        this.generatorSupplier = null;
    }

    public DumpSettings getSettings() {
        return settings == null ? builder.setAnchorGenerator(generatorSupplier.get()).build() : settings;
    }

    public static Builder builder() {
        return new Builder(DumpSettings.builder());
    }

    public static Builder builder(DumpSettingsBuilder builder) {
        return new Builder(builder);
    }

    public static class Builder {

        private final DumpSettingsBuilder builder;
        private Supplier<AnchorGenerator> anchorGeneratorSupplier;
        private boolean resetAnchorGenerator;

        private Builder(DumpSettingsBuilder builder) {
            this.builder = builder;
            applyStylePreset(Preset.YAML);
        }

        public Builder setAnchorGenerator(Supplier<AnchorGenerator> generator, boolean reset) {
            this.anchorGeneratorSupplier = generator;
            this.resetAnchorGenerator = reset;
            return this;
        }

        public Builder setAnchorGenerator(Supplier<AnchorGenerator> generator) {
            return setAnchorGenerator(generator, true);
        }

        public Builder applyStylePreset(Preset preset) {
            setFlowStyle(preset == Preset.YAML ? FlowStyle.BLOCK : FlowStyle.FLOW);
            return setScalarStyle(preset == Preset.YAML ? ScalarStyle.PLAIN : ScalarStyle.DOUBLE_QUOTED);
        }
        public Builder setFlowStyle(FlowStyle flowStyle) {
            builder.setDefaultFlowStyle(flowStyle);
            return this;
        }
        public Builder setScalarStyle(ScalarStyle scalarStyle) {
            builder.setDefaultScalarStyle(scalarStyle);
            return this;
        }
        public Builder setAddExplicitStart(boolean explicitStart) {
            builder.setExplicitStart(explicitStart);
            return this;
        }
        public Builder setAddExplicitEnd(boolean explicitEnd) {
            builder.setExplicitEnd(explicitEnd);
            return this;
        }

        public Builder setScalarResolver(ScalarResolver resolver) {
            builder.setScalarResolver(resolver);
            return this;
        }

        public Builder setRootTag(Tag rootTag) {
            builder.setExplicitRootTag(Optional.ofNullable(rootTag));
            return this;
        }

        public Builder setYamlDirective(SpecVersion directive) {
            builder.setYamlDirective(Optional.ofNullable(directive));
            return this;
        }

        public Builder setTagDirectives(Map<String, String> directives) {
            builder.setTagDirective(directives);
            return this;
        }

        public Builder setCanonical(boolean canonical) {
            builder.setCanonical(canonical);
            return this;
        }

        public Builder setMultilineFlow(boolean multilineFlow) {
            builder.setMultiLineFlow(multilineFlow);
            return this;
        }

        public Builder setUseUnicodeEncoding(boolean unicodeEncoding) {
            builder.setUseUnicodeEncoding(unicodeEncoding);
            return this;
        }

        public Builder setIndentation(int spaces) {
            builder.setIndent(spaces);
            return this;
        }
        public Builder setIndicatorIndentation(int spaces) {
            builder.setIndentWithIndicator(true);
            builder.setIndicatorIndent(spaces);
            return this;
        }
        public Builder setMaxLineWidth(int width) {
            builder.setWidth(width <= 0 ? Integer.MAX_VALUE : width);
            return this;
        }
        public Builder setLineBreak(String lineBreak) {
            builder.setBestLineBreak(lineBreak);
            return this;
        }
        public Builder setMaxSimpleKeyLength(int width) {
            builder.setMaxSimpleKeyLength(width <= 0 ? 1024 : width);
            return this;
        }
        public Builder setEscapeUnprintable(boolean escape) {
            return setUnprintableStyle(escape ? NonPrintableStyle.ESCAPE : NonPrintableStyle.BINARY);
        }
        public Builder setUnprintableStyle(NonPrintableStyle style) {
            builder.setNonPrintableStyle(style);
            return this;
        }

        public DumperSettings build() {
            return new DumperSettings(this);
        }

    }

}