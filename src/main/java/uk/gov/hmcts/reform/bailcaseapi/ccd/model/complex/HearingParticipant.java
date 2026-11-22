package uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

/**
 * An attendee of a hearing, recorded on the record the decision event and sent to HMC as an actual day party.
 *
 * <p>Pre-filled rows show the read-only label copies (set by the backend only on those rows) and hide the
 * editable fields, which keep their values for the submit; rows added by the user show the editable fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ComplexType(name = "hearingParticipant", generate = true)
public class HearingParticipant {
    @CCD(label = "First name")
    private String firstNameLabel;

    @CCD(label = "First name", showCondition = "prefilled!=\"Yes\"", retainHiddenValue = true)
    private String firstName;

    @CCD(label = "Last name")
    private String lastNameLabel;

    @CCD(label = "Last name", showCondition = "prefilled!=\"Yes\"", retainHiddenValue = true)
    private String lastName;

    @CCD(label = "Role")
    private String roleLabel;

    @CCD(label = "Role", showCondition = "prefilled!=\"Yes\"", typeOverride = FieldType.DynamicList,
        retainHiddenValue = true)
    private String role;

    @CCD(label = "Attendee representing", showCondition = "prefilled!=\"Yes\"",
        typeOverride = FieldType.DynamicList, retainHiddenValue = true)
    private String representedParty;

    @CCD(label = "Attendance type", typeOverride = FieldType.DynamicList)
    private String attendanceType;

    @CCD(label = "Organisation")
    private String organisation;

    @CCD(label = " ", showCondition = "partyId=\"DUMMY_VALUE_TO_HIDE_FIELD\"", retainHiddenValue = true)
    private String partyId;

    @CCD(label = " ", showCondition = "prefilled=\"DUMMY_VALUE_TO_HIDE_FIELD\"", retainHiddenValue = true)
    private YesOrNo prefilled;
}
