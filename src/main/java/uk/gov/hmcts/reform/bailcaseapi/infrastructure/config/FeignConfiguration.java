package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.StringHttpMessageConverter;

@Slf4j
@SuppressWarnings("removal")
@Configuration
public class FeignConfiguration {

//    @Bean
//    public HttpMessageConverterCustomizer feignJacksonConverterCustomizer(
//        ObjectMapper objectMapper) {
//        return converters -> {
//            log.info("feign converters BEFORE: {}", converters.stream().map(
//                c -> c.getClass().getSimpleName()).toList());
//            converters.removeIf(
//                c ->
//                    c instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
//                || c instanceof org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter);
//
//            int idx = 0;
//            for (int i = 0; i < converters.size(); i++) {
//                if (converters.get(i) instanceof ByteArrayHttpMessageConverter
//                    || converters.get(i) instanceof StringHttpMessageConverter) {
//                    idx = i + 1;
//                }
//            }
//            converters.add(idx,
//                           new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper)
//            );
//            log.info("feign converters AFTER: {}", converters.stream().map(
//                c -> c.getClass().getSimpleName()).toList());
//        };
//    }
//
//    @Bean
//    public ClientHttpMessageConvertersCustomizer jacksonClientCustomizer(@Qualifier("feign") ObjectMapper objectMapper) {
//        return builder -> builder.withJsonConverter(
//            new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper));
//    }

    @Primary
    public Encoder feignFormEncoder(
        ObjectProvider<FeignHttpMessageConverters> messageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

}
