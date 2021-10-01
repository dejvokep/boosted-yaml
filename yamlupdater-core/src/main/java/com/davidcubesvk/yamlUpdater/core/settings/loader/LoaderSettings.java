package com.davidcubesvk.yamlUpdater.core.settings.loader;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v2.env.EnvConfig;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;

import java.util.Map;
import java.util.Optional;

public class LoaderSettings {

    public static final LoaderSettings DEFAULT = builder().build();
    public static final boolean DEFAULT_CREATE_FILE_IF_ABSENT = true;
    public static final boolean DEFAULT_AUTO_UPDATE = true;

    private final LoadSettingsBuilder builder;
    private final boolean createFileIfAbsent, autoUpdate;

    private LoaderSettings(Builder builder) {
        this.builder = builder.builder;
        this.autoUpdate = builder.autoUpdate;
        this.createFileIfAbsent = builder.createFileIfAbsent;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public boolean isCreateFileIfAbsent() {
        return createFileIfAbsent;
    }

    public LoadSettings getSettings(GeneralSettings generalSettings) {
        return this.builder.setParseComments(true).setDefaultList(generalSettings::getDefaultList).setDefaultSet(generalSettings::getDefaultSet).setDefaultMap(generalSettings::getDefaultMap).build();
    }

    public static Builder builder() {
        return new Builder(LoadSettings.builder());
    }
    public static Builder builder(LoadSettingsBuilder builder) {
        return new Builder(builder);
    }

    public static class Builder {

        private final LoadSettingsBuilder builder;
        private boolean autoUpdate, createFileIfAbsent;

        private Builder(LoadSettingsBuilder builder) {
            this.builder = builder.setParseComments(true);
            this.autoUpdate = DEFAULT_AUTO_UPDATE;
            this.createFileIfAbsent = DEFAULT_CREATE_FILE_IF_ABSENT;
        }

        public Builder setCreateFileIfAbsent(boolean createFileIfAbsent) {
            this.createFileIfAbsent = createFileIfAbsent;
            return this;
        }

        public Builder setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            return this;
        }

        public Builder setLabel(String label) {
            builder.setLabel(label);
            return this;
        }

        public Builder setUseMarks(boolean useMarks) {
            builder.setUseMarks(useMarks);
            return this;
        }

        public Builder setPermitDuplicateKeys(boolean permitDuplicateKeys) {
            builder.setAllowDuplicateKeys(permitDuplicateKeys);
            return this;
        }

        public Builder setPermitRecursiveKeys(boolean permitDuplicateKeys) {
            builder.setAllowRecursiveKeys(permitDuplicateKeys);
            return this;
        }

        public Builder setMaxCollectionAliases(int maxCollectionAliases) {
            builder.setMaxAliasesForCollections(maxCollectionAliases);
            return this;
        }

        public Builder setConstructors(Map<Tag, ConstructNode> constructors) {
            builder.setTagConstructors(constructors);
            return this;
        }

        public Builder setScalarResolver(ScalarResolver resolver) {
            builder.setScalarResolver(resolver);
            return this;
        }

        public Builder setEnvironmentConfig(EnvConfig envConfig) {
            builder.setEnvConfig(Optional.ofNullable(envConfig));
            return this;
        }

        public LoaderSettings build() {
            return new LoaderSettings(this);
        }
    }

}