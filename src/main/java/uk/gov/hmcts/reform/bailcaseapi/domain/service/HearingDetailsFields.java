package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_CASE_REFERENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_END;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;

public final class HearingDetailsFields {

    public static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";

    private static final List<BailCaseFieldDefinition> ALL = List.of(
        ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE,
        ATTENDEES_AND_DURATION_HEARING,
        ATTENDEES_AND_DURATION_CASE_REFERENCE,
        ATTENDEES_AND_DURATION_HEARING_ID,
        ATTENDEES_AND_DURATION_HEARING_DATE,
        ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY,
        ATTENDEES_AND_DURATION_PAPER_HEARING,
        ATTENDEES_AND_DURATION_HEARING_METHODS,
        ATTENDEES_AND_DURATION_HEARING_STAGE,
        ATTENDEES_AND_DURATION_HEARING_RESULT,
        ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON,
        ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON,
        ATTENDEES_AND_DURATION_DATE_OF_HEARING,
        ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED,
        ATTENDEES_AND_DURATION_START_TIME,
        ATTENDEES_AND_DURATION_FINISH_TIME,
        ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED,
        ATTENDEES_AND_DURATION_PAUSE_START,
        ATTENDEES_AND_DURATION_PAUSE_END,
        ATTENDEES_AND_DURATION_PARTICIPANTS,
        ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE
    );

    private HearingDetailsFields() {
    }

    public static void clear(BailCase bailCase) {
        ALL.forEach(bailCase::clear);
    }

    public static Optional<String> selectedCode(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.read(field, DynamicList.class)
            .map(DynamicList::getValue)
            .map(Value::getCode)
            .filter(code -> !code.isBlank());
    }
}
