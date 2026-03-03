package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DecisionTypeTest {

    @ParameterizedTest
    @EnumSource(value = DecisionType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CONDITIONAL_GRANT"})
    void decisionTypeIsValidForUploadSignedDecisionNotice(DecisionType decisionType) {
        assertTrue(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE));
        assertFalse(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT));
    }

    @ParameterizedTest
    @EnumSource(value = DecisionType.class, mode = EnumSource.Mode.INCLUDE, names = {"CONDITIONAL_GRANT"})
    void decisionTypeIsNotValidForUploadSignedDecisionNotice(DecisionType decisionType) {
        assertFalse(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE));
        assertTrue(decisionType.isValidFor(Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT));
    }
}
