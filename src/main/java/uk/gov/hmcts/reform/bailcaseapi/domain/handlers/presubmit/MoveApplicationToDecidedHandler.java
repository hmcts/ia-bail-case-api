package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingDecisionProcessor;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OUTCOME_STATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;

@Component
public class MoveApplicationToDecidedHandler implements PreSubmitCallbackHandler<BailCase> {

    private final HearingDecisionProcessor hearingDecisionProcessor;
    private final DateProvider dateProvider;

    public MoveApplicationToDecidedHandler(
        HearingDecisionProcessor hearingDecisionProcessor,
        DateProvider dateProvider
    ) {
        this.hearingDecisionProcessor = hearingDecisionProcessor;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.MOVE_APPLICATION_TO_DECIDED;
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

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        Optional<Document> maybeUploadSignedDecision =
            bailCase.read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class);

        if (maybeUploadSignedDecision.isEmpty()) {
            response.addError("You must upload a signed decision notice before moving the application to decided.");
        }

        bailCase.write(OUTCOME_DATE, dateProvider.nowWithTime().toString());
        bailCase.write(OUTCOME_STATE, State.DECISION_DECIDED);

        hearingDecisionProcessor.processHearingDecision(bailCase);

        return response;
    }
}
