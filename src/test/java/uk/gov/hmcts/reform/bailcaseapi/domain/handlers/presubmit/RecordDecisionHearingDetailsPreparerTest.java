package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.JUDGE_DETAILS_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseServiceResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordDecisionHearingDetailsPreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    private final BailCase bailCase = new BailCase();
    private RecordDecisionHearingDetailsPreparer preparer;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        bailCase.write(JUDGE_DETAILS_NAME, "Judge Judy");

        preparer = new RecordDecisionHearingDetailsPreparer(iaHearingsApiService);
    }

    @Test
    void should_mark_hearings_as_available_when_hmc_has_one_awaiting_details() {
        BailCase fromHearingsApi = new BailCase();
        fromHearingsApi.write(
            ATTENDEES_AND_DURATION_HEARING,
            new DynamicList(new Value("", ""), List.of(new Value("2000001", "Bail (Awaiting hearing details)")))
        );
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(fromHearingsApi);

        PreSubmitCallbackResponse<BailCase> response = preparer.handle(ABOUT_TO_START, callback);

        assertEquals(fromHearingsApi, response.getData());
        assertEquals(
            YesOrNo.YES,
            response.getData().read(ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE, YesOrNo.class).orElseThrow()
        );
    }

    @Test
    void should_mark_hearings_as_unavailable_when_there_are_none() {
        BailCase fromHearingsApi = new BailCase();
        fromHearingsApi.write(ATTENDEES_AND_DURATION_HEARING, new DynamicList(new Value("", ""), List.of()));
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(fromHearingsApi);

        PreSubmitCallbackResponse<BailCase> response = preparer.handle(ABOUT_TO_START, callback);

        assertEquals(
            YesOrNo.NO,
            response.getData().read(ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE, YesOrNo.class).orElseThrow()
        );
    }

    @Test
    void should_still_start_the_event_when_ia_hearings_api_fails() {
        when(iaHearingsApiService.aboutToStart(callback))
            .thenThrow(new BailCaseServiceResponseException("failed", new RuntimeException()));

        PreSubmitCallbackResponse<BailCase> response = preparer.handle(ABOUT_TO_START, callback);

        assertEquals(bailCase, response.getData());
        assertEquals(
            YesOrNo.NO,
            response.getData().read(ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE, YesOrNo.class).orElseThrow()
        );
        assertEquals("Judge Judy", response.getData().read(JUDGE_DETAILS_NAME, String.class).orElseThrow());
    }

    @Test
    void should_clear_details_left_from_an_earlier_run_before_asking_for_hearings() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, "1999999");
        bailCase.write(ATTENDEES_AND_DURATION_START_TIME, "10:00");
        when(iaHearingsApiService.aboutToStart(callback)).thenAnswer(invocation -> {
            assertNull(bailCase.get(ATTENDEES_AND_DURATION_HEARING_ID.value()));
            assertNull(bailCase.get(ATTENDEES_AND_DURATION_START_TIME.value()));
            return bailCase;
        });

        preparer.handle(ABOUT_TO_START, callback);

        assertNull(bailCase.get(ATTENDEES_AND_DURATION_HEARING_ID.value()));
    }

    @Test
    void should_only_handle_about_to_start_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == ABOUT_TO_START;
                assertEquals(expected, preparer.canHandle(stage, callback));
            }
        }

        assertThatThrownBy(() -> preparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> preparer.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> preparer.canHandle(ABOUT_TO_START, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
