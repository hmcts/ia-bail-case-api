package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailCasePostNotificationApiSenderTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String CCD_SUBMITTED_PATH = "/path";

    @Mock
    private BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    @Mock
    private Callback<BailCase> callback;

    private BailCasePostNotificationApiSender bailCasePostNotificationApiSender;

    @BeforeEach
    public void setUp() {

        bailCasePostNotificationApiSender =
            new BailCasePostNotificationApiSender(
                bailCaseCallbackApiDelegator,
                ENDPOINT,
                CCD_SUBMITTED_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api() {

        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(bailCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        final PostSubmitCallbackResponse postSubmitCallbackResponse = bailCasePostNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, postSubmitCallbackResponse);
    }

    @Test
    void should_delegate_about_to_start_callback_to_downstream_api() {

        final PostSubmitCallbackResponse notifiedAsylumCase = mock(PostSubmitCallbackResponse.class);

        when(bailCaseCallbackApiDelegator.delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH))
            .thenReturn(notifiedAsylumCase);

        final PostSubmitCallbackResponse actualAsylumCase = bailCasePostNotificationApiSender.send(callback);

        verify(bailCaseCallbackApiDelegator, times(1))
            .delegatePostSubmit(callback, ENDPOINT + CCD_SUBMITTED_PATH);

        assertEquals(notifiedAsylumCase, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> bailCasePostNotificationApiSender.send(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
