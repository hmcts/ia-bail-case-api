package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.SEND_BAIL_DIRECTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;


@Component
public class SendDirectionHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<Direction> directionAppender;
    private final DateProvider dateProvider;


    public SendDirectionHandler(
        Appender<Direction> directionAppender,
        DateProvider dateProvider
    ) {
        this.directionAppender = directionAppender;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(SEND_BAIL_DIRECTION);

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

        String sendDirectionDescription = bailCase
                .read(SEND_DIRECTION_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionDescription is not present"));

        String sendDirectionList = bailCase
                .read(SEND_DIRECTION_LIST, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionList is not present"));

        String dateOfCompliance = bailCase
                .read(DATE_OF_COMPLIANCE, String.class)
                .orElseThrow(() -> new IllegalStateException("dateOfCompliance is not present"));


        Optional<List<IdValue<Direction>>> maybeExistingDirections =

            bailCase.read(DIRECTIONS);


        final Direction newDirection = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            dateProvider.now().toString()
        );


        List<IdValue<Direction>> allDirections =
            directionAppender.append(newDirection, maybeExistingDirections.orElse(emptyList()));
        bailCase.write(DIRECTIONS, allDirections);


        bailCase.clear(SEND_DIRECTION_DESCRIPTION);
        bailCase.clear(SEND_DIRECTION_LIST);
        bailCase.clear(DATE_OF_COMPLIANCE);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
