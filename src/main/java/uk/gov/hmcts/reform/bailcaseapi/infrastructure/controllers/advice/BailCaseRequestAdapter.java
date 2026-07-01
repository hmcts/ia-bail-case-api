package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers.advice;

import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@Slf4j
@ControllerAdvice
@SuppressWarnings("unchecked")
public class BailCaseRequestAdapter extends RequestBodyAdviceAdapter {

    public static final String CCD_CASE_ID_ATTRIBUTE = "CCDCaseId";

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        try {
            Callback<BailCase> callback = (Callback<BailCase>) body;
            CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
            String caseId = String.valueOf(caseDetails.getId());

            // Set in RequestAttributes for ErrorResponseBuilder
            RequestContextHolder.currentRequestAttributes()
                .setAttribute(CCD_CASE_ID_ATTRIBUTE, caseDetails.getId(), RequestAttributes.SCOPE_REQUEST);

            // Set in MDC for logging pattern
            MDC.put(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY, caseId);
        } catch (ClassCastException e) {
            log.warn("Unable to extract CCD Case ID from request body: {}", e.getMessage());
        }

        return body;
    }
}
