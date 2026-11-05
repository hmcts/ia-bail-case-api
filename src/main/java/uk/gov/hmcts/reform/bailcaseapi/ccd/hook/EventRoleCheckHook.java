package uk.gov.hmcts.reform.bailcaseapi.ccd.hook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.api.callback.PostEventHook;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

/**
 * Post-event hook that refuses an about-to-submit from a caller whose roles do not grant the event
 * in {@code security.roleEventAccess}. An event id with no {@link Event} constant is checked as
 * {@link Event#UNKNOWN}, which no role grants, so it is refused.
 */
@Component
@RequiredArgsConstructor
public class EventRoleCheckHook implements PostEventHook<CaseData, State> {

    private final CcdEventAuthorizor ccdEventAuthorizor;

    @Override
    public AboutToStartOrSubmitResponse<CaseData, State> handle(
        String eventId,
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore,
        AboutToStartOrSubmitResponse<CaseData, State> response
    ) {
        ccdEventAuthorizor.throwIfNotAuthorized(Event.fromId(eventId));
        return response;
    }
}
