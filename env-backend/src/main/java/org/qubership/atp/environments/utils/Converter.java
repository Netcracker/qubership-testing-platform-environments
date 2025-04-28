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

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class Converter {

    protected static final ModelMapper INTERNAL_MAPPER = new ModelMapper();

    static {
        INTERNAL_MAPPER.getConfiguration().getConverters().add(new Timestamp2OffsetDateTimeConverter());
        INTERNAL_MAPPER.getConfiguration().getConverters().add(new OffsetDateTime2TimestampConverter());
    }

    public static class Timestamp2OffsetDateTimeConverter implements ConditionalConverter<Timestamp, OffsetDateTime> {
        @Override
        public MatchResult match(Class<?> sourceType, Class<?> destinationType) {
            if (Timestamp.class.isAssignableFrom(sourceType)
                    && OffsetDateTime.class.isAssignableFrom(destinationType)) {
                return MatchResult.FULL;
            }
            return MatchResult.NONE;
        }

        @Override
        public OffsetDateTime convert(MappingContext<Timestamp, OffsetDateTime> context) {
            if (context.getSource() == null) {
                return null;
            }
            return OffsetDateTime.ofInstant(context.getSource().toInstant(), ZoneOffset.UTC);
        }
    }

    public static class OffsetDateTime2TimestampConverter implements ConditionalConverter<OffsetDateTime, Timestamp> {
        @Override
        public MatchResult match(Class<?> sourceType, Class<?> destinationType) {
            if (OffsetDateTime.class.isAssignableFrom(sourceType)
                    && Timestamp.class.isAssignableFrom(destinationType)) {
                return MatchResult.FULL;
            }
            return MatchResult.NONE;
        }

        @Override
        public Timestamp convert(MappingContext<OffsetDateTime, Timestamp> context) {
            if (context.getSource() == null) {
                return null;
            }
            return new Timestamp(context.getSource().toEpochSecond());
        }
    }

    /**
     * Convert t.
     *
     * @param <T>  the type parameter
     * @param from the from
     * @param to   the to
     * @return the t
     */
    public <T> T convert(Object from, Class<T> to) {
        return INTERNAL_MAPPER.map(from, to);
    }

    /**
     * Convert list list.
     *
     * @param <T>  the type parameter
     * @param from the from
     * @param to   the to
     * @return the list
     */
    public <T> List<T> convertList(List from, Class<T> to) {
        if (from == null) {
            return new ArrayList<>();
        }
        return (List) from.stream().map(o -> convert(o, to)).collect(Collectors.toList());
    }
}

