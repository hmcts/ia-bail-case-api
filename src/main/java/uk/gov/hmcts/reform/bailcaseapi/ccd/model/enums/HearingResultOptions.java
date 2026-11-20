package uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Fixed list {@code hearingResultOptions}.
 */
@ComplexType(name = "hearingResultOptions", generate = true)
@Getter
@AllArgsConstructor
public enum HearingResultOptions implements HasLabel {
    COMPLETED("Heard", "COMPLETED"),

    ADJOURNED("Part-heard, adjourned or postponed", "ADJOURNED"),

    CANCELLED("Withdrawn, settled or vacated", "CANCELLED");

    private final String label;

    @JsonValue
    private final String code;
}
