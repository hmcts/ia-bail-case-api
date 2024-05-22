package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;


import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_LISTING_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.RELISTING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.PreviousListingDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DueDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CaseListingHandlerTest {

    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private CaseDetails<BailCase> caseDetailsBefore;
    @Mock private BailCase bailCase;
    @Mock private BailCase bailCaseBefore;
    @Mock private DueDateService dueDateService;
    @Mock private Appender<PreviousListingDetails> previousListingDetailsAppender;

    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;
    @Captor
    private ArgumentCaptor<String> bailValueCaptor;

    private CaseListingHandler caseListingHandler;
    private final String caseListHearingDate = "2023-12-01T12:00:00";

    @BeforeEach
    public void setUp() {
        caseListingHandler = new CaseListingHandler(previousListingDetailsAppender, dueDateService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(caseListHearingDate));
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(INITIAL_LISTING));
    }

    @Test
    void should_set_case_listing_data() {
        final ZonedDateTime hearingLocalDate =
            LocalDateTime.parse(caseListHearingDate, ISO_DATE_TIME).toLocalDate().atStartOfDay(ZoneOffset.UTC);
        String dueDate = "2023-11-30";
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(dueDate).atStartOfDay(ZoneOffset.UTC);

        when(dueDateService.calculateHearingDirectionDueDate(hearingLocalDate,
                                                             LocalDate.now()
        )).thenReturn(zonedDueDateTime);

        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCase, times(4)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        List<BailCaseFieldDefinition> extractors = bailExtractorCaptor.getAllValues();
        List<String> bailCaseValues = bailValueCaptor.getAllValues();

        verify(bailCase, times(1)).write(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.YES);
        assertThat(bailCaseValues.get(extractors.indexOf(SEND_DIRECTION_DESCRIPTION)))
            .containsSequence("You must upload the Bail Summary by the date indicated below.");
        verify(bailCase, times(1)).write(SEND_DIRECTION_LIST, "Home Office");
        verify(bailCase, times(1)).write(DATE_OF_COMPLIANCE,
                                         zonedDueDateTime.toLocalDate().toString());
    }

    @Test
    void should_not_set_case_listing_data() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(RELISTING));

        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCase, times(0)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        verify(bailCase, times(0)).write(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.YES);
        verify(bailCase, times(0)).write(SEND_DIRECTION_LIST, "Home Office");
    }

    @Test
    void should_throw_for_empty_list_case_event() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("listingEvent is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = caseListingHandler.canHandle(callbackStage, callback);

                assertThat(canHandle).isEqualTo(
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && event.equals(Event.CASE_LISTING)
                );
            }

            reset(callback);
        }
    }

    @Test
    void should_throw_for_empty_list_case_hearing_date() {
        when(bailCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("listingHearingDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseListingHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_handle_relisting_without_previous_hearing_details_list() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.INITIAL_LISTING));
        when(bailCaseBefore.read(LISTING_LOCATION, ListingHearingCentre.class)).thenReturn(Optional.of(ListingHearingCentre.BIRMINGHAM));
        when(bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(caseListHearingDate));
        when(bailCaseBefore.read(LISTING_HEARING_DURATION, String.class)).thenReturn(Optional.of("60"));
        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCaseBefore, times(1)).read(LISTING_EVENT, ListingEvent.class);
        verify(bailCaseBefore, times(1)).read(LISTING_LOCATION, ListingHearingCentre.class);
        verify(bailCaseBefore, times(1)).read(LIST_CASE_HEARING_DATE, String.class);
        verify(bailCaseBefore, times(1)).read(LISTING_HEARING_DURATION, String.class);
        verify(bailCase, times(1)).read(PREVIOUS_LISTING_DETAILS);
        final PreviousListingDetails newPreviousListingDetails =
            new PreviousListingDetails(ListingEvent.INITIAL_LISTING,
                                       ListingHearingCentre.BIRMINGHAM,
                                       caseListHearingDate,
                                       "60");
        verify(previousListingDetailsAppender, times(1)).append(newPreviousListingDetails, emptyList());
        verify(bailCase, times(1)).write(eq(PREVIOUS_LISTING_DETAILS), any(List.class));
    }

    @Test
    void should_handle_relisting_with_previous_hearing_details_list() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(bailCaseBefore.read(LISTING_LOCATION, ListingHearingCentre.class))
            .thenReturn(Optional.of(ListingHearingCentre.BELFAST));
        when(bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(caseListHearingDate));
        when(bailCaseBefore.read(LISTING_HEARING_DURATION, String.class)).thenReturn(Optional.of("100"));
        List<PreviousListingDetails> storedPrevListingDetails =
            List.of(new PreviousListingDetails(ListingEvent.INITIAL_LISTING,
                                               ListingHearingCentre.BIRMINGHAM,
                                               caseListHearingDate,
                                               "60"));
        List<IdValue<PreviousListingDetails>> idValueStoredPrevListingDetails = new ArrayList<>();
        idValueStoredPrevListingDetails.add(new IdValue<>("1", storedPrevListingDetails.get(0)));
        when(bailCase.read(PREVIOUS_LISTING_DETAILS)).thenReturn(Optional.of(idValueStoredPrevListingDetails));

        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCaseBefore, times(1)).read(LISTING_EVENT, ListingEvent.class);
        verify(bailCaseBefore, times(1)).read(LISTING_LOCATION, ListingHearingCentre.class);
        verify(bailCaseBefore, times(1)).read(LIST_CASE_HEARING_DATE, String.class);
        verify(bailCaseBefore, times(1)).read(LISTING_HEARING_DURATION, String.class);
        verify(bailCase, times(1)).read(PREVIOUS_LISTING_DETAILS);
        final PreviousListingDetails newPreviousListingDetails =
            new PreviousListingDetails(ListingEvent.RELISTING,
                                       ListingHearingCentre.BELFAST,
                                       caseListHearingDate,
                                       "100");
        verify(previousListingDetailsAppender, times(1)).append(newPreviousListingDetails,
                                                                idValueStoredPrevListingDetails);
        verify(bailCase, times(1)).write(eq(PREVIOUS_LISTING_DETAILS), any(List.class));
    }

    @Test
    void should_throw_exception_when_initial_listing_event_is_missing() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.empty());

        assertThrows(RequiredFieldMissingException.class, () -> {
            caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        });
    }

    @Test
    void should_throw_exception_when_initial_listing_location_is_missing() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.INITIAL_LISTING));
        when(bailCaseBefore.read(LISTING_LOCATION, ListingHearingCentre.class)).thenReturn(Optional.empty());

        assertThrows(RequiredFieldMissingException.class, () -> {
            caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        });
    }

    @Test
    void should_throw_exception_when_initial_listing_hearing_date_is_missing() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.INITIAL_LISTING));
        when(bailCaseBefore.read(LISTING_LOCATION, ListingHearingCentre.class)).thenReturn(Optional.of(ListingHearingCentre.BIRMINGHAM));
        when(bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThrows(RequiredFieldMissingException.class, () -> {
            caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        });
    }

    @Test
    void should_throw_exception_when_initial_listing_hearing_duration_is_missing() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.RELISTING));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(bailCaseBefore);
        when(bailCaseBefore.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(ListingEvent.INITIAL_LISTING));
        when(bailCaseBefore.read(LISTING_LOCATION, ListingHearingCentre.class)).thenReturn(Optional.of(ListingHearingCentre.BIRMINGHAM));
        when(bailCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(caseListHearingDate));
        when(bailCaseBefore.read(LISTING_HEARING_DURATION, String.class)).thenReturn(Optional.empty());

        assertThrows(RequiredFieldMissingException.class, () -> {
            caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        });
    }
}
