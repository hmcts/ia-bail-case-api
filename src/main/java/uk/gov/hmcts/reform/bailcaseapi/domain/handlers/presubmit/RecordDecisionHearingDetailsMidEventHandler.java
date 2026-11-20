package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingDetailsFields;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

/**
 * Once a hearing is chosen, ia-hearings-api fills in its details from HMC and the reason lists used on the
 * hearing stage page are loaded from reference data.
 */
@Component
@RequiredArgsConstructor
public class RecordDecisionHearingDetailsMidEventHandler implements PreSubmitCallbackHandler<BailCase> {

    static final String ACTUAL_CANCELLATION_REASON_CODES = "ActualCancellationReasonCodes";
    static final String ACTUAL_PART_HEARD_REASON_CODES = "ActualPartHeardReasonCodes";
    private static final String IS_CHILD_REQUIRED = "N";

    private final IaHearingsApiService iaHearingsApiService;
    private final RefDataUserService refDataUserService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.RECORD_THE_DECISION
               && Objects.equals(callback.getPageId(), HearingDetailsFields.SELECT_HEARING_PAGE_ID);
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

        populateReasons(bailCase, ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, ACTUAL_CANCELLATION_REASON_CODES);
        populateReasons(bailCase, ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, ACTUAL_PART_HEARD_REASON_CODES);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void populateReasons(BailCase bailCase, BailCaseFieldDefinition field, String categoryId) {
        CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(categoryId, IS_CHILD_REQUIRED);
        List<CategoryValues> reasons =
            refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, categoryId);
        List<Value> listItems = refDataUserService.mapCategoryValuesToDynamicListValues(reasons);

        // keeps the reason already chosen when the user comes back to the select hearing page
        Value selected = bailCase.read(field, DynamicList.class)
            .map(DynamicList::getValue)
            .orElse(new Value("", ""));

        bailCase.write(field, new DynamicList(selected, listItems));
    }
}
