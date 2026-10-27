package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.WithUserDetailsStub;

@EnableAutoConfiguration(exclude = {CacheAutoConfiguration.class})
class AddCaseNoteTest extends SpringBootIntegrationTest implements WithUserDetailsStub {

    private static final String SERVICE_JWT_TOKEN = "Bearer service-token";
    private static final String USER_JWT_TOKEN = "Bearer user-token";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void adds_a_case_note() throws Exception {

        addAdminOfficerUserDetailsStub(server);

        JsonNode data = aboutToSubmit(Map.of(
            "addCaseNoteSubject", "some-subject",
            "addCaseNoteDescription", "some-description",
            "applicantGivenNames", "some-given-name",
            "applicantFamilyName", "some-family-name"
        ));

        JsonNode caseNotes = data.get("caseNotes");
        assertThat(caseNotes).hasSize(1);

        JsonNode caseNote = caseNotes.get(0).get("value");
        assertThat(caseNotes.get(0).get("id").asText()).isEqualTo("1");
        assertThat(caseNote.get("user").asText()).isEqualTo("Admin Officer");
        assertThat(caseNote.get("caseNoteSubject").asText()).isEqualTo("some-subject");
        assertThat(caseNote.get("caseNoteDescription").asText()).isEqualTo("some-description");
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-admofficer"})
    void clears_the_entry_fields_so_the_next_note_starts_empty() throws Exception {

        addAdminOfficerUserDetailsStub(server);

        JsonNode data = aboutToSubmit(Map.of(
            "addCaseNoteSubject", "some-subject",
            "addCaseNoteDescription", "some-description"
        ));

        assertThat(data.hasNonNull("addCaseNoteSubject")).isFalse();
        assertThat(data.hasNonNull("addCaseNoteDescription")).isFalse();
    }

    private JsonNode aboutToSubmit(Map<String, Object> caseData) throws Exception {

        Map<String, Object> request = Map.of(
            "event_id", "addCaseNote",
            "case_details", Map.of(
                "id", 1234,
                "jurisdiction", "IA",
                "case_type_id", "Bail",
                "state", "applicationSubmitted",
                "case_data", caseData
            )
        );

        MvcResult result = mockMvc
            .perform(post("/callbacks/about-to-submit")
                .header("Authorization", USER_JWT_TOKEN)
                .header("ServiceAuthorization", SERVICE_JWT_TOKEN)
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_VALUE))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        return objectMapper
            .readTree(result.getResponse().getContentAsString())
            .get("data");
    }
}
