package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

@Component
public class ChangeDirectionDueDateHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DateProvider dateProvider;

    public ChangeDirectionDueDateHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CHANGE_BAIL_DIRECTION_DUE_DATE;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final Optional<List<IdValue<Direction>>> maybeDirections = bailCase.read(DIRECTIONS);

        Optional<DynamicList> dynamicList = bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_LIST, DynamicList.class);

        // new path when dynamic list is present
        if (dynamicList.isPresent()) {

            List<IdValue<Direction>> changedDirections =
                maybeDirections.orElse(emptyList())
                    .stream()
                    .map(idValue -> {

                        if (dynamicList.get().getValue().getCode().contains("Direction " + (maybeDirections.orElse(emptyList()).size() - (Integer.parseInt(idValue.getId())) + 1))) {

                            // MidEvent does not pass temp fields
                            bailCase.write(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES, idValue.getValue().getSendDirectionList());

                            return new IdValue<>(
                                idValue.getId(),
                                new Direction(
                                    idValue.getValue().getSendDirectionDescription(),
                                    idValue.getValue().getSendDirectionList(),
                                    bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE, String.class).orElse(""),
                                    dateProvider.now().toString(),
                                    Collections.emptyList()
                                )
                            );
                        } else {
                            return idValue;
                        }
                    })
                    .collect(toList());

            bailCase.clear(BAIL_DIRECTION_LIST);
            bailCase.write(DIRECTIONS, changedDirections);

        } /* compatibility with old CCD definitions (remove on next release) */ else {

            Map<String, Direction> existingDirectionsById =
                maybeDirections
                    .orElseThrow(() -> new IllegalStateException("directions is not present"))
                    .stream()
                    .collect(toMap(
                        IdValue::getId,
                        IdValue::getValue
                    ));

            Optional<List<IdValue<EditableDirection>>> maybeEditableDirections =
                bailCase.read(EDITABLE_DIRECTIONS);

            List<IdValue<Direction>> changedDirections =
                maybeEditableDirections
                    .orElse(emptyList())
                    .stream()
                    .map(idValue -> {

                        Direction existingDirection =
                            existingDirectionsById
                                .get(idValue.getId());

                        if (existingDirection == null) {
                            throw new IllegalStateException("Cannot find original direction to update");
                        }

                        return new IdValue<>(
                            idValue.getId(),
                            new Direction(
                                existingDirection.getSendDirectionDescription(),
                                existingDirection.getSendDirectionList(),
                                bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE, String.class).orElse(""),
                                existingDirection.getDateSent(),
                                existingDirection.getPreviousDates()
                            )
                        );

                    })
                    .collect(toList());

            bailCase.clear(EDITABLE_DIRECTIONS);
            bailCase.write(DIRECTIONS, changedDirections);

        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private List<IdValue<PreviousDates>> appendPreviousDates(List<IdValue<PreviousDates>> previousDates, String dateDue, String dateSent) {

        if (CollectionUtils.isEmpty(previousDates)) {

            return newArrayList(new IdValue<>("1", new PreviousDates(dateDue, dateSent)));
        } else {

            int index = previousDates.size() + 1;

            final List<IdValue<PreviousDates>> allPreviousDates = new ArrayList<>();
            allPreviousDates.add(new IdValue<>(String.valueOf(index--), new PreviousDates(dateDue, dateSent)));

            for (IdValue<PreviousDates> previousDate : previousDates) {
                allPreviousDates.add(new IdValue<>(String.valueOf(index--), previousDate.getValue()));
            }

            return allPreviousDates;
        }
    }

}
