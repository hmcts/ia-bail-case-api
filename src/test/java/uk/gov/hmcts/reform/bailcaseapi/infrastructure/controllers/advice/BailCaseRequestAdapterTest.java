package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpInputMessage;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
class BailCaseRequestAdapterTest {

    private static final long CASE_ID = 1234567890123456L;

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Mock
    private HttpInputMessage inputMessage;

    @Mock
    private RequestAttributes requestAttributes;

    private BailCaseRequestAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BailCaseRequestAdapter();
        RequestContextHolder.setRequestAttributes(requestAttributes);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    void supports_shouldReturnTrue() {
        assertTrue(adapter.supports(null, null, null));
    }

    @Test
    void afterBodyRead_shouldSetCaseIdInRequestAttributesAndMdc() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);

        Object result = adapter.afterBodyRead(callback, inputMessage, null, null, null);

        assertSame(callback, result);
        assertEquals(String.valueOf(CASE_ID), MDC.get(CorrelationIdFilter.CCD_CASE_ID_MDC_KEY));
    }

    @Test
    void afterBodyRead_shouldHandleClassCastException() {
        String notACallback = "not a callback";

        Object result = adapter.afterBodyRead(notACallback, inputMessage, null, null, null);

        assertSame(notACallback, result);
        // Should not throw, just log warning
    }
}
