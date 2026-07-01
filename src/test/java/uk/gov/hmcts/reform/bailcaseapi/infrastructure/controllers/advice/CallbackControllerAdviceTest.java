package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.AsylumCaseServiceResponseException;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseServiceResponseException;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdDataIntegrationException;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CallbackControllerAdviceTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id-123";
    private static final String TEST_URI = "/bail/ccdAboutToSubmit";

    @Mock
    private HttpServletRequest request;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    @Mock
    private ErrorResponseLogger errorResponseLogger;

    private CallbackControllerAdvice handler;

    @BeforeEach
    void setUp() {
        handler = new CallbackControllerAdvice(errorResponseLogger, errorResponseBuilder);
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
        when(request.getRequestURI()).thenReturn(TEST_URI);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleAccessDeniedException_returns403_withoutLoggingCause() {
        AccessDeniedException ex = new AccessDeniedException("User not authorized");
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.ACCESS_DENIED, null);
        when(errorResponseBuilder.build(eq(ErrorCode.ACCESS_DENIED), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(request, ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.ACCESS_DENIED.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.ACCESS_DENIED.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getBody().getRequestId());
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        verify(errorResponseBuilder).logError(ex, ErrorCode.ACCESS_DENIED, request);
        verifyNoInteractions(errorResponseLogger);
    }

    @Test
    void handleRequiredFieldMissingException_returns400WithFieldName() {
        String errorMessage = "applicantName is required";
        RequiredFieldMissingException ex = new RequiredFieldMissingException(errorMessage);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.REQUIRED_FIELD_MISSING, errorMessage);
        when(errorResponseBuilder.build(eq(ErrorCode.REQUIRED_FIELD_MISSING), eq(request), eq(errorMessage)))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleRequiredFieldMissingException(request, ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.REQUIRED_FIELD_MISSING.getCode(), response.getBody().getErrorCode());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.REQUIRED_FIELD_MISSING, request);
    }

    @Test
    void handleBailCaseServiceResponseException_returns502_andLogsCause() {
        RuntimeException cause = new RuntimeException("Connection timeout");
        BailCaseServiceResponseException ex =
            new BailCaseServiceResponseException("Service failed", cause);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.BAIL_CASE_SERVICE_ERROR, null);
        when(errorResponseBuilder.build(eq(ErrorCode.BAIL_CASE_SERVICE_ERROR), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleBailCaseServiceResponseException(request, ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAIL_CASE_SERVICE_ERROR.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.BAIL_CASE_SERVICE_ERROR.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.BAIL_CASE_SERVICE_ERROR, request);
        verify(errorResponseLogger).maybeLogException(cause);
    }

    @Test
    void handleAsylumCaseServiceResponseException_returns502_andLogsCause() {
        RuntimeException cause = new RuntimeException("Connection timeout");
        AsylumCaseServiceResponseException ex =
            new AsylumCaseServiceResponseException("Service failed", cause);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.ASYLUM_CASE_SERVICE_ERROR, null);
        when(errorResponseBuilder.build(eq(ErrorCode.ASYLUM_CASE_SERVICE_ERROR), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleAsylumCaseServiceResponseException(request, ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.ASYLUM_CASE_SERVICE_ERROR.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.ASYLUM_CASE_SERVICE_ERROR.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.ASYLUM_CASE_SERVICE_ERROR, request);
        verify(errorResponseLogger).maybeLogException(cause);
    }

    @Test
    void handleCcdDataIntegrationException_returns502_andLogsCause() {
        RuntimeException cause = new RuntimeException("Timeout");
        CcdDataIntegrationException ex =
            new CcdDataIntegrationException("CCD service failed", cause);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.CCD_INTEGRATION_ERROR, null);
        when(errorResponseBuilder.build(eq(ErrorCode.CCD_INTEGRATION_ERROR), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleCcdDataIntegrationException(request, ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.CCD_INTEGRATION_ERROR.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.CCD_INTEGRATION_ERROR.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.CCD_INTEGRATION_ERROR, request);
        verify(errorResponseLogger).maybeLogException(cause);
    }

    @Test
    void handleIdentityManagerResponseException_returns503_andLogsCause() {
        RuntimeException cause = new RuntimeException("Connection refused");
        IdentityManagerResponseException ex =
            new IdentityManagerResponseException("IDAM unavailable", cause);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.IDENTITY_SERVICE_ERROR, null);
        when(errorResponseBuilder.build(eq(ErrorCode.IDENTITY_SERVICE_ERROR), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleIdentityManagerResponseException(request, ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.IDENTITY_SERVICE_ERROR.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.IDENTITY_SERVICE_ERROR.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.IDENTITY_SERVICE_ERROR, request);
        verify(errorResponseLogger).maybeLogException(cause);
    }

    @Test
    void handleIllegalArgumentException_returns400_withDefaultMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid case ID format");
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.INVALID_ARGUMENT, null);
        when(errorResponseBuilder.build(eq(ErrorCode.INVALID_ARGUMENT), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(request, ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INVALID_ARGUMENT.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.INVALID_ARGUMENT.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.INVALID_ARGUMENT, request);
    }

    @Test
    void handleIllegalStateException_returns400_withDefaultMessage() {
        IllegalStateException ex = new IllegalStateException("Invalid state transition");
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.INVALID_STATE, null);
        when(errorResponseBuilder.build(eq(ErrorCode.INVALID_STATE), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException(request, ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INVALID_STATE.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.INVALID_STATE.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.INVALID_STATE, request);
    }

    @Test
    void handleGenericException_returns500_withDefaultMessage_andLogsCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        Exception ex = new RuntimeException("Something went wrong", cause);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.INTERNAL_ERROR, null);
        when(errorResponseBuilder.build(eq(ErrorCode.INTERNAL_ERROR), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(request, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getErrorCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getDefaultMessage(), response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        verify(errorResponseBuilder).logError(ex, ErrorCode.INTERNAL_ERROR, request);
        verify(errorResponseLogger).maybeLogException(cause);
    }

    @Test
    void errorResponse_containsCorrelationId() {
        RuntimeException cause = new RuntimeException("Root cause");
        Exception ex = new RuntimeException("Test error", cause);
        ErrorResponse errorResponse = buildMockErrorResponse(ErrorCode.INTERNAL_ERROR, null);
        when(errorResponseBuilder.build(eq(ErrorCode.INTERNAL_ERROR), eq(request), any()))
            .thenReturn(errorResponse);

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(request, ex);

        assertNotNull(response.getBody());
        assertEquals(TEST_CORRELATION_ID, response.getBody().getRequestId());
    }

    private ErrorResponse buildMockErrorResponse(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
            .errorCode(errorCode.getCode())
            .message(customMessage != null ? customMessage : errorCode.getDefaultMessage())
            .timestamp(Instant.now())
            .requestId(TEST_CORRELATION_ID)
            .path(TEST_URI)
            .build();
    }
}
