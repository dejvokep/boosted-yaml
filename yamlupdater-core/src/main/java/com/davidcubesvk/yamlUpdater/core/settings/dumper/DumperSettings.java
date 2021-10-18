package com.davidcubesvk.yamlUpdater.core.settings.dumper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;
import org.snakeyaml.engine.v2.serializer.NumberAnchorGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Loader settings; wrapper for SnakeYAML Engine's {@link DumpSettings} class which is more
 * detailed, provides more options and possibilities, hides options which should not be configured.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class DumperSettings {

    /**
     * Dumping style presets.
     */
    public enum StylePreset {
        /**
         * Dumping preset in extended, human-readable format.
         * <p>
         * Represents {@link FlowStyle#BLOCK} and {@link ScalarStyle#PLAIN}.
         */
        YAML,

        /**
         * Dumping preset in JSON-compatible format (is still compatible with YAML)
         * <p>
         * Represents {@link FlowStyle#FLOW} and {@link ScalarStyle#DOUBLE_QUOTED}.
         */
        JSON
    }

    /**
     * Default dumper settings.
     */
    public static final DumperSettings DEFAULT = builder().build();

    //SnakeYAML Engine dump settings builder
    private final DumpSettingsBuilder builder;
    //Anchor generator supplier
    private final Supplier<AnchorGenerator> generatorSupplier;

    /**
     * Creates final, immutable dumper settings from the given builder.
     *
     * @param builder the builder
     */
    private DumperSettings(Builder builder) {
        this.builder = builder.builder;
        this.generatorSupplier = builder.anchorGeneratorSupplier;
    }

    /**
     * Builds new settings using the configured generator supplier and returns them.
     *
     * @return the new settings
     */
    public DumpSettings getSettings() {
        return builder.setAnchorGenerator(generatorSupplier.get()).build();
    }

    /**
     * Returns a new builder.
     *
     * @return the new builder
     */
    public static Builder builder() {
        return new Builder(DumpSettings.builder());
    }

    /**
     * Creates and returns a new builder from the given, already created SnakeYAML Engine settings builder.
     * <p>
     * <b>Note that the given builder is not cloned, so it is in the caller's best interest to never change it's
     * settings from now on.</b>
     * <p>
     * Please note that {@link Builder#setAnchorGenerator(Supplier)} still has to be called (if you want to alter the
     * default), as they are not part of the Engine's settings.
     *
     * @param builder the underlying builder
     * @return the new builder
     */
    public static Builder builder(DumpSettingsBuilder builder) {
        return new Builder(builder);
    }

    /**
     * Builder for dumper settings; wrapper for SnakeYAML Engine's {@link DumpSettingsBuilder} class which is more
     * detailed, provides more options and possibilities, hides options which should not be configured.
     */
    public static class Builder {

        /**
         * Default anchor generator supplier.
         */
        public static final Supplier<AnchorGenerator> DEFAULT_ANCHOR_GENERATOR = () -> new NumberAnchorGenerator(1);
        /**
         * Default flow style.
         */
        public static final FlowStyle DEFAULT_FLOW_STYLE = FlowStyle.BLOCK;
        /**
         * Default scalar style.
         */
        public static final ScalarStyle DEFAULT_SCALAR_STYLE = ScalarStyle.PLAIN;
        /**
         * If to add explicit document start by default.
         */
        public static final boolean DEFAULT_ADD_EXPLICIT_START = false;
        /**
         * If to add explicit document end by default.
         */
        public static final boolean DEFAULT_ADD_EXPLICIT_END = false;
        /**
         * If to dump in canonical form by default.
         */
        public static final boolean DEFAULT_CANONICAL = false;
        /**
         * If to use multiline format by default.
         */
        public static final boolean DEFAULT_MULTILINE_FORMAT = false;
        /**
         * If to use unicode instead of ASCII charset by default.
         */
        public static final boolean DEFAULT_UNICODE = true;
        /**
         * Default spaces per one indentation level.
         */
        public static final int DEFAULT_INDENTATION = 2;
        /**
         * Default spaces to use to indent indicators.
         */
        public static final int DEFAULT_INDICATOR_INDENTATION = -1;
        /**
         * Max line width by default.
         */
        public static final int DEFAULT_MAX_LINE_WIDTH = -1;
        /**
         * Max length for a key to have to be dumped in simple format.
         */
        public static final int DEFAULT_MAX_SIMPLE_KEY_LENGTH = -1;
        /**
         * If to escape unprintable unicode characters by default.
         */
        public static final boolean DEFAULT_ESCAPE_UNPRINTABLE = true;

        //Underlying SnakeYAML Engine settings builder
        private final DumpSettingsBuilder builder;
        //Anchor generator
        private Supplier<AnchorGenerator> anchorGeneratorSupplier = DEFAULT_ANCHOR_GENERATOR;

        /**
         * Creates a new builder from the given, already created SnakeYAML Engine settings builder.
         * <p>
         * Please note that {@link #setAnchorGenerator(Supplier)} still has to be called (if you want to alter the
         * default), as they are not part of the Engine's settings.
         *
         * @param builder the underlying builder
         */
        private Builder(DumpSettingsBuilder builder) {
            this.builder = builder;
        }

        /**
         * Creates a new builder. Automatically applies the defaults, compatible with Spigot/BungeeCord API.
         */
        private Builder() {
            //Create
            builder = DumpSettings.builder();
            //Set defaults
            setFlowStyle(DEFAULT_FLOW_STYLE);
            setScalarStyle(DEFAULT_SCALAR_STYLE);
            setAddExplicitStart(DEFAULT_ADD_EXPLICIT_START);
            setAddExplicitEnd(DEFAULT_ADD_EXPLICIT_END);
            setCanonical(DEFAULT_CANONICAL);
            setMultilineFormat(DEFAULT_MULTILINE_FORMAT);
            setUseUnicode(DEFAULT_UNICODE);
            setIndentation(DEFAULT_INDENTATION);
            setIndicatorIndentation(DEFAULT_INDICATOR_INDENTATION);
            setMaxLineWidth(DEFAULT_MAX_LINE_WIDTH);
            setMaxSimpleKeyLength(DEFAULT_MAX_SIMPLE_KEY_LENGTH);
            setEscapeUnprintable(DEFAULT_ESCAPE_UNPRINTABLE);
        }

        /**
         * Sets anchor generator supplier used to supply generators when dumping.
         * <p>
         * It is a supplier to ensure that brand new, yet unused generator, is used on every dump and it is not reused
         * (which might cause higher anchor IDs being generated as the file is saved more for the default number-based
         * generator for example).
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ANCHOR_GENERATOR}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setAnchorGenerator(AnchorGenerator)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setAnchorGenerator(org.snakeyaml.engine.v2.serializer.AnchorGenerator)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#3222-anchors-and-aliases">anchors and aliases</a>
         *
         * @param generator the new anchor generator supplier
         * @return the builder
         * @see DumpSettingsBuilder#setAnchorGenerator(AnchorGenerator)
         */
        public Builder setAnchorGenerator(@NotNull Supplier<AnchorGenerator> generator) {
            this.anchorGeneratorSupplier = generator;
            return this;
        }

        /**
         * Applies the given style preset to this builder.
         * <table class="preset-table">
         *     <tr>
         *         <th>Given {@link StylePreset}</th>
         *         <th>Sets {@link #setFlowStyle(FlowStyle)} to</th>
         *         <th>Sets {@link #setScalarStyle(ScalarStyle)} to</th>
         *     </tr>
         *     <tr>
         *         <td>{@link StylePreset#YAML}</td>
         *         <td>{@link FlowStyle#BLOCK}</td>
         *         <td>{@link ScalarStyle#PLAIN}</td>
         *     </tr>
         *     <tr>
         *         <td>{@link StylePreset#JSON}</td>
         *         <td>{@link FlowStyle#FLOW}</td>
         *         <td>{@link ScalarStyle#DOUBLE_QUOTED}</td>
         *     </tr>
         * </table>
         * Please note you can always alter the options modified by this preset, by calling their respective methods.
         *
         * @param stylePreset the style preset to apply
         * @return the builder
         * @see #setFlowStyle(FlowStyle)
         * @see #setScalarStyle(ScalarStyle)
         */
        public Builder applyStylePreset(@NotNull StylePreset stylePreset) {
            setFlowStyle(stylePreset == StylePreset.YAML ? FlowStyle.BLOCK : FlowStyle.FLOW);
            return setScalarStyle(stylePreset == StylePreset.YAML ? ScalarStyle.PLAIN : ScalarStyle.DOUBLE_QUOTED);
        }

        /**
         * Sets the given flow style. Flow style applies to sequences (lists, arrays) and maps, where they can either be
         * dumped as blocks (expanded), or branched (using brackets).
         * <p>
         * Sample output with flow style set to {@link FlowStyle#BLOCK BLOCK}:
         * <pre>{@code
         * a:
         *   b:
         *   - x
         *   - y
         *   c: false
         * }</pre>
         * Sample output with flow style set to {@link FlowStyle#FLOW FLOW} (the same file context):
         * <pre>{@code
         * {a: {b: [x, y], c: false}}
         * }</pre>
         * If using {@link FlowStyle#FLOW FLOW}, you may also want to dump the contents in multiline format for better
         * readability - please use {@link #setMultilineFormat(boolean)} to do that.
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_FLOW_STYLE}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setDefaultFlowStyle(FlowStyle)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setDefaultFlowStyle(org.snakeyaml.engine.v2.common.FlowStyle)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#3231-node-styles">node styles</a>
         *
         * @param flowStyle the flow style to use
         * @return the builder
         * @see DumpSettingsBuilder#setDefaultFlowStyle(FlowStyle)
         */
        public Builder setFlowStyle(@NotNull FlowStyle flowStyle) {
            builder.setDefaultFlowStyle(flowStyle);
            return this;
        }

        /**
         * Sets the given scalar style.
         * <p>
         * Sample output with scalar style set to {@link ScalarStyle#PLAIN}:
         * <pre>{@code
         * a:
         *   b: true
         *   c: false
         *   d: 1
         * }</pre>
         * Sample output with flow style set to {@link FlowStyle#FLOW FLOW} (the same file context):
         * <pre>{@code
         * {a: {b: [x, y], c: false}}
         * }</pre>
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_SCALAR_STYLE}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setDefaultScalarStyle(ScalarStyle)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setDefaultScalarStyle(org.snakeyaml.engine.v2.common.ScalarStyle)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#81-block-scalar-styles">for <code>BLOCK</code> flow style</a>, <a href="https://yaml.org/spec/1.2.2/#73-flow-scalar-styles">for <code>FLOW</code> flow style</a>
         *
         * @param scalarStyle the scalar style to use
         * @return the builder
         * @see DumpSettingsBuilder#setDefaultScalarStyle(ScalarStyle)
         */
        public Builder setScalarStyle(@NotNull ScalarStyle scalarStyle) {
            builder.setDefaultScalarStyle(scalarStyle);
            return this;
        }

        /**
         * Sets if to explicitly add document start marker (<code>---</code>). If there are any directives to be
         * dumped, it will be added automatically not depending on the value set.
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ADD_EXPLICIT_START}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setExplicitStart(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setExplicitStart(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#document-markers">document markers</a>
         *
         * @param explicitStart if to add explicit document start marker
         * @return the builder
         * @see DumpSettingsBuilder#setExplicitStart(boolean)
         */
        public Builder setAddExplicitStart(boolean explicitStart) {
            builder.setExplicitStart(explicitStart);
            return this;
        }

        /**
         * Sets if to explicitly add document end marker (<code>...</code>).
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ADD_EXPLICIT_END}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setExplicitEnd(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setExplicitEnd(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#document-markers">document markers</a>
         *
         * @param explicitEnd if to add explicit document end marker
         * @return the builder
         * @see DumpSettingsBuilder#setExplicitEnd(boolean)
         */
        public Builder setAddExplicitEnd(boolean explicitEnd) {
            builder.setExplicitEnd(explicitEnd);
            return this;
        }

        /**
         * Sets custom scalar resolver, used to resolve tags for objects in string format (<code>!str "x"</code>).
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setScalarResolver(ScalarResolver)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setScalarResolver(org.snakeyaml.engine.v2.resolver.ScalarResolver)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param resolver the resolver to set
         * @return the builder
         * @see DumpSettingsBuilder#setScalarResolver(ScalarResolver)
         */
        public Builder setScalarResolver(@NotNull ScalarResolver resolver) {
            builder.setScalarResolver(resolver);
            return this;
        }

        /**
         * Sets (explicit) tag of the top level element in the document. As this library does not support anything other
         * than {@link Map} for the top-level object, the given tag must be referring to a class implementing {@link Map}
         * interface, serious issues will occur otherwise (the given tag is not validated).
         * <p>
         * If <code>null</code>, does not dump any tag for the root section (which will make the resolver resolve it
         * automatically on next load).
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setExplicitRootTag(Optional)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setExplicitRootTag(java.util.Optional)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param rootTag the root section tag (type)
         * @return the builder
         * @see DumpSettingsBuilder#setExplicitRootTag(Optional)
         */
        public Builder setRootTag(@Nullable Tag rootTag) {
            builder.setExplicitRootTag(Optional.ofNullable(rootTag));
            return this;
        }

        /**
         * Sets the version (<code>%YAML</code>) directive. If <code>null</code>, does not dump any explicit version
         * directive. SnakeYAML engine supports YAML v1.2 only, however, per the engine specification, most of the older
         * YAML can be processed. Proceed with caution.
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setYamlDirective(Optional)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setYamlDirective(java.util.Optional)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#681-yaml-directives">YAML directives</a>
         *
         * @param directive the version directive
         * @return the builder
         * @see DumpSettingsBuilder#setYamlDirective(Optional)
         */
        public Builder setYamlDirective(@Nullable SpecVersion directive) {
            builder.setYamlDirective(Optional.ofNullable(directive));
            return this;
        }

        /**
         * Sets the given tag (<code>%TAG</code>) directives in form of a map, where key is the <code>handle</code> and
         * value the <code>prefix</code> (per the YAML spec). If there was anything set previously, it is overwritten.
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setTagDirective(Map)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setTagDirective(java.util.Map)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#682-tag-directives">TAG directives</a>
         *
         * @param directives the tag directives
         * @return the builder
         * @see DumpSettingsBuilder#setTagDirective(Map)
         */
        public Builder setTagDirectives(@NotNull Map<String, String> directives) {
            builder.setTagDirective(directives);
            return this;
        }

        /**
         * Sets if to dump in canonical form.
         * <p>
         * If set to <code>true</code>, each node and scalar will have it's own tag explicitly specified, with map keys
         * and values having their indicators added beforehand. Strings, integers and booleans will be formatted as
         * strings (double quoted).
         * <p>
         * Also, the dumped result will have the style of {@link FlowStyle#FLOW} with {@link #setMultilineFormat(boolean)} and
         * {@link #setAddExplicitStart(boolean)} set to <code>true</code>. Please keep in mind that those settings will
         * be overwritten (still, only if set to <code>true</code>).
         * <p>
         * Please see the differences below:
         * <p>
         * Sample output with canonical set to <code>false</code>:
         * <pre>{@code
         * a:
         *   b: true
         *   c: false
         * }</pre>
         * Sample output with canonical set to <code>true</code> (the same file context):
         * <pre>{@code
         * ---
         * !!map {
         *   ? !!str "a"
         *   : !!map {
         *     ? !!str "b"
         *     : !!bool "true",
         *     ? !!str "c"
         *     : !!bool "false",
         *   },
         * }
         * }</pre>
         * <b>The specification displayed above is rather informative as it was made only from what's the difference,
         * there is no SnakeYAML Engine documentation provided.</b>
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_CANONICAL}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setCanonical(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setCanonical(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#canonical-form">canonical form</a>
         *
         * @param canonical if to use canonical format
         * @return the builder
         * @see DumpSettingsBuilder#setCanonical(boolean)
         */
        public Builder setCanonical(boolean canonical) {
            builder.setCanonical(canonical);
            return this;
        }

        /**
         * Sets if to separate content of the document using newlines to make the dumped file somewhat readable; has
         * effect if and only if flow style is set to {@link FlowStyle#FLOW}.
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_MULTILINE_FORMAT}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setMultiLineFlow(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setMultiLineFlow(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param multilineFlow if to use multiline format
         * @return the builder
         * @see DumpSettingsBuilder#setMultiLineFlow(boolean)
         */
        public Builder setMultilineFormat(boolean multilineFlow) {
            builder.setMultiLineFlow(multilineFlow);
            return this;
        }

        /**
         * Sets if to use unicode encoding for content characters instead of ASCII charset (where all non-ASCII chars will be escaped).
         * <p>
         * For additional information regarding this option and charsets, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_UNICODE}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setUseUnicodeEncoding(boolean)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setUseUnicodeEncoding(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#51-character-set">character sets</a>
         *
         * @param unicodeEncoding if to use full unicode encoding
         * @return the builder
         * @see DumpSettingsBuilder#setUseUnicodeEncoding(boolean)
         */
        public Builder setUseUnicode(boolean unicodeEncoding) {
            builder.setUseUnicodeEncoding(unicodeEncoding);
            return this;
        }

        /**
         * Sets how many spaces to use per one indentation level.
         * <p>
         * For additional information please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_INDENTATION}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setIndent(int)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setIndent(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#61-indentation-spaces">indentation</a>
         *
         * @param spaces amount of spaces per one indentation level
         * @return the builder
         * @see DumpSettingsBuilder#setUseUnicodeEncoding(boolean)
         * @see <a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setIndent(int)">docs for the wrapped method</a>
         */
        public Builder setIndentation(int spaces) {
            builder.setIndent(spaces);
            return this;
        }

        /**
         * Sets how many spaces to use per one indentation level for indicators. If the given value is less than or
         * equal to <code>0</code>, disables indicator indentation.
         * <p>
         * For additional information regarding this option and indicators, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_INDICATOR_INDENTATION}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setIndent(int)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setIndicatorIndent(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#61-indentation-spaces">indentation</a>, <a href="https://yaml.org/spec/1.2.2/#indicator-characters">indicators</a>
         *
         * @param spaces amount of spaces to use to indent indicators
         * @return the builder
         * @see DumpSettingsBuilder#setIndicatorIndent(int)
         * @see <a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setIndicatorIndent(int)">docs for the wrapped method</a>
         */
        public Builder setIndicatorIndentation(int spaces) {
            builder.setIndentWithIndicator(spaces > 0);
            builder.setIndicatorIndent(spaces);
            return this;
        }

        /**
         * Sets the maximum line width. If any scalar makes the line longer than the given width, the dumper attempts to
         * break the line at the nearest (next) whitespace. If the given value is less than or equal to <code>0</code>,
         * disables the limit and therefore, allows for theoretically unlimited line lengths (up to {@link Integer#MAX_VALUE}).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_MAX_LINE_WIDTH}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setWidth(int)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setWidth(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param width maximum line width
         * @return the builder
         * @see DumpSettingsBuilder#setWidth(int)
         * @see <a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setWidth(int)">docs for the wrapped method</a>
         */
        public Builder setMaxLineWidth(int width) {
            builder.setWidth(width <= 0 ? Integer.MAX_VALUE : width);
            return this;
        }

        /**
         * Sets the line break appended at the end of each line.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setBestLineBreak(String)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setBestLineBreak(java.lang.String)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param lineBreak line break
         * @return the builder
         * @see DumpSettingsBuilder#setBestLineBreak(String)
         */
        public Builder setLineBreak(String lineBreak) {
            builder.setBestLineBreak(lineBreak);
            return this;
        }

        /**
         * Sets the maximum length a key can (in serialized form, also applies to flow sequence and map keys) have to be
         * printed in simple format (without the explicit key indicator <code>?</code>). If the given value is less than
         * or equal to <code>0</code>, disables the limit and therefore, allows for keys of length up to <code>1024</code> (limit
         * preserved by the YAML spec).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_MAX_SIMPLE_KEY_LENGTH}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setMaxSimpleKeyLength(int)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setMaxSimpleKeyLength(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#example-explicit-block-mapping-entries">explicit keys</a>
         *
         * @param length maximum length for simple key format
         * @return the builder
         * @see DumpSettingsBuilder#setMaxSimpleKeyLength(int)
         */
        public Builder setMaxSimpleKeyLength(int length) {
            builder.setMaxSimpleKeyLength(length <= 0 ? 1024 : length + 6);
            return this;
        }

        /**
         * Sets if strings containing unprintable characters should have those characters escaped, or the whole string
         * dumped as binary data.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ESCAPE_UNPRINTABLE}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setNonPrintableStyle(NonPrintableStyle)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setMaxSimpleKeyLength(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#51-character-set">character sets</a>
         *
         * @param escape if to escape rather than dumping as binary
         * @return the builder
         * @see DumpSettingsBuilder#setNonPrintableStyle(NonPrintableStyle)
         */
        public Builder setEscapeUnprintable(boolean escape) {
            return setUnprintableStyle(escape ? NonPrintableStyle.ESCAPE : NonPrintableStyle.BINARY);
        }

        /**
         * Sets if strings containing unprintable characters should have those characters escaped, or the whole string
         * dumped as binary data.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> corresponding to {@link #DEFAULT_ESCAPE_UNPRINTABLE}<br>
         * <b>Alias method: </b>{@link #setEscapeUnprintable(boolean)}<br>
         * <b>Parent method: </b> {@link DumpSettingsBuilder#setNonPrintableStyle(NonPrintableStyle)}<br>
         * <b>Method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/DumpSettingsBuilder.html#setNonPrintableStyle(org.snakeyaml.engine.v2.common.NonPrintableStyle)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#51-character-set">character sets</a>
         *
         * @param style style to use
         * @return the builder
         * @see DumpSettingsBuilder#setNonPrintableStyle(NonPrintableStyle)
         */
        public Builder setUnprintableStyle(@NotNull NonPrintableStyle style) {
            builder.setNonPrintableStyle(style);
            return this;
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public DumperSettings build() {
            return new DumperSettings(this);
        }

    }

}