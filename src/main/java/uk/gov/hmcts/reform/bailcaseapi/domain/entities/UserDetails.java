package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import java.util.List;

public interface UserDetails {

    String getAccessToken();

    String getId();

    List<String> getRoles();
}
