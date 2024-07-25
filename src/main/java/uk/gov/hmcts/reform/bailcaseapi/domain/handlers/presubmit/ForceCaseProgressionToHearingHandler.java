package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.BailCaseUtils;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;

@Component
public class ForceCaseProgressionToHearingHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public ForceCaseProgressionToHearingHandler(Appender<CaseNote> caseNoteAppender,
                                                DateProvider dateProvider,
                                                UserDetails userDetails) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.FORCE_CASE_TO_HEARING;
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

        String caseNoteDescription = bailCase
            .read(REASON_TO_FORCE_CASE_TO_HEARING, String.class)
            .orElseThrow(() -> new IllegalStateException("reasonToForceCaseToHearing is not present"));

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            bailCase.read(CASE_NOTES);

        final CaseNote newCaseNote = new CaseNote(
            "Reason for forcing case progression to hearing",
            caseNoteDescription,
            buildFullName(),
            dateProvider.now().toString()
        );

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        bailCase.write(CASE_NOTES, allCaseNotes);

        bailCase.clear(REASON_TO_FORCE_CASE_TO_HEARING);
        bailCase.clear(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE);

        // If the case is progressed past Bail summary, then default IMA selection is NO
        YesOrNo hoSelectedIma = bailCase.read(HO_SELECT_IMA_STATUS, YesOrNo.class).orElse(YesOrNo.NO);
        bailCase.write(HO_HAS_IMA_STATUS, BailCaseUtils.isImaEnabled(bailCase) ? hoSelectedIma : YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
