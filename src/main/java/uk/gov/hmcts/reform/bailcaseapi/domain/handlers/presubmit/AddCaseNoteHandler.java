package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ADD_CASE_NOTE_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ADD_CASE_NOTE_DOCUMENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ADD_CASE_NOTE_SUBJECT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;


@Component
public class AddCaseNoteHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public AddCaseNoteHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(ADD_CASE_NOTE);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String caseNoteSubject = bailCase
                .read(ADD_CASE_NOTE_SUBJECT, String.class)
                .orElseThrow(() -> new IllegalStateException("addCaseNoteSubject is not present"));

        String caseNoteDescription = bailCase
                .read(ADD_CASE_NOTE_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("addCaseNoteDescription is not present"));

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            bailCase.read(CASE_NOTES);

        Optional<Document> caseNoteDocument =
            bailCase.read(ADD_CASE_NOTE_DOCUMENT, Document.class);

        final CaseNote newCaseNote = new CaseNote(
            caseNoteSubject,
            caseNoteDescription,
            buildFullName(),
            dateProvider.now().toString()
        );

        caseNoteDocument.ifPresent(newCaseNote::setCaseNoteDocument);

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        bailCase.write(CASE_NOTES, allCaseNotes);

        bailCase.clear(ADD_CASE_NOTE_SUBJECT);
        bailCase.clear(ADD_CASE_NOTE_DESCRIPTION);
        bailCase.clear(ADD_CASE_NOTE_DOCUMENT);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
