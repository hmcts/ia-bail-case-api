package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.CompanyNameProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class MakeApplicationSubmitHandler implements PreSubmitCallbackHandler<BailCase> {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private final String ccdUrl;

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    private final CompanyNameProvider companyNameProvider;

    protected CoreCaseDataApi coreCaseDataApi;
    private final RestTemplate restTemplate;

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;

    private static final String CASE_REFERENCE = "REF123";

    private static final Map<String, String> CREATE_CASE_DATA = new HashMap<String, String>() {
        {
            put("applicantGivenNames", "test...");
            put("applicantFamilyName", "test...");
        }
    };

    public MakeApplicationSubmitHandler(UserDetails userDetails, UserDetailsHelper userDetailsHelper,
                                        CompanyNameProvider companyNameProvider,
                                        AuthTokenGenerator serviceAuthTokenGenerator,
                                        CoreCaseDataApi coreCaseDataApi,
                                        AccessTokenProvider accessTokenProvider,
                                        RestTemplate restTemplate,
                                        @Value("${core_case_data.api.url}") String ccdUrl) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.companyNameProvider = companyNameProvider;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
        this.accessTokenProvider = accessTokenProvider;
        this.restTemplate = restTemplate;
        this.ccdUrl = ccdUrl;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MAKE_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        var previousCaseDetails = callback.getCaseDetailsBefore();

        var userAccessToken = userDetails.getAccessToken();
        var accessTokenFromProvider = accessTokenProvider.getAccessToken();
        String s2sToken = serviceAuthTokenGenerator.generate();


//        CaseDetails sss = createCaseForCaseworker(userDetails, callback.getCaseDetails(), callback);

        return new PreSubmitCallbackResponse<>(previousCaseDetails.get().getCaseData());
    }

    private CaseDetails createCaseCall(final Callback<BailCase> callback, CaseDataContent cdc, String userId) {
        requireNonNull(callback, "callback must not be null");

        final long caseId = callback.getCaseDetails().getId();
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final String accessToken = userDetails.getAccessToken();
        final String idamUserId = userDetails.getId();


        HttpEntity<CaseDataContent> requestEntity =
            new HttpEntity<>(
                cdc,
                setHeaders(serviceAuthorizationToken, accessToken)
            );

        ResponseEntity<Object> response = null;
        try {
            response = restTemplate
                .exchange(
                    ccdUrl + "/caseworkers/"+userId+"/jurisdictions/IA/case-types/Bail/cases?ignore-warning=true",
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
                );

        } catch (RestClientResponseException e) {
            e.getMessage();
            e.getStatusText();
        }

        ResponseEntity<Object> ss = response;
        ArrayList ssd1 = (ArrayList) response.getBody();

        LinkedHashMap result = (LinkedHashMap) ssd1.get(0);

        ObjectMapper mapper = new ObjectMapper();
//        CaseDetails ssd = null;
//        try {
//            ssd = mapper.readValue((JsonParser) ssd1.get(0), CaseDetails.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return null;

    }

    private CaseDetails createCaseForCaseworker(UserDetails caseworker,
                                                uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails<BailCase> caseDetails,
                                                Callback<BailCase> callback) {

        var oldCaseId = caseDetails.getId();
        var oldCaseJur = caseDetails.getJurisdiction();
        var oldCaseData = caseDetails.getCaseData();
        var userAccessToken = caseworker.getAccessToken();
        var accessTokenFromProvider = accessTokenProvider.getAccessToken();
        String s2sToken = serviceAuthTokenGenerator.generate();
        var userid = caseworker.getId();

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
            accessTokenFromProvider,
            s2sToken,
            caseworker.getId(),
            "IA",
            "Bail",
            "startApplication"
        );


//        var fn = oldCaseData.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES);
//        var sn = oldCaseData.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME);
//
//        oldCaseData.clear();
//        oldCaseData.write(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, fn);
//        oldCaseData.write(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, sn);

        oldCaseData.remove("decisionUnsignedDocument");
        oldCaseData.remove("applicationSubmissionDocument");
        oldCaseData.remove("uploadBailSummaryDocs");
        oldCaseData.remove("decisionUnsignedDocMetadata");
        oldCaseData.remove("uploadBailSummaryMetadata");

        oldCaseData.entrySet().removeIf(entry -> !validFields.contains(entry.getKey()));

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(startEventResponse.getEventId()).build())
            .data(oldCaseData)
            .build();


