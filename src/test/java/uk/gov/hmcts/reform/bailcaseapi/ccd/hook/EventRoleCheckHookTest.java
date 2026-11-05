package uk.gov.hmcts.reform.bailcaseapi.ccd.hook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
class EventRoleCheckHookTest {

    @Mock private AuthorizedRolesProvider authorizedRolesProvider;

    private final CaseDetails<CaseData, State> details =
        CaseDetails.<CaseData, State>builder().data(new CaseData()).build();
    private final AboutToStartOrSubmitResponse<CaseData, State> response =
        AboutToStartOrSubmitResponse.<CaseData, State>builder().data(new CaseData()).build();

    private EventRoleCheckHook hook;

    @BeforeEach
    void setUp() {
        CcdEventAuthorizor authorizor = new CcdEventAuthorizor(
            Map.of("caseworker-ia-admofficer", List.of(Event.ADD_CASE_NOTE)),
            authorizedRolesProvider
        );
        hook = new EventRoleCheckHook(authorizor);
    }

    @Test
    void passes_the_response_through_for_a_caller_with_the_event_role() {
        when(authorizedRolesProvider.getRoles()).thenReturn(Set.of("caseworker-ia", "caseworker-ia-admofficer"));

        assertThat(hook.handle("addCaseNote", details, null, response)).isSameAs(response);
    }

    @Test
    void refuses_a_caller_without_the_event_role() {
        when(authorizedRolesProvider.getRoles()).thenReturn(Set.of("caseworker-ia", "caseworker-ia-homeofficebail"));

        assertThatThrownBy(() -> hook.handle("addCaseNote", details, null, response))
            .isExactlyInstanceOf(AccessDeniedException.class)
            .hasMessage("Event 'addCaseNote' not allowed");
    }

    @Test
    void refuses_an_event_with_no_bail_event_constant() {
        when(authorizedRolesProvider.getRoles()).thenReturn(Set.of("caseworker-ia", "caseworker-ia-admofficer"));

        assertThatThrownBy(() -> hook.handle("notABailEvent", details, null, response))
            .isExactlyInstanceOf(AccessDeniedException.class)
            .hasMessage("Event 'unknown' not allowed");
    }
}
