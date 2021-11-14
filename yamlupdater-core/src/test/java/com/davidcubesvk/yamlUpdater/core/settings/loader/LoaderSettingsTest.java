package com.davidcubesvk.yamlUpdater.core.settings.loader;

import com.davidcubesvk.yamlUpdater.core.settings.general.GeneralSettings;
import com.davidcubesvk.yamlUpdater.core.utils.supplier.ListSupplier;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoaderSettingsTest {

    @Test
    void isAutoUpdate() {
        assertTrue(LoaderSettings.builder().setAutoUpdate(true).build().isAutoUpdate());
        assertFalse(LoaderSettings.builder().setAutoUpdate(false).build().isAutoUpdate());
    }

    @Test
    void isCreateFileIfAbsent() {
        assertTrue(LoaderSettings.builder().setAutoUpdate(true).build().isCreateFileIfAbsent());
        assertFalse(LoaderSettings.builder().setAutoUpdate(false).build().isCreateFileIfAbsent());
    }

    @Test
    void buildEngineSettings() {
        assertTrue(LoaderSettings.builder().setDetailedErrors(true).build().buildEngineSettings(GeneralSettings.DEFAULT).getUseMarks());
        assertFalse(LoaderSettings.builder().setDetailedErrors(false).build().buildEngineSettings(GeneralSettings.DEFAULT).getUseMarks());
        assertTrue(LoaderSettings.builder().build().buildEngineSettings(GeneralSettings.builder().setDefaultList(new ListSupplier() {
            @Override
            public <T> List<T> supply(int size) {
                return new LinkedList<>();
            }
        }).build()).getDefaultList() instanceof LinkedList);
    }

}