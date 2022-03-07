package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.CompanyNameProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@ExtendWith(MockitoExtension.class)
class LegalRepresentativeDetailsAppenderTest {

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
    @Mock
    private OrganisationEntityResponse organisationResponse;
    @Mock
    CompanyNameProvider companyNameProvider;

    private LegalRepresentativeDetailsAppender legalRepresentativeDetailsAppender;
    private final String organisationName = "some company name";
    private final String legalRepEmailAddress = "john.doe@example.com";


    @BeforeEach
    public void setUp() {
        legalRepresentativeDetailsAppender =
            new LegalRepresentativeDetailsAppender(userDetails, userDetailsHelper, companyNameProvider);
    }

    @Test
    void should_write_to_bail_case_field_for_legal_rep_user() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetails.getId()).thenReturn(legalRepEmailAddress);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(organisationResponse.getName()).thenReturn(organisationName);


        PreSubmitCallbackResponse<BailCase> response =
            legalRepresentativeDetailsAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();

        verify(companyNameProvider, times(1)).prepareCompanyName(callback);
        assertEquals(organisationName, organisationResponse.getName());
        verify(bailCase, times(1)).write(LEGAL_REP_EMAIL_ADDRESS, legalRepEmailAddress);
    }

    @Test
    void should_not_write_to_bail_case_field_it_is_not_legal_rep_user() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetails.getId()).thenReturn(legalRepEmailAddress);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.ADMIN_OFFICER);


        PreSubmitCallbackResponse<BailCase> response =
            legalRepresentativeDetailsAppender.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();

        verify(companyNameProvider, times(0)).prepareCompanyName(callback);
        verify(bailCase, times(0)).write(LEGAL_REP_EMAIL_ADDRESS, legalRepEmailAddress);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = legalRepresentativeDetailsAppender.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START
                    && (callback.getEvent() == Event.START_APPLICATION)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeDetailsAppender
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeDetailsAppender
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeDetailsAppender
            .handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeDetailsAppender
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> legalRepresentativeDetailsAppender.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        assertThatThrownBy(() -> legalRepresentativeDetailsAppender.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
