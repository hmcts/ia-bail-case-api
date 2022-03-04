package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CompanyNameProviderTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock
    private OrganisationEntityResponse organisationResponse;

    private CompanyNameProvider companyNameProvider;
    private final String organisationIdentifier = "some identifier";
    private final String organisationName = "some company name";

    @BeforeEach
    public void setup() {

        companyNameProvider = new CompanyNameProvider(professionalOrganisationRetriever);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationResponse);
    }

    @Test
    void should_write_to_bail_case_field_for_start_application_event() {

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        when(organisationResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(organisationResponse.getName()).thenReturn(organisationName);

        companyNameProvider.prepareCompanyName(callback);

        verify(bailCase, times(1)).write(LEGAL_REP_COMPANY, organisationName);
    }

    @Test
    void should_not_write_to_bail_case_field_when_organisation_response_is_null() {

        when(professionalOrganisationRetriever.retrieve()).thenReturn(null);

        companyNameProvider.prepareCompanyName(callback);

        verify(bailCase, times(0)).write(LEGAL_REP_COMPANY, organisationName);
    }
}
