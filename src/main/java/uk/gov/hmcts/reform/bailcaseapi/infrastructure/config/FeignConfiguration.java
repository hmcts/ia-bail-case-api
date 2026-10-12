package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@Slf4j
@SuppressWarnings("removal")
@Configuration
public class FeignConfiguration {

    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
        ObjectProvider<ClientHttpMessageConvertersCustomizer> converters,
        ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        return new FeignHttpMessageConverters(converters, customizers);
    }

    // Jackson 2 converter, because Boot 4 won't register one as a bean for you
    @Bean
    public HttpMessageConverter<?> jackson2HttpMessageConverter(ObjectMapper objectMapper) {
        return new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
    }

}
