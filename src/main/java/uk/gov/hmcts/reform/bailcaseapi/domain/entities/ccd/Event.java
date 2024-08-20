package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    ADD_CASE_NOTE("addCaseNote"),
    APPLY_NOC_DECISION("applyNocDecision"),
    CASE_LISTING("caseListing"),
    CHANGE_BAIL_DIRECTION_DUE_DATE("changeBailDirectionDueDate"),
    CHANGE_TRIBUNAL_CENTRE("changeTribunalCentre"),
    CLEAR_LEGAL_REPRESENTATIVE_DETAILS("clearLegalRepresentativeDetails"),
    CONFIRM_DETENTION_LOCATION("confirmDetentionLocation"),
    CREATE_BAIL_CASE_LINK("createBailCaseLink"),
    CREATE_FLAG("createFlag"),
    END_APPLICATION("endApplication"),
    EDIT_BAIL_APPLICATION("editBailApplication"),
    EDIT_BAIL_APPLICATION_AFTER_SUBMIT("editBailApplicationAfterSubmit"),
    EDIT_BAIL_DOCUMENTS("editBailDocuments"),
    FORCE_CASE_TO_HEARING("forceCaseToHearing"),
    IMA_STATUS("imaStatus"),
    MAINTAIN_BAIL_CASE_LINKS("maintainBailCaseLinks"),
    MAKE_NEW_APPLICATION("makeNewApplication"),
    MOVE_APPLICATION_TO_DECIDED("moveApplicationToDecided"),
    NOC_REQUEST("nocRequest"),
    NOC_REQUEST_BAIL("nocRequestBail"),
    RECORD_THE_DECISION("recordTheDecision"),
    REMOVE_BAIL_LEGAL_REPRESENTATIVE("removeBailLegalRepresentative"),
    SEND_BAIL_DIRECTION("sendBailDirection"),
    START_APPLICATION("startApplication"),
    STOP_LEGAL_REPRESENTING("stopLegalRepresenting"),
    SUBMIT_APPLICATION("submitApplication"),
    UPDATE_BAIL_LEGAL_REP_DETAILS("updateBailLegalRepDetails"),
    UPDATE_INTERPRETER_BOOKING_STATUS("updateInterpreterBookingStatus"),
    UPDATE_INTERPRETER_DETAILS("updateInterpreterDetails"),
    UPLOAD_BAIL_SUMMARY("uploadBailSummary"),
    UPLOAD_DOCUMENTS("uploadDocuments"),
    UPLOAD_HEARING_RECORDING("uploadHearingRecording"),
    UPLOAD_SIGNED_DECISION_NOTICE("uploadSignedDecisionNotice"),
    VIEW_PREVIOUS_APPLICATIONS("viewPreviousApplications"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
