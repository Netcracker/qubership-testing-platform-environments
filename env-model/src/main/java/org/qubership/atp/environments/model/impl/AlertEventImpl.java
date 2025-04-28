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

import org.qubership.atp.environments.model.Alert;
import org.qubership.atp.environments.model.AlertEvent;

public class AlertEventImpl implements AlertEvent {

    private Alert alert;
    private UUID alertId;
    private UUID entityId;
    private String tagList;
    private Integer status;
    private Long lastUpdated;

    public AlertEventImpl() {
    }

    /**
     * TODO Make javadoc documentation for this method.
     *
     * @param alert       TODO
     * @param alertId     TODO
     * @param entityId    TODO
     * @param tagList     TODO
     * @param status      TODO
     * @param lastUpdated TODO
     */
    public AlertEventImpl(Alert alert, UUID alertId,
                          UUID entityId, String tagList, Integer status, Long lastUpdated) {
        setAlertId(alertId);
        setAlert(alert);
        setEntityId(entityId);
        setTagList(tagList);
        setStatus(status);
        setLastUpdated(lastUpdated);
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    @Override
    public UUID getAlertId() {
        return alertId;
    }

    @Override
    public void setAlertId(UUID alertId) {
        this.alertId = alertId;
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getTagList() {
        return tagList;
    }

    @Override
    public void setTagList(@Nonnull String tagList) {
        this.tagList = tagList;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public void setStatus(@Nonnull Integer status) {
        this.status = status;
    }

    @Override
    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
