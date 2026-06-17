package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.LegRepAddressUk;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class LegalRepOrganisationFormatterTest {

    private LegalRepOrganisationFormatter legalRepOrganisationFormatter;
    private String companyName = "LBC";
    private long ccdCaseId = 12345L;
    private String organisationIdentifier = "ZE2KIWO";
    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String townCity = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";
    private OrganisationPolicy organisationPolicy;

    @Mock ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock OrganisationEntityResponse organisationEntityResponse;

    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;

    @BeforeEach
    public void setUp() throws Exception {

        legalRepOrganisationFormatter = new LegalRepOrganisationFormatter(
            professionalOrganisationRetriever
        );

        organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(organisationIdentifier)
                                  .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();
    }

    @Test
    void should_respond_with_bail_case_with_results() {
        List<LegRepAddressUk> addresses = new ArrayList<>();
        LegRepAddressUk legRepAddressUk = new LegRepAddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country
        );
        addresses.add(legRepAddressUk);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(addresses);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PreSubmitCallbackResponse<BailCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        verify(bailCase, times(1)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);

        AddressUK addressUk = new AddressUK(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country
        );
        assertEquals(addressLine1, addressUk.getAddressLine1().get());
        assertEquals(addressLine2, addressUk.getAddressLine2().get());
        assertEquals(addressLine3, addressUk.getAddressLine3().get());
        assertEquals(postCode, addressUk.getPostCode().get());
        assertEquals(townCity, addressUk.getPostTown().get());
        assertEquals(county, addressUk.getCounty().get());
        assertEquals(country, addressUk.getCountry().get());
    }

    @Test
    void should_return_empty_lr_address_if_does_not_exist() {
        List<LegRepAddressUk> addresses = new ArrayList<>();
        LegRepAddressUk legRepAddressUk = new LegRepAddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country
        );
        addresses.add(legRepAddressUk);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(null);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PreSubmitCallbackResponse<BailCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        verify(bailCase, times(1)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);

        AddressUK addressUk = new AddressUK(
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );
        assertEquals("", addressUk.getAddressLine1().get());
        assertEquals("", addressUk.getAddressLine2().get());
        assertEquals("", addressUk.getAddressLine3().get());
        assertEquals("", addressUk.getPostCode().get());
        assertEquals("", addressUk.getPostTown().get());
        assertEquals("", addressUk.getCounty().get());
        assertEquals("", addressUk.getCountry().get());
    }

    @Test
    void should_not_write_to_local_authority_policy_if_organisation_entity_response_is_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(professionalOrganisationRetriever.retrieve()).thenReturn(null);

        PreSubmitCallbackResponse<BailCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(bailCase, response.getData());

        verify(bailCase, times(0)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepOrganisationFormatter.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepOrganisationFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
