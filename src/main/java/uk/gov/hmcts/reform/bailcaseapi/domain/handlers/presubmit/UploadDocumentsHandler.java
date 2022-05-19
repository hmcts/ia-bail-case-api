package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_DOCUMENTS_SUPPLIED_BY;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@Component
public class UploadDocumentsHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private static final String SUPPLIED_BY_APPLICANT = "Applicant";
    private static final String SUPPLIED_BY_LEGAL_REPRESENTATIVE = "Legal Representative";
    private static final String SUPPLIED_BY_HOME_OFFICE = "Home Office";

    public UploadDocumentsHandler(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPLOAD_DOCUMENTS;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<DocumentWithDescription>>> maybeDocument = bailCase.read(UPLOAD_DOCUMENTS);
        Optional<String> maybeSuppliedBy = bailCase.read(UPLOAD_DOCUMENTS_SUPPLIED_BY);

        String suppliedBy = maybeSuppliedBy
            .orElse(isLegalRep(bailCase)
                        ? "Legal Representative"
                        : isHomeOffice(bailCase)
                ? "Home Office"
                : "");

        if (maybeDocument.isPresent()) {
            List<DocumentWithMetadata> document =
                maybeDocument
                    .orElseThrow(() -> new IllegalStateException("document is not present"))
                    .stream()
                    .map(IdValue::getValue)
                    .map(doc -> documentReceiver.tryReceive(doc, DocumentTag.UPLOAD_DOCUMENT, suppliedBy))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            BailCaseFieldDefinition appropriateCollectionForUserRole =
                selectAppropriateDocumentsCollection(bailCase, suppliedBy);

            Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingDocuments =
                bailCase.read(appropriateCollectionForUserRole);

            final List<IdValue<DocumentWithMetadata>> existingHomeOfficeDocuments =
                maybeExistingDocuments.orElse(Collections.emptyList());

            List<IdValue<DocumentWithMetadata>> allDocuments =
                documentsAppender.append(existingHomeOfficeDocuments, document);

            bailCase.write(appropriateCollectionForUserRole, allDocuments);

            bailCase.clear(UPLOAD_DOCUMENTS);
            bailCase.clear(UPLOAD_DOCUMENTS_SUPPLIED_BY);
        }
        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private boolean isLegalRep(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, YesOrNo.class).map(flag -> flag.equals(
            YesOrNo.YES)).orElse(false);
    }

    private boolean isHomeOffice(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.IS_HOME_OFFICE, YesOrNo.class).map(flag -> flag.equals(
            YesOrNo.YES)).orElse(false);
    }

    private BailCaseFieldDefinition selectAppropriateDocumentsCollection(BailCase bailCase, String suppliedBy) {
        BailCaseFieldDefinition appropriateDocumentsCollection;

        if (isHomeOffice(bailCase)) {
            appropriateDocumentsCollection = HOME_OFFICE_DOCUMENTS_WITH_METADATA;
        } else if (isLegalRep(bailCase)) {
            appropriateDocumentsCollection = APPLICANT_DOCUMENTS_WITH_METADATA;
        } else {
            appropriateDocumentsCollection = suppliedBy.equals(SUPPLIED_BY_APPLICANT)
                                             || suppliedBy.equals(SUPPLIED_BY_LEGAL_REPRESENTATIVE)
                ? APPLICANT_DOCUMENTS_WITH_METADATA
                : suppliedBy.equals(SUPPLIED_BY_HOME_OFFICE)
                ? HOME_OFFICE_DOCUMENTS_WITH_METADATA
                : null;
        }

        if (appropriateDocumentsCollection == null) {
            throw new IllegalStateException("Impossible to determine which collection to append the document to");
        }

        return appropriateDocumentsCollection;
    }

}
