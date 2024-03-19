package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
public class CurrentUserRoleAppenderTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;

    private CurrentUserRoleAppender currentUserRoleAppender;

    @BeforeEach
    public void setUp() {
        currentUserRoleAppender =
            new CurrentUserRoleAppender(userDetails, userDetailsHelper);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"UPLOAD_DOCUMENTS",
            "VIEW_PREVIOUS_APPLICATIONS"})
    void handler_writes_admin_as_current_user(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);


        PreSubmitCallbackResponse<BailCase> response =
            currentUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.ADMIN_OFFICER.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.HOME_OFFICE_BAIL.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.JUDGE.toString());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"UPLOAD_DOCUMENTS",
            "VIEW_PREVIOUS_APPLICATIONS"})
    void handler_writes_home_office_as_current_user(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.HOME_OFFICE_BAIL);


        PreSubmitCallbackResponse<BailCase> response =
            currentUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.HOME_OFFICE_BAIL.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.ADMIN_OFFICER.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.JUDGE.toString());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"UPLOAD_DOCUMENTS",
            "VIEW_PREVIOUS_APPLICATIONS"})
    void handler_writes_legal_rep_as_current_user(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);


        PreSubmitCallbackResponse<BailCase> response =
            currentUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.ADMIN_OFFICER.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.HOME_OFFICE_BAIL.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.JUDGE.toString());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"UPLOAD_DOCUMENTS",
            "VIEW_PREVIOUS_APPLICATIONS"})
    void handler_writes_judge_as_current_user(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.JUDGE);


        PreSubmitCallbackResponse<BailCase> response =
            currentUserRoleAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
        verify(bailCase, times(1)).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.JUDGE.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.ADMIN_OFFICER.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.HOME_OFFICE_BAIL.toString());
        verify(bailCase, never()).write(
            BailCaseFieldDefinition.CURRENT_USER, UserRoleLabel.LEGAL_REPRESENTATIVE.toString());
    }


    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> currentUserRoleAppender.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentUserRoleAppender.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentUserRoleAppender.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentUserRoleAppender.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"UPLOAD_DOCUMENTS",
            "VIEW_PREVIOUS_APPLICATIONS"})
    void handler_throws_error_if_cannot_actually_handle(Event event) {

        assertThatThrownBy(() -> currentUserRoleAppender.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(event);
        assertThatThrownBy(() -> currentUserRoleAppender.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
