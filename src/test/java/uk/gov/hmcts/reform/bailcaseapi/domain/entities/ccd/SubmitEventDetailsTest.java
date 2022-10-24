package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class SubmitEventDetailsTest {

    private final long id = 1000L;
    private final String jurisdiction = "Jurisdiction";
    private final State state = State.APPLICATION_ENDED;
    private Map<String, Object> data = Map.of("data", "data");
    private final int callbackResponseStatusCode = 200;
    private final String callbackResponseStatus = "callbackResponseStatus";

    private final SubmitEventDetails submitEventDetails = new SubmitEventDetails(
        id,
        jurisdiction,
        state,
        data,
        callbackResponseStatusCode,
        callbackResponseStatus);

    @Test
    void should_hold_onto_values() {
        assertEquals(id, submitEventDetails.getId());
        assertEquals(jurisdiction, submitEventDetails.getJurisdiction());
        assertEquals(state, submitEventDetails.getState());
        assertEquals(data, submitEventDetails.getData());
        assertEquals(callbackResponseStatusCode, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals(callbackResponseStatus, submitEventDetails.getCallbackResponseStatus());
    }
}
