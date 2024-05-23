package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousDecisionDetails {

    private String decisionDetailsDate;
    private String recordDecisionType;
    private Document uploadSignedDecisionNoticeDocument;
}
