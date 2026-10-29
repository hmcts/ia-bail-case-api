package uk.gov.hmcts.reform.bailcaseapi.ccd.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddCaseNoteTest {

    private static final LocalDate NOW = LocalDate.of(2026, 10, 27);

    @Mock private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock private DateProvider dateProvider;
    @Mock private UserDetails userDetails;

    private AddCaseNote event;

    @BeforeEach
    void setUp() {
        when(dateProvider.now()).thenReturn(NOW);
        when(userDetails.getForename()).thenReturn("Jane");
        when(userDetails.getSurname()).thenReturn("Doe");
        event = new AddCaseNote(ccdEventAuthorizor, dateProvider, userDetails);
    }

    @Test
    void appends_the_new_note_above_the_existing_ones_and_renumbers() {
        CaseNote existing = CaseNote.builder().caseNoteSubject("Earlier").build();
        CaseData caseData = caseDataWith("Subject", "Description");
        caseData.setCaseNotes(List.of(new ListValue<>("1", existing)));

        AboutToStartOrSubmitResponse<CaseData, State> response = event.aboutToSubmit(details(caseData), null);

        List<ListValue<CaseNote>> caseNotes = response.getData().getCaseNotes();
        assertThat(caseNotes).hasSize(2);
        assertThat(caseNotes.get(0).getId()).isEqualTo("2");
        assertThat(caseNotes.get(0).getValue().getCaseNoteSubject()).isEqualTo("Subject");
        assertThat(caseNotes.get(0).getValue().getCaseNoteDescription()).isEqualTo("Description");
        assertThat(caseNotes.get(0).getValue().getUser()).isEqualTo("Jane Doe");
        assertThat(caseNotes.get(0).getValue().getDateAdded()).isEqualTo(NOW);
        assertThat(caseNotes.get(1).getId()).isEqualTo("1");
        assertThat(caseNotes.get(1).getValue()).isEqualTo(existing);
    }

    @Test
    void adds_the_first_note_when_there_are_none() {
        AboutToStartOrSubmitResponse<CaseData, State> response =
            event.aboutToSubmit(details(caseDataWith("Subject", "Description")), null);

        assertThat(response.getData().getCaseNotes()).hasSize(1);
        assertThat(response.getData().getCaseNotes().get(0).getId()).isEqualTo("1");
    }

    @Test
    void carries_the_uploaded_document_onto_the_note() {
        Document document = Document.builder().filename("note.pdf").build();
        CaseData caseData = caseDataWith("Subject", "Description");
        caseData.setAddCaseNoteDocument(document);

        AboutToStartOrSubmitResponse<CaseData, State> response = event.aboutToSubmit(details(caseData), null);

        assertThat(response.getData().getCaseNotes().get(0).getValue().getCaseNoteDocument())
            .isEqualTo(document);
    }

    @Test
    void clears_the_entry_fields() {
        CaseData caseData = caseDataWith("Subject", "Description");
        caseData.setAddCaseNoteDocument(Document.builder().filename("note.pdf").build());

        CaseData amended = event.aboutToSubmit(details(caseData), null).getData();

        assertThat(amended.getAddCaseNoteSubject()).isNull();
        assertThat(amended.getAddCaseNoteDescription()).isNull();
        assertThat(amended.getAddCaseNoteDocument()).isNull();
    }

    @Test
    void throws_when_the_subject_is_missing() {
        assertThatThrownBy(() -> event.aboutToSubmit(details(caseDataWith(null, "Description")), null))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("addCaseNoteSubject is not present");
    }

    @Test
    void throws_when_the_description_is_missing() {
        assertThatThrownBy(() -> event.aboutToSubmit(details(caseDataWith("Subject", null)), null))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("addCaseNoteDescription is not present");
    }

    @Test
    void refuses_a_caller_without_the_event_role() {
        doThrow(new AccessDeniedException("Event 'addCaseNote' not allowed"))
            .when(ccdEventAuthorizor).throwIfNotAuthorized(Event.ADD_CASE_NOTE);

        assertThatThrownBy(() -> event.aboutToSubmit(details(caseDataWith("Subject", "Description")), null))
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    void confirmation_links_to_the_case_notes_tab() {
        var response = event.submitted(details(caseDataWith("Subject", "Description")), null);

        assertThat(response.getConfirmationHeader()).isEqualTo("# You have added a case note");
        assertThat(response.getConfirmationBody()).contains("/case/IA/Bail/1234#Case%20notes");
    }

    private static CaseData caseDataWith(String subject, String description) {
        CaseData caseData = new CaseData();
        caseData.setAddCaseNoteSubject(subject);
        caseData.setAddCaseNoteDescription(description);
        return caseData;
    }

    private static CaseDetails<CaseData, State> details(CaseData caseData) {
        return CaseDetails.<CaseData, State>builder().id(1234L).data(caseData).build();
    }
}
