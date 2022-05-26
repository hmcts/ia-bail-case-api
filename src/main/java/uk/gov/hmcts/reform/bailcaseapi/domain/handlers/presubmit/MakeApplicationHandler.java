package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;
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
import java.util.stream.Stream;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class MakeApplicationHandler implements PreSubmitCallbackHandler<BailCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    private final CompanyNameProvider companyNameProvider;

    protected CoreCaseDataApi coreCaseDataApi;

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;

    private static final String CASE_REFERENCE = "REF123";

    private static final Map<String, String> CREATE_CASE_DATA = new HashMap<String, String>() {
        {
            put("applicantGivenNames", "test...");
            put("applicantFamilyName", "test...");
        }
    };

    public MakeApplicationHandler(UserDetails userDetails, UserDetailsHelper userDetailsHelper,
                                  CompanyNameProvider companyNameProvider,
                                  AuthTokenGenerator serviceAuthTokenGenerator,
                                  CoreCaseDataApi coreCaseDataApi,
                                  AccessTokenProvider accessTokenProvider) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.companyNameProvider = companyNameProvider;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.coreCaseDataApi = coreCaseDataApi;
        this.accessTokenProvider = accessTokenProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && callback.getEvent() == Event.MAKE_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        BailCase newBailCase = SerializationUtils.clone(bailCase);

        newBailCase.remove("decisionUnsignedDocument");
        newBailCase.remove("applicationSubmissionDocument");
        newBailCase.remove("uploadBailSummaryDocs");
        newBailCase.remove("decisionUnsignedDocMetadata");
        newBailCase.remove("uploadBailSummaryMetadata");

        newBailCase.entrySet().removeIf(entry -> !validFields.contains(entry.getKey()));

        return new PreSubmitCallbackResponse<>(newBailCase);
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
