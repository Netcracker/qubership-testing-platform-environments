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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

public class Context {

    private Predicate<JsonStreamContext> unfoldPredicate = (ctx) -> false;

    private boolean isFullSerialization;

    private boolean isFullDbFetching;

    public Context(boolean isFullSerialization) {
        this.isFullSerialization = isFullSerialization;
        this.isFullDbFetching = isFullSerialization;
    }

    public Context() {
    }

    public boolean isFullSerialization() {
        return isFullSerialization;
    }

    public void setFullSerialization(boolean fullSerialization) {
        isFullSerialization = fullSerialization;
    }

    public void setFullDbFetching(boolean fullDbFetching) {
        isFullDbFetching = fullDbFetching;
    }

    public boolean isFullDbFetching() {
        return isFullDbFetching;
    }


    public Predicate<JsonStreamContext> getUnfoldPredicate() {
        return unfoldPredicate;
    }

    public void setFieldsToUnfold(String... fieldsToUnwrap) {
        this.unfoldPredicate = new UnfoldPredicate(fieldsToUnwrap);
    }

    private static class UnfoldPredicate implements Predicate<JsonStreamContext> {

        private final Set<String> fieldsToUnwrap;

        private UnfoldPredicate(String... fieldsToUnwrap) {
            this(new HashSet<>(Arrays.asList(fieldsToUnwrap)));
        }

        private UnfoldPredicate(Set<String> fieldsToUnwrap) {
            this.fieldsToUnwrap = fieldsToUnwrap;
        }

        private static Iterator<JsonStreamContext> pathToParent(JsonStreamContext context) {
            return Iterators.filter(new AbstractIterator<JsonStreamContext>() {
                private JsonStreamContext current = context;

                @Override
                protected JsonStreamContext computeNext() {
                    if (current == null) {
                        return endOfData();
                    }
                    JsonStreamContext toReturn = current;
                    current = current.getParent();
                    return toReturn;
                }
            }, mayBeArray -> !"Array".equals(mayBeArray.typeDesc()));
        }

        @Override
        public boolean test(JsonStreamContext context) {
            Iterator<JsonStreamContext> path = pathToParent(context);
            if (!path.hasNext()) {
                return false;
            }
            JsonStreamContext leaf = path.next();
            if (!path.hasNext()) {
                return false; //leaf is root actually
            } else {
                path.next();
                if (path.hasNext()) {
                    return false; //nesting level more than one
                }
            }
            return fieldsToUnwrap.contains(leaf.getCurrentName());
        }
    }
}
