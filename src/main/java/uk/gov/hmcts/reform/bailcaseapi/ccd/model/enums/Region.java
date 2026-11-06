package uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@ComplexType(name = "region", generate = true)
@Getter
@AllArgsConstructor
public enum Region implements HasLabel {
    _1("National", "1");

    private final String label;

    @JsonValue
    private final String code;
}
