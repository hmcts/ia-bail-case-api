package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.AGREES_TO_BOUND_BY_FINANCIAL_COND;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETENTION_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GENDER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_HAS_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_HAS_MOBILE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_ACTIVITIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_APPEARANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_ELECTRONIC_MONITORING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_OTHER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_REPORTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_RESIDENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DISABILITY_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_COND_AMOUNT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.GROUNDS_FOR_BAIL_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NO_TRANSFER_BAIL_MANAGEMENT_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.REASONS_JUDGE_IS_MINDED_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.REASON_FOR_REFUSAL_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SECRETARY_OF_STATE_REFUSAL_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SIGNED_DECISION_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_OPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_REFUSAL_REASON;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING_YESNO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@Service
public class ShowPreviousApplicationService {


    public String getDecisionLabel(BailCase previousBailCase, Value selectedApplicationValue) {
        String decisionDetails = selectedApplicationValue.getLabel().contains("Ended")
            ? populateEndDetails(previousBailCase) : populateDecisionDetails(previousBailCase);

        return "|Decision details||\n|--------|--------|\n"
            + decisionDetails;
    }

    public String getDocumentsLabel(BailCase previousBailCase) {
        if (previousBailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA).isPresent()
            || previousBailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA).isPresent()
            || previousBailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA).isPresent()
            || previousBailCase.read(SIGNED_DECISION_DOCUMENTS_WITH_METADATA).isPresent()
        ) {
            return "|Documents||\n|--------|--------|\n"
                + getApplicantDocumentsDetails(previousBailCase)
                + getTribunalDocumentsDetails(previousBailCase)
                + getHODocumentsDetails(previousBailCase)
                + getDecisionDocumentsDetails(previousBailCase);
        }
        return null;
    }

    public String getDirectionLabel(BailCase previousBailCase) {
        Optional<List<IdValue<Direction>>> mayBeDirections = previousBailCase.read(DIRECTIONS);
        AtomicInteger index = new AtomicInteger(0);
        if (mayBeDirections.isPresent()) {
            String label = "|Directions||\n|--------|--------|\n|Directions|";
            List<IdValue<Direction>> directions = mayBeDirections.get();
            String directionDetails = directions
                .stream()
                .map((idValue) -> "Directions " + index.incrementAndGet()
                    + "<br>*Explanation:* " + idValue.getValue().getSendDirectionDescription()
                    + "<br>*Parties:* " + idValue.getValue().getSendDirectionList()
                    + "<br>*Date due:* " + formatDate(idValue.getValue().getDateOfCompliance())
                    + "<br>*Date sent:* " + formatDate(idValue.getValue().getDateSent())
                    + "<br>")
                .collect(Collectors.joining("<br>"));
            return label + directionDetails + "|";
        }
        return null;
    }

    public String getCaseNoteLabel(BailCase previousBailCase) {
        Optional<List<IdValue<CaseNote>>> mayBeCaseNotes = previousBailCase.read(CASE_NOTES);
        AtomicInteger index = new AtomicInteger(0);
        if (mayBeCaseNotes.isPresent()) {
            String label = "|Case notes||\n|--------|--------|\n"
                + getColumnTitle("Case notes", 84);
            List<IdValue<CaseNote>> caseNote = mayBeCaseNotes.get();
            String caseNoteDetails = caseNote
                .stream()
                .map((idValue) -> "Case notes " + index.incrementAndGet()
                    + "<br>*Subject:* " + idValue.getValue().getCaseNoteSubject()
                    + "<br>*Case note:* " + idValue.getValue().getCaseNoteDescription()
                    + "<br>*Document:* " + createDocumentLabel(idValue.getValue().getCaseNoteDocument())
                    + "<br>*Added by:* " + idValue.getValue().getUser()
                    + "<br>*Date added:* " + formatDate(idValue.getValue().getDateAdded())
                    + "<br>")
                .collect(Collectors.joining("<br>"));
            return label + caseNoteDetails;
        }
        return null;
    }

    public String getHearingReqDetails(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder("|Hearing requirements||\n|--------|--------|\n");
        stringBuilder.append("|Interpreter|")
            .append(previousBailCase.read(INTERPRETER_YESNO, YesOrNo.class).orElse(YesOrNo.NO))
            .append("|\n");

        String interpreterLang = "";
        if (previousBailCase.read(INTERPRETER_YESNO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            Optional<List<IdValue<InterpreterLanguage>>> mayBeInterpreterLangs =
                previousBailCase.read(INTERPRETER_LANGUAGES);
            interpreterLang = mayBeInterpreterLangs.orElseThrow(getErrorThrowable(INTERPRETER_LANGUAGES)).stream()
                .map(idValue -> idValue.getValue().getLanguage()
                    + " (" + idValue.getValue().getLanguageDialect() + ")")
                .collect(Collectors.joining("<br>"));
            stringBuilder.append("|Language|")
                .append(interpreterLang)
                .append("|\n");
        }

        stringBuilder.append("|Disability|")
            .append(previousBailCase.read(DISABILITY_YESNO, YesOrNo.class).orElse(YesOrNo.NO));
        if (previousBailCase.read(DISABILITY_YESNO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            stringBuilder.append("|\n|Explain any special <br>arrangements needed for the <br>hearing|")
                .append(previousBailCase.read(APPLICANT_DISABILITY_DETAILS).orElse(""));
        }
        stringBuilder.append("|\n");

        stringBuilder.append("|Video hearing|")
            .append(previousBailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class).orElse(YesOrNo.NO));
        if (previousBailCase.read(DISABILITY_YESNO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            stringBuilder
                .append("|\n|Explain why the applicant <br>would not be able to join the <br>hearing by video link|")
                .append(previousBailCase.read(VIDEO_HEARING_DETAILS).orElse(""));
        }
        stringBuilder.append("|\n");

        return stringBuilder.toString();
    }

    public String getSubmissionDetails(BailCase previousBailCase) {
        return "|Submission||\n|-------------|-------------:|\n|Application submitted by|"
            + previousBailCase.read(APPLICATION_SUBMITTED_BY)
            .orElseThrow(getErrorThrowable(APPLICATION_SUBMITTED_BY))
            + "|/n";
    }

    public String getPersonalInfoLabel(BailCase previousBailCase) {
        String nationalities = getNationalities(
            previousBailCase.read(APPLICANT_NATIONALITIES));

        StringBuilder stringBuilder = new StringBuilder(
            "|Personal information||\n|--------|--------|\n"
        );
        stringBuilder
            .append("|Given names|")
            .append(previousBailCase.read(APPLICANT_GIVEN_NAMES)
                        .orElseThrow(getErrorThrowable(APPLICANT_GIVEN_NAMES)))
            .append("|\n|Family name|")
            .append(previousBailCase.read(APPLICANT_FAMILY_NAME)
                        .orElseThrow(getErrorThrowable(APPLICANT_FAMILY_NAME)))
            .append("|\n|Date of birth")
            .append(getColumnTitle(formatDate(
                String.valueOf(previousBailCase.read(APPLICANT_DOB)
                                   .orElseThrow(getErrorThrowable(APPLICANT_DOB)))), 18))
            .append("\n|Gender|")
            .append(previousBailCase.read(APPLICANT_GENDER)
                        .orElseThrow(getErrorThrowable(APPLICANT_GENDER)))
            .append("|\n|Nationalities|")
            .append(nationalities)
            .append("\n|Mobile phone|").append(previousBailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class)
                                                   .orElse(YesOrNo.NO)).append("|\n");

        if (previousBailCase.read(APPLICANT_HAS_MOBILE).orElse(YesOrNo.NO) == YesOrNo.YES) {
            stringBuilder
                .append("|Mobile phone number|")
                .append(previousBailCase.read(APPLICANT_MOBILE_NUMBER)
                            .orElseThrow(getErrorThrowable(APPLICANT_MOBILE_NUMBER)))
                .append("|\n");
        }

        return stringBuilder.toString();
    }

    public String getApplicantInfo(BailCase previousBailCase) {

        StringBuilder stringBuilder = new StringBuilder(
            "|Applicant information||\n|--------|--------|\n"
        );
        stringBuilder
            .append(getColumnTitle("Home office reference", 85))
            .append(previousBailCase.read(HOME_OFFICE_REFERENCE_NUMBER)
                        .orElseThrow(getErrorThrowable(HOME_OFFICE_REFERENCE_NUMBER)))
            .append("|\n");

        String applicantDetainedLoc = previousBailCase.read(APPLICANT_DETENTION_LOCATION, String.class)
            .orElseThrow(getErrorThrowable(APPLICANT_DETENTION_LOCATION));

        if (applicantDetainedLoc.equals("prison")) {
            stringBuilder
                .append("|Prison|Yes|\n|NOMS number|")
                .append(previousBailCase.read(APPLICANT_PRISON_DETAILS)
                            .orElseThrow(getErrorThrowable(APPLICANT_PRISON_DETAILS)))
                .append("|\n|Name of prison|HM Prison")
                .append(previousBailCase.read(PRISON_NAME)
                            .orElseThrow(getErrorThrowable(PRISON_NAME)))
                .append("|\n");
        } else if (applicantDetainedLoc.equals("immigrationRemovalCentre")) {
            stringBuilder
                .append("|Immigration Removal Centre|Yes|\n|Name of Immigration Removal Centre|")
                .append(previousBailCase.read(IRC_NAME)
                            .orElseThrow(getErrorThrowable(IRC_NAME)))
                .append("|\n");
        }

        if (previousBailCase.read(APPLICANT_ARRIVAL_IN_UK).isPresent()) {
            stringBuilder.append("|Arrival date into the UK|")
                .append(formatDate(previousBailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)
                                       .orElseThrow(getErrorThrowable(APPLICANT_ARRIVAL_IN_UK))))
                .append("|\n");
        }

        stringBuilder.append("|Pending appeal hearing|")
            .append(previousBailCase.read(HAS_APPEAL_HEARING_PENDING).orElse(YesOrNo.NO))
            .append("|\n");

        if (previousBailCase.read(HAS_APPEAL_HEARING_PENDING)
            .orElse(YesOrNo.NO.toString()).equals(YesOrNo.YES.toString())) {
            stringBuilder
                .append("|Pending appeal reference|")
                .append(previousBailCase.read(APPEAL_REFERENCE_NUMBER)
                            .orElseThrow(getErrorThrowable(APPEAL_REFERENCE_NUMBER)))
                .append("|\n");
        }

        stringBuilder.append("|Address if bail granted|")
            .append(previousBailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO))
            .append("|\n");

        if (previousBailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            stringBuilder.append(fetchAddress(previousBailCase.read(APPLICANT_ADDRESS, AddressUK.class)));
        }

        return stringBuilder.toString();
    }

    public String getFinancialCondCommitment(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder("|Financial condition commitment||\n|--------|--------|\n");
        stringBuilder
            .append("|Financial condition")
            .append(getColumnTitle(String.valueOf(
                previousBailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class).orElse(YesOrNo.NO)), 36))
            .append("\n");
        if (previousBailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            stringBuilder
                .append("|Financial condition amount|")
                .append(previousBailCase.read(FINANCIAL_COND_AMOUNT)
                            .orElseThrow(getErrorThrowable(FINANCIAL_COND_AMOUNT)))
                .append("|\n");
        }
        return stringBuilder.toString();
    }

    @Nullable
    public String getFinancialConditionSupporterLabel(
        BailCase previousBailCase,
        BailCaseFieldDefinition hasFinancialCondSupporter,
        BailCaseFieldDefinition supporterGivenNames,
        BailCaseFieldDefinition supporterFamilyNames,
        BailCaseFieldDefinition supporterAddressDetails,
        BailCaseFieldDefinition supporterTelephoneNumber,
        BailCaseFieldDefinition supporterMobileNumber,
        BailCaseFieldDefinition supporterEmailAddress,
        BailCaseFieldDefinition supporterDOB,
        BailCaseFieldDefinition supporterRelation,
        BailCaseFieldDefinition supporterOccupation,
        BailCaseFieldDefinition supporterImmigration,
        BailCaseFieldDefinition supporterNationality,
        BailCaseFieldDefinition supporterHasPassport,
        BailCaseFieldDefinition supporterPassport,
        BailCaseFieldDefinition financialAmountSupporterUndertakes
    ) {
        if (previousBailCase.read(hasFinancialCondSupporter, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {

            StringBuilder stringBuilder =
                new StringBuilder("|Financial condition supporter 1||\n|--------|--------|\n");
            stringBuilder.append("|Financial condition supporter|Yes|\n")
                .append("|Given names|")
                .append(previousBailCase.read(supporterGivenNames)
                            .orElseThrow(getErrorThrowable(supporterGivenNames)))
                .append("|\n|Family name|")
                .append(previousBailCase.read(supporterFamilyNames)
                            .orElseThrow(getErrorThrowable(supporterFamilyNames)))
                .append("|\n")
                .append(fetchAddress(previousBailCase.read(supporterAddressDetails, AddressUK.class)));

            if (!previousBailCase.read(supporterTelephoneNumber, String.class).orElse("").isBlank()) {
                stringBuilder.append("|Telephone number|")
                    .append(previousBailCase.read(supporterTelephoneNumber, String.class)
                                .orElseThrow(getErrorThrowable(supporterTelephoneNumber)))
                    .append("|\n");
            }

            if (!previousBailCase.read(supporterMobileNumber, String.class).orElse("").isBlank()) {
                stringBuilder.append("|Mobile number|")
                    .append(previousBailCase.read(supporterMobileNumber, String.class)
                                .orElseThrow(getErrorThrowable(supporterMobileNumber)))
                    .append("|\n");
            }

            if (!previousBailCase.read(supporterEmailAddress, String.class).orElse("").isBlank()) {
                stringBuilder.append("|Email address|")
                    .append(previousBailCase.read(supporterEmailAddress, String.class)
                                .orElseThrow(getErrorThrowable(supporterEmailAddress)))
                    .append("|\n");
            }
            stringBuilder.append("|Date of birth|")
                .append(formatDate(previousBailCase.read(supporterDOB, String.class)
                                       .orElseThrow(getErrorThrowable(supporterDOB))))
                .append("|\n");

            stringBuilder.append("|Relationship to the applicant|")
                .append(previousBailCase.read(supporterRelation)
                            .orElseThrow(getErrorThrowable(supporterRelation)))
                .append("|\n");

            stringBuilder.append("|Occupation|")
                .append(previousBailCase.read(supporterOccupation)
                            .orElseThrow(getErrorThrowable(supporterOccupation)))
                .append("|\n");

            stringBuilder.append("|Immigration status|")
                .append(previousBailCase.read(supporterImmigration)
                            .orElseThrow(getErrorThrowable(supporterImmigration)))
                .append("|\n");

            stringBuilder.append("|Nationalities|")
                .append(getNationalities(previousBailCase.read(supporterNationality)))
                .append("|\n");

            stringBuilder.append("|Passport number|")
                .append(previousBailCase.read(supporterHasPassport).orElse(YesOrNo.NO))
                .append("|\n");

            if (previousBailCase.read(supporterHasPassport).orElse(YesOrNo.NO).equals("Yes")) {
                stringBuilder.append("|Passport number|")
                    .append(previousBailCase.read(supporterPassport)
                                .orElseThrow(getErrorThrowable(supporterPassport)))
                    .append("|\n");
            }

            stringBuilder.append("|Financial condition amount (£)|")
                .append(previousBailCase.read(financialAmountSupporterUndertakes)
                            .orElseThrow(getErrorThrowable(financialAmountSupporterUndertakes)))
                .append("|\n");

            return stringBuilder.toString();
        }
        return null;
    }

    public String getGroundsForBail(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder("|Grounds for bail||\n|--------|--------|\n");
        stringBuilder
            .append("|Bail Grounds|")
            .append(previousBailCase.read(GROUNDS_FOR_BAIL_REASONS, String.class)
                        .orElseThrow(getErrorThrowable(GROUNDS_FOR_BAIL_REASONS))
                        .replaceAll("[\\n]", "<br>"))
            .append("|\n");

        stringBuilder
            .append("|Transfer bail management|")
            .append(previousBailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class).orElse(YesOrNo.YES))
            .append("|\n");

        if (previousBailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class).orElse(YesOrNo.YES) == YesOrNo.NO) {
            stringBuilder
                .append("|Reasons applicant does not consent to bail transfer|")
                .append(previousBailCase.read(NO_TRANSFER_BAIL_MANAGEMENT_REASONS, String.class)
                            .orElseThrow(getErrorThrowable(NO_TRANSFER_BAIL_MANAGEMENT_REASONS))
                            .replaceAll("[\\n]", "<br>"))
                .append("|\n");
        }
        return stringBuilder.toString();
    }

    public String getLegalRepDetails(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder();
        if (previousBailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            stringBuilder.append("|Legal representative||\n|--------|--------|\n");
            stringBuilder.append("|Company|")
                .append(previousBailCase.read(LEGAL_REP_COMPANY)
                            .orElseThrow(getErrorThrowable(LEGAL_REP_COMPANY)))
                .append("|\n|Name|")
                .append(previousBailCase.read(LEGAL_REP_NAME)
                            .orElseThrow(getErrorThrowable(LEGAL_REP_NAME)))
                .append("|\n|Email address|")
                .append(previousBailCase.read(LEGAL_REP_EMAIL_ADDRESS)
                            .orElseThrow(getErrorThrowable(LEGAL_REP_EMAIL_ADDRESS)))
                .append("|\n|Phone number|")
                .append(previousBailCase.read(LEGAL_REP_PHONE)
                            .orElseThrow(getErrorThrowable(LEGAL_REP_PHONE)))
                .append("|\n|Reference|")
                .append(previousBailCase.read(LEGAL_REP_REFERENCE)
                            .orElseThrow(getErrorThrowable(LEGAL_REP_REFERENCE)))
                .append("|\n");
        }
        return stringBuilder.toString();
    }

    private String getColumnTitle(String title, int padLen) {
        return "|<pre>" + StringUtils.rightPad(title, padLen) + "</pre>|";
    }

    private String populateDecisionDetails(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder();
        String conditions = populateConditions(previousBailCase).replaceAll("[\\n]", "<br>");
        stringBuilder
            .append(getColumnTitle("Decision", 95))
            .append(formatDecisionStr(previousBailCase.read(RECORD_DECISION_TYPE, String.class).orElse("")))
            .append("|\n|Decision date|")
            .append(formatDate(previousBailCase.read(DECISION_DETAILS_DATE, String.class)
                                   .orElseThrow(getErrorThrowable(DECISION_DETAILS_DATE))));

        boolean isRefused = previousBailCase.read(RECORD_DECISION_TYPE, String.class)
            .orElse("")
            .equalsIgnoreCase("refused");

        if (isRefused) {
            boolean isMindedToGrant = previousBailCase.read(RECORD_THE_DECISION_LIST, String.class)
                .orElse("")
                .equalsIgnoreCase("mindedToGrant");

            if (isMindedToGrant) {
                stringBuilder.append("|\n|Reasons judge minded to grant bail|")
                    .append(previousBailCase.read(REASONS_JUDGE_IS_MINDED_DETAILS, String.class)
                                         .orElseThrow(getErrorThrowable(REASONS_JUDGE_IS_MINDED_DETAILS)));
            }

            stringBuilder.append("|\n|Reasons for refusal|")
                .append(previousBailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)
                                     .orElse(""))
                .append(previousBailCase.read(REASON_FOR_REFUSAL_DETAILS, String.class).orElse(""))
                .append(previousBailCase.read(TRIBUNAL_REFUSAL_REASON, String.class).orElse(""));
        } else {
            stringBuilder.append((conditions.isEmpty() ? "|" : "|\n|Conditions|" + conditions + "|"));
        }

        return stringBuilder.toString();
    }

    private String populateEndDetails(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append(getColumnTitle("Outcome", 95))
            .append(formatDecisionStr(previousBailCase.read(END_APPLICATION_OUTCOME, String.class)
                                          .orElseThrow(getErrorThrowable(END_APPLICATION_OUTCOME))))
            .append("|\n|End date|")
            .append(formatDate(previousBailCase.read(END_APPLICATION_DATE, String.class)
                                   .orElseThrow(getErrorThrowable(END_APPLICATION_DATE))))
            .append("||\n|End reasons|")
            .append(previousBailCase.read(END_APPLICATION_REASONS)
                        .orElseThrow(getErrorThrowable(END_APPLICATION_REASONS)))
            .append("|");
        return stringBuilder.toString();
    }

    /**
     * Get conditions if present in String format separated by <br>
     * as /n breaks the formatting with ExUI.
     */
    private String populateConditions(BailCase previousBailCase) {
        StringBuilder stringBuilder = new StringBuilder();

        if (previousBailCase.read(CONDITION_APPEARANCE).isPresent()) {
            stringBuilder.append("*Appearance*\n")
                .append(previousBailCase.read((CONDITION_APPEARANCE))
                            .orElseThrow(getErrorThrowable(CONDITION_APPEARANCE)));
        }
        if (previousBailCase.read(CONDITION_ACTIVITIES).isPresent()) {
            stringBuilder.append("\n\n *Activities*\n")
                .append(previousBailCase.read((CONDITION_ACTIVITIES))
                            .orElseThrow(getErrorThrowable(CONDITION_ACTIVITIES)));
        }
        if (previousBailCase.read(CONDITION_RESIDENCE).isPresent()) {
            stringBuilder.append("\n\n *Residence*\n")
                .append(previousBailCase.read((CONDITION_RESIDENCE))
                            .orElseThrow(getErrorThrowable(CONDITION_RESIDENCE)));
        }
        if (previousBailCase.read(CONDITION_REPORTING).isPresent()) {
            stringBuilder.append("\n\n *Reporting*\n")
                .append(previousBailCase.read((CONDITION_REPORTING))
                            .orElseThrow(getErrorThrowable(CONDITION_REPORTING)));
        }
        if (previousBailCase.read(CONDITION_ELECTRONIC_MONITORING).isPresent()) {
            stringBuilder.append("\n\n *Electronic monitoring*\n")
                .append(previousBailCase.read((CONDITION_ELECTRONIC_MONITORING))
                            .orElseThrow(getErrorThrowable(CONDITION_ELECTRONIC_MONITORING)));
        }
        if (previousBailCase.read(CONDITION_OTHER).isPresent()) {
            stringBuilder.append("\n\n*Other*\n")
                .append(previousBailCase.read((CONDITION_OTHER))
                            .orElseThrow(getErrorThrowable(CONDITION_OTHER)));
        }
        return stringBuilder.toString();
    }

    private String formatDecisionStr(String decision) {
        return StringUtils
            .capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(decision), StringUtils.SPACE));
    }

    private String createDocumentLabel(Document document) {
        return "<a href=\""
            + document.getDocumentBinaryUrl().substring(document.getDocumentBinaryUrl().indexOf("/documents/"))
            + "\">" + document.getDocumentFilename() + "</a>";
    }

    private Supplier<RequiredFieldMissingException> getErrorThrowable(BailCaseFieldDefinition field) {
        return () -> new RequiredFieldMissingException("Missing field: " + field.value());
    }

    /**
     * Get list of nationalities separated by <br>.
     */
    private String getNationalities(Optional<List<IdValue<NationalityFieldValue>>> mayBeNationalities) {
        List<IdValue<NationalityFieldValue>> nationalityList =
            mayBeNationalities.orElse(Collections.emptyList());

        return nationalityList
            .stream()
            .map(idValue -> idValue.getValue().getCode())
            .collect(Collectors.joining("<br>"));
    }

    /**
     * Converts address from object to String(with<br>) format.
     */
    private String fetchAddress(Optional<AddressUK> optionalAddressUK) {
        StringBuilder stringBuilder = new StringBuilder();
        if (optionalAddressUK.isPresent()) {
            stringBuilder.append("|Address|");
            AddressUK applicantAddress = optionalAddressUK.get();

            if (applicantAddress.getAddressLine1().isPresent()
                && !applicantAddress.getAddressLine1().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getAddressLine1().orElseThrow()).append("<br>");
            }

            if (applicantAddress.getAddressLine2().isPresent()
                && !applicantAddress.getAddressLine2().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getAddressLine2().orElseThrow()).append("<br>");
            }

            if (applicantAddress.getAddressLine3().isPresent()
                && !applicantAddress.getAddressLine3().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getAddressLine3().orElseThrow()).append("<br>");
            }

            if (applicantAddress.getPostTown().isPresent()
                && !applicantAddress.getPostTown().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getPostTown().orElseThrow()).append("<br>");
            }

            if (applicantAddress.getCounty().isPresent()
                && !applicantAddress.getCounty().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getCounty().orElseThrow()).append("<br>");
            }

            if (applicantAddress.getPostCode().isPresent()
                && !applicantAddress.getPostCode().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getPostCode().orElseThrow()).append("<br>");
            }

            if (applicantAddress.getCountry().isPresent()
                && !applicantAddress.getCountry().orElseThrow().isBlank()) {
                stringBuilder.append(applicantAddress.getCountry().orElseThrow()).append("<br>");
            }
            stringBuilder.append("|\n");
        }

        return stringBuilder.toString();
    }

    private String getApplicantDocumentsDetails(BailCase previousBailCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> mayBeApplicantDocs = previousBailCase
            .read(APPLICANT_DOCUMENTS_WITH_METADATA);
        return mayBeApplicantDocs.isEmpty()
            ? "" : getDetailsForGivenCollection(mayBeApplicantDocs, "Applicant") + "|\n";
    }

    private String getTribunalDocumentsDetails(BailCase previousBailCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> mayBeTribunalDocs = previousBailCase
            .read(TRIBUNAL_DOCUMENTS_WITH_METADATA);
        return mayBeTribunalDocs.isEmpty()
            ? "" : getDetailsForGivenCollection(mayBeTribunalDocs, "Tribunal") + "|\n";
    }


    private String getDecisionDocumentsDetails(BailCase previousBailCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> mayBeUnsignedDecisionDoc = previousBailCase
            .read(SIGNED_DECISION_DOCUMENTS_WITH_METADATA);
        return mayBeUnsignedDecisionDoc.isEmpty()
            ? "" : getDetailsForGivenCollection(mayBeUnsignedDecisionDoc, "Decision") + "|\n";
    }

    private String getHODocumentsDetails(BailCase previousBailCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> mayBeHODocs = previousBailCase
            .read(HOME_OFFICE_DOCUMENTS_WITH_METADATA);
        return mayBeHODocs.isEmpty()
            ? "" : getDetailsForGivenCollection(mayBeHODocs, "Home Office") + "|\n";
    }

    /**
     * Helper method to generate details for every document
     * Including heading (like Applicant document 1)
     * filename, description and date uploaded.
     */
    private String getDetailsForGivenCollection(Optional<List<IdValue<DocumentWithMetadata>>> mayBeDocs,
                                                String prefix) {
        StringBuilder stringBuilder = new StringBuilder(getColumnTitle(prefix + " documents", 84));
        AtomicInteger index = new AtomicInteger(0);
        mayBeDocs.orElseThrow().stream()
            .forEach((idValue) -> stringBuilder
                .append(prefix + " document " + index.incrementAndGet())
                .append("<br>")
                .append(createDocumentLabelWithMetadata(idValue.getValue())));

        return stringBuilder.toString();
    }

    /**
     * Document: Filename(with url).
     * Date uploaded: date.
     * Description: description.
     */
    private String createDocumentLabelWithMetadata(DocumentWithMetadata documentWithMetadata) {
        Document document = documentWithMetadata.getDocument();
        StringBuilder stringBuilder = new StringBuilder("*Document:* <a href=\"");
        stringBuilder
            .append(document.getDocumentBinaryUrl().substring(document.getDocumentBinaryUrl().indexOf("/documents/")))
            .append("\" target=\"_blank\">")
            .append(document.getDocumentFilename())
            .append("</a><br>*Date uploaded:* ")
            .append(formatDate(documentWithMetadata.getDateUploaded()))
            .append((documentWithMetadata.getDescription() == null || documentWithMetadata.getDescription().isEmpty()
                ? "<br><br>" : "<br>*Description:* " + documentWithMetadata.getDescription() + "<br><br>"));

        return stringBuilder.toString();
    }

    private String formatDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }
}
