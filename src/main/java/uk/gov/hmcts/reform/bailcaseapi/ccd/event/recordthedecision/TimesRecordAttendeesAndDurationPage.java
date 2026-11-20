package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

/**
 * Wizard page {@code timesRecordAttendeesAndDuration} of event {@code recordTheDecision}.
 */
public final class TimesRecordAttendeesAndDurationPage {
    private TimesRecordAttendeesAndDurationPage() {
    }

    /**
     * Applies this page's fields to the event.
     *
     * @param fields the event's field-collection builder
     */
    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("timesRecordAttendeesAndDuration");
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\" AND attendeesAndDurationHearingDayRequired=\"Yes\"");
        fields.mandatory(CaseData::getAttendeesAndDurationStartTime)
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationFinishTime)
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationPausedTimeRequired)
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationPauseStart)
                    .fieldShowCondition("attendeesAndDurationPausedTimeRequired=\"Yes\"")
                    .publish(false);
        fields.mandatory(CaseData::getAttendeesAndDurationPauseEnd)
                    .fieldShowCondition("attendeesAndDurationPausedTimeRequired=\"Yes\"")
                    .publish(false);
    }
}
