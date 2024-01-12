package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates.HolidayService;

@ExtendWith(MockitoExtension.class)
class DueDateServiceTest {

    @Mock
    HolidayService holidayService;

    private DueDateService dueDateService;

    @BeforeEach
    void setUp() {
        dueDateService = new DueDateService(holidayService);
    }

    @Test
    void should_return_next_working_day_4_pm_when_calculated_due_date_matches_holiday() {
        ZonedDateTime eventDateTime =
            ZonedDateTime.of(
                2023, 12, 25,
                9, 0, 0, 0,
                ZoneId.systemDefault()
            );

        when(holidayService.isWeekend(eventDateTime.minusDays(1)))
            .thenReturn(true);
        when(holidayService.isWeekend(eventDateTime.minusDays(2)))
            .thenReturn(true);

        ZonedDateTime expectedDueDate = eventDateTime.minusDays(3);
        ZonedDateTime expectedDueDateTime = expectedDueDate.with(
            LocalTime.of(16, 0, 0, 0)
        );
        ZonedDateTime actualDateTime = dueDateService.calculateHearingDirectionDueDate(eventDateTime);

        assertThat(actualDateTime, is(expectedDueDateTime));
        verify(holidayService, times(3)).isWeekend(any(ZonedDateTime.class));
        verify(holidayService, times(1)).isHoliday(any(ZonedDateTime.class));
    }

}

