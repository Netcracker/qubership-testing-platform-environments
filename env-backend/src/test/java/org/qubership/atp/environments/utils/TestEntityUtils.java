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

package org.qubership.atp.environments.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.qubership.atp.environments.model.Connection;
import org.qubership.atp.environments.model.ConnectionParameters;
import org.qubership.atp.environments.model.Subscription;
import org.qubership.atp.environments.model.UpdateEvent;
import org.qubership.atp.environments.model.impl.ConnectionImpl;
import org.qubership.atp.environments.model.impl.SubscriptionImpl;
import org.qubership.atp.environments.model.impl.UpdateEventImpl;
import org.qubership.atp.environments.model.utils.Constants;

public class TestEntityUtils {

    public static Connection createTaEngineConnection() {
        Connection newConnection = new ConnectionImpl();
        newConnection.setParameters(new ConnectionParameters());
        newConnection.getParameters().putAll(
                Stream.of(new String[][]{
                        {"Acquire_Create_Tool_Request_Body",
                                "{\"image\":\"artifactory-url/path-to-image\",\"args\":[\"-version=1.1\",\"-git=some-url/*.jar+actions\",\"-git=some-other-url/*+files\",\"-svn=one-more-url/+*.jar+resources/dependencies\",\"-jvm=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005\",\"--argline=-log=debug -atp.log=TEST -lifetimeout=9 -ntt.web.service.enabled=test -ntt.web.service.port=test webdriver.capabilities.name=advance-analytics-platform-05c1r chrome.option.1=--no-sandbox -ram.mode=test -environments.url=test -Dbootstrap.servers=test -Datp.ram.url=test webdriver.hub.url=http://ggr.selenoid.svc:5555/wd/hub\"],\"name\":\"advance-analytics-platform\",\"env\":[{\"name\":\"MAX_RAM\",\"value\":\"2048m\"},{\"name\":\"GRAYLOG_HOST\",\"value\":\"tcp:graylog-service-address\"},{\"name\":\"GRAYLOG_PORT\",\"value\":\"12201\"},{\"name\":\"Datp2.ram.enabled\",\"value\":\"test\"},{\"name\":\"Datp.ram.url\",\"value\":\"test\"},{\"name\":\"Dbootstrap.servers\",\"value\":\"test\"},{\"name\":\"Dram.adapter.type\",\"value\":\"test\"},{\"name\":\"Dkafka.topic.name\",\"value\":\"test\"}]}"
                        }})
                        .collect(HashMap::new, (m, v) -> m.put(v[0], v[1]), HashMap::putAll));
        return newConnection;
    }

    public static void processIndexZipForLists(List<?> firstList, List<?> secondList, Consumer<Integer> consumer) { ;
        IntStream.range(0, Math.min(firstList.size(), secondList.size()))
                .boxed()
                .forEach(consumer);

    }

    public static List<Connection> createConnectionList() {
        Connection connection1 = createConnection("connection1", new ConnectionParameters(),
                Constants.Environment.System.Connection.HTTP);
        Connection connection2 = createConnection("connection2", new ConnectionParameters(),
                Constants.Environment.System.Connection.DB);
        Connection connection3 = createConnection("connection3", new ConnectionParameters(),
                Constants.Environment.System.Connection.SSH);
        return new ArrayList<>(Arrays.asList(connection1, connection2, connection3));
    }

    public static Connection createConnection(String name,
                                              ConnectionParameters parameters,
                                              UUID sourceTemplateId) {
        Connection connection = new ConnectionImpl();
        connection.setName(name);
        connection.setId(UUID.randomUUID());
        connection.setSystemId(UUID.randomUUID());
        connection.setParameters(parameters);
        connection.setSourceTemplateId(sourceTemplateId);
        return connection;
    }

    public static List<UpdateEvent> createUpdateEventList() {
        UpdateEventImpl updateEvent1 = new UpdateEventImpl();
        Subscription subscription = new SubscriptionImpl();
        subscription.setId(UUID.randomUUID());
        updateEvent1.setSubscription(subscription);
        updateEvent1.setEntityId(UUID.randomUUID());
        updateEvent1.setSubscriptionId(subscription.getId());
        UpdateEventImpl updateEvent2 = new UpdateEventImpl();
        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setId(UUID.randomUUID());
        updateEvent2.setSubscription(subscription2);
        updateEvent2.setEntityId(UUID.randomUUID());
        updateEvent2.setSubscriptionId(subscription2.getId());
        return new ArrayList<>(Arrays.asList(updateEvent1, updateEvent2));
    }

}
