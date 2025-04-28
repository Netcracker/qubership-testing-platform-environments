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

import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.EnvironmentCategory;

public class EnvironmentCategoryImpl extends AbstractCreatedModified implements EnvironmentCategory {

    private String tagList;

    public EnvironmentCategoryImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public EnvironmentCategoryImpl(UUID uuid, String name, String description, String tagList, Long created,
                                   Long modified) {
        setId(uuid);
        setName(name);
        setDescription(description);
        setTagList(tagList);
        setCreated(created);
        setModified(modified);
    }

    @Override
    public String getTagList() {
        return tagList;
    }

    @Override
    public void setTagList(@Nonnull String tagList) {
        this.tagList = tagList;
    }
}
