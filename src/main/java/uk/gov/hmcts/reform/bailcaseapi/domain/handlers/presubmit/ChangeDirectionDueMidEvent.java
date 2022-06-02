package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueMidEvent implements PreSubmitCallbackHandler<BailCase> {

    private static final String direction = "Direction ";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Optional<List<IdValue<Direction>>> maybeDirections = bailCase.read(DIRECTIONS);

        DynamicList bailDirectionList = bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_LIST, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("bailDirectionList is missing"));

        maybeDirections
            .orElse(emptyList())
            .stream()
            .filter(idValue -> bailDirectionList.getValue().getCode().contains(
                direction + (maybeDirections.orElse(emptyList()).size() - (Integer.parseInt(idValue.getId())) + 1)))
            .forEach(idValue -> {

                bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_EXPLANATION,
                               idValue.getValue().getSendDirectionDescription());
                bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES,
                               idValue.getValue().getSendDirectionList());
                bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE,
                               idValue.getValue().getDateOfCompliance());
                bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_SENT, idValue.getValue().getDateSent());
            });

        List<Value> directionListElements = maybeDirections
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value(direction + idValue.getId(), direction + idValue.getId()))
            .collect(Collectors.toList());

        Collections.reverse(directionListElements);
        DynamicList newDirectionList = new DynamicList(new Value(bailDirectionList.getValue().getCode(),
                                                                 bailDirectionList.getValue().getCode()),
                                                       directionListElements);
        bailCase.write(BAIL_DIRECTION_LIST, newDirectionList);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
