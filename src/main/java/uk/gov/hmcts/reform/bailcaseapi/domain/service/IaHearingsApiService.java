package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseCallbackApiDelegator;

@Service
public class IaHearingsApiService {

    private final BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    private final String hearingsApiEndpoint;
    private final String aboutToStartPath;
    private final String midEventPath;
    private final String aboutToSubmitPath;

    public IaHearingsApiService(
        BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator,
        @Value("${hearingsApi.endpoint}") String hearingsApiEndpoint,
        @Value("${hearingsApi.aboutToStartPath}") String aboutToStartPath,
        @Value("${hearingsApi.midEventPath}") String midEventPath,
        @Value("${hearingsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.bailCaseCallbackApiDelegator = bailCaseCallbackApiDelegator;
        this.hearingsApiEndpoint = hearingsApiEndpoint;
        this.aboutToStartPath = aboutToStartPath;
        this.midEventPath = midEventPath;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public BailCase aboutToStart(Callback<BailCase> callback) {
        return bailCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath);
    }

    public BailCase midEvent(Callback<BailCase> callback) {
        return bailCaseCallbackApiDelegator.delegate(
            callback,
            hearingsApiEndpoint + midEventPath + "?pageId=" + callback.getPageId()
        );
    }

    public BailCase aboutToSubmit(Callback<BailCase> callback) {
        return bailCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToSubmitPath);
    }
}
