package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DirectionTest {

    private final String sendDirectionDescription = "some-description";
    private final String dateOfCompliance = "2022-05-26";
    private final String dateSent = "2022-05-25";
    private final String sendDirectionList = "Applicant";
    private final String dateTimeDirectionCreated = "2022-05-25T10:00:00Z";
    private final String dateTimeDirectionModified = "2022-05-25T15:00:00Z";

    private Direction direction;

    @BeforeEach
    public void setUp() {
        direction = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            dateSent,
            dateTimeDirectionCreated,
            dateTimeDirectionModified
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
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new Direction(null, "", "", "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", null, "", "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", null, "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", "", null, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", "", "", null, ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertDoesNotThrow(() -> new Direction("", "", "", "", "", null));
    }

}
