package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IntermediateStateTest {

    @Test
    void has_correct_values() {
        assertEquals("decisionRecorded", IntermediateState.SIGNED_DECISION_NOTICE_UPLOADED.toString());
        assertEquals("signedDecisionNoticeUploaded", IntermediateState.SIGNED_DECISION_NOTICE_UPLOADED.toString());
        assertEquals("movedToDecided", IntermediateState.SIGNED_DECISION_NOTICE_UPLOADED.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(4, IntermediateState.values().length);
    }

}
