package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

import static java.util.Arrays.stream;

public enum TransferBailObjectionValue {
    YES("yesObjection"),
    NO("noObjection");

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
