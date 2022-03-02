package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;

public enum BailCaseFieldDefinition {
    IS_ADMIN(
        "isAdmin", new TypeReference<String>() {}),
    IS_LEGAL_REP(
        "isLegalRep", new TypeReference<String>() {}),
    IS_HOME_OFFICE(
        "isHomeOffice", new TypeReference<String>() {}),
    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),
    LEGAL_REP_EMAIL_ADDRESS(
        "legalRepDetailsEmailAddress", new TypeReference<String>(){})
    ;

    private final String value;
    private final TypeReference typeReference;

    BailCaseFieldDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
