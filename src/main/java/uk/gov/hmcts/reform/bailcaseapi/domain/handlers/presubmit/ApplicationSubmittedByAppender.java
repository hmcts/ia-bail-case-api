package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ApplicationSubmittedByAppender implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callbackStage, "callbackStage must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.START_APPLICATION;
    }

    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        boolean isLegalRep = bailCase.read(BailCaseFieldDefinition.IS_LEGAL_REP, String.class).map(flag -> flag.equals(
            "Yes")).orElse(false);

        boolean isAdmin = bailCase.read(
            BailCaseFieldDefinition.IS_ADMIN,
            String.class
        ).map(flag -> flag.equals("Yes")).orElse(false);

        boolean isHomeOffice = bailCase.read(
            BailCaseFieldDefinition.IS_HOME_OFFICE,
            String.class
        ).map(flag -> flag.equals("Yes")).orElse(false);

        String applicationSubmittedBy = null;

        if (isAdmin) {
            applicationSubmittedBy = bailCase.read(
                BailCaseFieldDefinition.APPLICATION_SENT_BY, String.class
            ).orElseThrow(() -> new IllegalStateException("Missing the field for Admin - APPLICATION_SENT_BY"));
        } else if (isLegalRep) {
            applicationSubmittedBy = UserRoleLabel.LEGAL_REPRESENTATIVE.toString();
        } else if (isHomeOffice) {
            applicationSubmittedBy = UserRoleLabel.HOME_OFFICE_GENERIC.toString();
        } else {
            throw new IllegalStateException("Unknown user");
        }

        bailCase.write(BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY, applicationSubmittedBy);
        return new PreSubmitCallbackResponse<>(bailCase);
    }
}