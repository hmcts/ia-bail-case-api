package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_FCS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
class CreateFlagHandler implements PreSubmitCallbackHandler<BailCase> {

    public static final List<BailCaseFieldDefinition> FCS_N_GIVEN_NAME_FIELD = List.of(
        SUPPORTER_GIVEN_NAMES,
        SUPPORTER_2_GIVEN_NAMES,
        SUPPORTER_3_GIVEN_NAMES,
        SUPPORTER_4_GIVEN_NAMES
    );

    public static final List<BailCaseFieldDefinition> FCS_N_FAMILY_NAME_FIELD = List.of(
        SUPPORTER_FAMILY_NAMES,
        SUPPORTER_2_FAMILY_NAMES,
        SUPPORTER_3_FAMILY_NAMES,
        SUPPORTER_4_FAMILY_NAMES
    );

    public static final List<BailCaseFieldDefinition> FCS_N_INTERPRETER_CATEGORY_FIELD = List.of(
        FCS1_INTERPRETER_LANGUAGE_CATEGORY,
        FCS2_INTERPRETER_LANGUAGE_CATEGORY,
        FCS3_INTERPRETER_LANGUAGE_CATEGORY,
        FCS4_INTERPRETER_LANGUAGE_CATEGORY
    );

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.CREATE_FLAG;
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

        Optional<StrategicCaseFlag> existingCaseLevelFlags = bailCase.read(CASE_FLAGS);

        Optional<StrategicCaseFlag> existingAppellantLevelFlags = bailCase.read(APPELLANT_LEVEL_FLAGS);

        if (existingAppellantLevelFlags.isEmpty()
            || existingAppellantLevelFlags.get().getPartyName() == null
            || existingAppellantLevelFlags.get().getPartyName().isBlank()) {

            final String appellantNameForDisplay =
                bailCase
                    .read(APPLICANT_FULL_NAME, String.class)
                    .orElseThrow(() -> new IllegalStateException("applicantFullName is not present"));

            bailCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(appellantNameForDisplay, ROLE_ON_CASE_APPLICANT));
        } else {
            log.info("Existing Appellant Level flags: {}", existingAppellantLevelFlags);
        }

        handleFcsLevelFlags(bailCase);

        if (existingCaseLevelFlags.isEmpty()) {
            bailCase.write(CASE_FLAGS, new StrategicCaseFlag());
        }  else {
            log.info("Existing Case Level flags: {}", existingCaseLevelFlags);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void handleFcsLevelFlags(BailCase bailCase) {
        List<IdValue<StrategicCaseFlag>> fcsLevelFlags = new ArrayList<>();
        int i = 0;
        while (i < 4) {
            Optional<List<String>> fcsInterpreterCategoryOptional = bailCase.read(FCS_N_INTERPRETER_CATEGORY_FIELD.get(i));
            if (fcsInterpreterCategoryOptional.isPresent() && !fcsInterpreterCategoryOptional.get().isEmpty()){
                fcsLevelFlags.add(new IdValue<>(String.valueOf(i),
                                  new StrategicCaseFlag(buildFcsFullName(bailCase, i), ROLE_ON_CASE_FCS)));
            }
            i++;
        }

        if (!fcsLevelFlags.isEmpty()){
            bailCase.write(FCS_LEVEL_FLAGS, fcsLevelFlags);
        }
    }

    private String buildFcsFullName(BailCase bailCase, int index) {
        String givenNames =  bailCase.read(FCS_N_GIVEN_NAME_FIELD.get(index), String.class)
            .orElseThrow(() -> new IllegalStateException(FCS_N_GIVEN_NAME_FIELD.get(index).value() + " is not present"));
        String familyName = bailCase.read(FCS_N_FAMILY_NAME_FIELD.get(index), String.class)
            .orElseThrow(() -> new IllegalStateException(FCS_N_FAMILY_NAME_FIELD.get(index).value() + " is not present"));

        return givenNames + " " + familyName;
    }
}
