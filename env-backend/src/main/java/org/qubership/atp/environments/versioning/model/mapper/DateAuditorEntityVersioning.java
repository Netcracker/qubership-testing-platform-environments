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

package org.qubership.atp.environments.versioning.model.mapper;

import java.util.Date;
import java.util.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class DateAuditorEntityVersioning extends AbstractEntityVersioning {

    protected Date modified;
    protected String modifiedBy;

    public Date getModified() {
        return getModifiedDate(modified);
    }

    public void setModified(Date modified) {
        this.modified = getModifiedDate(modified);
    }

    private Date getModifiedDate(Date modified) {
        return Optional
                .ofNullable(modified)
                .map(Date::getTime)
                .map(Date::new)
                .orElse(null);
    }
}
