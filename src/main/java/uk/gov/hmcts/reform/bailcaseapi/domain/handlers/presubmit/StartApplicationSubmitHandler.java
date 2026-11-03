package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_HOME_OFFICE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGAL_REP;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils.FCS_N_INTERPRETER_LANGUAGE_CATEGORY_FIELD;
import static uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils.FCS_N_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils.FCS_N_INTERPRETER_SPOKEN_LANGUAGE;

/**
 * Handles the about-to-submit callback of the {@code startApplication} event: sanitises the interpreter
 * language fields and chooses the end state of the event. A CCD definition generated from this
 * repository's Java config can only name one post state per event, so the choice between
 * {@code applicationStartedByLR}, {@code applicationStartedByHO} and {@code applicationStarted} is made
 * here; the state returned by an about-to-submit callback replaces the definition's post state.
 */
@Slf4j
@Component
public class StartApplicationSubmitHandler implements PreSubmitCallbackStateHandler<BailCase> {

    public StartApplicationSubmitHandler() {
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.START_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback,
                                                      PreSubmitCallbackResponse<BailCase> callbackResponse) {

        requireNonNull(callbackResponse, "callback must not be null");

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        bailCase.write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YES);

        boolean isApplicantInterpreterNeeded = bailCase.read(INTERPRETER_YESNO, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(yesOrNo, YES))
            .orElse(false);

        if (isApplicantInterpreterNeeded) {
            InterpreterLanguagesUtils.sanitizeInterpreterLanguageRefDataComplexType(
                bailCase,
                APPLICANT_INTERPRETER_SPOKEN_LANGUAGE
            );
            InterpreterLanguagesUtils.sanitizeInterpreterLanguageRefDataComplexType(
                bailCase,
                APPLICANT_INTERPRETER_SIGN_LANGUAGE
            );
        }

        boolean isFcsInterpreterNeeded = bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(yesOrNo, YES))
            .orElse(false);

        if (isFcsInterpreterNeeded) {
            for (int i = 0; i < FCS_N_INTERPRETER_LANGUAGE_CATEGORY_FIELD.size(); i++) {
                InterpreterLanguagesUtils.sanitizeInterpreterLanguageRefDataComplexType(
                    bailCase,
                    FCS_N_INTERPRETER_SPOKEN_LANGUAGE.get(i)
                );
                InterpreterLanguagesUtils.sanitizeInterpreterLanguageRefDataComplexType(
                    bailCase,
                    FCS_N_INTERPRETER_SIGN_LANGUAGE.get(i)
                );
            }
        }

        State state;
        if (isYes(bailCase, IS_LEGAL_REP)) {
            state = State.APPLICATION_STARTED_BY_LR;
        } else if (isYes(bailCase, IS_HOME_OFFICE)) {
            state = State.APPLICATION_STARTED_BY_HO;
        } else {
            state = State.APPLICATION_STARTED;
        }

        return new PreSubmitCallbackResponse<>(bailCase, state);
    }

    private boolean isYes(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.read(field, YesOrNo.class)
            .map(YES::equals)
            .orElse(false);
    }
}
