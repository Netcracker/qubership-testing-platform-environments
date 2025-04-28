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

package org.qubership.atp.environments.model.utils;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.environments.model.Identified;

import com.google.common.base.Joiner;

public class Utils {

    public static final Joiner JOINER_DOT = Joiner.on('.');
    private static final Supplier<?> EMPTY_SUP = new Supplier<Object>() {

        @Override
        public String toString() {
            return "null supplier";
        }

        @Override
        public Object get() {
            return null;
        }
    };

    @Nonnull
    public static <T> Supplier<T> emptySup() {
        //noinspection unchecked
        return (Supplier<T>) EMPTY_SUP;
    }

    /**
     * To not to copy paste {@link Object#equals(Object)} logic.
     *
     * @param o1 object to compare with o2
     * @param o2 object to compare with o1
     * @return true if equals
     */
    public static boolean isEqual(@Nonnull Identified o1, @Nullable Object o2) {
        return isEqual(Identified.class, o1, o2);
    }

    /**
     * To not to copy paste {@link Object#equals(Object)} logic.
     *
     * @param clazz general superclass of o1 and o2
     * @param o1    object to compare with o2
     * @param o2    object to compare with o1
     * @param <T>   general superclass of o1 and o2
     * @return true if equals
     */
    public static <T extends Identified> boolean isEqual(@Nonnull Class<T> clazz,
                                                         @Nonnull T o1, @Nullable Object o2) {
        return o1 == o2 || isEqual(clazz, o1.getId(), o2);
    }

    /**
     * Checks target object has expected class and id.
     *
     * @param clazz  which should be extended by target
     * @param id     expected id of target
     * @param target checking object
     * @param <T>    type
     * @return true if target has expected type and id
     */
    @SuppressWarnings("SimplifiableIfStatement")
    public static <T extends Identified> boolean isEqual(@Nonnull Class<T> clazz,
                                                         @Nonnull UUID id, @Nullable Object target) {
        if (target == null) {
            return false;
        }
        if (!clazz.isAssignableFrom(target.getClass())) {
            return false;
        }
        return Objects.equals(id, clazz.cast(target).getId());
    }
}
