package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.utils.InterpreterLanguagesUtils;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ApplicantInterpreterLanguagesDynamicListUpdaterTest {
    public static final String INTERPRETER_LANGUAGES = "InterpreterLanguage";
    public static final String SIGN_LANGUAGES = "SignLanguage";
    public static final String IS_CHILD_REQUIRED = "Y";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private CaseDetails<BailCase> caseDetailsBefore;
    @Mock
    private BailCase asylumCase;
    @Mock
    private BailCase asylumCaseBefore;
    @Mock
    private CommonDataResponse commonDataResponse;
    @Mock
    private InterpreterLanguageRefData spokenLanguages;
    @Mock
    private InterpreterLanguageRefData signLanguages;
    @Mock
    private InterpreterLanguageRefData spokenLanguagesSelected;
    @Mock
    private InterpreterLanguageRefData signLanguagesSelected;

    private MockedStatic<InterpreterLanguagesUtils> interpreterLanguagesUtils;

    private RefDataUserService refDataUserService;
    private ApplicantInterpreterLanguagesDynamicListUpdater applicantInterpreterLanguagesDynamicListUpdater;

    @BeforeEach
    void setup() {
        refDataUserService = mock(RefDataUserService.class);
        applicantInterpreterLanguagesDynamicListUpdater =
            new ApplicantInterpreterLanguagesDynamicListUpdater(refDataUserService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        interpreterLanguagesUtils = mockStatic(InterpreterLanguagesUtils.class);
    }

    @AfterEach
    void tearDown() {
        interpreterLanguagesUtils.close();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPLICATION",
        "EDIT_BAIL_APPLICATION",
        "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
        "MAKE_NEW_APPLICATION"
    })
    void should_populate_dynamic_lists_for_appellant_for_valid_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails)); // asylumCase doesn't differ when drafting hearing reqs

        when(asylumCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);

        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, INTERPRETER_LANGUAGES))
            .thenReturn(spokenLanguages);
        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, SIGN_LANGUAGES))
            .thenReturn(signLanguages);

        applicantInterpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(asylumCase).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(asylumCase).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void should_populate_dynamic_lists_for_appellant_for_edit_application() {
        when(callback.getEvent()).thenReturn(Event.EDIT_BAIL_APPLICATION);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore)); // asylumCase doesn't differ when drafting hearing reqs
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE, InterpreterLanguageRefData.class))
            .thenReturn(Optional.empty());

        when(asylumCaseBefore.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenLanguagesSelected));
        when(asylumCaseBefore.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.of(signLanguagesSelected));
        when(spokenLanguagesSelected.getLanguageManualEntry()).thenReturn(List.of("Yes"));
        when(signLanguagesSelected.getLanguageManualEntry()).thenReturn(List.of("Yes"));
        when(spokenLanguagesSelected.getLanguageManualEntryDescription()).thenReturn("desc");
        when(signLanguagesSelected.getLanguageManualEntryDescription()).thenReturn("desc");

        when(refDataUserService.retrieveCategoryValues(INTERPRETER_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);
        when(refDataUserService.retrieveCategoryValues(SIGN_LANGUAGES, IS_CHILD_REQUIRED))
            .thenReturn(commonDataResponse);

        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, INTERPRETER_LANGUAGES))
            .thenReturn(spokenLanguages);
        interpreterLanguagesUtils.when(() -> InterpreterLanguagesUtils.generateDynamicList(refDataUserService, SIGN_LANGUAGES))
            .thenReturn(signLanguages);

        applicantInterpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_START, callback);

        verify(spokenLanguages).setLanguageManualEntry(List.of("Yes"));
        verify(signLanguages).setLanguageManualEntry(List.of("Yes"));
        verify(spokenLanguages).setLanguageManualEntryDescription("desc");
        verify(signLanguages).setLanguageManualEntryDescription("desc");
        verify(asylumCase).write(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, spokenLanguages);
        verify(asylumCase).write(APPLICANT_INTERPRETER_SIGN_LANGUAGE, signLanguages);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> applicantInterpreterLanguagesDynamicListUpdater.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = applicantInterpreterLanguagesDynamicListUpdater.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START
                    && Set.of(START_APPLICATION, EDIT_BAIL_APPLICATION, EDIT_BAIL_APPLICATION_AFTER_SUBMIT, MAKE_NEW_APPLICATION).contains(callback.getEvent())) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> applicantInterpreterLanguagesDynamicListUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> applicantInterpreterLanguagesDynamicListUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicantInterpreterLanguagesDynamicListUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applicantInterpreterLanguagesDynamicListUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
