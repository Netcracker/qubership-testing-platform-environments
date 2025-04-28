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

package org.qubership.atp.environments.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.qubership.atp.environments.errorhandling.hazelcastcache.EnvironmentHazelcastCacheIllegalValueTypeException;
import org.qubership.atp.environments.errorhandling.hazelcastcache.EnvironmentHazelcastCacheLookupTimeoutException;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.hazelcast.internal.util.ExceptionUtil;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvironmentHazelcastCache implements Cache {

    private  int ttl;
    private TimeUnit timeUnit;


    /**
     * Sprint related {@link Cache} implementation for Environment.
     */
    public EnvironmentHazelcastCache(IMap<Object, Object> map,
                                     int ttl, TimeUnit timeUnit) {
        this.ttl = ttl;
        this.timeUnit = timeUnit;
        this.map = map;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    private static final DataSerializable NULL = new NullDataSerializable();

    private final IMap<Object, Object> map;

    /**
     * Read timeout for cache value retrieval operations.
     * If {@code 0} or negative, get() operations block, otherwise uses getAsync() with defined timeout.
     */
    private long readTimeout;


    @Override
    public String getName() {
        return map.getName();
    }

    @Override
    public IMap<Object, Object> getNativeCache() {
        return map;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (key == null) {
            return null;
        }
        Object value = lookup(key);
        return value != null ? new SimpleValueWrapper(fromStoreValue(value)) : null;
    }

    /**
     * get system cache entity.
     *
     * @return system cache entity
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Object value = fromStoreValue(lookup(key));
        if (type != null && value != null && !type.isInstance(value)) {
            log.error("Cached value '{}' is not of required type: {}", value, type.getName());
            throw new EnvironmentHazelcastCacheIllegalValueTypeException();
        }
        return (T) value;
    }

    /**
     * get system cache entity.
     *
     * @return system cache entity
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) fromStoreValue(value);
        } else {
            this.map.lock(key);
            try {
                value = lookup(key);
                if (value != null) {
                    return (T) fromStoreValue(value);
                } else {
                    return loadValue(key, valueLoader);
                }
            } finally {
                this.map.unlock(key);
            }
        }
    }

    private <T> T loadValue(Object key, Callable<T> valueLoader) {
        T value;
        try {
            value = valueLoader.call();
        } catch (Exception ex) {
            throw ValueRetrievalExceptionResolver.resolveException(key, valueLoader, ex);
        }
        put(key, value);
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        if (key != null) {
            map.put(key, toStoreValue(value),ttl,timeUnit);
        }
    }

    protected Object toStoreValue(Object value) {
        if (value == null) {
            return NULL;
        }
        return value;
    }

    protected Object fromStoreValue(Object value) {
        if (NULL.equals(value)) {
            return null;
        }
        return value;
    }

    @Override
    public void evict(Object key) {
        if (key != null) {
            map.delete(key);
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object result = map.putIfAbsent(key, toStoreValue(value),ttl,timeUnit);
        return result != null ? new SimpleValueWrapper(fromStoreValue(result)) : null;
    }

    private Object lookup(Object key) {
        if (readTimeout > 0) {
            try {
                return this.map.getAsync(key).toCompletableFuture().get(readTimeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.error("The lookup cache operation for key '{}' exceeded the maximum time allowed", key, e);
                throw new EnvironmentHazelcastCacheLookupTimeoutException();
            } catch (InterruptedException e) {
                log.error("Interrupted exception during hazelcast cache lookup operation for key '{}'", key, e);
                Thread.currentThread().interrupt();
                throw ExceptionUtil.rethrow(e);
            } catch (Exception e) {
                log.error("Exception during hazelcast cache lookup operation for key '{}'", key, e);
                throw ExceptionUtil.rethrow(e);
            }
        }
        return this.map.get(key);
    }

    static final class NullDataSerializable implements DataSerializable {

        @Override
        public void writeData(ObjectDataOutput out) {
        }

        @Override
        public void readData(ObjectDataInput in) {
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass() == getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    private static class ValueRetrievalExceptionResolver {

        static RuntimeException resolveException(Object key, Callable<?> valueLoader,
                                                 Throwable ex) {
            return new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }
}
