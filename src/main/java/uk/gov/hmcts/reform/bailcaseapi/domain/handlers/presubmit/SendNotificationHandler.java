package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.NotificationSender;


@Component
public class SendNotificationHandler implements PreSubmitCallbackHandler<BailCase> {

    private final NotificationSender<BailCase> notificationSender;

    public SendNotificationHandler(
        NotificationSender<BailCase> notificationSender
    ) {
        this.notificationSender = notificationSender;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && getEventsToHandle().contains(callback.getEvent());
    }

    private List<Event> getEventsToHandle() {
        List<Event> eventsToHandle = Lists.newArrayList(
            Event.SUBMIT_APPLICATION,
            Event.UPLOAD_BAIL_SUMMARY,
            Event.UPLOAD_SIGNED_DECISION_NOTICE,
            Event.END_APPLICATION,
            Event.UPLOAD_DOCUMENTS,
            Event.SEND_BAIL_DIRECTION,
            Event.EDIT_BAIL_DOCUMENTS,
            Event.CHANGE_BAIL_DIRECTION_DUE_DATE,
            Event.MAKE_NEW_APPLICATION,
            Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
            Event.CREATE_BAIL_CASE_LINK,
            Event.MAINTAIN_BAIL_CASE_LINKS,
            Event.RECORD_THE_DECISION
        );
        return eventsToHandle;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCaseWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(bailCaseWithNotificationMarker);
    }
}
