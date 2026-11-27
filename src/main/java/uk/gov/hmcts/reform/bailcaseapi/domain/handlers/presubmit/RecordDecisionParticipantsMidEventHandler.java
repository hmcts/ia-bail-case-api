package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

/**
 * After the times page, ia-hearings-api pre-fills the participants from HMC and the attendance type and role
 * lists of every participant are loaded from reference data.
 */
@Component
@RequiredArgsConstructor
public class RecordDecisionParticipantsMidEventHandler implements PreSubmitCallbackHandler<BailCase> {

    static final String TIMES_PAGE_ID = "timesRecordAttendeesAndDuration";
    static final String HEARING_SUB_CHANNEL = "HearingSubChannel";
    static final String ENTITY_ROLE_CODE = "EntityRoleCode";
    private static final String IS_CHILD_REQUIRED = "N";

    private static final String VALUE = "value";
    private static final String ATTENDANCE_TYPE = "attendanceType";
    private static final String ROLE = "role";
    private static final String ROLE_LABEL = "roleLabel";
    private static final String REPRESENTED_PARTY = "representedParty";
    private static final String PREFILLED = "prefilled";

    private final IaHearingsApiService iaHearingsApiService;
    private final RefDataUserService refDataUserService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.RECORD_THE_DECISION
               && Objects.equals(callback.getPageId(), TIMES_PAGE_ID);
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = iaHearingsApiService.midEvent(callback);

        populateParticipantLists(bailCase);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    @SuppressWarnings("unchecked")
    private void populateParticipantLists(BailCase bailCase) {
        List<Object> participants = bailCase.<List<Object>>read(ATTENDEES_AND_DURATION_PARTICIPANTS)
            .orElse(List.of());
        if (participants.isEmpty()) {
            return;
        }

        List<Value> subChannels = listItems(HEARING_SUB_CHANNEL, true);
        List<Value> roles = listItems(ENTITY_ROLE_CODE, false);
        List<Object> updated = new ArrayList<>();
        for (Object item : participants) {
            if (!(item instanceof Map<?, ?> original) || !(original.get(VALUE) instanceof Map<?, ?> value)) {
                updated.add(item);
                continue;
            }
            Map<String, Object> participant = new LinkedHashMap<>((Map<String, Object>) value);
            participant.put(ATTENDANCE_TYPE, toDynamicList(participant.get(ATTENDANCE_TYPE), subChannels));
            participant.put(REPRESENTED_PARTY, toDynamicList(participant.get(REPRESENTED_PARTY), roles));
            DynamicList role = toDynamicList(participant.get(ROLE), roles);
            participant.put(ROLE, role);
            if ("Yes".equals(participant.get(PREFILLED)) && !role.getValue().getCode().isEmpty()) {
                // read-only role shown on pre-filled rows uses the ref data wording
                participant.put(ROLE_LABEL, role.getValue().getLabel());
            }
            Map<String, Object> row = new LinkedHashMap<>((Map<String, Object>) original);
            row.put(VALUE, participant);
            updated.add(row);
        }
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, updated);
    }

    private List<Value> listItems(String categoryId, boolean activeOnly) {
        CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(categoryId, IS_CHILD_REQUIRED);
        List<CategoryValues> categoryValues = activeOnly
            ? refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, categoryId)
            : refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, categoryId);
        return refDataUserService.mapCategoryValuesToDynamicListValues(categoryValues);
    }

    private static DynamicList toDynamicList(Object current, List<Value> listItems) {
        String selectedCode = selectedCode(current).orElse("");
        Value selected = listItems.stream()
            .filter(item -> selectedCode.equals(item.getCode()))
            .findFirst()
            .orElse(new Value("", ""));
        return new DynamicList(selected, listItems);
    }

    private static Optional<String> selectedCode(Object current) {
        // HMC's value from ia-hearings-api, or the caseworker's choice when they come back to the times page
        if (current instanceof DynamicList list) {
            return Optional.ofNullable(list.getValue()).map(Value::getCode).filter(code -> !code.isBlank());
        }
        if (current instanceof Map<?, ?> list && list.get(VALUE) instanceof Map<?, ?> value) {
            return Optional.ofNullable(value.get("code")).map(Object::toString).filter(code -> !code.isBlank());
        }
        return Optional.empty();
    }
}
