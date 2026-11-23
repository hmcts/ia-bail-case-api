package uk.gov.hmcts.reform.bailcaseapi.ccd.event.recordthedecision;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.HearingParticipant;

public final class ParticipantsRecordAttendeesAndDurationPage {
    private ParticipantsRecordAttendeesAndDurationPage() {
    }

    public static void apply(
            FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fields) {
        fields.page("participantsRecordAttendeesAndDuration");
        fields.pageLabel("Participants");
        fields.showCondition("attendeesAndDurationHearingsAvailable=\"Yes\" AND attendeesAndDurationHearingDayRequired=\"Yes\"");
        fields.list(CaseData::getAttendeesAndDurationParticipants)
                    .readonly(HearingParticipant::getFirstNameLabel)
                    .mandatory(HearingParticipant::getFirstName)
                    .readonly(HearingParticipant::getLastNameLabel)
                    .mandatory(HearingParticipant::getLastName)
                    .readonly(HearingParticipant::getRoleLabel)
                    .mandatory(HearingParticipant::getRole)
                    .optional(HearingParticipant::getRepresentedParty)
                    .mandatory(HearingParticipant::getAttendanceType)
                    .optional(HearingParticipant::getOrganisation)
                    .readonly(HearingParticipant::getPartyId)
                    .readonly(HearingParticipant::getPrefilled)
                    .done();
        fields.readonlyNoSummary(CaseData::getAttendeesAndDurationParticipantsSource)
                    .fieldShowCondition("attendeesAndDurationHearingId=\"DUMMY_VALUE_TO_HIDE_FIELD\"")
                    .retainHiddenValue()
                    .publish(false);
    }
}
