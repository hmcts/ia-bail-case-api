package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SIGNED_DECISION_NOTICE_METADATA;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@Component
public class UploadSignedDecisionNoticeHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;

    public UploadSignedDecisionNoticeHandler(
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
               && callback.getEvent() == Event.UPLOAD_SIGNED_DECISION_NOTICE;
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

        Document maybeSignedDecisionNotice = bailCase.read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class)
            .orElseThrow(() -> new IllegalStateException("signed decision notice is not present"));

        List<DocumentWithMetadata> signedDecisionNotice = new ArrayList<>();
        documentReceiver.tryReceive(
                new DocumentWithDescription(maybeSignedDecisionNotice, ""), DocumentTag.SIGNED_DECISION_NOTICE)
            .ifPresent(signedDecisionNotice::add);

        bailCase.clear(SIGNED_DECISION_NOTICE_METADATA);

        List<IdValue<DocumentWithMetadata>> newSignedDecisionNotice =
            documentsAppender.append(new ArrayList<>(), signedDecisionNotice);

        bailCase.write(SIGNED_DECISION_NOTICE_METADATA, newSignedDecisionNotice);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}