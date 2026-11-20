package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

/**
 * Wizard page {@code dateOfHearingRecordAttendeesAndDuration} of event {@code recordTheDecision}.
 */
public final class DateOfHearingRecordAttendeesAndDurationPage {
    private DateOfHearingRecordAttendeesAndDurationPage() {
    }

    /**
     * Applies this page's fields to the event.
     *
     * @param fields the event's field-collection builder
     */
    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("dateOfHearingRecordAttendeesAndDuration");
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\"");
        fields.mandatory(CaseData::getAttendeesAndDurationDateOfHearing)
                    .publish(false);
    }
}
