package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailSupplementaryDataFixingHandlerTest {
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;

    private BailSupplementaryDataFixingHandler bailSupplementaryDataFixingHandler;

    @BeforeEach
    public void setUp() {
        bailSupplementaryDataFixingHandler = new BailSupplementaryDataFixingHandler(ccdSupplementaryUpdater);
    }

    @Test
    void set_to_earliest() {
        assertThat(bailSupplementaryDataFixingHandler.getDispatchPriority()).isEqualTo(EARLIEST);
    }

    @Test
    void should_invoke_supplementary_updater() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode orgsAssignedUsersValue = null;

        try {
            orgsAssignedUsersValue = mapper.readValue("\"testValue\"", JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, JsonNode> supplementaryData = new HashMap<>();
        supplementaryData.put("OrgAssignedUsers:", orgsAssignedUsersValue);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getSupplementaryData()).thenReturn(supplementaryData);

        PreSubmitCallbackResponse<BailCase> response = bailSupplementaryDataFixingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdSupplementaryUpdater).setHmctsServiceIdSupplementary(callback);
    }

    @Test
    void should_not_update_as_service_id_exists() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode orgsAssignedUsersValue = null;

        try {
            orgsAssignedUsersValue = mapper.readValue("\"testServiceId\"", JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, JsonNode> supplementaryData = new HashMap<>();
        supplementaryData.put("HMCTSServiceId", orgsAssignedUsersValue);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getSupplementaryData()).thenReturn(supplementaryData);

        PreSubmitCallbackResponse<BailCase> response = bailSupplementaryDataFixingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdSupplementaryUpdater, times(0)).setHmctsServiceIdSupplementary(callback);
    }
}
