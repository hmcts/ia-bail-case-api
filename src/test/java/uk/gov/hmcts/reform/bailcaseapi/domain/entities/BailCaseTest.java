package uk.gov.hmcts.reform.bailcaseapi.domain.entities;


import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class BailCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    //Need more tests as we add more fields
    @Test
    void reads_string() throws IOException {

        String caseData = "{\"appellantGivenNames\":\"John Smith\"}";

        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);
        Optional<String> readApplicantName = bailCase.read(BailCaseFieldDefinition.APPELLANT_GIVEN_NAMES);

        assertThat(readApplicantName.get()).isEqualTo("John Smith");
    }
}
