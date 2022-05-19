package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final AuthTokenGenerator serviceAuthorization;
    private final AccessTokenProvider accessTokenProvider;
    private final UserDetails userDetails;

    public CcdDataService(CcdDataApi ccdDataApi,
                          AuthTokenGenerator serviceAuthorization,
                          AccessTokenProvider accessTokenProvider,
                          UserDetails userDetails) {

        this.ccdDataApi = ccdDataApi;
        this.serviceAuthorization = serviceAuthorization;
        this.accessTokenProvider = accessTokenProvider;
        this.userDetails = userDetails;
    }

    private StartEventDetails getCase(
        String userToken, String s2sToken, String uid, String jurisdiction, String caseType, String caseId) {

        return ccdDataApi.startEvent(userToken, s2sToken, uid, jurisdiction, caseType,
                                     caseId, Event.MAKE_NEW_APPLICATION.toString());
    }

    private SubmitEventDetails startCaseCreation(
        String userToken, String s2sToken, String caseId, Map<String, Object> data,
        Map<String, Object> eventData, String eventToken, boolean ignoreWarning) {

        CaseDataContent request =
            new CaseDataContent(caseId, data, eventData, eventToken, ignoreWarning);

        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);

    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> data,
        Map<String, Object> eventData, String eventToken, boolean ignoreWarning) {

        CaseDataContent request =
            new CaseDataContent(caseId, data, eventData, eventToken, ignoreWarning);

        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);

    }

    private StartEventDetails createCase(String userToken, String s2sToken, String uid,
                                         String jurisdiction, String caseType, String eventId) {

        return ccdDataApi.startCaseCreation(userToken, s2sToken, uid, jurisdiction, caseType, eventId);
    }

    public StartEventDetails updatePaymentStatus() {

        String event = "startApplication";
//        String caseId = String.valueOf(caseMetaData.getCaseId());
        String jurisdiction = "IA";
        String caseType = "Bail";

        String userToken;
        String s2sToken;
        String uid;
        try {
            s2sToken = serviceAuthorization.generate();
            userToken = accessTokenProvider.getAccessToken();

            uid = userDetails.getAccessToken();
//            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        var ss = createCase(userToken, s2sToken, uid, jurisdiction, caseType, event);
        var ss2 = createCase(userToken, s2sToken, uid, jurisdiction, caseType, event);


//        // Get case details by Id
//        final StartEventDetails startEventDetails = getCase(userToken, s2sToken, uid, jurisdiction, caseType, caseId);
//        log.info("Case details found for the caseId: {}", caseId);
//
//        if (!isPaymentReferenceExists(startEventDetails.getCaseDetails().getCaseData(),
//                                      caseMetaData.getPaymentReference())) {
//
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                                              "Payment reference not found for the caseId: " + caseId);
//        }

//        // Update case payment status
//        Map<String, Object> caseData = new HashMap<>();
//        caseData.put(PAYMENT_STATUS.value(), caseMetaData.getPaymentStatus());
//
//        Map<String, Object> eventData = new HashMap<>();
//        eventData.put("id", caseMetaData.getEvent().toString());
//
//        SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, caseData, eventData,
//                                                            startEventDetails.getToken(), true);
//
//        log.info("Case payment status updated for the caseId: {}, Status: {}, Message: {}", caseId,
//                 submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

        return ss;
    }


//    private boolean isPaymentReferenceExists(BailCase bailCase, String reference) {
//
//        Optional<String> paymentReference = bailCase.read(PAYMENT_REFERENCE, String.class);
//
//        return paymentReference.isPresent() && paymentReference.get().equals(reference);
//    }
}
