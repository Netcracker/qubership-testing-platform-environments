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
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.CreatedModified;

public abstract class AbstractCreatedModified extends AbstractDescribed implements CreatedModified {

    protected Long created;
    protected UUID createdBy;
    protected Long modified;
    protected UUID modifiedBy;

    @Nonnull
    @Override
    public Long getCreated() {
        return created;
    }

    @Override
    public void setCreated(@Nonnull Long created) {
        this.created = created;
    }

    @Override
    public UUID getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(@Nullable UUID createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Long getModified() {
        return modified;
    }

    @Override
    public void setModified(Long modified) {
        this.modified = modified;
    }

    @Override
    public UUID getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(@Nullable UUID modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
