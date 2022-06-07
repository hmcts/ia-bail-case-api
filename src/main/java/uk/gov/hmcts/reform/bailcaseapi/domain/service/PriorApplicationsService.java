package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRIOR_APPLICATIONS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Service
public class PriorApplicationsService {

    @Autowired
    private Appender<PriorApplication> appender;

    public void appendPriorApplication(BailCase bailCase, BailCase bailCaseBefore) {

        Optional<List<IdValue<PriorApplication>>> maybeExistingPriorApplication = bailCase.read(BailCaseFieldDefinition.PRIOR_APPLICATIONS);

        String nextAppId = String.valueOf(maybeExistingPriorApplication.orElse(Collections.emptyList()).size() + 1);

        List<IdValue<PriorApplication>> allPriorApplications = appender.append(
            buildNewPriorApplication(nextAppId, bailCaseBefore), maybeExistingPriorApplication.orElse(Collections.emptyList()));

        bailCase.write(PRIOR_APPLICATIONS, allPriorApplications);

    }

    private PriorApplication buildNewPriorApplication(String nextAppId, BailCase bailCaseBefore) {
        return new PriorApplication(
            nextAppId,
            bailCaseBefore
        );
    }
}
