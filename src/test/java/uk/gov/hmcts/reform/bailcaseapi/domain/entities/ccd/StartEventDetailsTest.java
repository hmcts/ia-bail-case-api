package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;

@SuppressWarnings("unchecked")
public class StartEventDetailsTest {

    private Event eventId = Event.SUBMIT_APPLICATION;
    private String token = "token";
    private CaseDetails<BailCase> caseDetails = mock(CaseDetails.class);

    private StartEventDetails startEventDetails = new StartEventDetails(eventId, token, caseDetails);

    @Test
    void should_hold_onto_values() {
        assertEquals(eventId, startEventDetails.getEventId());
        assertEquals(token, startEventDetails.getToken());
        assertEquals(caseDetails, startEventDetails.getCaseDetails());
    }
}
