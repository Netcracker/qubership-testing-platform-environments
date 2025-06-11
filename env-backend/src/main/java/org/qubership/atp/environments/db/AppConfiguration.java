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

package org.qubership.atp.environments.db;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.qubership.atp.environments.model.Identified;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.impl.MetricService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Configuration
public class AppConfiguration implements WebMvcConfigurer {

    private final ContextRepository repo;
    private final MetricService metricService;

    @Value("${spring.resources.static-locations}")
    private String webLocation;
    @Value("${application.web.root-page}")
    private String rootPage;
    @Value("${spring.resources.cache.period}")
    private Integer cachePeriodInSec;

    public AppConfiguration(ContextRepository repo, MetricService metricService) {
        this.repo = repo;
        this.metricService = metricService;
    }

    /**
     * Add path to static recources.
     * Enable version strategy got resources
     * Set cache period for resources in seconds
     *
     * @param registry for registration resources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(webLocation)
                .setCachePeriod(cachePeriodInSec)
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/** ** "))
                .addTransformer(new CssLinkResourceTransformer())
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@Nonnull String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists()
                                && requestedResource.isReadable()) {
                            return location.createRelative(resourcePath);
                        } else {
                            return new FileSystemResource(rootPage);
                        }
                    }
                });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ContextInterceptor(repo, metricService))
                .addPathPatterns("/api/**").addPathPatterns("/catalog/**");
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().setSerializerModifier(new SerModif(repo)));
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(
                new SimpleModule().addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer()));
        objectMapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(0, jsonConverter);
    }

    static class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

        private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

        @Override
        public void serialize(OffsetDateTime odt, JsonGenerator generator, SerializerProvider sp) throws IOException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            generator.writeString(formatter.format(odt));
        }
    }

    static class SerModif extends BeanSerializerModifier {

        private final ContextRepository contextRepo;

        private SerModif(ContextRepository contextRepo) {
            this.contextRepo = contextRepo;
        }

        @Override
        public JsonSerializer<?> modifySerializer(
                SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            if (Identified.class.isAssignableFrom(beanDesc.getBeanClass())
                    && serializer instanceof BeanSerializerBase) {
                return new FlatSerializer(contextRepo, (BeanSerializerBase) serializer);
            }
            return serializer;
        }
    }

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    static class FlatSerializer extends BeanSerializerBase {

        static final long serialVersionUID = 42L;
        private final transient ContextRepository contextRepo;

        BeanSerializerBase defaultSerializer;

        public FlatSerializer(ContextRepository contextRepo, BeanSerializerBase src) {
            super(src);
            this.contextRepo = contextRepo;
            defaultSerializer = src;
        }

        protected FlatSerializer(BeanSerializerBase src, Set<String> toIgnore, ContextRepository contextRepo) {
            super(src, toIgnore);
            this.contextRepo = contextRepo;
            defaultSerializer = src;
        }

        protected FlatSerializer(BeanSerializerBase src,
                                 ObjectIdWriter oiw, ContextRepository contextRepo) {
            super(src, oiw);
            defaultSerializer = src;
            this.contextRepo = contextRepo;
        }

        protected FlatSerializer(BeanSerializerBase src,
                                 BeanPropertyWriter[] properties,
                                 BeanPropertyWriter[] filteredProperties,
                                 ContextRepository contextRepo) {
            super(src, properties, filteredProperties);
            this.contextRepo = contextRepo;
            defaultSerializer = src;
        }

        protected FlatSerializer(BeanSerializerBase src,
                                 Set<String> toIgnore,
                                 Set<String> toInclude,
                                 ContextRepository contextRepo) {
            super(src, toIgnore, toInclude);
            this.contextRepo = contextRepo;
            defaultSerializer = src;
        }

        protected FlatSerializer(BeanSerializerBase src,
                                 ObjectIdWriter oiw, Object filterId, ContextRepository contextRepo) {
            super(src, oiw, filterId);
            defaultSerializer = src;
            this.contextRepo = contextRepo;
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new FlatSerializer(defaultSerializer, objectIdWriter, contextRepo);
        }

        @Override
        protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
            return new FlatSerializer(defaultSerializer, toIgnore, contextRepo);
        }

        @Override
        protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
            return new FlatSerializer(this, toIgnore, toInclude, contextRepo);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            if (_objectIdWriter == null && _anyGetterWriter == null && _propertyFilterId == null) {
                return new BeanAsArraySerializer(this);
            }
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new FlatSerializer(this, _objectIdWriter, filterId, contextRepo);
        }

        @Override
        protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties,
                                                    BeanPropertyWriter[] filteredProperties) {
            return new FlatSerializer(this, properties, filteredProperties, contextRepo);
        }

        @Override
        public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (_objectIdWriter != null) {
                gen.setCurrentValue(bean); // [databind#631]
                flatSerializeWithObjectId(bean, gen, provider, true, () -> asId(gen));
                return;
            }
            gen.writeStartObject(bean);
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
            gen.writeEndObject();
        }

        protected void flatSerializeWithObjectId(Object bean, JsonGenerator gen, SerializerProvider provider,
                                                 boolean startEndObject, Supplier<Boolean> asId) throws IOException {
            final ObjectIdWriter w = _objectIdWriter;
            WritableObjectId objectId = provider.findObjectId(bean, w.generator);
            if ("Array".equals(gen.getOutputContext().typeDesc())) {
                boolean b = w.alwaysAsId || asId.get();
                // If not, need to inject the id:
                Object id = objectId.generateId(bean);
                if (b && id != null) {
                    w.serializer.serialize(id, gen, provider);
                    return;
                }
                fullSerialize(bean, gen, provider, startEndObject, w, objectId);
            } else {
                // If not, need to inject the id:
                Object id = objectId.generateId(bean);
                if (w.alwaysAsId || asId.get()) {
                    w.serializer.serialize(id, gen, provider);
                    return;
                }
                fullSerialize(bean, gen, provider, startEndObject, w, objectId);
            }
        }

        private void fullSerialize(Object bean, JsonGenerator gen,
                                   SerializerProvider provider,
                                   boolean startEndObject,
                                   ObjectIdWriter w,
                                   WritableObjectId objectId) throws IOException {
            if (startEndObject) {
                gen.writeStartObject(bean);
            }
            objectId.writeAsField(gen, provider, w);
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
            if (startEndObject) {
                gen.writeEndObject();
            }
        }

        private boolean asId(JsonGenerator gen) {
            JsonStreamContext current = gen.getOutputContext();
            if (contextRepo.getContext().isFullSerialization()) {
                if (contextRepo.getContext().getUnfoldPredicate().test(gen.getOutputContext())) {
                    return false;
                }
                Identified parent = null;
                Identified child = (Identified) current.getCurrentValue();
                while (!current.inRoot()) {
                    current = current.getParent();
                    Object objToCheck = current.getCurrentValue();
                    if (objToCheck == null) {
                        continue;
                    }
                    if (Identified.class.isAssignableFrom(objToCheck.getClass()) && !child.equals(objToCheck)) {
                        parent = (Identified) objToCheck;
                        break;
                    }
                }
                if (parent == null) {
                    return false;
                }
                return !child.isParent(parent);
            }
            while (!current.inRoot()) {
                current = current.getParent();
                Object objToCheck = current.getCurrentValue();
                if (objToCheck == null) {
                    continue;
                }
                if (Identified.class.isAssignableFrom(objToCheck.getClass())) {
                    return true;
                }
            }
            return false;
        }
    }
}
