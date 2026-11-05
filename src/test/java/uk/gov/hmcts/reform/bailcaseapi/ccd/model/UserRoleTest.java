package uk.gov.hmcts.reform.bailcaseapi.ccd.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.config.JacksonConfiguration;

class UserRoleTest {

    private final ObjectMapper mapper = applicationObjectMapper();

    @Test
    void reads_and_writes_the_stored_case_role_unchanged() throws Exception {
        UserRole role = mapper.readValue("\"[LEGALREPRESENTATIVE]\"", UserRole.class);

        assertThat(role).isEqualTo(UserRole.LEGALREPRESENTATIVE);
        assertThat(mapper.writeValueAsString(role)).isEqualTo("\"[LEGALREPRESENTATIVE]\"");
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void every_role_round_trips_as_its_role_string(UserRole role) throws Exception {
        String json = mapper.writeValueAsString(role);

        assertThat(json).isEqualTo("\"" + role.getRole() + "\"");
        assertThat(mapper.readValue(json, UserRole.class)).isEqualTo(role);
    }

    private static ObjectMapper applicationObjectMapper() {
        JacksonConfiguration configuration = new JacksonConfiguration();
        return configuration.objectMapper(configuration.jackson2ObjectMapperBuilder());
    }
}
