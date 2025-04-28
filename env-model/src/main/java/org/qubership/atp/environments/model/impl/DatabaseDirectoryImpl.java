/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.environments.model.impl;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.DatabaseDirectory;

public class DatabaseDirectoryImpl implements DatabaseDirectory {

    protected String name;
    protected String urlFormat;

    public DatabaseDirectoryImpl() {
    }

    public DatabaseDirectoryImpl(String name, String urlFormat) {
        this.name = name;
        this.urlFormat = urlFormat;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public String getUrlFormat() {
        return urlFormat;
    }

    @Override
    public void setUrlFormat(String urlFormat) {
    }
}
