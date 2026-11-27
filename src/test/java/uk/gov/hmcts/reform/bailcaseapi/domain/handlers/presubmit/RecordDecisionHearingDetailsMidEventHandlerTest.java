package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordDecisionHearingDetailsMidEventHandlerTest {

    private static final String PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final String CANCELLATION = "ActualCancellationReasonCodes";
    private static final String PART_HEARD = "ActualPartHeardReasonCodes";
    private static final List<Value> CANCELLATION_REASONS = List.of(new Value("withdrawn", "Withdrawn"));
    private static final List<Value> PART_HEARD_REASONS = List.of(new Value("noTime", "Ran out of time"));

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private IaHearingsApiService iaHearingsApiService;
    @Mock
    private RefDataUserService refDataUserService;
    @Mock
    private CommonDataResponse cancellationResponse;
    @Mock
    private CommonDataResponse partHeardResponse;
    @Mock
    private List<CategoryValues> cancellationCategoryValues;
    @Mock
    private List<CategoryValues> partHeardCategoryValues;

    private final BailCase fromHearingsApi = new BailCase();
    private RecordDecisionHearingDetailsMidEventHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn(PAGE_ID);
        when(iaHearingsApiService.midEvent(callback)).thenReturn(fromHearingsApi);

        when(refDataUserService.retrieveCategoryValues(CANCELLATION, "N")).thenReturn(cancellationResponse);
        when(refDataUserService.filterCategoryValuesByCategoryId(cancellationResponse, CANCELLATION))
            .thenReturn(cancellationCategoryValues);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(cancellationCategoryValues))
            .thenReturn(CANCELLATION_REASONS);
        when(refDataUserService.retrieveCategoryValues(PART_HEARD, "N")).thenReturn(partHeardResponse);
        when(refDataUserService.filterCategoryValuesByCategoryId(partHeardResponse, PART_HEARD))
            .thenReturn(partHeardCategoryValues);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(partHeardCategoryValues))
            .thenReturn(PART_HEARD_REASONS);

        handler = new RecordDecisionHearingDetailsMidEventHandler(iaHearingsApiService, refDataUserService);
    }

    @Test
    void should_return_the_hearing_details_with_the_reason_lists_from_ref_data() {
        PreSubmitCallbackResponse<BailCase> response = handler.handle(MID_EVENT, callback);

        assertEquals(fromHearingsApi, response.getData());
        assertEquals(
            new DynamicList(new Value("", ""), CANCELLATION_REASONS),
            fromHearingsApi.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, DynamicList.class).orElseThrow()
        );
        assertEquals(
            new DynamicList(new Value("", ""), PART_HEARD_REASONS),
            fromHearingsApi.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, DynamicList.class).orElseThrow()
        );
    }

    @Test
    void should_keep_a_reason_that_was_already_chosen() {
        Value chosen = new Value("withdrawn", "Withdrawn");
        fromHearingsApi.write(
            ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON,
            new DynamicList(chosen, List.of(chosen))
        );

        handler.handle(MID_EVENT, callback);

        assertEquals(
            new DynamicList(chosen, CANCELLATION_REASONS),
            fromHearingsApi.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, DynamicList.class).orElseThrow()
        );
    }

    @Test
    void should_only_handle_the_select_hearing_page_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == MID_EVENT;
                assertEquals(expected, handler.canHandle(stage, callback));
            }
        }

        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn("tribunalDecision");
        assertFalse(handler.canHandle(MID_EVENT, callback));
        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
