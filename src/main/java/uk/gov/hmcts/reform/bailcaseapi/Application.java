package uk.gov.hmcts.reform.bailcaseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// uk.gov.hmcts.ccd.sdk carries the SDK's own CallbackController and the executor behind it, which
// serve the /callbacks/* endpoints an event's method-reference callbacks are published as. The SDK
// ships no Spring auto-configuration, so without this they are never registered and CCD's calls
// 404. This is what nfdiv-case-api does too.
@ComponentScan(basePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.reform.bailcaseapi"})
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.auth",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.bailcaseapi",
        "uk.gov.hmcts.reform.document",
        "uk.gov.hmcts.reform.ccd.document"
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
