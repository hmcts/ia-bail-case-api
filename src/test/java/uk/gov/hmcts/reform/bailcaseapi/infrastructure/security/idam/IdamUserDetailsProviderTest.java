package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

import feign.FeignException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IdamUserDetailsProviderTest {

    @Mock
    private AccessTokenProvider accessTokenProvider;
    @Mock
    private IdamApi idamApi;

    private IdamUserDetailsProvider idamUserDetailsProvider;

    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        idamUserDetailsProvider =
            new IdamUserDetailsProvider(
                accessTokenProvider,
                idamApi
            );
    }

    @Test
    void should_call_idam_api_to_get_user_details() {
        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedRoles = Arrays.asList("role-1", "role-2");
        UserInfo userInfo = new UserInfo(
            expectedId,
            expectedRoles
        );
        when(accessTokenProvider.getAccessToken()).thenReturn(expectedAccessToken);

        when(idamApi.userInfo(expectedAccessToken)).thenReturn(userInfo);

        UserDetails actualUserDetails = idamUserDetailsProvider.getUserDetails();
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserDetails.getId());
        assertEquals(expectedRoles, actualUserDetails.getRoles());
    }

    @Test
    void should_throw_exception_if_idam_id_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            null,
            Arrays.asList("role")
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'uid' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_if_idam_roles_missing() {

        String accessToken = "ABCDEFG";

        UserInfo userInfo = new UserInfo(
            "some-id",
            null
        );

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .hasMessage("IDAM user details missing 'roles' field")
            .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void should_wrap_server_exception_when_calling_idam() {

        String accessToken = "ABCDEFG";

        FeignException restClientException = mock(FeignException.class);

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(anyString())).thenThrow(restClientException);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details with IDAM")
            .hasCause(restClientException);
    }

    @Test
    void should_wrap_client_exception_when_calling_idam() {

        String accessToken = "ABCDEFG";

        FeignException restClientException = mock(FeignException.class);

        when(accessTokenProvider.getAccessToken()).thenReturn(accessToken);

        when(idamApi.userInfo(anyString())).thenThrow(restClientException);

        assertThatThrownBy(() -> idamUserDetailsProvider.getUserDetails())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get user details with IDAM")
            .hasCause(restClientException);
    }
}