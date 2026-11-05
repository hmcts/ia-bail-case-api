package uk.gov.hmcts.reform.bailcaseapi.ccd.hook;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.api.callback.PostEventHook;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;

/**
 * Post-event hook that copies the state the event ends in into the per-audience
 * {@code currentCaseStateVisibleTo*} fields read by the overview tab's show conditions, as
 * {@code CurrentCaseStateUpdater} does for dispatcher-served events.
 */
@Component
public class CurrentCaseStateHook implements PostEventHook<CaseData, State> {

    @Override
    public AboutToStartOrSubmitResponse<CaseData, State> handle(
        String eventId,
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore,
        AboutToStartOrSubmitResponse<CaseData, State> response
    ) {
        State state = response.getState() != null ? response.getState() : details.getState();
        CaseData data = response.getData() != null ? response.getData() : details.getData();
        if (state == null || data == null) {
            return response;
        }

        String stateId = state.name();
        data.setCurrentCaseStateVisibleToLegalRepresentative(stateId);
        data.setCurrentCaseStateVisibleToJudge(stateId);
        data.setCurrentCaseStateVisibleToAdminOfficer(stateId);
        data.setCurrentCaseStateVisibleToHomeOffice(stateId);
        data.setCurrentCaseStateVisibleToAllUsers(stateId);
        response.setData(data);
        return response;
    }
}
