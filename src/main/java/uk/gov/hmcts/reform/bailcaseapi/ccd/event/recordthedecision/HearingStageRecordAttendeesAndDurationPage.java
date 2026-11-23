package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

public final class HearingStageRecordAttendeesAndDurationPage {
    private HearingStageRecordAttendeesAndDurationPage() {
    }

    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("hearingStageRecordAttendeesAndDuration");
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\"");
        fields.mandatory(CaseData::getAttendeesAndDurationHearingStage)
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationHearingResult)
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationHearingCancellationReason)
                    .fieldShowCondition("attendeesAndDurationHearingResult=\"CANCELLED\"")
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationHearingPartHeardReason)
                    .fieldShowCondition("attendeesAndDurationHearingResult=\"ADJOURNED\"")
                    .publish(false);
    }
}
