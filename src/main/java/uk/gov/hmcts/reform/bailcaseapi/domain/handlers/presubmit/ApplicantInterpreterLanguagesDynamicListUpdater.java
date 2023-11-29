package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.*;

import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

@Component
public class ApplicantInterpreterLanguagesDynamicListUpdater implements PreSubmitCallbackHandler<BailCase> {

    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    private final RefDataUserService refDataUserService;

    public ApplicantInterpreterLanguagesDynamicListUpdater(RefDataUserService refDataUserService) {
        this.refDataUserService = refDataUserService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && Set.of(START_APPLICATION, MAKE_NEW_APPLICATION, EDIT_BAIL_APPLICATION_AFTER_SUBMIT, EDIT_BAIL_APPLICATION).contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        BailCase bailCaseBefore = callback.getCaseDetailsBefore()
            .map(caseDetails -> caseDetails.getCaseData())
            .orElse(bailCase);

        boolean shouldPopulateSpokenLanguage = bailCase
            .read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class)
            .map(applicantSpoken -> applicantSpoken.getLanguageRefData() == null)
            .orElse(true);

        boolean shouldPopulateSignLanguage = bailCase
            .read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class)
            .map(applicantSign -> applicantSign.getLanguageRefData() == null)
            .orElse(true);

        if (shouldPopulateSpokenLanguage) {
            populateDynamicList(bailCase, bailCaseBefore, INTERPRETER_LANGUAGES, APPLICANT_INTERPRETER_SPOKEN_LANGUAGE);
        }
        if (shouldPopulateSignLanguage) {
            populateDynamicList(bailCase, bailCaseBefore, SIGN_LANGUAGES, APPLICANT_INTERPRETER_SIGN_LANGUAGE);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void populateDynamicList(BailCase bailCase, BailCase bailCaseBefore, String languageCategory, BailCaseFieldDefinition languageCategoryDefinition) {
        InterpreterLanguageRefData interpreterLanguage = generateDynamicList(languageCategory);
        Optional<InterpreterLanguageRefData> optionalExistingInterpreterLanguageRefData = bailCaseBefore.read(languageCategoryDefinition);

        optionalExistingInterpreterLanguageRefData.ifPresent(existing -> {
            interpreterLanguage.setLanguageManualEntry(existing.getLanguageManualEntry());
            interpreterLanguage.setLanguageManualEntryDescription(existing.getLanguageManualEntryDescription());
        });

        bailCase.write(languageCategoryDefinition, interpreterLanguage);
    }

    private InterpreterLanguageRefData generateDynamicList(String languageCategory) {
        return InterpreterLanguagesUtils.generateDynamicList(refDataUserService, languageCategory);
    }
}
