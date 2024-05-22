package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_LISTING_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.RELISTING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.PreviousListingDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DueDateService;

@Component
@Slf4j
public class CaseListingHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<PreviousListingDetails> previousListingDetailsAppender;
    private final DueDateService dueDateService;

    public CaseListingHandler(
        Appender<PreviousListingDetails> previousListingDetailsAppender,
        DueDateService dueDateService
    ) {
        this.previousListingDetailsAppender = previousListingDetailsAppender;
        this.dueDateService = dueDateService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.CASE_LISTING;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        log.info("can it handle event?");
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        log.info("it can handle");
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        log.info("bailcase obtained");
        ListingEvent listingEvent = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .orElseThrow(() -> new RequiredFieldMissingException("listingEvent is not present"));
        log.info("listingEvent: " + listingEvent);
        if (listingEvent == INITIAL_LISTING) {
            String hearingDate = bailCase.read(LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("listingHearingDate is not present"));

            LocalDate date = LocalDateTime.parse(hearingDate, ISO_DATE_TIME).toLocalDate();

            String dueDate = dueDateService.calculateHearingDirectionDueDate(date.atStartOfDay(ZoneOffset.UTC),
                                                                             LocalDate.now())
                .toLocalDate()
                .toString();

            bailCase.write(SEND_DIRECTION_DESCRIPTION,
                           "You must upload the Bail Summary by the date indicated below.\n"
                               + "If the applicant does not have a legal representative, "
                               + "you must also send them a copy of the Bail Summary.\n"
                               + "The Bail Summary must include:\n"
                               + "\n"
                               + "- the date when the current period of immigration detention started\n"
                               + "- any concerns in relation to the factors listed in paragraph 3(2) of Schedule "
                               + "10 to the 2016 Act\n"
                               + "- the bail conditions being sought should bail be granted\n"
                               + "- whether removal directions are in place\n"
                               + "- whether the applicant’s release is subject to licence, and if so the relevant details\n"
                               + "- any other relevant information\n\n"
                               + "Next steps\n"
                               + "Sign in to your account to upload the Bail Summary.\n"
                               + "You must complete this direction by: "
                               + LocalDate.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE)
                               .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
            );

            bailCase.write(SEND_DIRECTION_LIST, "Home Office");
            bailCase.write(DATE_OF_COMPLIANCE, dueDate);
            bailCase.write(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.YES);
        } else if (listingEvent == RELISTING) {
            log.info("Attempting to relist case");
            CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
            BailCase bailCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
            log.info("bailCaseBefore: " + bailCaseBefore);
            if (bailCaseBefore != null) {
                log.info("bailCaseBefore is not null");
                ListingEvent prevListingEvent = bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)
                    .orElse(null);
                if (prevListingEvent != null) {
                    log.info("prevListingEvent:" + prevListingEvent);
                }
                ListingHearingCentre prevListingLocation = bailCaseBefore.read(LISTING_LOCATION,
                                                                               ListingHearingCentre.class)
                    .orElse(null);
                if (prevListingLocation != null) {
                    log.info("prevListingLocation:" + prevListingLocation);
                }
                String prevListingHearingDate = bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)
                    .orElse(null);
                if (prevListingHearingDate != null) {
                    log.info("prevListingHearingDate:" + prevListingHearingDate);
                }
                String prevListingHearingDuration = bailCaseBefore.read(LISTING_HEARING_DURATION, String.class)
                    .orElse(null);
                if (prevListingHearingDuration != null) {
                    log.info("prevListingHearingDuration:" + prevListingHearingDuration);
                }

                if (prevListingEvent == null || prevListingLocation == null || prevListingHearingDate == null || prevListingHearingDuration == null) {
                    PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);
                    response.addError("Relisting is only available after an initial listing.");
                    return response;
                }

                Optional<List<IdValue<PreviousListingDetails>>> maybeExistingPreviousListingDetails =
                    bailCase.read(PREVIOUS_LISTING_DETAILS);
                maybeExistingPreviousListingDetails.ifPresent(idValues -> log.info("maybeExistingPreviousListingDetails: " + idValues));
                if (maybeExistingPreviousListingDetails.isEmpty()) {
                    log.info("maybeExistingPreviousListingDetails is empty");
                }
                final PreviousListingDetails newPreviousListingDetails =
                    new PreviousListingDetails(prevListingEvent,
                                               prevListingLocation,
                                               prevListingHearingDate,
                                               prevListingHearingDuration);
                log.info("newPreviousListingDetails: " + newPreviousListingDetails);
                log.info(newPreviousListingDetails.getListingEvent().toString());
                log.info(newPreviousListingDetails.getListingLocation().toString());
                log.info(newPreviousListingDetails.getListingHearingDate());
                log.info(newPreviousListingDetails.getListingHearingDuration());
                List<IdValue<PreviousListingDetails>> allPreviousListingDetails =
                    previousListingDetailsAppender.append(newPreviousListingDetails,
                                                          maybeExistingPreviousListingDetails.orElse(emptyList()));
                log.info("allPreviousListingDetails" + allPreviousListingDetails);
                for (IdValue<PreviousListingDetails> element : allPreviousListingDetails) {
                    log.info("id: " + element.getId());
                    log.info("value: " + element.getValue());
                }

                bailCase.write(PREVIOUS_LISTING_DETAILS, allPreviousListingDetails);

            }
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
