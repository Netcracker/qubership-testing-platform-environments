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

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.Environment;
import org.qubership.atp.environments.model.Project;

public class ProjectImpl extends AbstractCreatedModified implements Project {

    private String shortName;
    private List<Environment> environments;

    public ProjectImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public ProjectImpl(UUID uuid, String name, String shortName, String description, List<Environment> environments,
                       Long created, Long modified) {
        setId(uuid);
        setName(name);
        setShortName(shortName);
        setDescription(description);
        setEnvironments(environments);
        setCreated(created);
        setModified(modified);
    }

    @Nonnull
    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public void setShortName(@Nonnull String shortName) {
        this.shortName = name;
    }

    @Override
    public List<Environment> getEnvironments() {
        return environments;
    }

    @Override
    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
}
