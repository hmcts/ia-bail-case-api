package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Component
public class EditDocsAuditService {
    public List<String> getUpdatedAndDeletedDocIdsForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        List<IdValue<DocumentWithMetadata>> removedDocs = new ArrayList<>(docBefore);
        removedDocs.removeAll(doc);

        return removedDocs.stream()
            .map(d -> getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl()))
            .collect(Collectors.toList());
    }

    public List<String> getUpdatedAndDeletedDocNamesForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                  BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        List<IdValue<DocumentWithMetadata>> removedDocs = docBefore.stream()
            .filter(beforeDoc -> !containsMatchingDocument(doc, beforeDoc))
            .collect(Collectors.toList());

        return removedDocs.stream()
            .map(d -> d.getValue().getDocument().getDocumentFilename())
            .collect(Collectors.toList());
    }

    private boolean containsMatchingDocument(List<IdValue<DocumentWithMetadata>> docs,
                                             IdValue<DocumentWithMetadata> target) {
        return docs.stream().anyMatch(d -> documentsMatch(d, target));
    }

    private boolean documentsMatch(IdValue<DocumentWithMetadata> doc1, IdValue<DocumentWithMetadata> doc2) {
        if (!doc1.getId().equals(doc2.getId())) {
            return false;
        }
        DocumentWithMetadata val1 = doc1.getValue();
        DocumentWithMetadata val2 = doc2.getValue();

        return val1.getDocument().equals(val2.getDocument())
            && Objects.equals(val1.getDescription(), val2.getDescription())
            && val1.getDateUploaded().equals(val2.getDateUploaded())
            && val1.getTag().equals(val2.getTag())
            && normalizeBlankToNull(val1.getSuppliedBy()).equals(normalizeBlankToNull(val2.getSuppliedBy()));
    }

    private String normalizeBlankToNull(String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }

    public List<String> getAddedDocNamesForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                      BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        List<IdValue<DocumentWithMetadata>> addedDocs = removeDocsWithSameId(doc, docBefore);

        List<String> docNames = new ArrayList<>();
        addedDocs.forEach(d -> docNames.add(d.getValue().getDocument().getDocumentFilename()));
        return docNames;
    }

    public List<String> getAddedDocIdsForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                    BailCaseFieldDefinition field) {
        List<IdValue<DocumentWithMetadata>> doc = getDocField(bailCase, field);
        List<IdValue<DocumentWithMetadata>> docBefore = getDocField(bailCaseBefore, field);

        List<IdValue<DocumentWithMetadata>> addedDocs = removeDocsWithSameId(doc, docBefore);

        List<String> docIds = new ArrayList<>();
        addedDocs.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl())));
        return docIds;
    }

    public static String getIdFromDocUrl(String documentUrl) {
        String regexToGetStringFromTheLastForwardSlash = "([^/]+$)";
        Pattern pattern = Pattern.compile(regexToGetStringFromTheLastForwardSlash);
        Matcher matcher = pattern.matcher(documentUrl);
        if (matcher.find()) {
            return matcher.group();
        }
        return documentUrl;
    }

    private List<IdValue<DocumentWithMetadata>> getDocField(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.<List<IdValue<DocumentWithMetadata>>>read(field).orElse(Collections.emptyList());
    }

    private List<IdValue<DocumentWithMetadata>> removeDocsWithSameId(List<IdValue<DocumentWithMetadata>> minuend,
                                                                     List<IdValue<DocumentWithMetadata>> subtrahend) {
        List<String> subtrahendIds = subtrahend.stream()
            .map(IdValue::getId)
            .collect(Collectors.toList());

        return minuend.stream()
            .filter(idValue -> !subtrahendIds.contains(idValue.getId()))
            .collect(Collectors.toList());
    }
}
