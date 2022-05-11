package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    void has_correct_values() {
        assertEquals("applicationStarted", State.APPLICATION_STARTED.toString());
        assertEquals("applicationStartedByLR", State.APPLICATION_STARTED_BY_LR.toString());
        assertEquals("applicationStartedByHO", State.APPLICATION_STARTED_BY_HO.toString());
        assertEquals("applicationEnded", State.APPLICATION_ENDED.toString());
        assertEquals("applicationSubmitted", State.APPLICATION_SUBMITTED.toString());
        assertEquals("bailSummaryUploaded", State.BAIL_SUMMARY_UPLOADED.toString());
        assertEquals("signedDecisionNoticeUploaded", State.SIGNED_DECISION_NOTICE_UPLOADED.toString());
        assertEquals("decisionDecided", State.DECISION_DECIDED.toString());
        assertEquals("decisionConditionalBail", State.DECISION_CONDITIONAL_BAIL.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(11, State.values().length);
    }

}
