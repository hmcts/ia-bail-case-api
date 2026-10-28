package uk.gov.hmcts.reform.bailcaseapi.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.SearchCriteria;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerApproverCaaLegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerApproverCaaLegalrepSolicitorSystemCrudHomeofficebailHomeofficepouIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudGSprofileRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCaseworkeriabailsIacjudgeLegalrepSolicitorSystemCrudGSprofileHomeofficebailHomeofficepouRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCruHomeofficebailIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerHomeofficebailHomeofficepouSystemCrudIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeCruHomeofficebailLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeCrudRparobotRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeRparobotSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeIaspecificaccessadminprofileIaspecificaccessctscprofileIaspecificaccessjudgeprofileIaspecificaccesslegalopsprofileLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudHomeofficebailIacjudgeIaspecificaccessadminprofileIaspecificaccessctscprofileIaspecificaccessjudgeprofileIaspecificaccesslegalopsprofileLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudHomeofficebailIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudIacjudgeCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.AdmofficerSystemCrudIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.CaseflagsSystemCrudAdmofficerRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.CaselinksCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.CaselinksSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.CaseworkeriabailsHomeofficebailHomeofficepouSystemCrudAdmofficerLegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.CaseworkeriabailsSystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorRuAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.HomeofficebailHomeofficepouCruAdmofficerIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.HomeofficebailHomeofficepouIacjudgeSystemCrudAdmofficerLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.IacjudgeCruAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.LegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.NoGrantsAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerCruHomeofficebailIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerHomeofficebailHomeofficepouCruIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeIaspecificaccessadminprofileIaspecificaccessctscprofileIaspecificaccessjudgeprofileIaspecificaccesslegalopsprofileLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerHomeofficebailLegalrepSolicitorCruIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerIacjudgeCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerIacjudgeCruHomeofficebailLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudAdmofficerIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudCaseflagsCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudCaseofficerCaseworkeriabailsTtlManagerCruCitizenLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudHomeofficebailHomeofficepouCruAdmofficerIacjudgeLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudHomeofficebailHomeofficepouRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudIacjudgeRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudLegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.access.SystemCrudLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.CaseManagementLocation;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.Direction;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.DocumentWithDescription;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.HearingDecision;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.HearingRecordingDocument;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.InterpreterDetails;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.InterpreterSignLanguage;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.InterpreterSpokenLanguage;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.LanguageList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.Notification;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.Parties;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.PreviousDecisionDetails;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.PreviousHearingDetails;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.complex.RefDataListingLocationDetail;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.ApplicationSentByList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.ConditionsForBailList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.DecisionGrantedOrRefusedList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.DeclarationOnSubmit;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.DetentionFacilityList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.DirectionToList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.EndApplicationOutcomeList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.FinancialConditionCorrectEdit;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.HasPendingAppealHearingList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.HearingCentreList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.HearingLocationList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.InterpreterLanguageCategory;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.IrcNameList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.ListingEventList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.PreviousApplicationExistsList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.PrisonNameList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.ProbationOffenderManagerContactDetails;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.RecordDecisionTypeList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.TribunalDecisionList;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.enums.UploadDocumentsSuppliedByList;

/**
 * Generated by ccd-definition-converter from CaseField — do not edit by hand.
 */
@Data
@NoArgsConstructor
public class CaseData {
    @JsonUnwrapped(prefix = "applicant")
    private Applicant applicant = new Applicant();

    @JsonUnwrapped(prefix = "application")
        private Application application = new Application();

    @JsonUnwrapped(prefix = "bail")
    private Bail bail = new Bail();

    @JsonUnwrapped(prefix = "fcs")
    private Fcs fcs = new Fcs();

    @JsonUnwrapped(prefix = "legal")
        private Legal legal = new Legal();

    @JsonUnwrapped(prefix = "prevApp")
        private PrevApp prevApp = new PrevApp();

    @JsonUnwrapped
    private Progress progress = new Progress();

    @JsonUnwrapped(prefix = "supporter")
        private Supporter supporter = new Supporter();

    @JsonUnwrapped(prefix = "supporter2")
    private Supporter2 supporter2 = new Supporter2();

    @JsonUnwrapped(prefix = "supporter3")
    private Supporter3 supporter3 = new Supporter3();

    @JsonUnwrapped(prefix = "supporter4")
    private Supporter4 supporter4 = new Supporter4();

    @CCD(
            label = "Internal flag managed by API to show correct case state for legal representatives",
            access = {SystemCrudLegalrepSolicitorRAccess.class}
    )
    private String currentCaseStateVisibleToLegalRepresentative;

    @CCD(
            label = "Internal flag managed by API to show correct case state for Judges",
            access = {SystemCrudIacjudgeRAccess.class}
    )
    private String currentCaseStateVisibleToJudge;

    @CCD(label = "Internal flag managed by API to show correct case state for admin", access = {SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private String currentCaseStateVisibleToAdminOfficer;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office generic",
            access = {SystemCrudHomeofficebailHomeofficepouRAccess.class}
    )
    private String currentCaseStateVisibleToHomeOffice;

    @CCD(
            label = "Internal flag managed by API to show correct case state for all users",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String currentCaseStateVisibleToAllUsers;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadBailSummaryAction",
            access = {AdmofficerHomeofficebailHomeofficepouSystemCrudIacjudgeLegalrepSolicitorRAccess.class}
    )
    private YesOrNo uploadBailSummaryActionAvailable;

    @CCD(
            label = "Has the applicant made a previous application before?",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private PreviousApplicationExistsList hasPreviousBailApplication;

    @CCD(
            label = "What is the bail number for the previous application?",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private String previousBailApplicationNumber;

    @CCD(
            label = "![]()\r\n"
                            + "## Probation offender manager",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudIacjudgeRAccess.class}
    )
    private String hasProbationOffenderManagerTitle;

    @CCD(
            label = "Probation offender manager",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private YesOrNo hasProbationOffenderManager;

    @CCD(label = "Given names", access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class})
    private String probationOffenderManagerGivenName;

    @CCD(label = "Family name", access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class})
    private String probationOffenderManagerFamilyName;

    @CCD(
            label = "Select email and at least one phone option.\n"
                            + " \n"
                            + " Contact details",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudAccess.class}
    )
    private String probationOffenderManagerContactDetailsLabel;

