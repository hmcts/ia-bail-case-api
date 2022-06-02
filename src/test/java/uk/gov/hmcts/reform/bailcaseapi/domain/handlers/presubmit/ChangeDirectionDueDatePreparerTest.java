package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.EDITABLE_DIRECTIONS;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeDirectionDueDatePreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase BailCase;

    @Captor
    private ArgumentCaptor<Object> editableDirectionsCaptor;
    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;

    private String direction1 = "Direction 1";
    private String direction2 = "Direction 2";

    private ChangeDirectionDueDatePreparer changeDirectionDueDatePreparer;

    @BeforeEach
    public void setUp() {
        changeDirectionDueDatePreparer =
            new ChangeDirectionDueDatePreparer();
    }

    @Test
    void should_prepare_editable_direction_fields() {

        final List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Legal representative",
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
        when(caseDetails.getCaseData()).thenReturn(BailCase);
        when(BailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(BailCase, callbackResponse.getData());

        verify(BailCase, times(2)).write(bailExtractorCaptor.capture(), editableDirectionsCaptor.capture());

        DynamicList dynamicList = (DynamicList) editableDirectionsCaptor.getAllValues().get(0);

        assertEquals(
            BAIL_DIRECTION_LIST,
            bailExtractorCaptor.getAllValues().get(0)
        );

        assertEquals(direction2, dynamicList.getValue().getCode());
        assertEquals(direction2, dynamicList.getValue().getLabel());

        assertEquals(2, dynamicList.getListItems().size());
        assertEquals(direction2, dynamicList.getListItems().get(0).getCode());
        assertEquals(direction2, dynamicList.getListItems().get(0).getLabel());
        assertEquals(direction1, dynamicList.getListItems().get(1).getCode());
        assertEquals(direction1, dynamicList.getListItems().get(1).getLabel());

        List<IdValue<EditableDirection>> actualEditableDirections =
            (List<IdValue<EditableDirection>>) editableDirectionsCaptor.getAllValues().get(1);

        assertEquals(
            existingDirections.size(),
            actualEditableDirections.size()
        );

        assertEquals(existingDirections.get(0).getId(), actualEditableDirections.get(0).getId());
        assertEquals(existingDirections.get(0).getValue().getSendDirectionDescription(),
            actualEditableDirections.get(0).getValue().getSendDirectionDescription());
        assertEquals(existingDirections.get(0).getValue().getSendDirectionList(),
            actualEditableDirections.get(0).getValue().getSendDirectionList());
        assertEquals(existingDirections.get(0).getValue().getDateOfCompliance(),
            actualEditableDirections.get(0).getValue().getDateOfCompliance());

        assertEquals(existingDirections.get(1).getId(), actualEditableDirections.get(1).getId());
        assertEquals(existingDirections.get(1).getValue().getSendDirectionDescription(),
            actualEditableDirections.get(1).getValue().getSendDirectionDescription());
        assertEquals(existingDirections.get(1).getValue().getSendDirectionList(),
            actualEditableDirections.get(1).getValue().getSendDirectionList());
        assertEquals(existingDirections.get(1).getValue().getDateOfCompliance(),
            actualEditableDirections.get(1).getValue().getDateOfCompliance());
    }


    @Test
    void handling_should_return_error_when_direction_is_empty_list() {

        final List<IdValue<Direction>> existingDirections = emptyList();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CHANGE_BAIL_DIRECTION_DUE_DATE);
        when(caseDetails.getCaseData()).thenReturn(BailCase);
        when(BailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));

        PreSubmitCallbackResponse<BailCase> callbackResponse =
            changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(BailCase, callbackResponse.getData());
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("There is no direction to edit"));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_BAIL_DIRECTION);
        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueDatePreparer.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

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

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueDatePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
