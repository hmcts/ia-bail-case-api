package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
class InterpreterFlagHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.CREATE_FLAG
            || callback.getEvent() == Event.MANAGE_FLAGS);
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

        Optional<StrategicCaseFlag> appellantLevelFlags = bailCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class);

        if (appellantLevelFlags.isPresent()) {
            List<CaseFlagDetail> flags = appellantLevelFlags.get().getDetails();
            boolean hasInterpreterFlag = flags.stream()
                .anyMatch(flag ->
                    flag.getCaseFlagValue().getName().toLowerCase().contains("interpreter")
                        && flag.getCaseFlagValue().getStatus().equals("Active"));
            if (hasInterpreterFlag) {
                bailCase.write(BailCaseFieldDefinition.HAS_ACTIVE_INTERPRETER_FLAG, YesOrNo.YES);
            } else {
                bailCase.clear(BailCaseFieldDefinition.HAS_ACTIVE_INTERPRETER_FLAG);
            }
        } else {
            bailCase.clear(BailCaseFieldDefinition.HAS_ACTIVE_INTERPRETER_FLAG);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
