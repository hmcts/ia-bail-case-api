package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

public final class HearingDataRecordAttendeesAndDurationPage {
    private HearingDataRecordAttendeesAndDurationPage() {
    }

    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("hearingDataRecordAttendeesAndDuration");
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\"");
        fields.readonly(CaseData::getAttendeesAndDurationCaseReference)
                    .publish(false);
        fields.readonly(CaseData::getAttendeesAndDurationHearingId)
                    .publish(false);
        fields.readonly(CaseData::getAttendeesAndDurationHearingDateDisplay)
                    .publish(false);
        fields.readonly(CaseData::getAttendeesAndDurationPaperHearing)
                    .publish(false);
        fields.readonly(CaseData::getAttendeesAndDurationHearingMethods)
                    .publish(false);
    }
}
