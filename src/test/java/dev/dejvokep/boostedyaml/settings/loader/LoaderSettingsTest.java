/*
 * Copyright 2022 https://dejvokep.dev/
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
package dev.dejvokep.boostedyaml.settings.loader;

import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.utils.supplier.ListSupplier;
import org.jetbrains.annotations.NotNull;
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
        assertTrue(LoaderSettings.builder().setCreateFileIfAbsent(true).build().isCreateFileIfAbsent());
        assertFalse(LoaderSettings.builder().setCreateFileIfAbsent(false).build().isCreateFileIfAbsent());
    }

    @Test
    void buildEngineSettings() {
        assertTrue(LoaderSettings.builder().setDetailedErrors(true).build().buildEngineSettings(GeneralSettings.DEFAULT).getUseMarks());
        assertFalse(LoaderSettings.builder().setDetailedErrors(false).build().buildEngineSettings(GeneralSettings.DEFAULT).getUseMarks());
        assertTrue(LoaderSettings.builder().build().buildEngineSettings(GeneralSettings.builder().setDefaultList(new ListSupplier() {
            @NotNull
            @Override
            public <T> List<T> supply(int size) {
                return new LinkedList<>();
            }
        }).build()).getDefaultList().apply(1) instanceof LinkedList);
    }

}