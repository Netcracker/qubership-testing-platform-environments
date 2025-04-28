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

package org.qubership.atp.environments.db.modification;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.service.direct.SubscriptionService;
import org.qubership.atp.environments.service.direct.UpdateEventService;

public abstract class AbstractNotifier {

    protected final UpdateEventService updateEventService;
    protected final SubscriptionService subscriptionService;
    protected final TrackedMethod method;
    protected final TrackedType type;

    /**
     * TODO Make javadoc documentation for this method.
     */
    public AbstractNotifier(UpdateEventService updateEventService, SubscriptionService subscriptionService,
                            TrackedMethod method,
                            TrackedType type) {
        this.method = method;
        this.updateEventService = updateEventService;
        this.subscriptionService = subscriptionService;
        this.type = type;
    }

    /**
     * TODO Make javadoc documentation for this method.
     * // When object creating, subscription can't exists, it impossible.
     * // For parent's objects make status = 1 (object)
     */
    public void changeStatusEntitiesUpdateEvents(List<UUID> entitySubscriptions, @Nullable UUID idEntity, @Nonnull
            TrackedMethod method, @Nonnull TrackedType entityType) {
        if (idEntity == null || entitySubscriptions.isEmpty()) {
            return;
        }
        for (UUID subscriptionId : entitySubscriptions) {
            UpdateEvent updateEvent = updateEventService.get(subscriptionId, idEntity);
            Integer currentStatus = method.dbNotificationStatus.dataBaseIndex;
            String entityForNotice = entityType.entityForNotice;
            if (updateEvent == null) {
                updateEventService.create(subscriptionId, idEntity, null, currentStatus, entityForNotice);
            } else {
                updateEventService.update(subscriptionId, idEntity, updateEvent.getTagList(), currentStatus,
                        entityForNotice);
            }
        }
    }

    /**
     * TODO Make javadoc documentation for this method.
     */
    public void notifyUpdateEntity(List<UUID> cascadeOwnerEntitySubscriptions, UUID targetEntityId,
                                   TrackedType entityType) {
        changeStatusEntitiesUpdateEvents(cascadeOwnerEntitySubscriptions, targetEntityId, TrackedMethod.UPDATE,
                entityType);
    }

    public void notifyUpdateCurrentEntity(List<UUID> entitySubscriptions, @Nullable UUID idEntity) {
        changeStatusEntitiesUpdateEvents(entitySubscriptions, idEntity, method, type);
    }
}
