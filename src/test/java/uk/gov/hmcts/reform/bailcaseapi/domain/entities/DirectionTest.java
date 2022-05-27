package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DirectionTest {

    private final String sendDirectionDescription = "some-description";
    private final String dateOfCompliance = "2022-05-26";
    private final String dateSent = "2022-05-25";
    private final String sendDirectionList = "Applicant";

    private Direction direction;

    @BeforeEach
    public void setUp() {
        direction = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            dateSent
        );
    }

    @Test
    void should_hold_onto_values() {

        assertThat(direction.getSendDirectionDescription()).isEqualTo(sendDirectionDescription);
        assertThat(direction.getDateOfCompliance()).isEqualTo(dateOfCompliance);
        assertThat(direction.getSendDirectionList()).isEqualTo(sendDirectionList);
        assertThat(direction.getDateSent()).isEqualTo(dateSent);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new Direction(null, "", "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", null, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", null, ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction("", "", "", null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
