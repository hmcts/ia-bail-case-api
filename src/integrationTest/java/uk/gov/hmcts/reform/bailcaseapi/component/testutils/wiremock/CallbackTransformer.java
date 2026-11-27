package uk.gov.hmcts.reform.bailcaseapi.component.testutils.wiremock;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.BailCaseForTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;


import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.BailCaseForTest.anBailCase;
import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest.PreSubmitCallbackResponseForTestBuilder.someCallbackResponseWith;

public abstract class CallbackTransformer extends ResponseDefinitionTransformer {

    private ObjectMapper objectMapper = new JsonMapper();
    private Map<String, Object> additionalBailCaseData = new HashMap<>();

    public CallbackTransformer() {
    }

    @Override
    public ResponseDefinition transform(
        Request request,
        ResponseDefinition responseDefinition,
        FileSource files,
        Parameters parameters
    ) {
        Callback<BailCase> bailCaseCallback = readValue(
            request,
            new TypeReference<Callback<BailCase>>() {});

        BailCase incomingBailCase = bailCaseCallback
            .getCaseDetails()
            .getCaseData();

        BailCaseForTest bailCaseToBeReturned = anBailCase()
            .withCaseDetails(incomingBailCase)
            .writeOrOverwrite(additionalBailCaseData);

        PreSubmitCallbackResponseForTest preSubmitCallbackResponseForTest =
            someCallbackResponseWith()
                .data(bailCaseToBeReturned)
                .build();

        ResponseDefinitionBuilder responseDefinitionBuilder = new ResponseDefinitionBuilder()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                writeValue(
                    preSubmitCallbackResponseForTest));

        return responseDefinitionBuilder.build();
    }

    @Override
    public abstract String getName();

    @Override
    public boolean applyGlobally() {
        return false;
    }

    public void addAdditionalBailCaseData(
        String fieldname,
        Object value
    ) {
        additionalBailCaseData.put(fieldname, value);
    }

    private String writeValue(PreSubmitCallbackResponseForTest preSubmitCallbackResponseForTest) {
        try {
            return objectMapper.writeValueAsString(preSubmitCallbackResponseForTest);
        } catch (JacksonException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }

    private Callback<BailCase> readValue(Request request, TypeReference<Callback<BailCase>> typeRef) {
        try {
            return objectMapper.readValue(request.getBodyAsString(), typeRef);
        } catch (JacksonException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }
}
