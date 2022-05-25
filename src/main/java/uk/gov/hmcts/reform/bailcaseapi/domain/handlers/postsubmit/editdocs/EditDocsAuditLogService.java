package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;

@Component
public class EditDocsAuditLogService {

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private EditDocsAuditService editDocsAuditService;

    public AuditDetails buildAuditDetails(long caseId, BailCase bailCase, BailCase bailCaseBefore) {
        return AuditDetails.builder()
            .caseId(caseId)
            .documentIds(getDeletedDocIds(bailCase, bailCaseBefore))
            .documentNames(getDeletedDocumentNames(bailCase, bailCaseBefore))
            .idamUserId(userDetails.getId())
            .user(getIdamUserName(userDetails))
            .reason(bailCase.read(EDIT_DOCUMENTS_REASON, String.class).orElse(null))
            .dateTime(LocalDateTime.now())
            .build();
    }

    private List<String> getDeletedDocumentNames(BailCase bailCase, BailCase bailCaseBefore) {
        if (bailCaseBefore == null) {
            return Collections.emptyList();
        }
        List<String> docNames = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> docNames.addAll(
            editDocsAuditService.getUpdatedAndDeletedDocNamesForGivenField(bailCase, bailCaseBefore, field)));
        return docNames;
    }

    private String getIdamUserName(UserDetails userDetails) {
        return userDetails.getForename() + " " + userDetails.getSurname();
    }

    private List<String> getDeletedDocIds(BailCase bailCase, BailCase bailCaseBefore) {
        if (bailCaseBefore == null) {
            return Collections.emptyList();
        }
        List<String> docIds = new ArrayList<>();
        getListOfDocumentFields().forEach(field -> docIds.addAll(
            editDocsAuditService.getUpdatedAndDeletedDocIdsForGivenField(bailCase, bailCaseBefore, field)));
        return docIds;
    }

    private List<BailCaseFieldDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            TRIBUNAL_DOCUMENTS_WITH_METADATA,
            HOME_OFFICE_DOCUMENTS_WITH_METADATA,
            APPLICANT_DOCUMENTS_WITH_METADATA);
    }
}
