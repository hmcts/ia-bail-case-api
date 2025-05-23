package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.config.DisableHystrixFeignConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.bailcaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "timed-event-service-api",
    url = "${timed-event-service.url}",
    configuration = {TimedEventServiceApi.Configuration.class, DisableHystrixFeignConfiguration.class}
)
public interface TimedEventServiceApi {

    @PostMapping(value = "/timed-event", produces = "application/json", consumes = "application/json")
    TimedEvent submitTimedEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @RequestBody TimedEvent content
    );

    @DeleteMapping(value = "/timed-event/{id}", produces = "application/json", consumes = "application/json")
    void deleteTimedEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("id") String jobKey
    );


    class Configuration {

        @Bean
        public Decoder decoder(ObjectMapper objectMapper) {
            HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
            return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(jacksonConverter), new TimedEventMessageConverterCustomizer<>()));
        }

        @Bean
        @Scope("prototype")
        public Feign.Builder feignBuilder() {
            return Feign.builder();
        }

    }



}

