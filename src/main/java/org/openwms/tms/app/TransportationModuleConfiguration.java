/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.tms.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micrometer.core.instrument.MeterRegistry;
import org.ameba.IDGenerator;
import org.ameba.JdkIDGenerator;
import org.ameba.annotation.EnableAspects;
import org.ameba.http.EnableMultiTenancy;
import org.ameba.http.RequestIDFilter;
import org.ameba.i18n.AbstractTranslator;
import org.ameba.i18n.Translator;
import org.openwms.tms.impl.TransportOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * A TransportationModuleConfiguration.
 *
 * @author Heiko Scherrer
 */
@Configuration
@EnableCaching
@EnableSpringConfigured
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaAuditing
@EnableJpaRepositories(basePackageClasses = TransportOrderRepository.class)
@EnableAspects(propagateRootCause = true)
@EnableMultiTenancy
class TransportationModuleConfiguration {

    public
    @Primary
    @Bean(name = "jacksonOM")
    ObjectMapper jackson2ObjectMapper() {
        var om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return om;
    }

    /*~ ------------- i18n handling ----------- */
    public
    @Bean
    LocaleResolver localeResolver() {
        var slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

    public
    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        var lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    public
    @Bean
    Translator translator() {
        return new AbstractTranslator() {
            @Override
            protected MessageSource getMessageSource() {
                return messageSource();
            }
        };
    }

    public
    @Bean
    MessageSource messageSource() {
        var nrrbm = new ResourceBundleMessageSource();
        nrrbm.setBasename("i18n");
        return nrrbm;
    }

    /*~ ------------- Request ID handling ----------- */
    public
    @Bean
    IDGenerator<String> uuidGenerator() {
        return new JdkIDGenerator();
    }

    public
    @Bean
    FilterRegistrationBean<RequestIDFilter> requestIDFilter(IDGenerator<String> uuidGenerator) {
        var frb = new FilterRegistrationBean<>(new RequestIDFilter(uuidGenerator));
        frb.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return frb;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(@Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }
}
