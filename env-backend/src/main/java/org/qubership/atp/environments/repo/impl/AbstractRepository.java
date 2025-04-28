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

package org.qubership.atp.environments.repo.impl;

import org.qubership.atp.environments.db.generated.QAlertEvents;
import org.qubership.atp.environments.db.generated.QAlerts;
import org.qubership.atp.environments.db.generated.QConnections;
import org.qubership.atp.environments.db.generated.QDatabaseDirectory;
import org.qubership.atp.environments.db.generated.QEnvironmentCategories;
import org.qubership.atp.environments.db.generated.QEnvironmentSystems;
import org.qubership.atp.environments.db.generated.QEnvironments;
import org.qubership.atp.environments.db.generated.QJvCommit;
import org.qubership.atp.environments.db.generated.QJvCommitProperty;
import org.qubership.atp.environments.db.generated.QJvGlobalId;
import org.qubership.atp.environments.db.generated.QJvSnapshot;
import org.qubership.atp.environments.db.generated.QProjects;
import org.qubership.atp.environments.db.generated.QSubscribers;
import org.qubership.atp.environments.db.generated.QSubscriptions;
import org.qubership.atp.environments.db.generated.QSystemCategories;
import org.qubership.atp.environments.db.generated.QSystems;
import org.qubership.atp.environments.db.generated.QUpdateEvents;
import org.qubership.atp.environments.db.generated.QUserSettings;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractRepository {

    public static final QProjects PROJECTS = new QProjects("PROJECTS");
    public static final QEnvironments ENVIRONMENTS = new QEnvironments("ENVIRONMENTS");
    public static final QEnvironmentCategories ENVIRONMENT_CATEGORIES = new QEnvironmentCategories(
            "ENVIRONMENT_CATEGORIES");
    public static final QEnvironmentSystems ENVIRONMENT_SYSTEMS = new QEnvironmentSystems(
            "ENVIRONMENT_SYSTEMS");
    public static final QSystems SYSTEMS = new QSystems("SYSTEMS");
    public static final QSystemCategories SYSTEM_CATEGORIES = new QSystemCategories("SYSTEM_CATEGORIES");
    public static final QConnections CONNECTIONS = new QConnections("CONNECTIONS");
    public static final QSubscribers SUBSCRIBERS = new QSubscribers("SUBSCRIBERS");
    public static final QSubscriptions SUBSCRIPTIONS = new QSubscriptions("SUBSCRIPTIONS");
    public static final QUpdateEvents UPDATE_EVENTS = new QUpdateEvents("UPDATE_EVENTS");
    public static final QAlerts ALERTS = new QAlerts("ALERTS");
    public static final QAlertEvents ALERT_EVENTS = new QAlertEvents("ALERT_EVENTS");
    public static final QJvCommit JV_COMMIT = new QJvCommit("JV_COMMIT");
    public static final QJvCommitProperty JV_COMMIT_PROPERTY = new QJvCommitProperty("JV_COMMIT_PROPERTY");
    public static final QJvGlobalId JV_GLOBAL_ID = new QJvGlobalId("JV_GLOBAL_ID");
    public static final QJvSnapshot JV_SNAPSHOT = new QJvSnapshot("JV_SNAPSHOT");
    public static final QDatabaseDirectory DATABASE_DIRECTORY = new QDatabaseDirectory("DATABASE_DIRECTORY");
    public static final QUserSettings USER_SETTINGS = new QUserSettings("USER_SETTINGS");


    protected AbstractRepository() {
    }
}
