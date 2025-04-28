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

import org.qubership.atp.environments.model.Alert;

public class AlertImpl extends AbstractNamed implements Alert {

    private String shortDescription;
    private String tagList;
    private String parameters;
    private UUID subscriberId;
    private Integer status;
    private Long created;

    public AlertImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     *
     * @param uuid             TODO
     * @param name             TODO
     * @param shortDescription TODO
     * @param tagList          TODO
     * @param parameters       TODO
     * @param subscriberId     TODO
     * @param status           TODO
     * @param created          TODO
     */
    public AlertImpl(UUID uuid, String name, String shortDescription, String tagList,
                     String parameters, UUID subscriberId, Integer status, Long created) {
        setId(uuid);
        setName(name);
        setShortDescription(shortDescription);
        setTagList(tagList);
        setParameters(parameters);
        setSubscriberId(subscriberId);
        setStatus(status);
        setCreated(created);
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    @Override
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Override
    public String getTagList() {
        return tagList;
    }

    @Override
    public void setTagList(String tagList) {
        this.tagList = tagList;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Nonnull
    @Override
    public UUID getSubscriberId() {
        return subscriberId;
    }

    @Override
    public void setSubscriberId(UUID subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public void setStatus(Integer status) {
        this.status = status;
    }

    @Nullable
    @Override
    public Long getCreated() {
        return created;
    }

    @Override
    public void setCreated(Long created) {
        this.created = created;
    }
}
