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
        return callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {

        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        var eventToUse = bailCase.read(BailCaseFieldDefinition.NEW_REF_ID).orElse(null);

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You have created a new application");
        postSubmitResponse.setConfirmationBody(
            "#### The new application are available to view in the " +
                "["+eventToUse+"](/cases/case-details/"+eventToUse+")"
        );
        return postSubmitResponse;
    }
}
