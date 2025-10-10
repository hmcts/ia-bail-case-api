package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_ACTIVE_INTERPRETER_FLAG;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag.ROLE_ON_CASE_APPLICANT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InterpreterFlagHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    private InterpreterFlagHandler interpreterFlagHandler;

    CaseFlagValue inactiveInterpreterFlagValue = CaseFlagValue.builder()
        .name("Language Interpreter")
        .status("Inactive")
        .build();
    CaseFlagValue randomFlagValue = CaseFlagValue.builder()
        .name("Some random flag")
        .status("Active")
        .build();
    CaseFlagValue activeInterpreterFlagValue = CaseFlagValue.builder()
        .name("Language Interpreter")
        .status("Active")
        .build();

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        interpreterFlagHandler = new InterpreterFlagHandler();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"})
    void should_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertTrue(interpreterFlagHandler.canHandle(ABOUT_TO_SUBMIT, callback));
        interpreterFlagHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(bailCase, times(1)).clear(HAS_ACTIVE_INTERPRETER_FLAG);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(interpreterFlagHandler.canHandle(ABOUT_TO_SUBMIT, callback));
        assertThatThrownBy(() -> interpreterFlagHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_invalid_callback_stages(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);

        assertFalse(interpreterFlagHandler.canHandle(callbackStage, callback));
        assertThatThrownBy(() -> interpreterFlagHandler.handle(callbackStage, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"})
    void should_clear_has_active_interpreter_flag_if_inactive(Event event) {
        when(callback.getEvent()).thenReturn(event);
        List<CaseFlagDetail> details = Collections.singletonList(
            new CaseFlagDetail("id1", inactiveInterpreterFlagValue)
        );
        StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(
            "some name",
            ROLE_ON_CASE_APPLICANT,
            details
        );

        when(bailCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(strategicCaseFlag));
        interpreterFlagHandler.handle(ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1))
            .clear(HAS_ACTIVE_INTERPRETER_FLAG);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"})
    void should_clear_has_active_interpreter_flag_if_active_not_interpreter(Event event) {
        when(callback.getEvent()).thenReturn(event);
        List<CaseFlagDetail> details = Collections.singletonList(
            new CaseFlagDetail("id1", randomFlagValue)
        );
        StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(
            "some name",
            ROLE_ON_CASE_APPLICANT,
            details
        );

        when(bailCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(strategicCaseFlag));
        interpreterFlagHandler.handle(ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1))
            .clear(HAS_ACTIVE_INTERPRETER_FLAG);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"})
    void should_write_has_active_interpreter_flag_if_active_interpreter_present(Event event) {
        when(callback.getEvent()).thenReturn(event);
        List<CaseFlagDetail> details = List.of(
            new CaseFlagDetail("id1", inactiveInterpreterFlagValue),
            new CaseFlagDetail("id1", randomFlagValue),
            new CaseFlagDetail("id1", activeInterpreterFlagValue)
        );
        StrategicCaseFlag strategicCaseFlag = new StrategicCaseFlag(
            "some name",
            ROLE_ON_CASE_APPLICANT,
            details
        );

        when(bailCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class))
            .thenReturn(Optional.of(strategicCaseFlag));
        interpreterFlagHandler.handle(ABOUT_TO_SUBMIT, callback);
        verify(bailCase, times(1))
            .write(HAS_ACTIVE_INTERPRETER_FLAG, YES);
    }
}

