package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


class OrganisationEntityResponseTest {

    private OrganisationEntityResponse testOrganisationEntityResponse;

    @BeforeEach
    public void setUp() throws Exception {

        String jsonResult = """
                            {
                                "organisationIdentifier": "0UFUG4Z123",
                                "name": "TestOrg1",
                                "status": "ACTIVE",
                                "sraRegulated": true,
                                "superUser": {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "email": "john.doe@example.com"
                                },
                                "paymentAccount": [
                                  "NUM1",
                                  "NUM2"
                                ],
                            "contactInformation" : []\
                            }\
                            """;

        ObjectMapper mapper = new ObjectMapper();
        testOrganisationEntityResponse = mapper.readValue(jsonResult, OrganisationEntityResponse.class);
    }

    @Test
    void should_successfully_get_organisation_entity_response() {
        assertThat(testOrganisationEntityResponse.getOrganisationIdentifier()).isNotNull();
        OrganisationEntityResponse organisationEntityResponse = testOrganisationEntityResponse;
        assertEquals("0UFUG4Z123", organisationEntityResponse.getOrganisationIdentifier());
        assertEquals("TestOrg1", organisationEntityResponse.getName());
        assertEquals("ACTIVE", organisationEntityResponse.getStatus());
        assertThat(organisationEntityResponse.isSraRegulated()).isTrue();
        assertThat(organisationEntityResponse.getSuperUser()).isNotNull();
        assertEquals("John", organisationEntityResponse.getSuperUser().getFirstName());
        assertEquals("Doe", organisationEntityResponse.getSuperUser().getLastName());
        assertEquals("john.doe@example.com", organisationEntityResponse.getSuperUser().getEmail());
        assertEquals("NUM1", organisationEntityResponse.getPaymentAccount().getFirst());
        assertEquals("NUM2", organisationEntityResponse.getPaymentAccount().get(1));
    }

    @Test
    void should_return_immutable_payment_account_list() {
        assertThatThrownBy(() -> testOrganisationEntityResponse.getPaymentAccount().add("NUM3"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_return_immutable_contact_information_list() {
        assertThatThrownBy(() -> testOrganisationEntityResponse.getContactInformation().add(null))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_return_empty_list_when_payment_account_is_null() {
        OrganisationEntityResponse response = new OrganisationEntityResponse();
        assertThat(response.getPaymentAccount()).isNull();
    }

    @Test
    void should_return_null_when_contact_information_is_null() {
        OrganisationEntityResponse response = new OrganisationEntityResponse();
        assertThat(response.getContactInformation()).isNull();
    }
}
