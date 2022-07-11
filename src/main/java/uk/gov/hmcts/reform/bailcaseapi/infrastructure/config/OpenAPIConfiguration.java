package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("case-event-handler-public")
            .pathsToMatch("/**")
            .addOpenApiCustomiser(publicApiCustomizer())
            .build();
    }

    @Bean
    public OpenApiCustomiser publicApiCustomizer() {
        return openApi -> openApi
            .components(new Components()
                .addSecuritySchemes("Authorization", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("Authorization")
                )
                .addSecuritySchemes("ServiceAuthorization", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("ServiceAuthorization")
                )
            );
    }

}
