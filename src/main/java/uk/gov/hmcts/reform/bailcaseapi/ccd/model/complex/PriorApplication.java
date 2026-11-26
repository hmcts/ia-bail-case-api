package uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ComplexType(name = "priorApplication", generate = true)
public class PriorApplication {
    @CCD(label = "Application Id")
    private String applicationId;

    @CCD(label = "Case data")
    private String caseDataJson;
}
