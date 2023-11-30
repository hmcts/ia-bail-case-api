package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_FCS;

import java.util.Collections;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StrategicCaseFlagTest {

    private final String appellantName = "some-appellant-name";
    private StrategicCaseFlag strategicCaseFlag;

    @ParameterizedTest
    @ValueSource(strings = {ROLE_ON_CASE_APPLICANT, ROLE_ON_CASE_FCS})
    void should_hold_onto_values(String value) {
        strategicCaseFlag = new StrategicCaseFlag(appellantName, value);
        assertThat(strategicCaseFlag.getPartyName()).isEqualTo((appellantName));
        assertThat(strategicCaseFlag.getRoleOnCase()).isEqualTo((value));
        assertThat(strategicCaseFlag.getDetails()).isEqualTo((Collections.emptyList()));
    }
}
