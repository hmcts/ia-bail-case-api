package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.MakeNewApplicationService;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ShowPreviousApplicationService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ShowApplicationHandlerTest {

    private ShowPreviousApplicationHandler showPreviousApplicationHandler;
    @Mock
    private MakeNewApplicationService makeNewApplicationService;
    @Mock
    private ShowPreviousApplicationService showPreviousApplicationService;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private BailCase previousBailCase;

    @BeforeEach
    void setUp() {
        showPreviousApplicationHandler = new ShowPreviousApplicationHandler(makeNewApplicationService,
                                                                            showPreviousApplicationService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.VIEW_PREVIOUS_APPLICATIONS);
    }

    @Test
    void should_generate_label() {
        setUpShowPrevApplicationService();
        String caseDataJson = "dummyCaseData json";
        PriorApplication priorApplication = new PriorApplication("1", caseDataJson);
        Value selectedApplication = new Value("1", "Bail Application 1");

        //List of labels of dropdowns of previous applications
        List<Value> listOfApplications = Arrays.asList(new Value("1", "Bail Application 1"),
                                                 new Value("2", "Bail Application 2"));
        //List of prior applications with case data json
        List<IdValue<PriorApplication>> priorApplications =  Arrays.asList(
            new IdValue<>("1", priorApplication),
            new IdValue<>("2", priorApplication));

        when(bailCase.read(PREVIOUS_APPLICATION_LIST, DynamicList.class)).thenReturn(
            Optional.of(new DynamicList(selectedApplication, listOfApplications)));
        when(bailCase.read(PRIOR_APPLICATIONS)).thenReturn(Optional.of(priorApplications));

        when(makeNewApplicationService.getBailCaseFromString(caseDataJson)).thenReturn(previousBailCase);

        PreSubmitCallbackResponse<BailCase> response = showPreviousApplicationHandler
            .handle(PreSubmitCallbackStage.MID_EVENT, callback);
        assertNotNull(response);
        verify(bailCase, times(1)).write(PREV_APP_ID, priorApplication.getApplicationId());
        verify(bailCase, times(1)).write(PREV_APP_DECISION_DETAILS_LABEL,"Decision label");
        verify(bailCase, times(1)).write(PREV_APP_APPLICANT_DOCS_DETAILS, "Documents label");
        verify(bailCase, times(1)).write(PREV_APP_SUBMISSION_DETAILS, "Submission label");
        verify(bailCase, times(1)).write(PREV_APP_DIRECTION_DETAILS, "Direction label");
        verify(bailCase, times(1)).write(PREV_APP_CASE_NOTES_DETAILS, "CaseNote label");
        verify(bailCase, times(1)).write(PREV_APP_GROUNDS_FOR_BAIL, "Bail grounds label");
        verify(bailCase, times(1)).write(PREV_APP_LEGAL_REP_DETAILS, "Legal rep label");
        verify(bailCase, times(1)).write(PREV_APP_APPLICANT_INFO, "Applicant info label");
        verify(bailCase, times(1))
            .write(PREV_APP_PERSONAL_INFO_DETAILS, "Personal info label");
        verify(bailCase, times(1))
            .write(PREV_APP_FINANCIAL_COND_COMMITMENT, "Financial commitment label");
        verify(bailCase, times(1))
            .write(PREV_APP_FINANCIAL_COND_SUPPORTER1, "Supporter 1 label");
        verify(bailCase, times(1))
            .write(PREV_APP_FINANCIAL_COND_SUPPORTER2, "Supporter 2 label");
        verify(bailCase, times(1))
            .write(PREV_APP_FINANCIAL_COND_SUPPORTER3, "Supporter 3 label");
        verify(bailCase, times(1))
            .write(PREV_APP_FINANCIAL_COND_SUPPORTER4, "Supporter 4 label");
    }

    @Test
    void should_throw_exception_for_mission_details() {
        // If Previous Application List Field is missing
        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Previous application details missing");

        // If Prior Application Field is missing
        Value selectedApplication = new Value("1", "Bail Application 1");
        List<Value> listOfApplications = List.of(new Value("1", "Bail Application 1"),
                                                 new Value("2", "Bail Application 2"));
        given(bailCase.read(eq(PREVIOUS_APPLICATION_LIST), eq(DynamicList.class)))
            .willReturn(Optional.of(new DynamicList(selectedApplication, listOfApplications)));
        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Prior Applications missing");

        // If selected application does not match any application from the list.
        // List contains id 1 & 2 but selected application has id 4.
        PriorApplication priorApplication = new PriorApplication("4", "dummy field");
        List<IdValue<PriorApplication>> priorApplications =  List.of(
            new IdValue<>("1", priorApplication));
        when(bailCase.read(PRIOR_APPLICATIONS)).thenReturn(Optional.of(priorApplications));
        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("No application found");
    }

    @Test
    void should_throw_exception_if_previous_application_details_missing() {
        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Previous application details missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
        when(bailCase.read(PREVIOUS_APPLICATION_LIST, DynamicList.class)).thenReturn(
            Optional.of(new DynamicList("Application 1")));
        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Prior Applications missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }


    @Test
    public void should_only_handle_valid_event_state() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage: PreSubmitCallbackStage.values()) {
                boolean canHandle = showPreviousApplicationHandler.canHandle(stage, callback);
                if (stage.equals(PreSubmitCallbackStage.MID_EVENT) && event == Event.VIEW_PREVIOUS_APPLICATIONS) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> showPreviousApplicationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> showPreviousApplicationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> showPreviousApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private void setUpShowPrevApplicationService() {
        when(showPreviousApplicationService
                 .getDecisionLabel(previousBailCase, new Value("1", "Bail Application 1")))
            .thenReturn("Decision label");
        when(showPreviousApplicationService.getDocumentsLabel(previousBailCase))
            .thenReturn("Documents label");
        when(showPreviousApplicationService.getDirectionLabel(previousBailCase))
            .thenReturn("Direction label");
        when(showPreviousApplicationService.getCaseNoteLabel(previousBailCase))
            .thenReturn("CaseNote label");
        when(showPreviousApplicationService.getHearingReqDetails(previousBailCase))
            .thenReturn("Hearing req label");
        when(showPreviousApplicationService.getSubmissionDetails(previousBailCase))
            .thenReturn("Submission label");
        when(showPreviousApplicationService.getPersonalInfoLabel(previousBailCase))
            .thenReturn("Personal info label");
        when(showPreviousApplicationService.getApplicantInfo(previousBailCase))
            .thenReturn("Applicant info label");
        when(showPreviousApplicationService.getFinancialCondCommitment(previousBailCase))
            .thenReturn("Financial commitment label");
        when(showPreviousApplicationService
                 .getFinancialConditionSupporterLabel(previousBailCase,
                                                      HAS_FINANCIAL_COND_SUPPORTER,
                                                      SUPPORTER_GIVEN_NAMES,
                                                      SUPPORTER_FAMILY_NAMES,
                                                      SUPPORTER_ADDRESS_DETAILS,
                                                      SUPPORTER_TELEPHONE_NUMBER,
                                                      SUPPORTER_MOBILE_NUMBER,
                                                      SUPPORTER_EMAIL_ADDRESS,
                                                      SUPPORTER_DOB,
                                                      SUPPORTER_RELATION,
                                                      SUPPORTER_OCCUPATION,
                                                      SUPPORTER_IMMIGRATION,
                                                      SUPPORTER_NATIONALITY,
                                                      SUPPORTER_HAS_PASSPORT,
                                                      SUPPORTER_PASSPORT,
                                                      FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES,
                                                      FCS1_INTERPRETER_SPOKEN_LANGUAGE,
                                                      FCS1_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn("Supporter 1 label");
        when(showPreviousApplicationService
                 .getFinancialConditionSupporterLabel(previousBailCase,
                                                      HAS_FINANCIAL_COND_SUPPORTER_2,
                                                      SUPPORTER_2_GIVEN_NAMES,
                                                      SUPPORTER_2_FAMILY_NAMES,
                                                      SUPPORTER_2_ADDRESS_DETAILS,
                                                      SUPPORTER_2_TELEPHONE_NUMBER,
                                                      SUPPORTER_2_MOBILE_NUMBER,
                                                      SUPPORTER_2_EMAIL_ADDRESS,
                                                      SUPPORTER_2_DOB,
                                                      SUPPORTER_2_RELATION,
                                                      SUPPORTER_2_OCCUPATION,
                                                      SUPPORTER_2_IMMIGRATION,
                                                      SUPPORTER_2_NATIONALITY,
                                                      SUPPORTER_2_HAS_PASSPORT,
                                                      SUPPORTER_2_PASSPORT,
                                                      FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES,
                                                      FCS2_INTERPRETER_SPOKEN_LANGUAGE,
                                                      FCS2_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn("Supporter 2 label");
        when(showPreviousApplicationService
                 .getFinancialConditionSupporterLabel(previousBailCase,
                                                      HAS_FINANCIAL_COND_SUPPORTER_3,
                                                      SUPPORTER_3_GIVEN_NAMES,
                                                      SUPPORTER_3_FAMILY_NAMES,
                                                      SUPPORTER_3_ADDRESS_DETAILS,
                                                      SUPPORTER_3_TELEPHONE_NUMBER,
                                                      SUPPORTER_3_MOBILE_NUMBER,
                                                      SUPPORTER_3_EMAIL_ADDRESS,
                                                      SUPPORTER_3_DOB,
                                                      SUPPORTER_3_RELATION,
                                                      SUPPORTER_3_OCCUPATION,
                                                      SUPPORTER_3_IMMIGRATION,
                                                      SUPPORTER_3_NATIONALITY,
                                                      SUPPORTER_3_HAS_PASSPORT,
                                                      SUPPORTER_3_PASSPORT,
                                                      FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES,
                                                      FCS3_INTERPRETER_SPOKEN_LANGUAGE,
                                                      FCS3_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn("Supporter 3 label");
        when(showPreviousApplicationService
                 .getFinancialConditionSupporterLabel(previousBailCase,
                                                      HAS_FINANCIAL_COND_SUPPORTER_4,
                                                      SUPPORTER_4_GIVEN_NAMES,
                                                      SUPPORTER_4_FAMILY_NAMES,
                                                      SUPPORTER_4_ADDRESS_DETAILS,
                                                      SUPPORTER_4_TELEPHONE_NUMBER,
                                                      SUPPORTER_4_MOBILE_NUMBER,
                                                      SUPPORTER_4_EMAIL_ADDRESS,
                                                      SUPPORTER_4_DOB,
                                                      SUPPORTER_4_RELATION,
                                                      SUPPORTER_4_OCCUPATION,
                                                      SUPPORTER_4_IMMIGRATION,
                                                      SUPPORTER_4_NATIONALITY,
                                                      SUPPORTER_4_HAS_PASSPORT,
                                                      SUPPORTER_4_PASSPORT,
                                                      FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES,
                                                      FCS4_INTERPRETER_SPOKEN_LANGUAGE,
                                                      FCS4_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn("Supporter 4 label");
        when(showPreviousApplicationService.getGroundsForBail(previousBailCase))
            .thenReturn("Bail grounds label");
        when(showPreviousApplicationService.getLegalRepDetails(previousBailCase))
            .thenReturn("Legal rep label");
    }
}
