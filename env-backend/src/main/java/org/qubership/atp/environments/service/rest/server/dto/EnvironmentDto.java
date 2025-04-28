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

package org.qubership.atp.environments.service.rest.server.dto;

import java.util.List;
import java.util.UUID;

import org.qubership.atp.environments.model.impl.AbstractCreatedModified;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnvironmentDto extends AbstractCreatedModified {
    static final long serialVersionUID = 42L;
    private UUID projectId;
    private List<CreateSystemDto> systems;
    private String graylogName;
    private String ssmSolutionAlias;
    private String ssmInstanceAlias;
    private String consulEgressConfigPath;
    private List<String> tags;

    /**
     * TODO Make javadoc documentation for this method.
     */
    public EnvironmentDto(UUID id, String name, List<CreateSystemDto> systems) {
        this.id = id;
        this.name = name;
        this.systems = systems;
    }
}
