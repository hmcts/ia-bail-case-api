package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CHANGE_BAIL_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CHANGE_BAIL_DIRECTION_DUE_DATE_EXPLANATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CHANGE_BAIL_DIRECTION_DUE_DATE_PARTIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CHANGE_BAIL_DIRECTION_DUE_DATE_DATE_SENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NEW_BAIL_DIRECTION_DUE_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NEW_DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.CHANGE_BAIL_DIRECTION_DUE_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.NewDirection;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;


@Component
public class ChangeBailDirectionDueDateHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<NewDirection> newDirectionAppender;
    private final DateProvider dateProvider;


    public ChangeBailDirectionDueDateHandler(
        Appender<NewDirection> newDirectionAppender,
        DateProvider dateProvider
    ) {
        this.newDirectionAppender = newDirectionAppender;
        this.dateProvider = dateProvider;
    }

        public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
        ) {
            requireNonNull(callbackStage, "callbackStage must not be null");
            requireNonNull(callback, "callback must not be null");

            return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(CHANGE_BAIL_DIRECTION_DUE_DATE);
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

            String changeBailDirectionList = bailCase
                .read(CHANGE_BAIL_DIRECTION_LIST, String.class)
                .orElseThrow(() -> new IllegalStateException("changeBailDirectionList is not present"));

            String changeBailDirectionDueDateExplanation = bailCase
                .read(CHANGE_BAIL_DIRECTION_DUE_DATE_EXPLANATION, String.class)
                .orElseThrow(() -> new IllegalStateException("changeBailDirectionDueDateExplanation is not present"));

            String changeBailDirectionDueDateParties = bailCase
                .read(CHANGE_BAIL_DIRECTION_DUE_DATE_PARTIES, String.class)
                .orElseThrow(() -> new IllegalStateException("changeBailDirectionDueDateParties is not present"));

            String changeBailDirectionDueDateDateSent = bailCase
                .read(CHANGE_BAIL_DIRECTION_DUE_DATE_DATE_SENT, String.class)
                .orElseThrow(() -> new IllegalStateException("changeBailDirectionDueDateDateSent is not present"));

            String newBailDirectionDueDate = bailCase
                .read(NEW_BAIL_DIRECTION_DUE_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("newBailDirectionDueDate is not present"));


            Optional<List<IdValue<NewDirection>>> maybeExistingDirections =
                bailCase.read(NEW_DIRECTIONS);


            final NewDirection newDirection = new NewDirection(
                changeBailDirectionList,
                changeBailDirectionDueDateExplanation,
                changeBailDirectionDueDateParties,
                changeBailDirectionDueDateDateSent,
                newBailDirectionDueDate
            );


            List<IdValue<NewDirection>> allDirections =
                newDirectionAppender.append(newDirection, maybeExistingDirections.orElse(emptyList()));
            bailCase.write(NEW_DIRECTIONS, allDirections);

            bailCase.clear(CHANGE_BAIL_DIRECTION_LIST);
            bailCase.clear(CHANGE_BAIL_DIRECTION_DUE_DATE_EXPLANATION);
            bailCase.clear(CHANGE_BAIL_DIRECTION_DUE_DATE_PARTIES);
            bailCase.clear(CHANGE_BAIL_DIRECTION_DUE_DATE_DATE_SENT);
            bailCase.clear(NEW_BAIL_DIRECTION_DUE_DATE);

            return new PreSubmitCallbackResponse<>(bailCase);
        }

    }

