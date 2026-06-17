package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers.advice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", HttpStatus.BAD_REQUEST, "A required field is missing"),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_ARGUMENT("INVALID_ARGUMENT", HttpStatus.BAD_REQUEST, "Invalid argument provided"),
    INVALID_STATE("INVALID_STATE", HttpStatus.BAD_REQUEST, "Invalid application state"),
    MALFORMED_REQUEST("MALFORMED_REQUEST", HttpStatus.BAD_REQUEST, "Malformed request body"),

    // 403 Forbidden
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access to this resource is denied"),

    // 502 Bad Gateway
    BAIL_CASE_SERVICE_ERROR("BAIL_CASE_SERVICE_ERROR", HttpStatus.BAD_GATEWAY, "Error processing bail case"),
    ASYLUM_CASE_SERVICE_ERROR("ASYLUM_CASE_SERVICE_ERROR", HttpStatus.BAD_GATEWAY, "Error processing asylum case"),
    CCD_INTEGRATION_ERROR("CCD_INTEGRATION_ERROR", HttpStatus.BAD_GATEWAY, "Error communicating with CCD"),

    // 503 Service Unavailable
    IDENTITY_SERVICE_ERROR("IDENTITY_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE,
        "Authentication service unavailable"),

    // 500 Internal Server Error
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
