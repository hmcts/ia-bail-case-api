package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit.editdocs;

import static uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService.getIdFromDocUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs.EditDocsAuditService;

@Service
public class EditDocsService {

    @Autowired
    private EditDocsAuditService docsAuditService;

    private List<String> getDeletedApplicantDocIds(BailCase bailCase, BailCase bailCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
            bailCase, bailCaseBefore, BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA);
        Optional<List<IdValue<DocumentWithMetadata>>> optionalApplicantDocuments = bailCase.read(
            BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA);
        List<String> finalApplicantDocIds = new ArrayList<>();
        if (optionalApplicantDocuments.isPresent()) {
            finalApplicantDocIds = getApplicantDocIds(optionalApplicantDocuments.get());
        }
        updatedAndDeletedDocIdsForGivenField.removeAll(finalApplicantDocIds);
        return updatedAndDeletedDocIdsForGivenField;
    }

    private List<String> getApplicantDocIds(
        List<IdValue<DocumentWithMetadata>> finalApplicantDocuments) {
        return finalApplicantDocuments.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
            .collect(Collectors.toList());
    }

    private List<String> getDeletedTribunalDocIds(BailCase bailCase, BailCase bailCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
            bailCase, bailCaseBefore, BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA);
        Optional<List<IdValue<DocumentWithMetadata>>> optionalTribunalDocuments = bailCase.read(
            BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA);
        List<String> finalTribunalDocIds = new ArrayList<>();
        if (optionalTribunalDocuments.isPresent()) {
            finalTribunalDocIds = getTribunalDocIds(optionalTribunalDocuments.get());
        }
        updatedAndDeletedDocIdsForGivenField.removeAll(finalTribunalDocIds);
        return updatedAndDeletedDocIdsForGivenField;
    }

    private List<String> getTribunalDocIds(
        List<IdValue<DocumentWithMetadata>> finalTribunalDocIds) {
        return finalTribunalDocIds.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
            .collect(Collectors.toList());
    }

    private List<String> getDeletedHomeOfficeDocIds(BailCase bailCase, BailCase bailCaseBefore) {
        List<String> updatedAndDeletedDocIdsForGivenField = docsAuditService.getUpdatedAndDeletedDocIdsForGivenField(
            bailCase, bailCaseBefore, BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA);
        Optional<List<IdValue<DocumentWithMetadata>>> optionalHomeOfficeDocuments = bailCase.read(
            BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA);
        List<String> finalHomeOfficeDocIds = new ArrayList<>();
        if (optionalHomeOfficeDocuments.isPresent()) {
            finalHomeOfficeDocIds = getHomeOfficeDocIds(optionalHomeOfficeDocuments.get());
        }
        updatedAndDeletedDocIdsForGivenField.removeAll(finalHomeOfficeDocIds);
        return updatedAndDeletedDocIdsForGivenField;
    }

    private List<String> getHomeOfficeDocIds(
        List<IdValue<DocumentWithMetadata>> finalHomeOfficeDocIds) {
        return finalHomeOfficeDocIds.stream()
            .map(idValue -> getIdFromDocUrl(idValue.getValue().getDocument().getDocumentUrl()))
            .collect(Collectors.toList());
    }

}
