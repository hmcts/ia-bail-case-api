package uk.gov.hmcts.reform.bailcaseapi.ccd.model.type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ComplexType(generate = false)
public class DynamicListElement {

    private String code;

    private String label;
}
