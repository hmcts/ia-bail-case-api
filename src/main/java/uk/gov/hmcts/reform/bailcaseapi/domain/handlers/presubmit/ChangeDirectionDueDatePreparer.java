package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.EditableDirection;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ChangeDirectionDueDatePreparer implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
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

        List<Value> directionListElements = maybeDirections
            .orElse(Collections.emptyList())
            .stream()
            .map(idValue -> new Value("Direction " + idValue.getId(), "Direction " + idValue.getId()))
            .collect(Collectors.toList());

        if (directionListElements.isEmpty()) {
            PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);
            response.addError("There is no direction to edit");
            return response;
        }

        Collections.reverse(directionListElements);
        DynamicList bailDirectionList = new DynamicList(directionListElements.get(0), directionListElements);
        bailCase.write(BAIL_DIRECTION_LIST, bailDirectionList);

        List<IdValue<EditableDirection>> editableDirections =
            maybeDirections
                .orElse(Collections.emptyList())
                .stream()
                .map(idValue ->
                    new IdValue<>(
                        idValue.getId(),
                        new EditableDirection(
                            idValue.getValue().getSendDirectionDescription(),
                            idValue.getValue().getSendDirectionList(),
                            idValue.getValue().getDateOfCompliance()
                        )
                    )
                )
                .collect(Collectors.toList());
        bailCase.write(DIRECTIONS, editableDirections);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
