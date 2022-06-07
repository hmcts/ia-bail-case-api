package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.PriorApplicationsService;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class MakeNewApplicationSubmitHandler implements PreSubmitCallbackStateHandler<BailCase> {

    private PriorApplicationsService priorApplicationsService;

    public MakeNewApplicationSubmitHandler(PriorApplicationsService priorApplicationsService) {
        this.priorApplicationsService = priorApplicationsService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback,
                                                      PreSubmitCallbackResponse<BailCase> callbackResponse) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        BailCase bailCase = caseDetails.getCaseData();
        var ss = bailCase.size();
        bailCase.entrySet().removeIf(entry -> !validFields.contains(entry.getKey()));

        CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);

        if (caseDetailsBefore == null) {
            throw new IllegalStateException("Case details before missing");
        }

        BailCase bailCaseBefore = caseDetailsBefore.getCaseData();
        priorApplicationsService.appendPriorApplication(bailCase, bailCaseBefore);

        return new PreSubmitCallbackResponse<>(bailCase, State.APPLICATION_SUBMITTED);
    }

    private final List<String> validFields = Arrays.asList("bailReferenceNumber",
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
