package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;

/**
 * Every SDK-served callback reads the stored case into CaseData and writes all of it back, so each
 * field of the Bail case data the functional scenarios carry must survive that unchanged.
 */
@EnableAutoConfiguration(exclude = {CacheAutoConfiguration.class})
class ScenarioCaseDataRoundTripTest extends SpringBootIntegrationTest {

    private static final Path FUNCTIONAL_RESOURCES = Path.of("src/functionalTest/resources");

    private static final Pattern TODAY = Pattern.compile("\\{\\$TODAY([+-]\\d+)?}");

    private static final Map<String, String> NOT_CASE_DATA = Map.of(
        "isBailsLocationReferenceDataEnabledFt",
        "a functional-test override of the location reference data feature flag; nothing reads it now",
        "decisionGrantedOrRefusedIma",
        "read by DecisionTypeAppender but absent from the Bail definition, so CCD never stores it",
        "recordTheDecisionListIma",
        "read by DecisionTypeAppender but absent from the Bail definition, so CCD never stores it"
    );

    /**
     * Differences the user has accepted as harmless. Each applies only to a CaseData field of the
     * matching Java type, so the same difference anywhere else is still reported.
     */
    private final List<Normalisation> normalisations = List.of(
        this::emptyStringInEnumReadAsNull,
        this::numericStringWrittenAsNumber,
        this::setWrittenInAnotherOrder,
        this::dateTimeWrittenWithoutZeroMillis
    );

    private Map<String, JavaType> fieldTypes;

    @Test
    void every_scenario_case_keeps_each_field_through_case_data() throws IOException {
        fieldTypes = caseDataFieldTypes();
        assertThat(fieldTypes).containsKeys(
            "applicantAddress", "legalRepCompanyAddress", "supporter4AddressDetails", "bailDirectionList");
        NOT_CASE_DATA.forEach((field, reason) -> assertThat(fieldTypes).as(reason).doesNotContainKey(field));

        Map<String, JsonNode> templates = templates();
        Map<String, JsonNode> cases = new LinkedHashMap<>();
        templates.forEach((name, data) -> cases.put("templates/" + name, data));

        try (Stream<Path> scenarios = Files.list(FUNCTIONAL_RESOURCES.resolve("scenarios"))) {
            for (Path scenario : scenarios.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                JsonNode input = objectMapper.readTree(scenario.toFile()).path("request").path("input");
                for (String part : List.of("caseData", "caseDataBefore")) {
                    if (input.has(part)) {
                        cases.put(scenario.getFileName() + " " + part, build(input.get(part), templates));
                    }
                }
            }
        }

        assertThat(cases).hasSizeGreaterThan(templates.size());

        List<String> mismatches = new ArrayList<>();
        cases.forEach((name, data) -> mismatches.addAll(mismatches(name, data)));

        assertThat(mismatches).isEmpty();
    }

    private List<String> mismatches(String name, JsonNode data) {
        JsonNode written;
        try {
            written = objectMapper.valueToTree(objectMapper.convertValue(data, CaseData.class));
        } catch (IllegalArgumentException e) {
            return List.of(name + ": cannot be read as CaseData: " + e.getMessage());
        }

        List<String> mismatches = new ArrayList<>();
        data.properties().forEach(field -> {
            if (NOT_CASE_DATA.containsKey(field.getKey())) {
                return;
            }
            JsonNode expected = withoutNullMembers(field.getValue());
            JsonNode actual = written.get(field.getKey());
            boolean kept = expected == null
                ? actual == null
                : expected.equals(actual) || normalised(field.getKey(), expected, actual);
            if (!kept) {
                mismatches.add(name + ": " + field.getKey() + " sent " + expected + " came back " + actual);
            }
        });
        return mismatches;
    }

    private boolean normalised(String field, JsonNode sent, JsonNode back) {
        JavaType type = fieldTypes.get(field);
        return type != null && normalisations.stream().anyMatch(rule -> rule.accepts(field, type, sent, back));
    }

    private boolean emptyStringInEnumReadAsNull(String field, JavaType type, JsonNode sent, JsonNode back) {
        return type.isEnumType() && sent.isTextual() && sent.textValue().isEmpty() && back == null;
    }

