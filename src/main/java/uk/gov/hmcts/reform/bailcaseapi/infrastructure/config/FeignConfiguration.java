package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter;

@Slf4j
@SuppressWarnings("removal")
@Configuration
public class FeignConfiguration {

//    @Bean
//    public FeignHttpMessageConverters feignHttpMessageConverters(
//        ObjectProvider<ClientHttpMessageConvertersCustomizer> converters,
//        ObjectProvider<HttpMessageConverterCustomizer> customizers) {
//        return new FeignHttpMessageConverters(converters, customizers);
//    }
//
//    // Jackson 2 converter, because Boot 4 won't register one as a bean for you
//    @Bean
//    public HttpMessageConverter<?> jackson2HttpMessageConverter(ObjectMapper objectMapper) {
//        return new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
//    }

    @Bean
    public HttpMessageConverterCustomizer feignJacksonConverterCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return converters -> {
            log.info("feign converters BEFORE: {}", converters.stream().map(c -> c.getClass().getSimpleName()).toList());
            converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter
                || c instanceof MappingJackson2YamlHttpMessageConverter);

            int idx = 0;
            for (int i = 0; i < converters.size(); i++) {
                if (converters.get(i) instanceof ByteArrayHttpMessageConverter
                    || converters.get(i) instanceof StringHttpMessageConverter) {
                    idx = i + 1;
                }
            }
            converters.add(idx, new MappingJackson2HttpMessageConverter(objectMapper));
            log.info("feign converters AFTER: {}", converters.stream().map(c -> c.getClass().getSimpleName()).toList());
        };
    }

    @Bean
    public ClientHttpMessageConvertersCustomizer jacksonClientCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
        return builder -> builder.withJsonConverter(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Bean
    @Qualifier("feign")
    public ObjectMapper feignObjectMapper(org.springframework.http.converter.json.Jackson2ObjectMapperBuilder builder) {
        return builder
            .modulesToInstall(
                new Jdk8Module(),
                new JavaTimeModule()
            )
            .build();
    }

    // TEMP for test logs
    @Bean
    ApplicationRunner dumpFeignConverters(ObjectProvider<FeignHttpMessageConverters> p) {
        return args -> p.ifAvailable(c ->
                                         c.getConverters().forEach(x -> log.info("feign converter: {}", x.getClass())));
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

}
