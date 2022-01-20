package uk.gov.hmcts.reform.bailcaseapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValid;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValidCheckers;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PreSubmitCallbackHandler<T>> sortedCallbackHandlers;
    private final List<PreSubmitCallbackStateHandler<T>> callbackStateHandlers;
    private final EventValidCheckers<T> eventValidChecker;

    public PreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<T>> callbackHandlers,
        List<PreSubmitCallbackStateHandler<T>> callbackStateHandlers,
        EventValidCheckers<T> eventValidChecker
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        // sorting handlers by handler class name
        this.sortedCallbackHandlers = callbackHandlers.stream()
            .sorted(Comparator.comparing(x -> x.getClass().getSimpleName()))
            .collect(Collectors.toList());
        this.eventValidChecker = eventValidChecker;
        // sorting handlers by handler class name
        this.callbackStateHandlers = callbackStateHandlers.stream()
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
    }

    public PreSubmitCallbackResponse<T> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        ccdEventAuthorizor.throwIfNotAuthorized(callback.getEvent());

        T caseData = callback.getCaseDetails().getCaseData();

        PreSubmitCallbackResponse<T> callbackResponse =
            new PreSubmitCallbackResponse<>(caseData);

        EventValid check = eventValidChecker.check(callback);

        if (check.isValid()) {
            //TODO : Dispatch to handlers
        } else {
            callbackResponse.addError(check.getInvalidReason());
        }
        return callbackResponse;
    }
}