//        oldCaseData.write(BailCaseFieldDefinition.EVENT_TO_CALL, startEventResponse.getEventId());
//        oldCaseData.write(BailCaseFieldDefinition.TOKEN_TO_USE, startEventResponse.getToken());

        return coreCaseDataApi.submitForCaseworker(
            accessTokenFromProvider,
            s2sToken,
            caseworker.getId(),
            "IA",
            "Bail",
            true,
            caseDataContent
        );
    }

    private HttpHeaders setHeaders(String serviceAuthorizationToken, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);
        return headers;
    }

    private final List<String> validFields = Arrays.asList("beforeYouStartLabel1",
                                                           "isAdmin",
                                                           "sentByChecklist",
                                                           "applicantGivenNames",
                                                           "applicantFamilyName",
                                                           "applicantDateOfBirth",
                                                           "applicantGender",
                                                           "applicantGenderEnterDetails",
                                                           "applicantNationality",
                                                           "applicantNationalities",
                                                           "homeOfficeReferenceNumber",
                                                           "applicantDetainedLoc",
                                                           "applicantPrisonDetails",
                                                           "ircDetailsTitle",
                                                           "ircName",
                                                           "prisonDetailsTitle",
                                                           "prisonName",
                                                           "applicantArrivalInUKDateTitle",
                                                           "applicantArrivalInUKDate",
                                                           "applicantHasMobileLabel",
                                                           "applicantHasMobile",
                                                           "applicantMobileNumber",
                                                           "hasAppealHearingPendingTitle",
                                                           "hasAppealHearingPending",
                                                           "appealReferenceNumber",
                                                           "hasPreviousBailApplicationTitle",
                                                           "hasPreviousBailApplication",
                                                           "previousBailApplicationNumber",
                                                           "applicantBeenRefusedBailLabel",
                                                           "applicantBeenRefusedBail",
                                                           "bailHearingDate",
                                                           "applicantHasAddress",
                                                           "applicantAddress",
                                                           "agreesToBoundByFinancialCond",
                                                           "financialCondAmount",
                                                           "hasFinancialCondSupporter",
                                                           "supporterGivenNames",
                                                           "supporterFamilyNames",
                                                           "supporterAddressDetails",
                                                           "supporterContactDetailsLabel",
                                                           "supporterContactDetails",
                                                           "supporterTelephoneNumber",
                                                           "supporterMobileNumber",
                                                           "supporterEmailAddress",
                                                           "supporterDOB",
                                                           "supporterRelation",
                                                           "supporterOccupation",
                                                           "supporterImmigration",
                                                           "supporterNationality",
                                                           "supporterHasPassport",
                                                           "supporterPassport",
                                                           "financialAmountSupporterUndertakes",
                                                           "hasFinancialCondSupporter2",
                                                           "supporter2GivenNames",
                                                           "supporter2FamilyNames",
                                                           "supporter2AddressDetails",
                                                           "supporter2ContactDetailsLabel",
                                                           "supporter2ContactDetails",
                                                           "supporter2TelephoneNumber",
                                                           "supporter2MobileNumber",
                                                           "supporter2EmailAddress",
                                                           "supporter2DOB",
                                                           "supporter2Relation",
                                                           "supporter2Occupation",
                                                           "supporter2Immigration",
                                                           "supporter2Nationality",
                                                           "supporter2HasPassport",
                                                           "supporter2Passport",
                                                           "financialAmountSupporter2Undertakes",
                                                           "hasFinancialCondSupporter3",
                                                           "supporter3GivenNames",
                                                           "supporter3FamilyNames",
                                                           "supporter3AddressDetails",
                                                           "supporter3ContactDetailsLabel",
                                                           "supporter3ContactDetails",
                                                           "supporter3TelephoneNumber",
                                                           "supporter3MobileNumber",
                                                           "supporter3EmailAddress",
                                                           "supporter3DOB",
                                                           "supporter3Relation",
                                                           "supporter3Occupation",
                                                           "supporter3Immigration",
                                                           "supporter3Nationality",
                                                           "supporter3HasPassport",
                                                           "supporter3Passport",
                                                           "financialAmountSupporter3Undertakes",
                                                           "hasFinancialCondSupporter4",
                                                           "supporter4GivenNames",
                                                           "supporter4FamilyNames",
                                                           "supporter4AddressDetails",
                                                           "supporter4ContactDetailsLabel",
                                                           "supporter4ContactDetails",
                                                           "supporter4TelephoneNumber",
                                                           "supporter4MobileNumber",
                                                           "supporter4EmailAddress",
                                                           "supporter4DOB",
                                                           "supporter4Relation",
                                                           "supporter4Occupation",
                                                           "supporter4Immigration",
                                                           "supporter4Nationality",
                                                           "supporter4HasPassport",
                                                           "supporter4Passport",
                                                           "financialAmountSupporter4Undertakes",
                                                           "groundsForBailLabel1",
                                                           "groundsForBailReasons",
                                                           "groundsForBailProvideEvidenceOption",
                                                           "uploadTheBailEvidenceLabel",
                                                           "uploadTheBailEvidenceDocs",
                                                           "transferBailManagementYesOrNo",
                                                           "noTransferBailManagementReasons",
                                                           "interpreterYesNo",
                                                           "interpreterLanguages",
                                                           "disabilityTitle",
                                                           "applicantDisability1",
                                                           "applicantDisabilityDetails",
                                                           "videoHearingTitle",
                                                           "videoHearingLabel1",
                                                           "videoHearing1",
                                                           "videoHearingDetails",
                                                           "isHomeOffice",
                                                           "hasLegalRep",
                                                           "isLegalRep",
                                                           "legalRepCompany",
                                                           "legalRepName",
                                                           "legalRepEmail",
                                                           "legalRepPhone",
                                                           "legalRepReference",
                                                           "uploadB1FormLabel",
                                                           "uploadB1FormDocs");
}