    @CCD(label = " ", access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudAccess.class})
    private Set<ProbationOffenderManagerContactDetails> probationOffenderManagerContactDetails;

    @CCD(
            label = "Telephone number",
            typeOverride = FieldType.PhoneUK,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private String probationOffenderManagerTelephoneNumber;

    @CCD(
            label = "Mobile number",
            typeOverride = FieldType.PhoneUK,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private String probationOffenderManagerMobileNumber;

    @CCD(
            label = "Email address",
            typeOverride = FieldType.Email,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private String probationOffenderManagerEmailAddress;

    @CCD(
            label = " ",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private YesOrNo previousApplicationDoneViaAria;

    @CCD(
            label = " ",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private YesOrNo previousApplicationDoneViaCcd;

    @CCD(
            label = "You don't have access to any previous applications for the applicant. Click Continue to create a new application.",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String noAccessToPreviousApplications;

    @CCD(
            label = "This applicant has made at least one previous bail application.\n"
                            + "\n"
                            + " You will need to choose one of the following options to continue:\n"
                            + "\n"
                            + "* If you did not make a previous application on the applicant’s behalf you will need to acquire the case record to continue. You can do so by using Notice of Change [here](/noc).\n"
                            + "\n"
                            + "* If you did, you need to find the applicant in the [case list](/case/case-list) and make a new application.",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String redirectToPreviousApplicationOrNoc;

    @CCD(
            label = "You will be asked to provide the following information about this application.\n"
                            + "\n"
                            + "* Applicant’s personal details\n"
                            + "* Application details\n"
                            + "* Financial condition details (if any)\n"
                            + "* Grounds for the application\n"
                            + "*Hearing requirements (if any)\n"
                            + "* Representative details (if any)\n"
                            + "\n"
                            + "Once you start, you will not be able to save your answers until you have completed the application.\n"
                            + "\n"
                            + "Please ensure you have all the information you need to hand and allow enough time to complete the application in one sitting.",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private String beforeYouStartLabel1;

    @CCD(label = "Application sent by", access = {SystemCrudAdmofficerCruAccess.class})
    private ApplicationSentByList sentByChecklist;

    @CCD(
            label = " ",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private YesOrNo isAdmin;

    @CCD(
            label = "Legal Representative",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private YesOrNo isLegalRep;

    @CCD(
            label = " ",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private YesOrNo isHomeOffice;

    @CCD(
            label = "Home Office reference\n",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private String homeOfficeReferenceNumber;

    @CCD(
            label = " ",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudAccess.class}
    )
    private String ircDetailsTitle;

    @CCD(
            label = "Immigration removal centre",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ircNameList",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private IrcNameList ircName;

    @CCD(
            label = " ",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudAccess.class}
    )
    private String prisonDetailsTitle;

    @CCD(
            label = "Prison",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "prisonNameList",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private PrisonNameList prisonName;

    @CCD(
            label = " ",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCrudAccess.class}
    )
    private String hasAppealHearingPendingTitle;

    @CCD(
            label = "Pending appeal hearing",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private HasPendingAppealHearingList hasAppealHearingPending;

    @CCD(
            label = "Appeal reference number",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private String appealReferenceNumber;

    @CCD(label = " ", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String hasAppealHearingPendingTitleUT;

    @CCD(label = "Appeal reference number in UT", access = {SystemCrudAccess.class})
    private String utAppealReferenceNumber;

    @CCD(
            label = "Financial condition",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private YesOrNo agreesToBoundByFinancialCond;

    @CCD(
            label = "Enter the financial condition amount (£)",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private Integer financialCondAmount1;

    @CCD(
            label = "Financial condition supporter",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo hasFinancialCondSupporter;

    @CCD(
            label = "Date of birth",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private LocalDate supporterDOB;

    @CCD(
            label = "Financial condition amount (£)",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private Integer financialAmountSupporterUndertakes1;

    @CCD(
            label = "Financial condition supporter 2",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo hasFinancialCondSupporter2;

    @CCD(
            label = "Date of birth",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private LocalDate supporter2DOB;

    @CCD(
            label = "Financial condition amount (£)",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private Integer financialAmountSupporter2Undertakes1;

    @CCD(
            label = "Financial condition supporter 3",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo hasFinancialCondSupporter3;

    @CCD(
            label = "Date of birth",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private LocalDate supporter3DOB;

    @CCD(
            label = "Financial condition amount (£)",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private Integer financialAmountSupporter3Undertakes1;

    @CCD(
            label = "Financial condition supporter 4",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo hasFinancialCondSupporter4;

    @CCD(
            label = "Date of birth",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private LocalDate supporter4DOB;

    @CCD(
            label = "Financial condition amount (£)",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private Integer financialAmountSupporter4Undertakes1;

    @CCD(
            label = "On the next screen you will be asked to set out all the reasons why the applicant should be released. Give as much detail as possible and please note the information below.\n"
                            + "\n"
                            + "# Previous applications\n"
                            + "\n"
                            + "If the applicant has had a previous application for bail refused, include full details of any additional grounds or change in circumstances since then on the next screen.\n"
                            + "\n"
                            + "If that refusal was within the last 28 days, this application will be dismissed without an oral hearing unless the applicant has demonstrated with evidence and information submitted with the application that there has been a material change of circumstances since the last bail decision.\n"
                            + "\n"
                            + "# Electronic monitoring\n"
                            + "\n"
                            + "If the applicant is currently detained and is subject to deportation proceedings, the Home Office may be required to electronically monitor the applicant as part of the bail conditions.\n"
                            + "\n"
                            + "The Home Office will use a GPS tag fitted to the applicant’s ankle to do this. Decisions about whether it is appropriate to do this will be made by the Home Office not the First-tier Tribunal.\n"
                            + "\n"
                            + "If there are reasons why the applicant feels he or she should not be required to wear a tag, state these reasons on the next screen to allow the Home Office to consider the matter before the bail hearing.",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private String groundsForBailLabel1;

    @CCD(
            label = "Supporting evidence",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private YesOrNo groundsForBailProvideEvidenceOption;

    @CCD(
            label = "#### Files should be:\n"
                            + "\n"
                            + "* a maximum of 100MB in size (larger files must be split)\n"
                            + "* labelled clearly, e.g. BailEvidence.pdf\n"
                            + "* in Word or PDF format",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private String uploadTheBailEvidenceLabel;

    @CCD(
            label = "Upload a file",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadTheBailEvidenceDocs;

    @CCD(
            label = "## Documents",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String documentTabTitle;

    @CCD(
            label = "Enter the bail grounds",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String groundsForBailReasons;

    @CCD(
            label = "Transfer bail management",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private YesOrNo transferBailManagementYesOrNo;

    @CCD(
            label = "Explain why the applicant does not consent to bail being transferred",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String noTransferBailManagementReasons;

    @CCD(
            label = "Management of bail",
            access = {SystemCrudAdmofficerHomeofficebailLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private YesOrNo transferBailManagementObjectionYesOrNo;

    @CCD(
            label = "Explain why applicant objects to the Management of bail being transferred",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerHomeofficebailLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String objectedTransferBailManagementReasons;

    @CCD(
            label = "Will the applicant need a spoken or sign language interpreter at the hearing?",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private YesOrNo interpreterYesNo;

    @CCD(
            label = "Language Details",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private List<ListValue<LanguageList>> interpreterLanguages;

    @CCD(label = " ", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String disabilityTitle;

    @CCD(label = " ", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String videoHearingTitle;

    @CCD(
            label = "The hearing of this bail application may be by a video link. If it is, the applicant will remain at the detention centre. Where there are exceptional circumstances and it is considered that the applicant is unable to use the video link, please give reasons and a judge will decide whether the application will be heard in person or by video link. All parties will be informed of this decision when the case is listed.",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private String videoHearingLabel1;

    @CCD(
            label = "Able to join video hearing",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private YesOrNo videoHearing1;

    @CCD(
            label = "Explain why the applicant would not be able to join the hearing by video link",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String videoHearingDetails;

    @CCD(
            label = "Legal Representative",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouCruIacjudgeRAccess.class}
    )
    private YesOrNo hasLegalRep;

    @CCD(
            label = "Upload a copy of the B1 form the applicant or legal representative sent to the Tribunal.\n"
                            + "\n"
                            + " #### Files should be:\n"
                            + "\n"
                            + "* a maximum of 100MB in size (larger files must be split)\n"
                            + "*   labelled clearly, e.g. applicant-name-B1-form.pdf",
            typeOverride = FieldType.Label,
            access = {AdmofficerCruAccess.class}
    )
    private String uploadB1FormLabel;

    @CCD(label = "Upload B1 form", access = {AdmofficerSystemCrudAccess.class})
    private List<ListValue<DocumentWithDescription>> uploadB1FormDocs;

    @CCD(label = " ", typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeRAccess.class})
    private String caseNotesTabTitle;

    @CCD(label = " ", typeOverride = FieldType.CaseHistoryViewer, access = {SystemCrudAdmofficerIacjudgeRAccess.class})
    private String caseHistoryTabTitle;

    @CCD(
            label = "![The applicant in this case is legally represented](https://raw.githubusercontent.com/hmcts/ia-bail-frontend/master/app/assets/images/journey_type_legally_represented.png)",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String applicant_has_legal_rep_flag;

    @CCD(
            label = "![The applicant has had bail refused within last 28 days](https://raw.githubusercontent.com/hmcts/ia-bail-frontend/master/app/assets/images/refusal_within_28days.png)",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String refusal_within_28days_flag;

    @CCD(
            label = "![The applicant is legally represented and has had bail refused within last 28 days](https://raw.githubusercontent.com/hmcts/ia-bail-frontend/master/app/assets/images/legal_rep_and_refusal_flag.png)",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String legal_rep_and_refusal_flag;

    @CCD(
            label = "![]()\n"
                            + "## Case details",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String caseDetailsTitle;

    @CCD(label = "![]()\r\n" + "## Hearing details", typeOverride = FieldType.Label, access = {SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private String hearingDetailsTitle;

    @CCD(
            label = "Hearing centre",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingCentreList",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private HearingCentreList hearingCentre;

    @CCD(
            label = "Designated Tribunal Centre",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingCentreList",
            access = {SystemCrudAdmofficerCruAccess.class}
    )
    private HearingCentreList designatedTribunalCentre;

    @CCD(
            label = "Backend flag combining isLegalRep and hasLegalRep into 1 field for the overview tab flag conditions",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private YesOrNo isLegallyRepresentedForFlag;

    @JsonProperty("HearingTabTitle")
    @CCD(
            label = "## Hearing",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String hearingTabTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Previous Hearing Details",
            typeOverride = FieldType.Label,
            access = {AdmofficerCruHomeofficebailIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String previousHearingDetailsTitle;

    @CCD(label = "Previous Hearing Details", access = {SystemCrudAdmofficerCruHomeofficebailIacjudgeLegalrepSolicitorRAccess.class})
    private List<ListValue<PreviousHearingDetails>> previousListingDetails;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeRAccess.class})
    private YesOrNo hasBeenRelisted;

    @CCD(
            label = "![]()\r\n"
                            + "## Previous Decision Details",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCruHomeofficebailLegalrepSolicitorRAccess.class}
    )
    private String previousDecisionDetailsTitle;

    @CCD(label = "Previous Decision Details", access = {SystemCrudAdmofficerIacjudgeCruHomeofficebailLegalrepSolicitorRAccess.class})
    private List<ListValue<PreviousDecisionDetails>> previousDecisionDetails;

    @CCD(label = " ", access = {LegalrepSolicitorSystemCrudAccess.class})
    private Set<DeclarationOnSubmit> declarationOnSubmit;

    @CCD(
            label = "Date and time",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private LocalDateTime hearingDate;

    @CCD(
            label = "Location",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingLocationList",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private HearingLocationList hearingLocation;

    @CCD(
            label = "Detention facility",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "detentionFacilityList",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruAccess.class}
    )
    private DetentionFacilityList detentionFacility;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private String submitNotificationStatus;

    @CCD(
            label = "Internal collection managed by API to track notifications sent using GovNotify for this case",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text",
            access = {SystemCrudAccess.class}
    )
    private List<ListValue<String>> notificationsSent;

    @CCD(
            label = "#### Files should be:\n"
                            + "\n"
                            + "* a maximum of 100MB in size (larger files must be split)\n"
                            + "* labelled clearly, e.g. BailSummary.pdf\n"
                            + "* in Word or PDF format",
            typeOverride = FieldType.Label,
            access = {HomeofficebailHomeofficepouCruAdmofficerIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String uploadBailSummaryLabel;

    @CCD(
            label = "Upload Bail Summary",
            access = {SystemCrudHomeofficebailHomeofficepouCruAdmofficerIacjudgeLegalrepSolicitorRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadBailSummaryDocs;

    @CCD(label = "Home Office documents", access = {AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class})
    private List<ListValue<DocumentWithMetadata>> homeOfficeDocumentsWithMetadata;

    @CCD(
            label = "Enter the name of the judge who heard the application.",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCruAccess.class}
    )
    private String judgeDetailsLabel;

    @CCD(label = "Judge name", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private String judgeDetailsName;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private YesOrNo secretaryOfStateConsentYesOrNo;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private DecisionGrantedOrRefusedList decisionGrantedOrRefused;

    @CCD(
            label = "Enter the reasons for refusal",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String reasonForRefusalDetails;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private YesOrNo releaseStatusYesOrNo;

    @CCD(
            label = "Select the conditions the applicant will be subject to",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsLabel;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private Set<ConditionsForBailList> conditionsForBail;

    @CCD(
            label = "Appearance (where bail is going to be transferred to the Home Office)",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsForBailAppearance;

    @CCD(
            label = "Activities",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsForBailActivities;

    @CCD(
            label = "Residence",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsForBailResidence;

    @CCD(
            label = "Reporting (where bail is NOT going to be transferred to the Home Office)",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsForBailReporting;

    @CCD(
            label = "Electronic Monitoring",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsForBailElectronicMonitoring;

    @CCD(
            label = "Other",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String conditionsForBailOther;

    @CCD(label = "Will the applicant be subject to a financial condition?", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String recordFinancialConditionLabel;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private YesOrNo recordFinancialConditionYesOrNo;

    @CCD(
            label = "|Applicant details|| \n"
                            + "| ------------- | ------------- |\n"
                            + "|Given names|${applicantGivenNames}| \n"
                            + "|Family name|${applicantFamilyName}| \n"
                            + "|Financial condition amount (£)|${financialCondAmount1}|",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCruAccess.class}
    )
    private String recordFinancialConditionDetails;

    @CCD(label = "Is the financial condition amount correct?", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String recordFinancialConditionCorrectLabel;

    @CCD(label = " ", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private FinancialConditionCorrectEdit recordFinancialConditionCorrectYesOrEdit;

    @CCD(
            label = "Confirm the financial condition amount is correct. \n"
                            + "\n"
                            + "## Financial condition amount",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCruAccess.class}
    )
    private String confirmFinancialConditionAmountLabel;

    @CCD(label = "Confirm the financial condition supporter details are correct.", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String financialCondSupporterConfirmationTitle;

    @CCD(label = "### Amount undertaken to be paid", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String financialCondSupporterConfirmationAmountTitle;

    @CCD(
            label = "Confirm the financial condition supporter details are correct.",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String supporter1ConfirmationTitle;

    @CCD(
            label = "|Financial condition supporter details|| \n"
                            + "| ------------- | ------------- |\n"
                            + "|Given names|${supporterGivenNames}| \n"
                            + "|Family name|${supporterFamilyNames}|",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String supporter1ConfirmationName;

    @CCD(
            label = "### Amount undertaken to be paid",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String supporter1ConfirmationAmountTitle;

    @CCD(
            label = "Review the details of this financial condition supporter for the applicant.",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String supporter1ConfirmationTitleJudgeAgree;

    @CCD(
            label = "|Financial condition supporter details|| \n"
                            + "| ------------- | ------------- | \n"
                            + "|Given names|${supporterGivenNames}| \n"
                            + "|Family name|${supporterFamilyNames}| \n"
                            + "|Amount undertaken to be paid (£)|${financialAmountSupporterUndertakes1}|",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String supporter1ConfirmationDetailsJudgeAgree;

    @CCD(
            label = "Did the judge agree to accept financial condition supporter 1?",
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private YesOrNo judgeHasAgreedToSupporter1;

    @CCD(
            label = "Did the judge agree to accept financial condition supporter 2?",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo judgeHasAgreedToSupporter2;

    @CCD(
            label = "Did the judge agree to accept financial condition supporter 3?",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo judgeHasAgreedToSupporter3;

    @CCD(
            label = "Did the judge agree to accept financial condition supporter 4?",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeCruAccess.class}
    )
    private YesOrNo judgeHasAgreedToSupporter4;

    @CCD(
            label = " ",
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private TribunalDecisionList recordTheDecisionList;

    @CCD(
            label = "Enter the reasons for refusal",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String tribunalRefusalReason;

    @CCD(
            label = " ",
            typeOverride = FieldType.Label,
            access = {IacjudgeCruAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String reasonsJudgeIsMindedLabel;

    @CCD(
            label = "Enter the reasons the judge is minded to grant bail",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String reasonsJudgeIsMindedDetails;

    @CCD(
            label = " ",
            typeOverride = FieldType.Label,
            access = {IacjudgeCruAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String ssConsentDecisionLabel;

    @CCD(
            label = "Does the Secretary of State give consent to grant bail?",
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private YesOrNo ssConsentDecision;

    @CCD(
            label = " ",
            typeOverride = FieldType.Label,
            access = {IacjudgeCruAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String secretaryOfStateRefusalReasonsLabel;

    @CCD(
            label = "Enter the reasons for refusal",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerSystemCrudIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String secretaryOfStateRefusalReasons;

    @CCD(
            label = "Upload a decision notice for this application that has been signed by the appropriate parties. \n"
                            + "\n"
                            + " #### Files should be:\n"
                            + "\n"
                            + "* a maximum of 100MB in size (larger files must be split)\n"
                            + "* labelled clearly, e.g. applicant-name-decision-notice.pdf",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String uploadSignedDecisionNoticeLabel;

    @CCD(label = "Decision document", access = {AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class})
    private Document uploadSignedDecisionNoticeDocument;

    @CCD(label = "Decision document", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private Document decisionUnsignedDocument;

    @CCD(label = "Tribunal documents", access = {AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class})
    private List<ListValue<DocumentWithMetadata>> tribunalDocumentsWithMetadata;

    @CCD(label = "Decision documents", access = {AdmofficerSystemCrudIacjudgeCruAccess.class})
    private List<ListValue<DocumentWithMetadata>> unsgnDecisionDocumentWithMetadata;

    @CCD(
            label = "Decision documents",
            access = {HomeofficebailHomeofficepouIacjudgeSystemCrudAdmofficerLegalrepSolicitorRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> signDecisionDocumentWithMetadata;

    @CCD(
            label = "Enter the details of the case note",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudRparobotRAccess.class}
    )
    private String addCaseNoteLabel;

    @CCD(label = "Subject", access = {AdmofficerIacjudgeRparobotSystemCrudAccess.class})
    private String addCaseNoteSubject;

    @CCD(
            label = "Case note",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerIacjudgeRparobotSystemCrudAccess.class}
    )
    private String addCaseNoteDescription;

    @CCD(label = "Upload a document", access = {AdmofficerIacjudgeRparobotSystemCrudAccess.class})
    private Document addCaseNoteDocument;

    @CCD(
            label = "[Add case note](/case/IA/Bail/${[CASE_REFERENCE]}/trigger/addCaseNote)",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudAccess.class}
    )
    private String addCaseNoteLink;

    @CCD(label = "Case notes", access = {SystemCrudAdmofficerIacjudgeRAccess.class})
    private List<ListValue<CaseNote>> caseNotes;

    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerIacjudgeSystemCrudAccess.class}
    )
    private String sendDirectionLabel;

    @CCD(
            label = "Explain the direction you are issuing",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerIacjudgeSystemCrudAccess.class}
    )
    private String sendDirectionDescription;

    @CCD(
            label = "Who are you giving direction to?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "directionToList",
            access = {AdmofficerIacjudgeSystemCrudAccess.class}
    )
    private DirectionToList sendDirectionList;

    @CCD(
            label = "By what date must they comply?",
            access = {AdmofficerIacjudgeSystemCrudAccess.class}
    )
    private LocalDate dateOfCompliance;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private LocalDateTime dateTimeDirectionCreated;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private LocalDateTime dateTimeDirectionModified;

    @CCD(
            label = "[Send a new direction](/case/IA/Bail/${[CASE_REFERENCE]}/trigger/sendBailDirection)",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudAccess.class}
    )
    private String sendANewDirectionLink;

    @CCD(
            label = "[Change a direction due date](/case/IA/Bail/${[CASE_REFERENCE]}/trigger/changeBailDirectionDueDate)",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudAccess.class}
    )
    private String changeADirectionDueDateLink;

    @CCD(label = "Directions", access = {SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private List<ListValue<Direction>> directions;

    @CCD(label = " ", access = {AdmofficerIacjudgeSystemCrudAccess.class})
    private Direction lastModifiedDirection;

    @CCD(
            label = "Select the direction you want to update.",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudAccess.class}
    )
    private String changeBailDirectionLabel;

    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "recordDecisionTypeList",
            access = {SystemCrudAdmofficerIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private RecordDecisionTypeList recordDecisionType;

    @CCD(
            label = "![]()\n"
                            + "## Decision details",
            typeOverride = FieldType.Label,
            access = {AdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String decisionDetailsTitle;

    @CCD(label = "Decision date", access = {SystemCrudAdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private LocalDate decisionDetailsDate;

    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "recordDecisionTypeList",
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private RecordDecisionTypeList recordUnsignedDecisionType;

    @CCD(
            label = "![]()\n"
                            + "## Decision details",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCruAccess.class}
    )
    private String decisionUnsignedDetailsTitle;

    @CCD(label = "Decision date", access = {SystemCrudAdmofficerIacjudgeCruAccess.class})
    private LocalDate decisionUnsignedDetailsDate;

    @CCD(
            label = "Click Continue to move this application from the Conditional grant state to the Decided state. Once the application is moved to decided, no further action will be required.",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String declarationOnDecision;

    @CCD(
            label = "Select the outcome that has led to the end of this application and provide the reasons for the outcome.",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudAccess.class}
    )
    private String endApplicationLabel;

    @CCD(
            label = "Reasons",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private String endApplicationReasons;

    @CCD(label = "Outcome", access = {AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class})
    private EndApplicationOutcomeList endApplicationOutcome;

    @CCD(label = "Date application ended", access = {SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private LocalDate endApplicationDate;

    @CCD(label = "![]()\n" + "## Ended application details", typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private String endApplicationDetailsTitle;

    @CCD(
            label = " ",
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private String currentUser;

    @CCD(
            label = " ",
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private String lastFileUploadedBy;

    @CCD(
            label = "Any document you upload will be visible to all parties in the documents tab",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String uploadDocumentsLabel;

    @CCD(
            label = "Documents supplied by",
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private UploadDocumentsSuppliedByList uploadDocumentsSuppliedBy;

    @CCD(
            label = "#### Files should be:\n"
                            + "\n"
                            + "* a maximum of 100MB in size (larger files must be split)\n"
                            + "* labelled clearly, e.g. applicant-name-additional-evidence.pdf",
            typeOverride = FieldType.Label,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String uploadDocumentsGuidelines;

    @CCD(
            label = "Upload documents",
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadDocuments;

    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAdmofficerIacjudgeCruAccess.class}
    )
    private String editDocumentsReason;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private String outcomeDate;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private String outcomeState;

    @CCD(
            label = "Prior Applications",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private List<ListValue<PriorApplication>> priorApplications1;

    @CCD(
            label = "[View previous applications](/case/IA/Bail/${[CASE_REFERENCE]}/trigger/viewPreviousApplications)",
            typeOverride = FieldType.Label
    ,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private String previousApplicationsTabLabel;

    @CCD(
            label = "Select previous application",
            typeOverride = FieldType.DynamicList,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private String previousApplicationList;

    @CCD(
            label = "Organisation policy",
            access = {AdmofficerApproverCaaLegalrepSolicitorSystemCrudHomeofficebailHomeofficepouIacjudgeRAccess.class}
    )
    private OrganisationPolicy localAuthorityPolicy;

    @CCD(
            label = "Change Organisation Request",
            access = {AdmofficerApproverCaaLegalrepSolicitorSystemCrudAccess.class}
    )
    private ChangeOrganisationRequest changeOrganisationRequestField;

    @CCD(
            label = "#### Once you've submitted this request\r\n"
                            + "- The legal representative will no longer have access to this case\r\n"
                            + "- If the case has been shared with other legal representatives, they will also lose access\r\n"
                            + "- If there are any cases linked to this case, they will not be affected",
            typeOverride = FieldType.Label,
            access = {AdmofficerRAccess.class}
    )
    private String removeRepresentationDescription;

    @CCD(
            label = "#### Once you've submitted this request\r\n"
                            + "- You will no longer have access to this case\r\n"
                            + "- If the case has been shared with any colleagues, they will also lose access\r\n"
                            + "- Linked appeals are not affected. If you want to stop representing a client on a linked appeal, you need to access the case and repeat this action.",
            typeOverride = FieldType.Label
    ,
            access = {NoGrantsAccess.class})
    private String stopRepresentingDescription;

    @CCD(label = "Company", access = {LegalrepSolicitorSystemCrudAccess.class})
    private String updateLegalRepCompany;

    @CCD(label = "Name", access = {SystemCrudLegalrepSolicitorCruAccess.class})
    private String updateLegalRepName;

    @CCD(label = "Family name", access = {SystemCrudLegalrepSolicitorCruAccess.class})
    private String updateLegalRepFamilyName;

    @CCD(
            label = "Email",
            hint = "This is for correspondence only. You cannot change your sign-in email.",
            access = {SystemCrudLegalrepSolicitorCruAccess.class}
    )
    private String updateLegalRepEmail1;

    @CCD(
            label = "Phone number",
            typeOverride = FieldType.PhoneUK,
            access = {SystemCrudLegalrepSolicitorCruAccess.class}
    )
    private String updateLegalRepPhone;

    @CCD(label = "Own Reference", access = {SystemCrudLegalrepSolicitorCruAccess.class})
    private String updateLegalRepReference;

    @CCD(label = " ", access = {CaselinksSystemCrudAccess.class})
    private String caseLinksFlag;

    @CCD(label = " ", access = {CaselinksSystemCrudAccess.class})
    private String maintainCaseLinksFlag;

    @CCD(label = " ", typeOverride = FieldType.Label, access = {CaselinksCrudAccess.class})
    private String caseLinksTabTitle;

    @CCD(label = "Linked Cases", access = {CaselinksSystemCrudAccess.class})
    private List<ListValue<CaseLink>> caseLinks;

    @JsonProperty("LinkedCasesComponentLauncher")
    @CCD(
            label = "Component Launcher (for displaying Linked Cases data)",
            typeOverride = FieldType.ComponentLauncher,
            access = {CaselinksSystemCrudAccess.class}
    )
    private String linkedCasesComponentLauncher;

    @CCD(label = " ", typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String caseFlagsTabTitle;

    @CCD(label = " ", access = {SystemCrudCaseflagsCruAccess.class})
    private Flags caseFlags;

    @CCD(label = " ", access = {SystemCrudCaseflagsCruAccess.class})
    private Flags appellantLevelFlags;

    @CCD(
            label = " ",
            typeOverride = FieldType.FlagLauncher,
            access = {SystemCrudCaseflagsCruAccess.class}
    )
    private String flagLauncher;

    @CCD(
            label = "Tell us which spoken language is needed for the applicant",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSpokenLanguage applicantInterpreterSpokenLanguage;

    @CCD(
            label = "Tell us which sign language is needed for the applicant",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSignLanguage applicantInterpreterSignLanguage;

    @CCD(
            label = "What kind of interpreter does ${supporterGivenNames} ${supporterFamilyNames} need?",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private Set<InterpreterLanguageCategory> fcs1InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${supporter2GivenNames} ${supporter2FamilyNames} need?",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private Set<InterpreterLanguageCategory> fcs2InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${supporter3GivenNames} ${supporter3FamilyNames} need?",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private Set<InterpreterLanguageCategory> fcs3InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${supporter4GivenNames} ${supporter4FamilyNames} need?",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private Set<InterpreterLanguageCategory> fcs4InterpreterLanguageCategory;

    @CCD(
            label = "Which spoken language is needed for ${supporterGivenNames} ${supporterFamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSpokenLanguage fcs1InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${supporterGivenNames} ${supporterFamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSignLanguage fcs1InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language is needed for ${supporter2GivenNames} ${supporter2FamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSpokenLanguage fcs2InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${supporter2GivenNames} ${supporter2FamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSignLanguage fcs2InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language is needed for ${supporter3GivenNames} ${supporter3FamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSpokenLanguage fcs3InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${supporter3GivenNames} ${supporter3FamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSignLanguage fcs3InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language is needed for ${supporter4GivenNames} ${supporter4FamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSpokenLanguage fcs4InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${supporter4GivenNames} ${supporter4FamilyNames}",
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudIacjudgeRAccess.class}
    )
    private InterpreterSignLanguage fcs4InterpreterSignLanguage;

    @CCD(
            label = " ",
            access = {SystemCrudAdmofficerHomeofficebailHomeofficepouIacjudgeIaspecificaccessadminprofileIaspecificaccessctscprofileIaspecificaccessjudgeprofileIaspecificaccesslegalopsprofileLegalrepSolicitorRAccess.class}
    )
    private String supporter1PartyId;

    @CCD(
            label = "Have you verified if the Applicant's place of detention is correct?",
            access = {AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeIaspecificaccessadminprofileIaspecificaccessctscprofileIaspecificaccessjudgeprofileIaspecificaccesslegalopsprofileLegalrepSolicitorRAccess.class}
    )
    private YesOrNo isDetentionLocationCorrect;

    @CCD(label = "Interpreter details", access = {AdmofficerSystemCrudAccess.class})
    private List<ListValue<InterpreterDetails>> interpreterDetails;

    @CCD(
            label = "![]()\r\n"
                            + "## Interpreter details",
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String interpreterDetailsTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Interpreter booking status",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudAccess.class}
    )
    private String interpreterBookingStatusTitle;

    @CCD(
            label = "The booking status of interpreters can be managed here. After updating an interpreters status you must update the hearing via the Hearings tab. This information will then be visible in List Assist.",
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudAccess.class}
    )
    private String interpreterBookingStatusDescription;

    @CCD(
            label = "Add the hearing details below.",
            typeOverride = FieldType.Label,
            access = {SystemCrudAdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String listCaseHearingTitle;

    @CCD(
            label = "Listing event",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "listingEventList",
            access = {AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private ListingEventList listingEvent;

    @CCD(
            label = "Location",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingLocationList",
            access = {AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private HearingLocationList listingLocation;

    @CCD(label = "Listing location", typeOverride = FieldType.DynamicList, access = {AdmofficerSystemCrudHomeofficebailIacjudgeLegalrepSolicitorRAccess.class})
    private String refDataListingLocation;

    @CCD(label = "Will the hearing be held remotely?", access = {AdmofficerSystemCrudHomeofficebailIacjudgeLegalrepSolicitorRAccess.class})
    private YesOrNo isRemoteHearing;

    @CCD(label = "Length in minutes", access = {AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private Integer listingHearingDuration;

    @CCD(label = "Date and time", access = {AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private LocalDateTime listingHearingDate;

    @CCD(
            label = "Ref data listing location details",
            access = {AdmofficerSystemCrudHomeofficebailIacjudgeLegalrepSolicitorRAccess.class}
    )
    private RefDataListingLocationDetail refDataListingLocationDetail;

    @CCD(label = "Hearing documents", access = {AdmofficerSystemCrudHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class})
    private List<ListValue<DocumentWithMetadata>> hearingDocuments;

    @CCD(
            label = "Current Hearing ID",
            access = {SystemCrudAdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private String currentHearingId;

    @CCD(
            label = "List of hearing IDs associated with case",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text",
            access = {SystemCrudAdmofficerCruHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorRAccess.class}
    )
    private List<ListValue<String>> hearingIdList;

    @CCD(
            label = "List of hearing IDs and decisions associated with case",
            access = {SystemCrudAdmofficerIacjudgeCruHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class}
    )
    private List<ListValue<HearingDecision>> hearingDecisionList;

    @CCD(label = "Is the applicant detained under the Illegal Migration Act 2023?", access = {SystemCrudAccess.class})
    private YesOrNo adminSelectImaStatus;

    @CCD(label = "Is the applicant detained under the Illegal Migration Act 2023?", access = {SystemCrudAccess.class})
    private YesOrNo hoSelectImaStatus;

    @CCD(label = "Detained under Illegal Migration Act 2023", access = {SystemCrudAccess.class})
    private YesOrNo adminHasImaStatus;

    @CCD(label = "Detained under Illegal Migration Act 2023", access = {SystemCrudAccess.class})
    private YesOrNo hoHasImaStatus;

    @CCD(label = "Feature toggle field to be populated in the backend", access = {SystemCrudAccess.class})
    private YesOrNo isImaEnabled;

    @CCD(
            label = "Bails location ref data feature flag value",
            access = {AdmofficerSystemCrudHomeofficebailIacjudgeIaspecificaccessadminprofileIaspecificaccessctscprofileIaspecificaccessjudgeprofileIaspecificaccesslegalopsprofileLegalrepSolicitorRAccess.class}
    )
    private YesOrNo isBailsLocationReferenceDataEnabled;

    @CCD(
            label = "Hearing centre",
            typeOverride = FieldType.DynamicList,
            access = {SystemCrudAdmofficerHomeofficebailLegalrepSolicitorCruIacjudgeRAccess.class}
    )
    private String hearingCentreRefData;

    @CCD(label = " ", access = {SystemCrudAdmofficerHomeofficebailLegalrepSolicitorCruIacjudgeRAccess.class})
    private String selectedHearingCentreRefData;

    @CCD(label = " ", access = {CaseflagsSystemCrudAdmofficerRAccess.class})
    private YesOrNo hasActiveInterpreterFlag;

    @CCD(label = " ", access = {AdmofficerIacjudgeSystemCrudHomeofficebailHomeofficepouLegalrepSolicitorRAccess.class})
    private YesOrNo hasCaseBeenForcedToHearing;

    @CCD(
            label = "Reasons to force the case progression",
            typeOverride = FieldType.TextArea,
            access = {AdmofficerIacjudgeSystemCrudAccess.class}
    )
    private String reasonToForceCaseToHearing;

    @CCD(
            label = "#### Files should be:\r\n"
                            + "- Files must be no more than 500Mb in size\r\n"
                            + "- You can upload mp3 files\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, hearing-recording -JSmith.mp3\r\n"
                            + "\r\n",
            typeOverride = FieldType.Label,
            access = {AdmofficerIacjudgeCrudAccess.class}
    )
    private String uploadHearingRecordingTitle;

    @CCD(label = "Hearing recording document", access = {AdmofficerIacjudgeSystemCrudAccess.class})
    private List<ListValue<HearingRecordingDocument>> hearingRecordingDocuments;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerHomeofficebailHomeofficepouLegalrepSolicitorSystemCrudAccess.class}
    )
    private LocalDate lastEditApplicationNotificationDate;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label,
            access = {NoGrantsAccess.class})
    private String notificationsTabTitle;

    @CCD(label = "Notifications", searchable = false, access = {AdmofficerSystemCrudIacjudgeRAccess.class})
    private List<ListValue<Notification>> notifications;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private LocalDate notificationStoreScheduleDate;

    @CCD(
            label = " ",
            access = {AdmofficerCaseworkeriabailsIacjudgeLegalrepSolicitorSystemCrudGSprofileHomeofficebailHomeofficepouRAccess.class}
    )
    private String caseNameHmctsInternal;

    @JsonProperty("SearchCriteria")
    @CCD(
            label = " ",
            access = {AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudGSprofileRAccess.class}
    )
    private SearchCriteria searchCriteria;

    @CCD(
            label = " ",
            access = {AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudAccess.class}
    )
    private List<ListValue<Parties>> searchParties;

    @CCD(
            label = "Case management location",
            searchable = false,
            access = {AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudGSprofileRAccess.class}
    )
    private CaseManagementLocation caseManagementLocation;

    @CCD(
            label = "Case management location",
            access = {AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudGSprofileRAccess.class}
    )
    private CaseManagementLocationRefData caseManagementLocationRefData;

    @CCD(
            label = "Case management category",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {AdmofficerCaseworkeriabailsHomeofficebailHomeofficepouIacjudgeLegalrepSolicitorSystemCrudGSprofileRAccess.class}
    )
    private String caseManagementCategory;

    @CCD(
            label = "Staff Location",
            searchable = false,
            access = {CaseworkeriabailsHomeofficebailHomeofficepouSystemCrudAdmofficerLegalrepSolicitorCruAccess.class}
    )
    private String staffLocation;

    @CCD(
            label = "Staff Location Id",
            searchable = false,
            access = {CaseworkeriabailsSystemCrudAdmofficerHomeofficebailHomeofficepouLegalrepSolicitorRuAccess.class}
    )
    private String staffLocationId;

    @CCD(
            label = " ",
            searchable = false,
            access = {SystemCrudCaseofficerCaseworkeriabailsTtlManagerCruCitizenLegalrepSolicitorRAccess.class}
    )
    private TTL TTL;
}
