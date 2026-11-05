package uk.gov.hmcts.reform.bailcaseapi.ccd.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    /**
     * Reads through a delegate because Jackson 2.19+ applies a {@code @JsonUnwrapped(prefix)} to the
     * properties of a bean nested inside the unwrapped group, so {@code bailDirectionList} would
     * otherwise be read as empty.
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    static DynamicList read(Stored stored) {
        return new DynamicList(stored.value(), stored.listItems());
    }

    record Stored(
        DynamicListElement value,
        @JsonProperty("list_items") List<DynamicListElement> listItems
    ) {
    }
}
