package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UploadHearingRecordingDocumentsMidEvent implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.UPLOAD_HEARING_RECORDING;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        // Reading the hearing recording documents
        Optional<List<IdValue<HearingRecordingDocument>>> maybeHearingRecordingDocuments = bailCase.read(HEARING_RECORDING_DOCUMENTS);

        // If the document list is present, proceed to validate filenames
        if (maybeHearingRecordingDocuments.isPresent()) {
            List<IdValue<HearingRecordingDocument>> hearingRecordingDocuments = maybeHearingRecordingDocuments.get();

            for (IdValue<HearingRecordingDocument> doc : hearingRecordingDocuments) {
                // Extract the filename
                String filename = doc.getValue().getDocument().getDocumentFilename();

                // Debugging: Log or print the filename for visibility
                System.out.println("Processing file: " + filename);

                // Check for null, empty or non-mp3 filenames
                if (filename == null || filename.isEmpty() || !filename.toLowerCase().endsWith(".mp3")) {
                    PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);
                    response.addError("All documents must be an mp3 file");
                    return response;
                }
            }
        }

        // If all files pass the validation, return a successful response
        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
