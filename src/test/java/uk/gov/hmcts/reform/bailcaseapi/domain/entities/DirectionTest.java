package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DirectionTest {

    private final String sendDirectionDescription = "some-description";
    private final String dateOfCompliance = "2022-05-26";
    private final String dateSent = "2022-05-25";
    private final String sendDirectionList = "Applicant";
    private final String dateTimeDirectionCreated = "2022-05-25T10:00:00Z";
    private final String dateTimeDirectionModified = "2022-05-25T15:00:00Z";
    private List<IdValue<PreviousDates>> previousDates = Collections.emptyList();


    private Direction direction;

    @BeforeEach
    public void setUp() {
        direction = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            dateSent,
            dateTimeDirectionCreated,
            dateTimeDirectionModified,
            previousDates
        );
    }

    @Test
    void should_hold_onto_values() {

        assertThat(direction.getSendDirectionDescription()).isEqualTo(sendDirectionDescription);
        assertThat(direction.getDateOfCompliance()).isEqualTo(dateOfCompliance);
        assertThat(direction.getSendDirectionList()).isEqualTo(sendDirectionList);
        assertThat(direction.getDateSent()).isEqualTo(dateSent);
        assertThat(direction.getDateTimeDirectionCreated()).isEqualTo(dateTimeDirectionCreated);
        assertThat(direction.getDateTimeDirectionModified()).isEqualTo(dateTimeDirectionModified);
        assertThat(direction.getPreviousDates()).isEqualTo(previousDates);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new Direction(null, "", "", "", "", "", previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", null, "", "", "", "", previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", null, "", "", "", previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", "", null, "", "", previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", "", "", null, "", previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertDoesNotThrow(() -> new Direction("", "", "", "", "", null, previousDates));

        assertThatThrownBy(() -> new Direction("", "", "", "", "", "", null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_immutable_previous_dates_list() {
        List<IdValue<PreviousDates>> mutableList = new ArrayList<>();
        mutableList.add(new IdValue<>("1", new PreviousDates("2022-05-20", "2022-05-21")));

        Direction directionWithDates = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            dateSent,
            dateTimeDirectionCreated,
            dateTimeDirectionModified,
            mutableList
        );

        assertThatThrownBy(() -> directionWithDates.getPreviousDates().add(
            new IdValue<>("2", new PreviousDates("2022-05-22", "2022-05-23"))))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_not_be_affected_by_modifications_to_original_list() {
        List<IdValue<PreviousDates>> mutableList = new ArrayList<>();
        mutableList.add(new IdValue<>("1", new PreviousDates("2022-05-20", "2022-05-21")));

        Direction directionWithDates = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            dateSent,
            dateTimeDirectionCreated,
            dateTimeDirectionModified,
            mutableList
        );

        mutableList.add(new IdValue<>("2", new PreviousDates("2022-05-22", "2022-05-23")));

        assertThat(directionWithDates.getPreviousDates()).hasSize(1);
    }
}
