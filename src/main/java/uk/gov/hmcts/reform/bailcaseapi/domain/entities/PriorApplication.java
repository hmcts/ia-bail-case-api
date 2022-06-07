package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

public class PriorApplication{

    private final String applicationId;
    private final BailCase caseData;

    public PriorApplication(String applicationId, BailCase caseData) {
        this.applicationId = applicationId;
        this.caseData = caseData;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public BailCase getCaseData() {
        return caseData;
    }
}