    private boolean numericStringWrittenAsNumber(String field, JavaType type, JsonNode sent, JsonNode back) {
        if (!Number.class.isAssignableFrom(type.getRawClass()) || !sent.isTextual() || back == null
            || !back.isNumber()) {
            return false;
        }
        try {
            if (new BigDecimal(sent.textValue()).compareTo(back.decimalValue()) != 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return readAsCaseData(field, sent).equals(readAsCaseData(field, back));
    }

    private boolean setWrittenInAnotherOrder(String field, JavaType type, JsonNode sent, JsonNode back) {
        return Set.class.isAssignableFrom(type.getRawClass())
            && sent.isArray() && back != null && back.isArray()
            && sent.size() == back.size()
            && elements(sent).equals(elements(back));
    }

    private boolean dateTimeWrittenWithoutZeroMillis(String field, JavaType type, JsonNode sent, JsonNode back) {
        if (type.getRawClass() != LocalDateTime.class || !sent.isTextual() || !sent.textValue().endsWith(".000")
            || back == null || !back.isTextual()) {
            return false;
        }
        try {
            return LocalDateTime.parse(sent.textValue()).equals(LocalDateTime.parse(back.textValue()));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private JsonNode readAsCaseData(String field, JsonNode value) {
        CaseData read = objectMapper.convertValue(Map.of(field, value), CaseData.class);
        return objectMapper.<JsonNode>valueToTree(read).get(field);
    }

    private static Set<JsonNode> elements(JsonNode array) {
        Set<JsonNode> elements = new HashSet<>();
        array.forEach(elements::add);
        return elements;
    }

    private Map<String, JavaType> caseDataFieldTypes() {
        Map<String, JavaType> types = new HashMap<>();
        collectFieldTypes(objectMapper.constructType(CaseData.class), NameTransformer.NOP, types);
        return types;
    }

    private void collectFieldTypes(JavaType type, NameTransformer names, Map<String, JavaType> types) {
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription bean = config.introspect(type);
        for (BeanPropertyDefinition property : bean.findProperties()) {
            NameTransformer unwrapping =
                config.getAnnotationIntrospector().findUnwrappingNameTransformer(property.getPrimaryMember());
            if (unwrapping == null) {
                types.put(names.transform(property.getName()), property.getPrimaryType());
            } else {
                collectFieldTypes(property.getPrimaryType(), NameTransformer.chainedTransformer(names, unwrapping),
                    types);
            }
        }
    }

    @FunctionalInterface
    private interface Normalisation {
        boolean accepts(String field, JavaType type, JsonNode sent, JsonNode back);
    }

    private JsonNode build(JsonNode caseDataInput, Map<String, JsonNode> templates) {
        ObjectNode data = templates.get(caseDataInput.get("template").asText()).deepCopy();
        if (caseDataInput.hasNonNull("replacements")) {
            merge(data, caseDataInput.get("replacements"));
        }
        return expand(data);
    }

    private Map<String, JsonNode> templates() throws IOException {
        Map<String, JsonNode> templates = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(FUNCTIONAL_RESOURCES.resolve("templates"))) {
            for (Path file : files.sorted().toList()) {
                templates.put(file.getFileName().toString(), expand(objectMapper.readTree(file.toFile())));
            }
        }
        return templates;
    }

    private static void merge(ObjectNode target, JsonNode replacements) {
        replacements.properties().forEach(replacement -> {
            JsonNode existing = target.get(replacement.getKey());
            if (existing instanceof ObjectNode existingObject && replacement.getValue().isObject()) {
                merge(existingObject, replacement.getValue());
            } else {
                target.set(replacement.getKey(), replacement.getValue().deepCopy());
            }
        });
    }

    private static JsonNode expand(JsonNode node) {
        if (node.isTextual()) {
            String text = node.asText();
            Matcher today = TODAY.matcher(text);
            StringBuilder expanded = new StringBuilder();
            while (today.find()) {
                long offset = today.group(1) == null ? 0 : Long.parseLong(today.group(1));
                today.appendReplacement(expanded, LocalDate.now().plusDays(offset).toString());
            }
            today.appendTail(expanded);
            return new TextNode(expanded.toString().replace("{$IA_USER_PREFIX}", ""));
        }
        if (node.isContainerNode()) {
            JsonNode copy = node.deepCopy();
            if (copy instanceof ObjectNode object) {
                object.properties().forEach(member -> member.setValue(expand(member.getValue())));
            } else {
                for (int i = 0; i < copy.size(); i++) {
                    ((ArrayNode) copy).set(i, expand(copy.get(i)));
                }
            }
            return copy;
        }
        return node;
    }

    private static JsonNode withoutNullMembers(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        JsonNode copy = node.deepCopy();
        stripNullMembers(copy);
        return copy;
    }

    private static void stripNullMembers(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.properties().removeIf(member -> member.getValue().isNull());
        }
        node.forEach(ScenarioCaseDataRoundTripTest::stripNullMembers);
    }
}
