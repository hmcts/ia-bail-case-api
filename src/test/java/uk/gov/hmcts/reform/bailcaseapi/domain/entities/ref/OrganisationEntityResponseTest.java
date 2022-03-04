package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class OrganisationEntityResponseTest {

    private OrganisationEntityResponse testOrganisationEntityResponse;

    @BeforeEach
    public void setUp() throws Exception {

        String jsonResult = "{\n"
                            + "    \"organisationIdentifier\": \"0UFUG4Z123\",\n"
                            + "    \"name\": \"TestOrg1\",\n"
                            + "    \"status\": \"ACTIVE\",\n"
                            + "    \"sraRegulated\": true,\n"
                            + "    \"superUser\": {\n"
                            + "      \"firstName\": \"John\",\n"
                            + "      \"lastName\": \"Doe\",\n"
                            + "      \"email\": \"john.doe@example.com\"\n"
                            + "    },\n"
                            + "    \"paymentAccount\": [\n"
                            + "      \"NUM1\",\n"
                            + "      \"NUM2\"\n"
                            + "    ],\n"
                            + "\"contactInformation\" : []"
                            + "}";

        ObjectMapper mapper = new ObjectMapper();
        testOrganisationEntityResponse = mapper.readValue(jsonResult, OrganisationEntityResponse.class);
    }

    @Test
    void should_successfully_get_organisation_entity_response() {
        assertThat(testOrganisationEntityResponse.getOrganisationIdentifier()).isNotNull();
        OrganisationEntityResponse organisationEntityResponse = testOrganisationEntityResponse;
        assertEquals(organisationEntityResponse.getOrganisationIdentifier(), "0UFUG4Z123");
        assertEquals(organisationEntityResponse.getName(), "TestOrg1");
        assertEquals(organisationEntityResponse.getStatus(), "ACTIVE");
        assertThat(organisationEntityResponse.isSraRegulated()).isTrue();
        assertThat(organisationEntityResponse.getSuperUser()).isNotNull();
        assertEquals(organisationEntityResponse.getSuperUser().getFirstName(), "John");
        assertEquals(organisationEntityResponse.getSuperUser().getLastName(), "Doe");
        assertEquals(organisationEntityResponse.getSuperUser().getEmail(), "john.doe@example.com");
        assertEquals(organisationEntityResponse.getPaymentAccount().get(0), "NUM1");
        assertEquals(organisationEntityResponse.getPaymentAccount().get(1), "NUM2");
    }

}