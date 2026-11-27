package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

public final class SelectHearingRecordAttendeesAndDurationPage {
    private SelectHearingRecordAttendeesAndDurationPage() {
    }

    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("selectHearingRecordAttendeesAndDuration", "${CCD_DEF_BAIL_URL}/bail/ccdMidEvent?pageId=selectHearingRecordAttendeesAndDuration", 5, 5, 5, 5);
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\"");
        fields.mandatory(CaseData::getAttendeesAndDurationHearing)
                    .publish(false);
        fields.readonlyNoSummary(CaseData::getAttendeesAndDurationHearingsAvailable)
                    .fieldShowCondition("attendeesAndDurationHearingId=\"DUMMY_VALUE_TO_HIDE_FIELD\"")
                    .retainHiddenValue()
                    .publish(false);
    }
}
