package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils.*;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UpdateInterpreterBookingStatusHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS;
    }

    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        Optional<String> applicantSpokenLanguageBookingOptional = bailCase
            .read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING);
        Optional<String> applicantSignLanguageBookingOptional = bailCase
            .read(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING);

        // clear case data for applicant spoken/sign booking statuses
        if (applicantSpokenLanguageBookingOptional.isEmpty()) {
            bailCase.clear(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        }
        if (applicantSignLanguageBookingOptional.isEmpty()) {
            bailCase.clear(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
        }

        // clear case data for FCS spoken booking statuses
        FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKINGS.forEach(booking -> {
            if (bailCase.read(booking, String.class).isEmpty()) {
                switch (booking) {
                    case FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1 -> bailCase.clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
                    case FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2 -> bailCase.clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
                    case FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3 -> bailCase.clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
                    case FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4 -> bailCase.clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
                    default -> {
                    }
                }
            }
        });

        // clear case data for FCS sign booking statuses
        FCS_INTERPRETER_SIGN_LANGUAGE_BOOKINGS.forEach(booking -> {
            if (bailCase.read(booking, String.class).isEmpty()) {
                switch (booking) {
                    case FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1 -> bailCase.clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
                    case FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2 -> bailCase.clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
                    case FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3 -> bailCase.clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
                    case FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4 -> bailCase.clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
                    default -> {

                    }
                }
            }
        });

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
