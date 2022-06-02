package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_SENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_EXPLANATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_DIRECTION_LIST;

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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeDirectionDueMidEventTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    @Captor
    private ArgumentCaptor<Object> editableDirectionsCaptor;
    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;

    private String direction1 = "Direction 1";
    private String direction2 = "Direction 2";

    private ChangeDirectionDueMidEvent changeDirectionDueMidEvent;

    @BeforeEach
    public void setUp() {
        changeDirectionDueMidEvent =
            new ChangeDirectionDueMidEvent();
    }

    @Test
    void should_perform_mid_event_for_editable_direction_fields() {

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
        when(bailCase.read(BAIL_DIRECTION_LIST, DynamicList.class)).thenReturn(Optional.of(new DynamicList(direction1)));

        changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(bailCase, times(5)).write(bailExtractorCaptor.capture(), editableDirectionsCaptor.capture());

        assertEquals(BAIL_DIRECTION_EDIT_EXPLANATION, bailExtractorCaptor.getAllValues().get(0));
        assertEquals(BAIL_DIRECTION_EDIT_PARTIES, bailExtractorCaptor.getAllValues().get(1));
        assertEquals(BAIL_DIRECTION_EDIT_DATE_DUE, bailExtractorCaptor.getAllValues().get(2));
        assertEquals(BAIL_DIRECTION_EDIT_DATE_SENT, bailExtractorCaptor.getAllValues().get(3));
        assertEquals(BAIL_DIRECTION_LIST, bailExtractorCaptor.getAllValues().get(4));

        assertEquals("explanation-2", editableDirectionsCaptor.getAllValues().get(0));
        assertEquals("Home Office", editableDirectionsCaptor.getAllValues().get(1));
        assertEquals("2020-11-01", editableDirectionsCaptor.getAllValues().get(2));
        assertEquals("2019-11-01", editableDirectionsCaptor.getAllValues().get(3));

        DynamicList exactDirectionList = (DynamicList) editableDirectionsCaptor.getAllValues().get(4);
        assertEquals(direction1, exactDirectionList.getValue().getCode());
        assertEquals(direction1, exactDirectionList.getValue().getLabel());
        assertEquals(2, exactDirectionList.getListItems().size());
        assertEquals(direction2, exactDirectionList.getListItems().get(0).getCode());
        assertEquals(direction2, exactDirectionList.getListItems().get(0).getLabel());
        assertEquals(direction1, exactDirectionList.getListItems().get(1).getCode());
        assertEquals(direction1, exactDirectionList.getListItems().get(1).getLabel());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeDirectionDueMidEvent.canHandle(callbackStage, callback);

                if (event == Event.CHANGE_BAIL_DIRECTION_DUE_DATE
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

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

        assertThatThrownBy(() -> changeDirectionDueMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeDirectionDueMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
