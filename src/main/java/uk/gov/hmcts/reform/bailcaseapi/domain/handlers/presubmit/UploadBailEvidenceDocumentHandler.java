package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_EVIDENCE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@Component
public class UploadBailEvidenceDocumentHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadBailEvidenceDocumentHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }


    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.SUBMIT_APPLICATION
            || callback.getEvent() == Event.MAKE_NEW_APPLICATION);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<DocumentWithDescription>>> maybeBailEvidence = bailCase.read(BAIL_EVIDENCE);

        if (maybeBailEvidence.isPresent()) {
            List<DocumentWithMetadata> bailEvidence =
                maybeBailEvidence
                    .orElseThrow(() -> new IllegalStateException("bailEvidence is not present"))
                    .stream()
                    .map(IdValue::getValue)
                    .map(document -> documentReceiver.tryReceive(document, DocumentTag.BAIL_EVIDENCE))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingGroundsForBailDocuments =
                bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA);

            final List<IdValue<DocumentWithMetadata>> existingExistingGroundsForBailDocuments =
                maybeExistingGroundsForBailDocuments.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allGroundsForBailDocuments =
                documentsAppender.append(existingExistingGroundsForBailDocuments, bailEvidence);

            bailCase.write(APPLICANT_DOCUMENTS_WITH_METADATA, allGroundsForBailDocuments);
        }
        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
