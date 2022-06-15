package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

@Service
public class MakeNewApplicationService {

    @Autowired
    private Appender<PriorApplication> appender;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private UserDetailsHelper userDetailsHelper;

    private final ObjectMapper mapper;

    public MakeNewApplicationService(Appender<PriorApplication> appender,
                                     UserDetails userDetails, UserDetailsHelper userDetailsHelper,
                                     ObjectMapper mapper) {
        this.appender = appender;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.mapper = mapper;
    }

    public void appendPriorApplication(BailCase bailCase, BailCase bailCaseBefore) {

        Optional<List<IdValue<PriorApplication>>> maybeExistingPriorApplication =
            bailCaseBefore.read(BailCaseFieldDefinition.PRIOR_APPLICATIONS);

        String nextAppId = String.valueOf(maybeExistingPriorApplication.orElse(Collections.emptyList()).size() + 1);

        String previousCaseDataJson;

        try {
            previousCaseDataJson = mapper.writeValueAsString(bailCaseBefore);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize data", e);
        }

        List<IdValue<PriorApplication>> allPriorApplications = appender.append(
            buildNewPriorApplication(nextAppId, previousCaseDataJson),
            maybeExistingPriorApplication.orElse(Collections.emptyList()));

        bailCase.write(BailCaseFieldDefinition.PRIOR_APPLICATIONS, allPriorApplications);

    }

    public void clearUnrelatedFields(BailCase bailCase) {
        List<String> fieldDefinitionsToBeRemoved = bailCase.keySet()
            .stream()
            .filter(o -> !VALID_MAKE_NEW_APPLICATION_FIELDS.contains(o))
            .collect(Collectors.toList());

        fieldDefinitionsToBeRemoved.forEach(bailCase::removeByString);

        clearRoleDependentFields(bailCase);
    }

    private void clearRoleDependentFields(BailCase bailCase) {
        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);

        if (userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE)) {
            bailCase.remove(BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS);
        }
        if (userRoleLabel.equals(UserRoleLabel.HOME_OFFICE_GENERIC)) {
            bailCase.remove(BailCaseFieldDefinition.UPLOAD_B1_FORM_DOCS);
        }
    }

    private PriorApplication buildNewPriorApplication(String nextAppId, String bailCaseBefore) {
        return new PriorApplication(
            nextAppId,
            bailCaseBefore
        );
    }

    public static final List<String> VALID_MAKE_NEW_APPLICATION_FIELDS = List.of(
        BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.PRIOR_APPLICATIONS.value(),
        BailCaseFieldDefinition.IS_ADMIN.value(),
        BailCaseFieldDefinition.APPLICATION_SENT_BY.value(),
        BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES.value(),
        BailCaseFieldDefinition.APPLICANT_FAMILY_NAME.value(),
        BailCaseFieldDefinition.APPLICANT_DOB.value(),
        BailCaseFieldDefinition.APPLICANT_GENDER.value(),
        BailCaseFieldDefinition.APPLICANT_GENDER_OTHER.value(),
        BailCaseFieldDefinition.APPLICANT_NATIONALITY.value(),
        BailCaseFieldDefinition.APPLICANT_NATIONALITIES.value(),
        BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER.value(),
        BailCaseFieldDefinition.APPLICANT_DETENTION_LOCATION.value(),
        BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS.value(),
        BailCaseFieldDefinition.IRC_NAME.value(),
        BailCaseFieldDefinition.PRISON_NAME.value(),
        BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK.value());
}
