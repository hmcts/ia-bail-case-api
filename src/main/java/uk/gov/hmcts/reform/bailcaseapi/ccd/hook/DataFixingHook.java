package uk.gov.hmcts.reform.bailcaseapi.ccd.hook;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.DataFixer;

/**
 * Pre-event hook that repairs case data with every {@link DataFixer} before an SDK callback
 * deserialises it, as {@code BailCaseDataFixingHandler} does for dispatcher-served events.
 */
@Component
@RequiredArgsConstructor
public class DataFixingHook implements Function<Map<String, Object>, Map<String, Object>> {

    private final List<DataFixer> dataFixers;

    @Override
    public Map<String, Object> apply(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        BailCase bailCase = new BailCase();
        bailCase.putAll(data);
        dataFixers.forEach(dataFixer -> dataFixer.fix(bailCase));
        return bailCase;
    }
}
