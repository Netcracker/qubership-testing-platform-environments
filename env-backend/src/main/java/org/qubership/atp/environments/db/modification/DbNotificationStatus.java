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

public enum DbNotificationStatus {
    // statuses in the Update_events table (for couple Subscription-Entity):
    UNDEFINED(0),
    UPDATED(1) /*object was changed (after import TA-tool must changes status to 2)*/,
    PROCESSED(2) /*object was successfully imported to the external TA-tool*/,
    DELETED(3) /*object was deleted (after maintenance in TA-tool must changes status to 4)*/,
    FROZEN(4) /*object was frozen*/;

    public final int dataBaseIndex;

    DbNotificationStatus(int dataBaseIndex) {
        this.dataBaseIndex = dataBaseIndex;
    }
}
