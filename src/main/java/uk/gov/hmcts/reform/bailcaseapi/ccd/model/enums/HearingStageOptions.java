package uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Fixed list {@code hearingStageOptions}.
 */
@ComplexType(name = "hearingStageOptions", generate = true)
@Getter
@AllArgsConstructor
public enum HearingStageOptions implements HasLabel {
    BAIL("Bail", "BFA1-BAI"),

    CASE_MANAGEMENT_REVIEW("Case Management Review", "BFA1-CMR"),

    COSTS("Costs", "BFA1-COS"),

    SUBSTANTIVE("Substantive", "BFA1-SUB");

    private final String label;

    @JsonValue
    private final String code;
}
