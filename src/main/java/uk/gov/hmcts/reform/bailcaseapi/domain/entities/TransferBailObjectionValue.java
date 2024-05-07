package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

import static java.util.Arrays.stream;

public enum TransferBailObjectionValue {
    YES("Yes, I object to the transfer"),
    NO("No, I consent to transferring to the Home Office");

    @JsonValue
    private final String value;

    TransferBailObjectionValue(String value) {
        this.value = value;
    }

    public static Optional<TransferBailObjectionValue> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
