package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.WithUserDetailsStub;

/**
 * The SDK reads the whole stored case into CaseData and writes it all back, so Notice of Change data
 * written by aac-manage-case-assignment must survive any SDK-served callback unchanged.
 */
@EnableAutoConfiguration(exclude = {CacheAutoConfiguration.class})
class NoticeOfChangeRoundTripTest extends SpringBootIntegrationTest implements WithUserDetailsStub {

    private static final String SERVICE_JWT_TOKEN = "Bearer service-token";
    private static final String USER_JWT_TOKEN = "Bearer user-token";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void keeps_an_organisation_policy_after_notices_of_change() throws Exception {
        JsonNode policy = fixture("localAuthorityPolicyAfterNoticesOfChange");

        JsonNode data = roundTrip(Map.of("localAuthorityPolicy", policy));

        assertThat(data.get("localAuthorityPolicy")).isEqualTo(policy);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void keeps_a_change_organisation_request_written_by_aac() throws Exception {
        JsonNode request = fixture("changeOrganisationRequestRequestedThroughAac");

        JsonNode data = roundTrip(Map.of("changeOrganisationRequestField", request));

        assertThat(data.get("changeOrganisationRequestField")).isEqualTo(request);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void keeps_both_together_on_one_case() throws Exception {
        JsonNode policy = fixture("localAuthorityPolicyAfterNoticesOfChange");
        JsonNode request = fixture("changeOrganisationRequestRequestedThroughAac");

        JsonNode data = roundTrip(Map.of(
            "localAuthorityPolicy", policy,
            "changeOrganisationRequestField", request
        ));

        assertThat(data.get("localAuthorityPolicy")).isEqualTo(policy);
        assertThat(data.get("changeOrganisationRequestField")).isEqualTo(request);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void keeps_the_request_aac_clears_after_applying_a_decision_less_its_null_members() throws Exception {
        JsonNode cleared = fixture("changeOrganisationRequestAppliedByAac");

        JsonNode data = roundTrip(Map.of("changeOrganisationRequestField", cleared));

        assertThat(data.get("changeOrganisationRequestField")).isEqualTo(withoutNullMembers(cleared));
    }

    private JsonNode roundTrip(Map<String, JsonNode> nocFields) throws Exception {
        addAdminOfficerUserDetailsStub(server);

        Map<String, Object> caseData = new HashMap<>(nocFields);
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

    private JsonNode fixture(String name) throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/notice-of-change-round-trip.json")) {
            return objectMapper.readTree(in).get(name).deepCopy();
        }
    }

    private static JsonNode withoutNullMembers(JsonNode node) {
        JsonNode copy = node.deepCopy();
        stripNullMembers(copy);
        return copy;
    }

    private static void stripNullMembers(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.properties().removeIf(member -> member.getValue().isNull());
            object.forEach(NoticeOfChangeRoundTripTest::stripNullMembers);
        }
    }
}
