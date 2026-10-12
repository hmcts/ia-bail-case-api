package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@SuppressWarnings("removal")
@Configuration
public class FeignConfiguration {

    @Bean
    @Primary
    public Encoder feignFormEncoder(
        ObjectProvider<FeignHttpMessageConverters> messageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    // TEMP for test logs
    @Bean
    ApplicationRunner dumpFeignConverters(ObjectProvider<FeignHttpMessageConverters> p) {
        return args -> p.ifAvailable(c ->
                                         c.getConverters().forEach(x ->
                                                                       log.info("feign converter: {}", x.getClass())));
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

}
