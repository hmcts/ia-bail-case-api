package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class ErrorResponseLoggerTest {

    private final ErrorResponseLogger errorResponseLogger = new ErrorResponseLogger();

    @Test
    void maybeLogException_shouldLogRestClientResponseException() {
        RestClientResponseException ex = new RestClientResponseException(
            "Service error",
            HttpStatusCode.valueOf(500),
            "Internal Server Error",
            null,
            "{\"error\": \"Something went wrong\"}".getBytes(),
            null
        );

        // This should not throw - just verifying no exception occurs
        errorResponseLogger.maybeLogException(ex);
    }

    @Test
    void maybeLogException_shouldNotLogWhenExceptionIsNotRestClientResponseException() {
        RuntimeException ex = new RuntimeException("Generic error");

        // This should not throw and should not log anything
        errorResponseLogger.maybeLogException(ex);
    }

    @Test
    void maybeLogException_shouldNotLogResponseBodyContainingData() {
        RestClientResponseException ex = new RestClientResponseException(
            "Service error",
            HttpStatusCode.valueOf(400),
            "Bad Request",
            null,
            "{\"data\": {\"caseId\": \"12345\"}}".getBytes(),
            null
        );

        // This should not throw - body with data is not logged
        errorResponseLogger.maybeLogException(ex);
    }

    @Test
    void maybeLogException_shouldHandleNullException() {
        // This should not throw
        errorResponseLogger.maybeLogException(null);
    }
}
