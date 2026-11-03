package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;

/**
 * Chooses the end state of the {@code caseListing} event: {@code bailSummaryUploaded} when an
 * {@code applicationSubmitted} case is given its initial listing, and the state the case was already in
 * otherwise. A CCD definition generated from this repository's Java config can only name one end state
 * per event, so the choice is made here; the state returned by an about-to-submit callback replaces the
 * definition's post state, and returning the current state is what leaves the case where it was.
 */
@Component
public class CaseListingSubmitStateHandler implements PreSubmitCallbackStateHandler<BailCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.CASE_LISTING;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback,
        PreSubmitCallbackResponse<BailCase> callbackResponse
    ) {
        requireNonNull(callbackResponse, "callback must not be null");

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        final BailCase bailCase = caseDetails.getCaseData();

        State state = caseDetails.getState();
        if (state == State.APPLICATION_SUBMITTED && isInitialListing(bailCase)) {
            state = State.BAIL_SUMMARY_UPLOADED;
        }

        return new PreSubmitCallbackResponse<>(bailCase, state);
    }

    private boolean isInitialListing(BailCase bailCase) {
        try {
            return bailCase.read(LISTING_EVENT, ListingEvent.class)
                .map(INITIAL_LISTING::equals)
                .orElse(false);
        } catch (IllegalArgumentException e) {
            // A missing or unusable listing event is reported by CaseListingHandler, not here.
            return false;
        }
    }
}
