package uk.gov.hmcts.reform.bailcaseapi.ccd.hook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;

class CurrentCaseStateHookTest {

    private final CurrentCaseStateHook hook = new CurrentCaseStateHook();

    @Test
    void stamps_the_state_the_response_moves_the_case_to() {
        CaseData data = new CaseData();
        var response = AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(State.decisionDecided)
            .build();

        var result = hook.handle("someEvent", details(State.unsignedDecision, new CaseData()), null, response);

        assertThat(result).isSameAs(response);
        assertThat(result.getData()).isSameAs(data);
        assertStamped(result.getData(), "decisionDecided");
    }

    @Test
    void stamps_the_case_state_when_the_response_leaves_it_unchanged() {
        var response = AboutToStartOrSubmitResponse.<CaseData, State>builder().data(new CaseData()).build();

        var result = hook.handle("addCaseNote", details(State.applicationSubmitted, new CaseData()), null, response);

        assertStamped(result.getData(), "applicationSubmitted");
        assertThat(result.getState()).isNull();
    }

    @Test
    void stamps_the_case_data_when_the_response_carries_none() {
        CaseData detailsData = new CaseData();
        detailsData.setAddCaseNoteSubject("Subject");
        var response = AboutToStartOrSubmitResponse.<CaseData, State>builder().build();

        var result = hook.handle("addCaseNote", details(State.bailSummaryUploaded, detailsData), null, response);

        assertThat(result.getData()).isSameAs(detailsData);
        assertThat(result.getData().getAddCaseNoteSubject()).isEqualTo("Subject");
        assertStamped(result.getData(), "bailSummaryUploaded");
    }

    private static CaseDetails<CaseData, State> details(State state, CaseData data) {
        return CaseDetails.<CaseData, State>builder().id(1234L).state(state).data(data).build();
    }

    private static void assertStamped(CaseData data, String stateId) {
        assertThat(data.getCurrentCaseStateVisibleToLegalRepresentative()).isEqualTo(stateId);
        assertThat(data.getCurrentCaseStateVisibleToJudge()).isEqualTo(stateId);
        assertThat(data.getCurrentCaseStateVisibleToAdminOfficer()).isEqualTo(stateId);
        assertThat(data.getCurrentCaseStateVisibleToHomeOffice()).isEqualTo(stateId);
        assertThat(data.getCurrentCaseStateVisibleToAllUsers()).isEqualTo(stateId);
    }
}
