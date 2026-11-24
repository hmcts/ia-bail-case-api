package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
class RecordDecisionParticipantsMidEventHandlerTest {

    private static final String SUB_CHANNEL = "HearingSubChannel";
    private static final String ROLE_CODE = "EntityRoleCode";
    private static final List<Value> SUB_CHANNELS = List.of(
        new Value("VIDCVP", "Video - CVP"), new Value("INTER", "In Person"));
    private static final List<Value> ROLES = List.of(
        new Value("APEL", "Appellant"), new Value("LGRP", "Legal Representative"));

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private IaHearingsApiService iaHearingsApiService;
    @Mock
    private RefDataUserService refDataUserService;
    @Mock
    private CommonDataResponse subChannelResponse;
    @Mock
    private CommonDataResponse roleResponse;
    @Mock
    private List<CategoryValues> subChannelValues;
    @Mock
    private List<CategoryValues> roleValues;

    private final BailCase fromHearingsApi = new BailCase();
    private RecordDecisionParticipantsMidEventHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn(RecordDecisionParticipantsMidEventHandler.TIMES_PAGE_ID);
        when(iaHearingsApiService.midEvent(callback)).thenReturn(fromHearingsApi);

        when(refDataUserService.retrieveCategoryValues(SUB_CHANNEL, "N")).thenReturn(subChannelResponse);
        when(refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(subChannelResponse, SUB_CHANNEL))
            .thenReturn(subChannelValues);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(subChannelValues)).thenReturn(SUB_CHANNELS);
        when(refDataUserService.retrieveCategoryValues(ROLE_CODE, "N")).thenReturn(roleResponse);
        when(refDataUserService.filterCategoryValuesByCategoryId(roleResponse, ROLE_CODE)).thenReturn(roleValues);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(roleValues)).thenReturn(ROLES);

        handler = new RecordDecisionParticipantsMidEventHandler(iaHearingsApiService, refDataUserService);
    }

    @Test
    void should_add_ref_data_lists_to_the_participants_from_hearings_api() {
        fromHearingsApi.write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            row("1", Map.of(
                "partyId", "P1",
                "role", hmcValue("APEL"),
                "roleLabel", "APPL",
                "attendanceType", hmcValue("VIDCVP"),
                "prefilled", "Yes")),
            row("2", Map.of("firstName", "Ann", "role", hmcValue("LGRP"), "representedParty", hmcValue("APEL")))
        ));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(MID_EVENT, callback);

        assertEquals(fromHearingsApi, response.getData());
        List<Map<String, Object>> participants = writtenParticipants();

        Map<String, Object> applicant = participants.get(0);
        assertEquals("P1", applicant.get("partyId"));
        assertEquals(new DynamicList(new Value("APEL", "Appellant"), ROLES), applicant.get("role"));
        assertEquals("Appellant", applicant.get("roleLabel"));
        assertEquals(new DynamicList(new Value("VIDCVP", "Video - CVP"), SUB_CHANNELS), applicant.get("attendanceType"));
        assertEquals(new DynamicList(new Value("", ""), ROLES), applicant.get("representedParty"));

        Map<String, Object> legalRep = participants.get(1);
        assertEquals(new DynamicList(new Value("LGRP", "Legal Representative"), ROLES), legalRep.get("role"));
        assertEquals(new DynamicList(new Value("APEL", "Appellant"), ROLES), legalRep.get("representedParty"));
        assertEquals(new DynamicList(new Value("", ""), SUB_CHANNELS), legalRep.get("attendanceType"));
        assertFalse(legalRep.containsKey("roleLabel"));
    }

    @Test
    void should_not_select_a_code_missing_from_ref_data() {
        fromHearingsApi.write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            row("1", Map.of("attendanceType", hmcValue("TELBTM"), "prefilled", "Yes"))));

        handler.handle(MID_EVENT, callback);

        Map<String, Object> participant = writtenParticipants().get(0);
        assertEquals(new DynamicList(new Value("", ""), SUB_CHANNELS), participant.get("attendanceType"));
        assertFalse(participant.containsKey("roleLabel"));
    }

    @Test
    void should_not_call_ref_data_when_there_are_no_participants() {
        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(refDataUserService);
    }

    @Test
    void should_only_handle_the_times_page_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == MID_EVENT;
                assertEquals(expected, handler.canHandle(stage, callback));
            }
        }

        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn("selectHearingRecordAttendeesAndDuration");
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

    private static Map<String, Object> row(String id, Map<String, Object> value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("value", new LinkedHashMap<>(value));
        return row;
    }

    private static Map<String, Object> hmcValue(String code) {
        return Map.of("value", Map.of("code", code, "label", code), "list_items", List.of());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> writtenParticipants() {
        // read as written, without converting the lists back to maps
        return ((List<Object>) fromHearingsApi.get(ATTENDEES_AND_DURATION_PARTICIPANTS.value()))
            .stream()
            .map(row -> (Map<String, Object>) ((Map<String, Object>) row).get("value"))
            .toList();
    }
}
