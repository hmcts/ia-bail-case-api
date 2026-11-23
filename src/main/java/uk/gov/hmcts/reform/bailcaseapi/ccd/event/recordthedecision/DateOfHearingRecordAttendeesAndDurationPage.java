package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

public final class DateOfHearingRecordAttendeesAndDurationPage {
    private DateOfHearingRecordAttendeesAndDurationPage() {
    }

    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("dateOfHearingRecordAttendeesAndDuration");
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\"");
        fields.mandatory(CaseData::getAttendeesAndDurationDateOfHearing)
                    .publish(false);
    }
}
