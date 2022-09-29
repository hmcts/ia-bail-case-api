package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_UNSIGNED_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_UNSIGNED_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RELEASE_STATUS_YES_OR_NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SECRETARY_OF_STATE_YES_OR_NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SS_CONSENT_DECISION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DecisionTypeAppenderTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private BailCase bailCase;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private DecisionTypeAppender decisionTypeAppender;
    @Mock
    private DateProvider dateProvider;

    private final LocalDate now = LocalDate.now();
    private static final String REFUSED = "refused";
    private static final String GRANTED = "granted";
    private static final String MINDED_TO_GRANT = "mindedToGrant";
    private static final String CONDITIONAL_GRANT = "conditionalGrant";

    @BeforeEach
    public void setUp() {
        decisionTypeAppender = new DecisionTypeAppender(dateProvider);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(dateProvider.now()).thenReturn(now);
    }

    @Test
    void set_decision_type_to_refused_branch_without_ss_consent() {

        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of(REFUSED));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(REFUSED));

        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.REFUSED);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, REFUSED);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_decision_type_to_refused_branch_with_ss_consent() {

        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(REFUSED));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(REFUSED));

        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.REFUSED);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, REFUSED);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_decision_type_to_refused_branch_with_ss_consent_minded_then_refused() {

        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(MINDED_TO_GRANT));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(REFUSED));

        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.REFUSED);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, REFUSED);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_decision_type_to_granted_branch_without_ss_consent() {

        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of(GRANTED));
        when(bailCase.read(RELEASE_STATUS_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(GRANTED));


        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.GRANTED);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, GRANTED);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_decision_type_to_granted_branch_with_ss_consent_minded_then_granted() {

        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(MINDED_TO_GRANT));
        when(bailCase.read(RELEASE_STATUS_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(GRANTED));

        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.GRANTED);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, GRANTED);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_decision_type_to_conditional_grant_branch_without_ss_consent() {

        when(bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class)).thenReturn(Optional.of(GRANTED));
        when(bailCase.read(RELEASE_STATUS_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(CONDITIONAL_GRANT));

        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.CONDITIONAL_GRANT);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, CONDITIONAL_GRANT);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_decision_type_to_conditional_grant_branch_with_ss_consent_minded_then_conditional_grant() {

        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(MINDED_TO_GRANT));
        when(bailCase.read(RELEASE_STATUS_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(SS_CONSENT_DECISION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(CONDITIONAL_GRANT));

        PreSubmitCallbackResponse<BailCase> response = decisionTypeAppender
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1))
            .write(RECORD_DECISION_TYPE, DecisionType.CONDITIONAL_GRANT);
        verify(bailCase, times(1))
            .write(RECORD_UNSIGNED_DECISION_TYPE, CONDITIONAL_GRANT);
        verify(bailCase, times(1))
            .write(DECISION_DETAILS_DATE, now.toString());
        verify(bailCase, times(1))
            .write(DECISION_UNSIGNED_DETAILS_DATE, now.toString());
    }

    @Test
    void set_throw_exception_if_cannot_append_decision_type() {

        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> decisionTypeAppender.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot assign a decision type")
            .isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    void handler_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = decisionTypeAppender.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT && (callback.getEvent() == Event.RECORD_THE_DECISION)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        Assertions.assertThatThrownBy(() -> decisionTypeAppender.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        Assertions.assertThatThrownBy(() -> decisionTypeAppender.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> decisionTypeAppender.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionTypeAppender
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionTypeAppender
            .handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionTypeAppender
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
