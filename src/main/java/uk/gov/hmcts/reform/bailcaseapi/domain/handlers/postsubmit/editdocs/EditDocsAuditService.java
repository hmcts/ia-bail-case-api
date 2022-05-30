package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit.editdocs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.HasDocument;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Component
public class EditDocsAuditService {
    public List<String> getUpdatedAndDeletedDocIdsForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                BailCaseFieldDefinition field) {
        List<IdValue<HasDocument>> doc = getDocField(bailCase, field);
        List<IdValue<HasDocument>> docBefore = getDocField(bailCaseBefore, field);
        docBefore.removeAll(doc);
        List<String> docIds = new ArrayList<>();
        docBefore.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl())));
        return docIds;
    }

    public List<String> getUpdatedAndDeletedDocNamesForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                  BailCaseFieldDefinition field) {
        List<IdValue<HasDocument>> doc = getDocField(bailCase, field);
        List<IdValue<HasDocument>> docBefore = getDocField(bailCaseBefore, field);
        docBefore.removeAll(doc);
        List<String> docNames = new ArrayList<>();
        docBefore.forEach(d -> docNames.add(d.getValue().getDocument().getDocumentFilename()));
        return docNames;
    }

    public List<String> getAddedDocIdsForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                BailCaseFieldDefinition field) {
        List<IdValue<HasDocument>> doc = getDocField(bailCase, field);
        List<IdValue<HasDocument>> docBefore = getDocField(bailCaseBefore, field);
        doc.removeAll(docBefore);
        List<String> docIds = new ArrayList<>();
        docBefore.forEach(d -> docIds.add(getIdFromDocUrl(d.getValue().getDocument().getDocumentUrl())));
        return docIds;
    }

    public List<String> getAddedDocNamesForGivenField(BailCase bailCase, BailCase bailCaseBefore,
                                                                  BailCaseFieldDefinition field) {
        List<IdValue<HasDocument>> doc = getDocField(bailCase, field);
        List<IdValue<HasDocument>> docBefore = getDocField(bailCaseBefore, field);
        doc.removeAll(docBefore);
        List<String> docNames = new ArrayList<>();
        doc.forEach(d -> docNames.add(d.getValue().getDocument().getDocumentFilename()));
        return docNames;
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

    private List<IdValue<HasDocument>> getDocField(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.<List<IdValue<HasDocument>>>read(field).orElse(Collections.emptyList());
    }
}
