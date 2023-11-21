package uk.gov.hmcts.reform.bailcaseapi.domain.utils;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.RefDataUserService;

import java.util.Collections;
import java.util.List;

public class InterpreterLanguagesUtils {
    public static final String IS_CHILD_REQUIRED = "Y";
    private InterpreterLanguagesUtils() {
        // Utils classes should not have public or default constructors
    }
    public static InterpreterLanguageRefData generateDynamicList(RefDataUserService refDataUserService, String languageCategory) {
        List<CategoryValues> languages;
        DynamicList dynamicListOfLanguages;

        try {
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                languageCategory,
                IS_CHILD_REQUIRED
            );

            languages = refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, languageCategory);

            dynamicListOfLanguages = new DynamicList(new Value("", ""),
                                                     refDataUserService.mapCategoryValuesToDynamicListValues(languages));

        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not read response by RefData service for %s(s)", languageCategory), e);
        }

        return new InterpreterLanguageRefData(
            dynamicListOfLanguages,
            Collections.emptyList(),
            "");
    }


}
