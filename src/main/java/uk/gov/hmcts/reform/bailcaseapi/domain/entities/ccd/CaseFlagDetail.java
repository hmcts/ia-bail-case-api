package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagDetail {
    private String id;
    @JsonProperty("value")
    private CaseFlagValue caseFlagValue;
}
