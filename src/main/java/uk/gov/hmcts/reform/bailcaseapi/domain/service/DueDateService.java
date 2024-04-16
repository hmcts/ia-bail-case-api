package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.holidaydates.HolidayService;

@Component
public class DueDateService {

    private final HolidayService holidayService;

    public DueDateService(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    public ZonedDateTime calculateHearingDirectionDueDate(ZonedDateTime delayUntil, LocalDate currentDate) {
        final ZonedDateTime zonedDateTime = minusWorkingDays(delayUntil, 1, currentDate);

        return resetTo4PmTime(zonedDateTime);
    }

    private ZonedDateTime minusWorkingDays(ZonedDateTime dueDate, int numberOfDays, LocalDate currentDate) {
        if (dueDate.toLocalDate().isEqual(currentDate)
            || dueDate.toLocalDate().isBefore(currentDate)) {
            return addWorkingDays(currentDate.atStartOfDay(ZoneOffset.UTC), 1);
        }

        if (numberOfDays == 0) {
            return dueDate;
        }

        ZonedDateTime newDate = dueDate.minusDays(1);
        if (holidayService.isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return minusWorkingDays(newDate, numberOfDays, currentDate);
        } else {
            return minusWorkingDays(newDate, numberOfDays - 1, currentDate);
        }
    }

    private ZonedDateTime addWorkingDays(ZonedDateTime dueDate, int numberOfDays) {
        if (numberOfDays == 0) {
            return dueDate;
        }

        ZonedDateTime newDate = dueDate.plusDays(1);
        if (holidayService.isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return addWorkingDays(newDate, numberOfDays);
        } else {
            return addWorkingDays(newDate, numberOfDays - 1);
        }
    }

    private ZonedDateTime resetTo4PmTime(ZonedDateTime eventDateTime) {
        final LocalTime fourPmTime = LocalTime.of(16, 0, 0, 0);

        return ZonedDateTime.of(eventDateTime.toLocalDate(), fourPmTime, eventDateTime.getZone());
    }
}
