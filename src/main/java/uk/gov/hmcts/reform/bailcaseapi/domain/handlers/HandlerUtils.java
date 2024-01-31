package uk.gov.hmcts.reform.bailcaseapi.domain.handlers;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

public class HandlerUtils {

    private HandlerUtils() {
    }

    public static boolean isImaEnabled(BailCase bailCase) {
        return bailCase.read(IS_IMA_ENABLED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

}
