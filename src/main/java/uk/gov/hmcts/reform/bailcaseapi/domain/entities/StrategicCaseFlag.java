package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

@Value
@Getter
@EqualsAndHashCode
public class StrategicCaseFlag {

    String partyName;
    String roleOnCase;

    List<String> details;

    public StrategicCaseFlag(String applicantNameForDisplay) {
        this.partyName = applicantNameForDisplay;
        this.roleOnCase = "Applicant";
        this.details = Collections.emptyList();
    }

    public StrategicCaseFlag() {
        this.details = Collections.emptyList();
        this.partyName = null;
        this.roleOnCase = null;
    }
}
