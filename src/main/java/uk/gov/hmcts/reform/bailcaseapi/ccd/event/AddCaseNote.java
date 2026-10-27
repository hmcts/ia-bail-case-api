package uk.gov.hmcts.reform.bailcaseapi.ccd.event;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

/**
 * Event {@code addCaseNote} for case type {@code Bail}: adds a note to the case and clears the
 * fields it was entered in.
 */
@Component
@RequiredArgsConstructor
public class AddCaseNote implements CCDConfig<CaseData, State, UserRole> {

    public static final String ADD_CASE_NOTE = "addCaseNote";

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        var fields = builder.event(ADD_CASE_NOTE)
            .forStates(State.applicationSubmitted, State.bailSummaryUploaded, State.unsignedDecision,
                State.decisionConditionalBail, State.decisionDecided, State.applicationEnded)
            .name("Add case note")
            .displayOrder(7)
            .showSummary()
            .endButtonLabel("Add case note")
            .aboutToSubmitCallback(this::aboutToSubmit, 5, 5, 5, 5)
            .submittedCallback(this::submitted, 5, 5, 5, 5)
            .explicitGrants()
            .grant(Permission.CRUD, UserRole.CASEWORKER_IA_ADMOFFICER, UserRole.CASEWORKER_IA_IACJUDGE,
                UserRole.CASEWORKER_IA_RPAROBOT)
            .fields();
        fields.page("addCaseNote");
        fields.pageLabel("Case note details");
        fields.readonly(CaseData::getAddCaseNoteLabel);
        fields.mandatory(CaseData::getAddCaseNoteSubject);
        fields.mandatory(CaseData::getAddCaseNoteDescription);
        fields.optional(CaseData::getAddCaseNoteDocument);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        // PreSubmitCallbackDispatcher did this for every event it routed; the SDK has no such hook.
        ccdEventAuthorizor.throwIfNotAuthorized(Event.ADD_CASE_NOTE);

        CaseData caseData = details.getData();

        CaseNote newCaseNote = CaseNote.builder()
            .caseNoteSubject(required(caseData.getAddCaseNoteSubject(), "addCaseNoteSubject"))
            .caseNoteDescription(required(caseData.getAddCaseNoteDescription(), "addCaseNoteDescription"))
            .caseNoteDocument(caseData.getAddCaseNoteDocument())
            .user(userDetails.getForename() + " " + userDetails.getSurname())
            .dateAdded(dateProvider.now())
            .build();

        caseData.setCaseNotes(prepend(newCaseNote, caseData.getCaseNotes()));

        caseData.setAddCaseNoteSubject(null);
        caseData.setAddCaseNoteDescription(null);
        caseData.setAddCaseNoteDocument(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You have added a case note")
            .confirmationBody(
                "### What happens next\n\n"
                    + "You can review this note in the [case notes tab](/case/IA/Bail/"
                    + details.getId()
                    + "#Case%20notes)."
            )
            .build();
    }

    /** Newest note first, ids renumbered from the new size down, as {@code Appender} did. */
    private static List<ListValue<CaseNote>> prepend(
        CaseNote newCaseNote,
        List<ListValue<CaseNote>> existingCaseNotes
    ) {
        List<ListValue<CaseNote>> existing = existingCaseNotes == null ? List.of() : existingCaseNotes;
        List<ListValue<CaseNote>> allCaseNotes = new ArrayList<>();
        int id = existing.size() + 1;

        allCaseNotes.add(new ListValue<>(String.valueOf(id--), newCaseNote));
        for (ListValue<CaseNote> existingCaseNote : existing) {
            allCaseNotes.add(new ListValue<>(String.valueOf(id--), existingCaseNote.getValue()));
        }

        return allCaseNotes;
    }

    private static String required(String value, String field) {
        if (value == null) {
            throw new IllegalStateException(field + " is not present");
        }
        return value;
    }
}
