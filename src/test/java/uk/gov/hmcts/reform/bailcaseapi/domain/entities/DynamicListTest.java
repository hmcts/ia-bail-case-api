package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DynamicListTest {

    @Test
    void should_hold_onto_values() {
        Value value = new Value("code1", "label1");
        List<Value> listItems = List.of(
            new Value("code1", "label1"),
            new Value("code2", "label2")
        );

        DynamicList dynamicList = new DynamicList(value, listItems);

        assertThat(dynamicList.getValue()).isEqualTo(value);
        assertThat(dynamicList.getListItems()).hasSize(2);
        assertThat(dynamicList.getListItems().get(0).getCode()).isEqualTo("code1");
    }

    @Test
    void should_create_with_string_value() {
        DynamicList dynamicList = new DynamicList("testValue");

        assertThat(dynamicList.getValue().getCode()).isEqualTo("testValue");
        assertThat(dynamicList.getValue().getLabel()).isEqualTo("testValue");
    }

    @Test
    void should_return_null_when_list_items_is_null() {
        DynamicList dynamicList = new DynamicList("testValue");

        assertThat(dynamicList.getListItems()).isNull();
    }

    @Test
    void should_return_immutable_list() {
        Value value = new Value("code1", "label1");
        List<Value> listItems = new ArrayList<>();
        listItems.add(new Value("code1", "label1"));

        DynamicList dynamicList = new DynamicList(value, listItems);

        assertThatThrownBy(() -> dynamicList.getListItems().add(new Value("code2", "label2")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_not_be_affected_by_modifications_to_original_list() {
        Value value = new Value("code1", "label1");
        List<Value> listItems = new ArrayList<>();
        listItems.add(new Value("code1", "label1"));

        DynamicList dynamicList = new DynamicList(value, listItems);

        listItems.add(new Value("code2", "label2"));

        assertThat(dynamicList.getListItems()).hasSize(1);
    }
}
