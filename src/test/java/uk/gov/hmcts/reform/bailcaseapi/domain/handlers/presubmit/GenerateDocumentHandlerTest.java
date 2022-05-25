package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DocumentGenerator;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class GenerateDocumentHandlerTest {

    @Mock
    private DocumentGenerator<BailCase> documentGenerator;

    @Mock
    private Callback<BailCase> callback;

    private GenerateDocumentHandler generateDocumentHandler;

    @BeforeEach
    public void setUp() {
        generateDocumentHandler = new GenerateDocumentHandler(
            documentGenerator
        );
        ReflectionTestUtils.setField(generateDocumentHandler, "isDocumentGenerationEnabled", true);
    }

    @Test
    public void should_only_handle_valid_event_state() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage: PreSubmitCallbackStage.values()) {
                boolean canHandle = generateDocumentHandler.canHandle(stage, callback);
                if (stage.equals(PreSubmitCallbackStage.ABOUT_TO_SUBMIT) && Arrays.asList(
                    Event.SUBMIT_APPLICATION,
                    Event.RECORD_THE_DECISION,
                    Event.END_APPLICATION
                ).contains(event)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_handle() {
        assertThatThrownBy(() -> generateDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> generateDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    public void should_throw_if_null_args() {
        assertThatThrownBy(() -> generateDocumentHandler.canHandle(
            null,
            callback
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> generateDocumentHandler.canHandle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            null
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> generateDocumentHandler.handle(
            null,
            callback
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> generateDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            null
        ))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_handle_generate_document_update_bailcase() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }

    @Test
    public void should_handle_generate_document_signed_decision_notice_upload() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }

    @Test
    public void should_handle_generate_document_end_application() {
        BailCase expectedBailCase = mock(BailCase.class);
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        when(documentGenerator.generate(callback)).thenReturn(expectedBailCase);

        PreSubmitCallbackResponse response = generateDocumentHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(expectedBailCase, response.getData());
        verify(documentGenerator, times(1)).generate(callback);
    }
}
