package uk.gov.hmcts.reform.bailcaseapi.ccd.callback;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

/**
 * Callbacks for the {@code addCaseNote} event, registered on the event itself rather than reached
 * through {@code PreSubmitCallbackDispatcher}. CCD calls the SDK's own {@code /callbacks/*}
 * endpoints, which hand the typed {@link CaseData} straight to these methods — so there is no
 * {@code canHandle} to keep in step with the event id, and the fields are read and written through
 * getters rather than {@code BailCase.read}/{@code write} with a field-definition key.
 */
@Component
public class AddCaseNoteCallbacks {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public AddCaseNoteCallbacks(
        CcdEventAuthorizor ccdEventAuthorizor,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    /**
     * Appends the note being added to {@code caseNotes} and clears the entry fields.
     *
     * @param details the case as submitted
     * @param detailsBefore the case before the event
     * @return the amended case data
     */
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        // The dispatcher applied this to every event it routed; the SDK has no equivalent hook, so
        // the event's own callback has to ask. CCD's AuthorisationCaseEvent grants gate the event
        // too, but this check is what the service itself enforces.
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

    /**
     * The confirmation screen shown once the note has been added.
     *
     * @param details the saved case
     * @param detailsBefore the case before the event
     * @return the confirmation header and body
     */
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

    /**
     * Puts the new note at the top and renumbers the collection, reproducing {@code Appender}: ids
     * run down from the new size, so the newest note carries the highest id.
     */
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
