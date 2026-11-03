package uk.gov.hmcts.reform.bailcaseapi.ccd.hook;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.config.BailCaseDataConfiguration;

class DataFixingHookTest {

    private final DataFixingHook hook =
        new DataFixingHook(List.of(new BailCaseDataConfiguration().caseNameAppender()));

    @Test
    void repairs_the_case_name_from_the_applicant_names() {
        Map<String, Object> data = new HashMap<>();
        data.put("applicantGivenNames", "Jane");
        data.put("applicantFamilyName", "Doe");
        data.put("caseNameHmctsInternal", "Stale Name");

        Map<String, Object> fixed = hook.apply(data);

        assertThat(fixed)
            .containsEntry("caseNameHmctsInternal", "Jane Doe")
            .containsEntry("applicantGivenNames", "Jane")
            .containsEntry("applicantFamilyName", "Doe");
    }

    @Test
    void leaves_data_the_fixers_do_not_apply_to_unchanged() {
        Map<String, Object> data = Map.of("addCaseNoteSubject", "Subject");

        assertThat(hook.apply(data)).isEqualTo(data);
    }

    @Test
    void passes_absent_data_through() {
        assertThat(hook.apply(null)).isNull();
    }
}
