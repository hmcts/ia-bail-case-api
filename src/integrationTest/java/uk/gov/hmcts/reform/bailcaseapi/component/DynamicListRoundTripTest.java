package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.WithUserDetailsStub;

/**
 * Bail's DynamicLists carry String codes (reference data ids, direction names, application numbers),
 * and the SDK reads and writes every one of them on each SDK-served callback.
 */
@EnableAutoConfiguration(exclude = {CacheAutoConfiguration.class})
class DynamicListRoundTripTest extends SpringBootIntegrationTest implements WithUserDetailsStub {

    private static final String SERVICE_JWT_TOKEN = "Bearer service-token";
    private static final String USER_JWT_TOKEN = "Bearer user-token";

    static List<String> fields() throws Exception {
        try (InputStream in = fixtureStream()) {
            List<String> fields = new ArrayList<>();
            new ObjectMapper().readTree(in).fieldNames().forEachRemaining(fields::add);
            return fields;
        }
    }

    @ParameterizedTest
    @MethodSource("fields")
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void keeps_a_dynamic_list_field_unchanged(String field) throws Exception {
        JsonNode value = fixture().get(field);

        JsonNode data = roundTrip(Map.of(field, value));

        assertThat(data.get(field)).isEqualTo(value);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void keeps_every_dynamic_list_field_together_on_one_case() throws Exception {
        JsonNode fixture = fixture();
        Map<String, JsonNode> fields = new HashMap<>();
        fixture.properties().forEach(entry -> fields.put(entry.getKey(), entry.getValue()));

        JsonNode data = roundTrip(fields);

        fields.forEach((field, value) -> assertThat(data.get(field)).as(field).isEqualTo(value));
    }

    private JsonNode roundTrip(Map<String, JsonNode> fields) throws Exception {
        addAdminOfficerUserDetailsStub(server);

        Map<String, Object> caseData = new HashMap<>(fields);
        caseData.put("addCaseNoteSubject", "some-subject");
        caseData.put("addCaseNoteDescription", "some-description");

        MvcResult result = mockMvc
            .perform(post("/callbacks/about-to-submit")
                .header("Authorization", USER_JWT_TOKEN)
                .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                .content(objectMapper.writeValueAsString(Map.of(
                    "event_id", "addCaseNote",
                    "case_details", Map.of(
                        "id", 1234,
                        "jurisdiction", "IA",
                        "case_type_id", "Bail",
                        "state", "applicationSubmitted",
                        "case_data", caseData
                    )
                )))
                .contentType(APPLICATION_JSON_VALUE))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    private JsonNode fixture() throws Exception {
        try (InputStream in = fixtureStream()) {
            return objectMapper.readTree(in);
        }
    }

    private static InputStream fixtureStream() {
        return DynamicListRoundTripTest.class.getResourceAsStream("/fixtures/dynamic-list-round-trip.json");
    }
}
