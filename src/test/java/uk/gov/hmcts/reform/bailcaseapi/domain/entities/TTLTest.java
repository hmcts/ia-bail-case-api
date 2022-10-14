package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TTLTest {

    private YesOrNo suspended = YesOrNo.YES;
    private String systemTTL = "some-systemTTL";
    private String overrideTTL = "some-overrideTTL";

    private TTL ttl;

    @BeforeEach
    public void setUp() {
        ttl = new TTL(
            suspended,
            systemTTL,
            overrideTTL);

    }

    @Test
    void should_hold_onto_values() {

        assertThat(ttl.getSuspended()).isEqualTo(suspended);
        assertThat(ttl.getSystemTTL()).isEqualTo(systemTTL);
        assertThat(ttl.getOverrideTTL()).isEqualTo(overrideTTL);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new TTL(null, "", ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new TTL(YesOrNo.YES, null, ""))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new TTL(YesOrNo.YES, "", null))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
