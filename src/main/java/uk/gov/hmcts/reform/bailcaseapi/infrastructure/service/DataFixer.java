package uk.gov.hmcts.reform.bailcaseapi.infrastructure.service;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;

public interface DataFixer {
    void fix(BailCase bailCase);

}
