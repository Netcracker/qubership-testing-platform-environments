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
import org.qubership.atp.environments.model.utils.enums.Status;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = System.class)
@JsonIgnoreProperties(value = {"sourceId", "checkVersionError"})
public interface System extends Identified, Named, CreatedModified, Described, Serializable, Sourced {

    @JsonView({View.FullVer1.class,View.Environments.class})
    @Nonnull
    List<Environment> getEnvironmentIds();

    void setEnvironmentIds(@Nonnull List<Environment> environments);

    @JsonView({View.FullVer2.class})
    @Nonnull
    List<Environment> getEnvironments();

    void setEnvironments(@Nonnull List<Environment> environments);

    @JsonView({View.FullVer1.class})
    @Nullable
    UUID getSystemCategoryId();

    @JsonView({View.FullVer2.class})
    @Nullable
    SystemCategory getSystemCategory();

    void setSystemCategory(@Nullable SystemCategory systemCategory);

    @JsonView({View.Full.class})
    @Nullable
    Status getStatus();

    void setStatus(@Nullable Status status);

    @JsonView({View.Full.class})
    List<Connection> getConnections();

    void setConnections(List<Connection> connectionsList);

    @JsonView({View.Full.class})
    @Nullable
    Long getDateOfLastCheck();

    void setDateOfLastCheck(Long dateOfLastCheck);

    @JsonView({View.Full.class})
    @Nullable
    String getVersion();

    void setVersion(@Nullable String version);

    @JsonView({View.Full.class})
    @Nullable
    Long getDateOfCheckVersion();

    void setDateOfCheckVersion(Long dateOfCheckVersion);

    @JsonView({View.Full.class})
    @Nullable
    ParametersGettingVersion getParametersGettingVersion();

    void setParametersGettingVersion(@Nonnull ParametersGettingVersion parametersGettingVersion);

    @JsonView({View.Full.class})
    @Nullable
    UUID getParentSystemId();

    void setParentSystemId(UUID parentSystemId);

    @JsonView({View.Full.class})
    @JsonProperty("serverITF")
    @Nullable
    ServerItf getServerItf();

    void setServerItf(@Nonnull ServerItf serverItf);

    @JsonView({View.Full.class})
    @Nullable
    Boolean getMergeByName();

    void setMergeByName(Boolean mergeByName);

    @JsonView({View.Full.class})
    @Nullable
    UUID getLinkToSystemId();

    void setLinkToSystemId(UUID linkToSystem);

    @JsonView({View.Full.class})
    @Nullable
    UUID getExternalId();

    void setExternalId(UUID externalId);

    @JsonView({View.Full.class})
    @Nullable
    String getExternalName();

    void setExternalName(String externalId);

    @JsonView({View.DetailedVersion.class})
    @Nullable
    String getCheckVersionError();

    void setCheckVersionError(String error);

    @Override
    default boolean isParent(Identified candidate) {
        return Environment.class.isAssignableFrom(candidate.getClass());
    }
}
