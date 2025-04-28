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

package org.qubership.atp.environments.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.utils.View;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Connection.class)
@JsonIgnoreProperties(value = {"sourceId"})
public interface Connection extends Identified, Named, CreatedModified, Described, Serializable, Sourced {

    @Nonnull
    @JsonProperty("systemId")
    @JsonView({View.Full.class})
    UUID getSystemId();

    void setSystemId(@Nonnull UUID systemId);

    @JsonView({View.Full.class})
    @Nonnull
    ConnectionParameters getParameters();

    void setParameters(@Nonnull ConnectionParameters parameters);

    @JsonView({View.Full.class})
    @Nullable
    UUID getSourceTemplateId();

    void setSourceTemplateId(@Nullable UUID sourceTemplateId);

    @JsonView({View.Full.class})
    @Nullable
    String getConnectionType();

    void setConnectionType(@Nullable String connectionType);

    @JsonView({View.Full.class})
    List<String> getServices();

    void setServices(List<String> services);

    @Override
    default boolean isParent(Identified candidate) {
        return System.class.isAssignableFrom(candidate.getClass());
    }
}
