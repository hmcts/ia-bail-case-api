package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.RELISTING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.PreviousListingDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DueDateService;

@Component
public class CaseListingHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DueDateService dueDateService;

    public CaseListingHandler(DueDateService dueDateService) {
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

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        ListingEvent listingEvent = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .orElseThrow(() -> new RequiredFieldMissingException("listingEvent is not present"));

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
        }
        if (listingEvent == RELISTING) {
            CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
            BailCase bailCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
            if (bailCaseBefore != null) {
                ListingEvent prevListingEvent = bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)
                    .orElseThrow(() -> new RequiredFieldMissingException(
                        "initial listingEvent must be present for relisting"));
                ListingHearingCentre prevListingLocation = bailCaseBefore.read(LISTING_LOCATION, ListingHearingCentre.class)
                    .orElseThrow(() -> new RequiredFieldMissingException(
                        "initial listingLocation must be present for relisting"));
                String prevListingHearingDate = bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)
                    .orElseThrow(() -> new RequiredFieldMissingException(
                        "initial listingHearingDate must be present for relisting"));
                String prevListingHearingDuration = bailCaseBefore.read(LISTING_HEARING_DURATION, String.class)
                    .orElseThrow(() -> new RequiredFieldMissingException(
                        "initial listingHearingDuration must be present for relisting"));
                Optional<List<IdValue<PreviousListingDetails>>> optionalPreviousListingDetailsList = bailCaseBefore.read(PREVIOUS_LISTING_DETAILS);
                ArrayList<IdValue<PreviousListingDetails>> previousListingDetailsList = new ArrayList<>(optionalPreviousListingDetailsList.orElse(Collections.emptyList()));
                String id = UUID.randomUUID().toString();
                IdValue<PreviousListingDetails> prevListingDetails = new IdValue<>(id, new PreviousListingDetails(prevListingEvent, prevListingLocation, prevListingHearingDate, prevListingHearingDuration));
                previousListingDetailsList.add(prevListingDetails);
                bailCase.write(PREVIOUS_LISTING_DETAILS, previousListingDetailsList);
            }
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
