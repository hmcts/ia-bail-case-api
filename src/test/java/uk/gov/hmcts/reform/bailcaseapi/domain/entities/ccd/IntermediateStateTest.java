package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IntermediateStateTest {

    @Test
    void has_correct_values() {

        assertEquals("unknown", State.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {

        assertEquals(1, IntermediateState.values().length);

    }

}
