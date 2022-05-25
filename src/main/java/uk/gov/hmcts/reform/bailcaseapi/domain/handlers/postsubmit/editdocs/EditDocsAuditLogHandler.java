package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
@Slf4j
public class EditDocsAuditLogHandler implements PostSubmitCallbackHandler<BailCase> {

    @Autowired
    private EditDocsAuditLogService editDocsAuditLogService;

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.EDIT_DOCUMENTS;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        long caseId = callback.getCaseDetails().getId();
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
        BailCase bailCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
        log.info("Edit Document audit logs: {}", editDocsAuditLogService
            .buildAuditDetails(caseId, bailCase, bailCaseBefore));
        return new PostSubmitCallbackResponse();
    }

}
