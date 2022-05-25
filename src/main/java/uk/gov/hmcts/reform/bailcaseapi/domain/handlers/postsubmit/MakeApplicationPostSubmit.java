package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.CompanyNameProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class MakeApplicationPostSubmit implements PostSubmitCallbackHandler<BailCase> {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private final String ccdUrl;

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    private final CompanyNameProvider companyNameProvider;

    protected CoreCaseDataApi coreCaseDataApi;
    private final RestTemplate restTemplate;

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;

    public MakeApplicationPostSubmit(UserDetails userDetails, UserDetailsHelper userDetailsHelper,
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
    public boolean canHandle(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.MAKE_APPLICATION;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {

        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        var previousCaseDetails = callback.getCaseDetailsBefore();

        var ss = userDetails.getAccessToken();
        var ss1 = accessTokenProvider.getAccessToken();
        String aa = serviceAuthTokenGenerator.generate();

        CaseDetails aaaaa = createCaseForCaseworker(userDetails, callback.getCaseDetails(), callback);

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You have created a new application");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "Link" + aaaaa.getId()
        );
        return postSubmitResponse;
    }

    private CaseDetails createCaseForCaseworker(UserDetails caseworker,
                                                uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails<BailCase> caseDetails,
                                                Callback<BailCase> callback) {

        var oldCaseId = caseDetails.getId();
        var oldCaseJur = caseDetails.getJurisdiction();
        var oldCaseData = caseDetails.getCaseData();
        var ss = caseworker.getAccessToken();
        var ss1 = accessTokenProvider.getAccessToken();
        String aa = serviceAuthTokenGenerator.generate();
        var gg = caseworker.getId();

        var eventToUse = (String) oldCaseData.read(BailCaseFieldDefinition.EVENT_TO_CALL).orElse(null);
        var token = (String) oldCaseData.read(BailCaseFieldDefinition.TOKEN_TO_USE).orElse(null);

        oldCaseData.remove("decisionUnsignedDocument");
        oldCaseData.remove("applicationSubmissionDocument");
        oldCaseData.remove("uploadBailSummaryDocs");
        oldCaseData.remove("decisionUnsignedDocMetadata");
        oldCaseData.remove("uploadBailSummaryMetadata");

        oldCaseData.entrySet().removeIf(entry -> !validFields.contains(entry.getKey()));

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(token)
            .event(uk.gov.hmcts.reform.ccd.client.model.Event.builder().id(eventToUse).build())
            .data(oldCaseData)
            .build();

        return coreCaseDataApi.submitForCaseworker(
            ss1,
            aa,
            caseworker.getId(),
            "IA",
            "Bail",
            true,
            caseDataContent
        );
//        return aaaaa;
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
