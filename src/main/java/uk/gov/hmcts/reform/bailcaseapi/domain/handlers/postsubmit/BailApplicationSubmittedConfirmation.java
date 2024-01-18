package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@Slf4j
@Component
public class BailApplicationSubmittedConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    private final CcdCaseAssignment ccdCaseAssignment;
    private UserDetails userDetails;
    private UserDetailsHelper userDetailsHelper;
    private final FeatureToggler featureToggler;

    public BailApplicationSubmittedConfirmation(ProfessionalOrganisationRetriever professionalOrganisationRetriever,
                                                CcdCaseAssignment ccdCaseAssignment,
                                                UserDetails userDetails,
                                                UserDetailsHelper userDetailsHelper,
                                                FeatureToggler featureToggler) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
        this.ccdCaseAssignment = ccdCaseAssignment;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.SUBMIT_APPLICATION);
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
            "### What happens next\n\n"
            + "All parties will be notified that the application has been submitted."
        );

        postSubmitResponse.setConfirmationHeader("# You have submitted this application");

        if (bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class).isPresent()
            && callback.getEvent() == Event.SUBMIT_APPLICATION
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

        YesOrNo isImaFeatureFlagEnabled = featureToggler.getValue("ima-feature-flag", false) ? YES : NO;
        bailCase.write(IS_IMA_ENABLED, isImaFeatureFlagEnabled);

        return postSubmitResponse;
    }
}
