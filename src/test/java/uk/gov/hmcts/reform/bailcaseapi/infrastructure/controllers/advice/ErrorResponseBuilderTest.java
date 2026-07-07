package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class ErrorResponseBuilderTest {

    private static final String TEST_CORRELATION_ID = "test-correlation-id-123";
    private static final String TEST_URI = "/bail/ccdAboutToSubmit";

    @Mock
    private HttpServletRequest request;

    private ErrorResponseBuilder errorResponseBuilder;

    @BeforeEach
    void setUp() {
        errorResponseBuilder = new ErrorResponseBuilder();
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, TEST_CORRELATION_ID);
        when(request.getRequestURI()).thenReturn(TEST_URI);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void build_shouldCreateErrorResponseWithDefaultMessage() {
        ErrorResponse response = errorResponseBuilder.build(ErrorCode.ACCESS_DENIED, request, null);

        assertNotNull(response);
        assertEquals(ErrorCode.ACCESS_DENIED.getCode(), response.getErrorCode());
        assertEquals(ErrorCode.ACCESS_DENIED.getDefaultMessage(), response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
        assertNull(response.getFieldErrors());
    }

    @Test
    void build_shouldCreateErrorResponseWithCustomMessage() {
        String customMessage = "Field 'applicantName' is required";
        ErrorResponse response = errorResponseBuilder.build(
            ErrorCode.REQUIRED_FIELD_MISSING, request, customMessage);

        assertNotNull(response);
        assertEquals(ErrorCode.REQUIRED_FIELD_MISSING.getCode(), response.getErrorCode());
        assertEquals(customMessage, response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void buildWithFieldErrors_shouldCreateErrorResponseWithFieldErrors() {
        List<ErrorResponse.FieldError> fieldErrors = List.of(
            ErrorResponse.FieldError.builder()
                .field("applicantName")
                .message("must not be blank")
                .build(),
            ErrorResponse.FieldError.builder()
                .field("applicantDob")
                .message("must be a past date")
                .build()
        );

        ErrorResponse response = errorResponseBuilder.buildWithFieldErrors(
            ErrorCode.VALIDATION_ERROR, request, fieldErrors);

        assertNotNull(response);
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR.getDefaultMessage(), response.getMessage());
        assertEquals(TEST_CORRELATION_ID, response.getRequestId());
        assertEquals(TEST_URI, response.getPath());
        assertNotNull(response.getTimestamp());
        assertNotNull(response.getFieldErrors());
        assertEquals(2, response.getFieldErrors().size());
        assertEquals("applicantName", response.getFieldErrors().get(0).getField());
        assertEquals("must not be blank", response.getFieldErrors().get(0).getMessage());
    }

    @Test
    void build_shouldHandleMissingCorrelationId() {
        MDC.clear();

        ErrorResponse response = errorResponseBuilder.build(ErrorCode.INTERNAL_ERROR, request, null);

        assertNotNull(response);
        assertNull(response.getRequestId());
    }
}
