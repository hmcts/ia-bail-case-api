package uk.gov.hmcts.reform.bailcaseapi.ccd.model.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

/**
 * A CCD DynamicList as Bail stores it. The SDK's own DynamicList requires UUID codes; Bail's codes
 * are reference data ids, direction names and application numbers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ComplexType(generate = false)
public class DynamicList {

    private DynamicListElement value;

    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;
}
