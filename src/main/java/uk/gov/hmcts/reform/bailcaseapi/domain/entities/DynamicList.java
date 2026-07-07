package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DynamicList {

    @Setter
    private Value value;
    private List<Value> listItems;

    public DynamicList(String value) {
        this.value = new Value(value, value);
    }

    private DynamicList() {
    }

    public List<Value> getListItems() {
        return listItems == null ? null : Collections.unmodifiableList(listItems);
    }

    public DynamicList(Value value, List<Value> listItems) {
        this.value = value;
        this.listItems = listItems == null ? null : List.copyOf(listItems);
    }

    public Value getValue() {
        return value;
    }

}
