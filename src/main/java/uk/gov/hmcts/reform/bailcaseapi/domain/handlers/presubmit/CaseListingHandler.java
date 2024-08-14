package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtVenue;

@Component
public class CaseListingHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<PreviousListingDetails> previousListingDetailsAppender;
    private final DueDateService dueDateService;
    private final LocationRefDataService locationRefDataService;

    public CaseListingHandler(
        Appender<PreviousListingDetails> previousListingDetailsAppender,
        DueDateService dueDateService,
        LocationRefDataService locationRefDataService
    ) {
        this.dueDateService = dueDateService;
        this.previousListingDetailsAppender = previousListingDetailsAppender;
        this.locationRefDataService = locationRefDataService;
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
        } else {
            CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
            BailCase bailCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
            if (bailCaseBefore != null) {
                ListingEvent prevListingEvent = bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)
                    .orElse(null);
                ListingHearingCentre prevListingLocation = bailCaseBefore.read(LISTING_LOCATION,
                                                                               ListingHearingCentre.class)
                    .orElse(null);
                String prevListingHearingDate = bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)
                    .orElse(null);
                String prevListingHearingDuration = bailCaseBefore.read(LISTING_HEARING_DURATION, String.class)
                    .orElse(null);

                if (prevListingEvent == null || prevListingLocation == null || prevListingHearingDate == null || prevListingHearingDuration == null) {
                    PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);
                    response.addError("Relisting is only available after an initial listing.");
                    return response;
                }

                Optional<List<IdValue<PreviousListingDetails>>> maybeExistingPreviousListingDetails =
                    bailCase.read(PREVIOUS_LISTING_DETAILS);
                final PreviousListingDetails newPreviousListingDetails =
                    new PreviousListingDetails(prevListingEvent,
                                               prevListingLocation,
                                               prevListingHearingDate,
                                               prevListingHearingDuration);
                List<IdValue<PreviousListingDetails>> allPreviousListingDetails =
                    previousListingDetailsAppender.append(newPreviousListingDetails,
                                                          maybeExistingPreviousListingDetails.orElse(emptyList()));

                bailCase.write(PREVIOUS_LISTING_DETAILS, allPreviousListingDetails);
                bailCase.write(HAS_BEEN_RELISTED, YesOrNo.YES);
            }
        }

        updateListingLocValueByUsingRefDataLocValue(bailCase);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void updateListingLocValueByUsingRefDataLocValue(BailCase bailCase) {
        YesOrNo isBailsLocationRefDataEnabled = bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)
            .orElse(NO);

        if (isBailsLocationRefDataEnabled == YES) {
            Value selectedRefDataLocation = bailCase.read(REF_DATA_LISTING_LOCATION, DynamicList.class)
                .map(dynamicList -> dynamicList.getValue()).orElse(null);

            if (selectedRefDataLocation != null) {
                saveRefDataListingLocationDetail(bailCase, selectedRefDataLocation.getCode());

                ListingHearingCentre listingHearingCentre = ListingHearingCentre.getEpimsIdMapping()
                    .get(selectedRefDataLocation.getCode());

                if (listingHearingCentre != null && listingHearingCentre.getValue() != null) {
                    bailCase.write(LISTING_LOCATION, listingHearingCentre);
                }
            }
        }
    }

    /**
     * This method is used for saving the information of reference data listing location.
     * This location information will be used for notification API and document API.
     */
    private void saveRefDataListingLocationDetail(BailCase bailCase, String epimmsId) {
        if (!StringUtils.isEmpty(epimmsId)) {
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtVenuesByEpimmsId(epimmsId);
            if (courtVenue.isPresent()) {
                bailCase.write(REF_DATA_LISTING_LOCATION_DETAIL, courtVenue.get());
            }
        }
    }
}
