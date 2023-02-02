package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@Value
@Builder
@JsonIgnoreProperties(value = "dateTimeCreated", ignoreUnknown = true)
public class CaseFlagValue {
    private String name;
    private String status;
    private String flagCode;
    private YesOrNo hearingRelevant;

}
