package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_EVIDENCE_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_EVIDENCE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UploadBailEvidenceDocumentHandlerTest {

    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private DocumentWithDescription groundsForBailEvidence1;
    @Mock
    private DocumentWithDescription groundsForBailEvidence2;
    @Mock
    private DocumentWithMetadata groundsForBailEvidence1WithMetadata;
    @Mock
    private DocumentWithMetadata groundsForBailEvidence2WithMetadata;
    @Mock
    private List<IdValue<DocumentWithMetadata>> existingEvidenceDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> allEvidenceDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> existingEvidenceDocumentsCaptor;

    private UploadBailEvidenceDocumentHandler uploadBailEvidenceDocumentHandler;

    @BeforeEach
    public void setUp() {
        uploadBailEvidenceDocumentHandler =
            new UploadBailEvidenceDocumentHandler(
                documentReceiver,
                documentsAppender
            );
    }

    @Test
    void should_append_new_evidence_to_existing_respondent_documents_for_the_case() {

        List<IdValue<DocumentWithDescription>> evidenceWithDescriptionList =
            Arrays.asList(
                new IdValue<>("1", groundsForBailEvidence1),
                new IdValue<>("2", groundsForBailEvidence2)
            );

        List<DocumentWithMetadata> evidenceList =
            Arrays.asList(
                groundsForBailEvidence1WithMetadata,
                groundsForBailEvidence2WithMetadata
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BAIL_EVIDENCE_WITH_METADATA)).thenReturn(Optional.of(existingEvidenceDocuments));
        when(bailCase.read(BAIL_EVIDENCE)).thenReturn(Optional.of(evidenceWithDescriptionList));

        when(documentReceiver.tryReceive(groundsForBailEvidence1, DocumentTag.BAIL_EVIDENCE))
            .thenReturn(Optional.of(groundsForBailEvidence1WithMetadata));

        when(documentReceiver.tryReceive(groundsForBailEvidence2, DocumentTag.BAIL_EVIDENCE))
            .thenReturn(Optional.of(groundsForBailEvidence2WithMetadata));

        when(documentsAppender.append(existingEvidenceDocuments, evidenceList))
            .thenReturn(allEvidenceDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadBailEvidenceDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(BAIL_EVIDENCE);

        verify(documentReceiver, times(1)).tryReceive(groundsForBailEvidence1, DocumentTag.BAIL_EVIDENCE);
        verify(documentReceiver, times(1)).tryReceive(groundsForBailEvidence2, DocumentTag.BAIL_EVIDENCE);

        verify(documentsAppender, times(1)).append(existingEvidenceDocuments, evidenceList);

        verify(bailCase, times(1)).write(BAIL_EVIDENCE_WITH_METADATA, allEvidenceDocuments);
        verify(bailCase, times(1)).clear(BAIL_EVIDENCE);
    }

    @Test
    void should_add_new_evidence_to_the_case_when_no_evidence_documents_exist() {

        List<IdValue<DocumentWithDescription>> evidenceWithDescriptionList =
            singletonList(new IdValue<>("1", groundsForBailEvidence1));
        List<DocumentWithMetadata> evidenceList = singletonList(groundsForBailEvidence1WithMetadata);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BAIL_EVIDENCE_WITH_METADATA)).thenReturn(Optional.empty());
        when(bailCase.read(BAIL_EVIDENCE)).thenReturn(Optional.of(evidenceWithDescriptionList));

        when(documentReceiver.tryReceive(groundsForBailEvidence1, DocumentTag.BAIL_EVIDENCE))
            .thenReturn(Optional.of(groundsForBailEvidence1WithMetadata));

        when(documentsAppender.append(any(List.class), eq(evidenceList)))
            .thenReturn(allEvidenceDocuments);

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            uploadBailEvidenceDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).read(BAIL_EVIDENCE);

        verify(documentReceiver, times(1)).tryReceive(groundsForBailEvidence1, DocumentTag.BAIL_EVIDENCE);

        verify(documentsAppender, times(1))
            .append(existingEvidenceDocumentsCaptor.capture(), eq(evidenceList));

        List<IdValue<DocumentWithMetadata>> actualexistingEvidenceDocuments =
            existingEvidenceDocumentsCaptor
                .getAllValues()
                .get(0);

        assertEquals(0, actualexistingEvidenceDocuments.size());

        verify(bailCase, times(1)).write(BAIL_EVIDENCE_WITH_METADATA, allEvidenceDocuments);
        verify(bailCase, times(1)).clear(BAIL_EVIDENCE);
    }

    @Test
    void should_throw_when_new_evidence_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(bailCase.read(BAIL_EVIDENCE)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> uploadBailEvidenceDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("bailEvidence is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> uploadBailEvidenceDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> uploadBailEvidenceDocumentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> uploadBailEvidenceDocumentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadBailEvidenceDocumentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> uploadBailEvidenceDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
