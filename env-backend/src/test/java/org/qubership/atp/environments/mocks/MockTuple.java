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

package org.qubership.atp.environments.mocks;

import java.util.Arrays;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;

public class MockTuple implements Tuple {

    private final Object[] a;
    private Object returnValue;

    public MockTuple(Object[] a) {
        this.a = a;
    }

    public MockTuple(Object[] a, Object returnValue) {
        this.a = a;
        this.returnValue = returnValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(int index, Class<T> type) {
        return (T) a[index];
    }

    @Override
    public <T> T get(Expression<T> expr) {
        return (T) returnValue;
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public Object[] toArray() {
        return a;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Tuple) {
            return Arrays.equals(a, ((Tuple) obj).toArray());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(a);
    }

    @Override
    public String toString() {
        return Arrays.toString(a);
    }

}
