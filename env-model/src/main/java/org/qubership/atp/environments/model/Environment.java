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
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Environment.class)
@JsonIgnoreProperties(value = {"categoryId", "sourceId"})
public interface Environment extends Identified, Named, CreatedModified, Described, Serializable, Sourced {

    @JsonView({View.FullVer1.class})
    @Nonnull
    UUID getProjectId();

    void setProjectId(@Nonnull UUID project);

    @JsonView({View.FullVer1.class})
    @Nullable
    List<System> getSystems();

    void setSystems(@Nullable List<System> systemsList);

    @Nullable
    UUID getCategoryId();

    void setCategoryId(@Nullable UUID categoryId);

    @JsonView({View.FullVer1.class})
    @Nullable
    String getGraylogName();

    void setGraylogName(@Nullable String graylogName);

    @JsonView({View.FullVer1.class})
    @Nullable
    String getSsmSolutionAlias();

    void setSsmSolutionAlias(@Nullable String ssmSolutionAlias);

    @JsonView({View.FullVer1.class})
    @Nullable
    String getSsmInstanceAlias();

    void setSsmInstanceAlias(@Nullable String ssmInstanceAlias);

    @JsonView({View.FullVer1.class})
    @Nullable
    String getConsulEgressConfigPath();

    void setConsulEgressConfigPath(@Nullable String consulEgressConfigPath);

    @JsonView({View.Full.class})
    List<String> getTags();

    void setTags(List<String> tags);


    @Override
    default boolean isParent(Identified candidate) {
        return Project.class.isAssignableFrom(candidate.getClass());
    }
}
