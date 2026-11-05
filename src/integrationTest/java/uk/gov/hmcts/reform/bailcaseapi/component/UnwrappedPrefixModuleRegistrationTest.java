package uk.gov.hmcts.reform.bailcaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import uk.gov.hmcts.ccd.sdk.jackson.UnwrappedPrefixModule;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.SpringBootIntegrationTest;

/**
 * The SDK's callbacks read case data with Bail's primary ObjectMapper, which must carry the module
 * that keeps nested values inside a prefixed {@code @JsonUnwrapped} group.
 */
@EnableAutoConfiguration(exclude = {CacheAutoConfiguration.class})
class UnwrappedPrefixModuleRegistrationTest extends SpringBootIntegrationTest {

    @Test
    void registers_the_module_on_the_primary_mapper() {
        assertThat(objectMapper.getRegisteredModuleIds()).contains(UnwrappedPrefixModule.class.getName());
    }

    @Test
    void sdk_callbacks_use_the_primary_mapper_rather_than_a_copy() {
        assertThat(UnwrappedPrefixModule.ensureRegistered(objectMapper)).isSameAs(objectMapper);
    }
}
