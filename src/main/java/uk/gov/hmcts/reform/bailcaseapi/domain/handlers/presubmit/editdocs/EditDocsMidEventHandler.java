package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.editdocs;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EditDocsMidEventHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.EDIT_BAIL_DOCUMENTS;
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

        final PreSubmitCallbackResponse<BailCase> bailCasePreSubmitCallbackResponse =
            new PreSubmitCallbackResponse<>(bailCase);

        for (BailCaseFieldDefinition field : getFieldDefinitions()) {
            Optional<List<IdValue<DocumentWithMetadata>>> idValuesOptional = bailCase.read(field);
            if (idValuesOptional.isPresent()) {
                for (IdValue<DocumentWithMetadata> documentWithMetadataIdValue : idValuesOptional.get()) {
                    try {
                        documentWithMetadataIdValue.getValue().getDocument();
                        documentWithMetadataIdValue.getValue().getDateUploaded();
                    } catch (NullPointerException npe) {
                        bailCasePreSubmitCallbackResponse.addError("If you are adding a new document please ensure all fields including the document and date fields are filled in and completed or you have selected 'remove' before submitting your changes.");
                        return bailCasePreSubmitCallbackResponse;
                    }
                }
            }
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private List<BailCaseFieldDefinition> getFieldDefinitions() {
        return Arrays.asList(
            TRIBUNAL_DOCUMENTS_WITH_METADATA,
            HOME_OFFICE_DOCUMENTS_WITH_METADATA,
            APPLICANT_DOCUMENTS_WITH_METADATA);
    }

}
