package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RELEASE_STATUS_YES_OR_NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SECRETARY_OF_STATE_YES_OR_NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SS_CONSENT_DECISION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class DateValidationHandler implements PreSubmitCallbackHandler<BailCase> {

    private final static Set<Event> eventsToHandle = Set.of(Event.START_APPLICATION,
                                               Event.EDIT_BAIL_APPLICATION,
                                               Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
                                               Event.MAKE_NEW_APPLICATION);

    private final static Set<BailCaseFieldDefinition> fieldsToHandle = Set.of(APPLICANT_ARRIVAL_IN_UK,
                                                 BAIL_HEARING_DATE,
                                                 SUPPORTER_DOB,
                                                 SUPPORTER_2_DOB,
                                                 SUPPORTER_3_DOB,
                                                 SUPPORTER_4_DOB);

    private static final Map<String, BailCaseFieldDefinition> pageIdsToHandle = fieldsToHandle.stream()
        .collect(Collectors.toMap(BailCaseFieldDefinition::value, def -> def));

    private final static String FUTURE_DATE_ERROR_MESSAGE = "The date must not be a future date.";

    private final DateProvider dateProvider;

    public DateValidationHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && eventsToHandle.contains(callback.getEvent())
               && pageIdsToHandle.containsKey(callback.getPageId());
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        LocalDate now = dateProvider.now();

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        String pageId = callback.getPageId();
        BailCaseFieldDefinition bailCaseFieldDefinition = pageIdsToHandle.get(pageId);

        String date = bailCase.read(bailCaseFieldDefinition, String.class).orElse("");

        Optional<LocalDate> optionalDateToBeVerified = parseDate(date, bailCaseFieldDefinition);

        optionalDateToBeVerified.ifPresent(dateToBeVerified -> {
            if (dateToBeVerified.isAfter(now)) {
                response.addError(FUTURE_DATE_ERROR_MESSAGE);
            }
        });

        return response;
    }

    private boolean isOptional(BailCaseFieldDefinition bailCaseFieldDefinition) {
        Set<BailCaseFieldDefinition> optionalDefinitions = Set.of(APPLICANT_ARRIVAL_IN_UK);
        return optionalDefinitions.contains(bailCaseFieldDefinition);
    }

    private Optional<LocalDate> parseDate(String date, BailCaseFieldDefinition bailCaseFieldDefinition) {
        try {
            return Optional.of(LocalDate.parse(date));
        } catch (DateTimeParseException e) {
            log.error("Date [{}] for field [{}] can't be parsed", date, bailCaseFieldDefinition);
        }
        return Optional.empty();
    }

}
