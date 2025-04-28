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

package org.qubership.atp.environments.versioning.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.javers.core.Javers;
import org.javers.core.commit.Commit;
import org.javers.repository.jql.GlobalIdDTO;
import org.javers.repository.jql.QueryBuilder;
import org.qubership.atp.environments.versioning.service.JaversAuthorProvider;
import org.qubership.atp.environments.versioning.service.JaversService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JaversServiceImpl implements JaversService {

    private static final String USERNAME_PROPERTY_NAME = "Username";

    private final Javers javers;
    private final JaversAuthorProvider authorProvider;

    @Value("${atp-environments.javers.enabled}")
    private boolean isJaversEnabled;

    public JaversServiceImpl(Javers javers, JaversAuthorProvider authorProvider) {
        this.javers = javers;
        this.authorProvider = authorProvider;
    }

    @Override
    public Commit commit(Object currentVersion) {
        return isJaversEnabled
                ? javers.commit(getAuthor(), currentVersion, getCommitProperties())
                : null;
    }

    @Override
    public CompletableFuture<Commit> commitAsync(Object currentVersion, Executor executor) {
        return isJaversEnabled
                ? javers.commitAsync(getAuthor(), currentVersion, getCommitProperties(), executor) :
                null;
    }

    @Override
    public Commit commitShallowDelete(Object deleted) {
        return isJaversEnabled
                ? javers.commitShallowDelete(getAuthor(), deleted, getCommitProperties())
                : null;
    }

    @Override
    public Commit commitShallowDeleteById(GlobalIdDTO globalId) {
        if (isJaversEnabled) {
            if (javers.findSnapshots(QueryBuilder.byGlobalId(globalId).build()).size() == 0) {
                return null;
            } else {
                return javers.commitShallowDeleteById(getAuthor(), globalId, getCommitProperties());
            }
        }
        return null;
    }

    private String getAuthor() {
        return authorProvider.provide();
    }

    private Map<String, String> getCommitProperties() {
        Map<String, String> commitProperties = new HashMap<>();
        commitProperties.put(USERNAME_PROPERTY_NAME, authorProvider.getUsername());
        return commitProperties;
    }
}
