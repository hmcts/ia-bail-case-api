package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@Slf4j
@Component
public class BailApplicationSavedConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    private final CcdCaseAssignment ccdCaseAssignment;
    private UserDetails userDetails;
    private UserDetailsHelper userDetailsHelper;

    public BailApplicationSavedConfirmation(ProfessionalOrganisationRetriever professionalOrganisationRetriever,
                                            CcdCaseAssignment ccdCaseAssignment,
                                            UserDetails userDetails,
                                            UserDetailsHelper userDetailsHelper) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
        this.ccdCaseAssignment = ccdCaseAssignment;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.START_APPLICATION);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationBody(
            "### Do this next\n\n"
                    + "Review and [edit the application](/case/IA/Bail/"
                    + callback.getCaseDetails().getId()
                    + "/trigger/editBailApplication) if necessary. [Submit the application](/case/IA/Bail/"
                    + callback.getCaseDetails().getId()
                    + "/trigger/submitApplication) when you’re ready."
        );

        postSubmitResponse.setConfirmationHeader("# You have saved this application");

        if (bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class).isPresent()
            && callback.getEvent() == Event.START_APPLICATION
            && userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE)) {

            final String organisationIdentifier =
                professionalOrganisationRetriever
                    .retrieve()
                    .getOrganisationIdentifier();

            log.info("PRD endpoint called for caseId [{}] orgId[{}]",
                     callback.getCaseDetails().getId(), organisationIdentifier);

            ccdCaseAssignment.assignAccessToCase(callback);
            ccdCaseAssignment.revokeAccessToCase(callback, organisationIdentifier);
        }

        return postSubmitResponse;
    }
}
