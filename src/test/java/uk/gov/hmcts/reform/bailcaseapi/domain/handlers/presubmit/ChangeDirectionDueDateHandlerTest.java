package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.EDITABLE_DIRECTIONS;

import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.EditableDirection;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeDirectionDueDateHandlerTest {

    @Mock
    private DateProvider dateProvider;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    @Captor
    private ArgumentCaptor<List<IdValue<Direction>>> bailValueCaptor;
    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;
    @Captor
    private ArgumentCaptor<List<IdValue<Parties>>> directionEditPartiesCaptor;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "09/01/2020";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "10/01/2020";
    private String applicationStatus = "In progress";


    private String direction1 = "Direction 1";
    private LocalDate dateSent = LocalDate.now();

    private ChangeDirectionDueDateHandler changeDirectionDueDateHandler;

    @BeforeEach
    public void setUp() {
        when(dateProvider.now()).thenReturn(dateSent);

        changeDirectionDueDateHandler =
            new ChangeDirectionDueDateHandler(dateProvider);
    }

    @Test
    void should_copy_due_date_back_into_main_direction_fields_ignoring_other_changes() {

        List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Applicant",
                    "2020-12-01",
                    "2019-12-01",
                    "",
                    "",
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    "Home Office",
                    "2020-11-01",
                    "2019-11-01",
                    "",
                    "",
                    Collections.emptyList()
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        when(bailCase.read(BAIL_DIRECTION_LIST,DynamicList.class)).thenReturn(Optional.of(new DynamicList(direction1)));
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of("2222-12-01"));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        //problem is here
        verify(bailCase, times(2)).write(bailExtractorCaptor.capture(), bailValueCaptor.capture());

        verify(bailCase).clear(BAIL_DIRECTION_LIST);
        verify(bailCase).write(eq(BAIL_DIRECTION_EDIT_PARTIES), directionEditPartiesCaptor.capture());

        List<List<IdValue<Direction>>> bailCaseValues = bailValueCaptor.getAllValues();
        List<BailCaseFieldDefinition> bailCaseFieldDefinitions = bailExtractorCaptor.getAllValues();
        List<IdValue<Direction>> actualDirections = bailCaseValues.get(bailCaseFieldDefinitions.indexOf(DIRECTIONS));
        assertEquals(existingDirections.size(), actualDirections.size());

        assertEquals("1", actualDirections.get(0).getId());
        assertEquals("explanation-1", actualDirections.get(0).getValue().getSendDirectionDescription());
        assertEquals("Applicant", actualDirections.get(0).getValue().getSendDirectionList());
        assertEquals("2020-12-01", actualDirections.get(0).getValue().getDateOfCompliance());
        assertEquals("2019-12-01", actualDirections.get(0).getValue().getDateSent());

        // "Direction 1" in UI is equivalent of Direction with IdValue "2" in backend
        assertEquals("2", actualDirections.get(1).getId());
        assertEquals("explanation-2", actualDirections.get(1).getValue().getSendDirectionDescription());
        assertEquals("Home Office", actualDirections.get(1).getValue().getSendDirectionList());
        assertEquals("2222-12-01", actualDirections.get(1).getValue().getDateOfCompliance());
        assertEquals(dateSent.toString(), actualDirections.get(1).getValue().getDateSent());

        assertEquals(1, actualDirections.get(1).getValue().getPreviousDates().size());
        assertEquals("1", actualDirections.get(1).getValue().getPreviousDates().get(0).getId());
        assertEquals("2020-11-01",
            actualDirections.get(1).getValue().getPreviousDates().get(0).getValue().getDateDue());
        assertEquals("2019-11-01",
            actualDirections.get(1).getValue().getPreviousDates().get(0).getValue().getDateSent());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_BAIL_DIRECTION);
        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueDateHandler.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeDirectionDueDateHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    // remove when new CCD definitions are in Prod
    @Test
    void should_copy_due_date_back_into_main_direction_fields_ignoring_other_changes_deprecated_path() {

        final List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Applicant",
                    "2020-12-01",
                    "2019-12-01",
                    "",
                    "",
                    Collections.emptyList()
                ))
            );

        final List<IdValue<EditableDirection>> editableDirections =
            Arrays.asList(
                new IdValue<>("1", new EditableDirection(
                    "some-other-explanation-1-that-should-be-ignored",
                    "Legal representative",
                    "2222-12-01"
                ))
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(bailCase.read(EDITABLE_DIRECTIONS)).thenReturn(Optional.of(editableDirections));
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of("2222-12-01"));


        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeDirectionDueDateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase, times(1)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        List<BailCaseFieldDefinition> bailCaseFieldDefinitions = bailExtractorCaptor.getAllValues();
        List<List<IdValue<Direction>>> bailCaseValues = bailValueCaptor.getAllValues();

        List<IdValue<Direction>> actualDirections =
            bailCaseValues.get(bailCaseFieldDefinitions.indexOf(DIRECTIONS));

        assertEquals(
            existingDirections.size(),
            actualDirections.size()
        );

        assertEquals("1", actualDirections.get(0).getId());
        assertEquals("explanation-1", actualDirections.get(0).getValue().getSendDirectionDescription());
        assertEquals("Applicant", actualDirections.get(0).getValue().getSendDirectionList());
        assertEquals("2222-12-01", actualDirections.get(0).getValue().getDateOfCompliance());
        assertEquals("2019-12-01", actualDirections.get(0).getValue().getDateSent());

    }
}
