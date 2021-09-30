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

    private final LoadSettingsBuilder builder;

    private LoaderSettings(LoadSettingsBuilder builder) {
        this.builder = builder;
    }

    public LoadSettings getSettings(GeneralSettings generalSettings) {
        return this.builder.setDefaultList(generalSettings::getDefaultList).setDefaultSet(generalSettings::getDefaultSet).setDefaultMap(generalSettings::getDefaultMap).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LoaderSettings from(LoadSettingsBuilder builder) {
        return new LoaderSettings(builder);
    }

    public static class Builder {

        private final LoadSettingsBuilder builder;

        private Builder() {
            this.builder = LoadSettings.builder();
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
            return new LoaderSettings(builder);
        }
    }

}