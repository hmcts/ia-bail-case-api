package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("addCaseNote", Event.ADD_CASE_NOTE.toString());
        assertEquals("applyNocDecision", Event.APPLY_NOC_DECISION.toString());
        assertEquals("caseListing", Event.CASE_LISTING.toString());
        assertEquals("changeBailDirectionDueDate", Event.CHANGE_BAIL_DIRECTION_DUE_DATE.toString());
        assertEquals("clearLegalRepresentativeDetails", Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS.toString());
        assertEquals("confirmDetentionLocation", Event.CONFIRM_DETENTION_LOCATION.toString());
        assertEquals("createBailCaseLink", Event.CREATE_BAIL_CASE_LINK.toString());
        assertEquals("createFlag", Event.CREATE_FLAG.toString());
        assertEquals("editBailApplication", Event.EDIT_BAIL_APPLICATION.toString());
        assertEquals("editBailApplicationAfterSubmit", Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString());
        assertEquals("editBailDocuments", Event.EDIT_BAIL_DOCUMENTS.toString());
        assertEquals("endApplication", Event.END_APPLICATION.toString());
        assertEquals("imaStatus", Event.IMA_STATUS.toString());
        assertEquals("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS.toString());
        assertEquals("makeNewApplication", Event.MAKE_NEW_APPLICATION.toString());
        assertEquals("moveApplicationToDecided", Event.MOVE_APPLICATION_TO_DECIDED.toString());
        assertEquals("nocRequest", Event.NOC_REQUEST.toString());
        assertEquals("nocRequestBail", Event.NOC_REQUEST_BAIL.toString());
        assertEquals("recordTheDecision", Event.RECORD_THE_DECISION.toString());
        assertEquals("sendBailDirection", Event.SEND_BAIL_DIRECTION.toString());
        assertEquals("startApplication", Event.START_APPLICATION.toString());
        assertEquals("stopLegalRepresenting", Event.STOP_LEGAL_REPRESENTING.toString());
        assertEquals("submitApplication", Event.SUBMIT_APPLICATION.toString());
        assertEquals("updateBailLegalRepDetails", Event.UPDATE_BAIL_LEGAL_REP_DETAILS.toString());
        assertEquals("updateInterpreterDetails", Event.UPDATE_INTERPRETER_DETAILS.toString());
        assertEquals("uploadBailSummary", Event.UPLOAD_BAIL_SUMMARY.toString());
        assertEquals("uploadDocuments", Event.UPLOAD_DOCUMENTS.toString());
        assertEquals("uploadHearingRecording", Event.UPLOAD_HEARING_RECORDING.toString());
        assertEquals("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE.toString());
        assertEquals("viewPreviousApplications", Event.VIEW_PREVIOUS_APPLICATIONS.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(33, Event.values().length);
    }
}
