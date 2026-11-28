package com.neura.loanmanagement.e2e;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Tag("e2e")
public abstract class AbstractE2ESupport {

    private static ConfigurableApplicationContext localContext;
    protected static String baseUrl;
    protected static final ObjectMapper objectMapper = new JsonMapper();

    @BeforeAll
    static void initBaseUrl() {
        String configured = System.getProperty("app.base-url");
        if (configured != null && !configured.isBlank()) {
            baseUrl = configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
        } else {
            localContext = SpringApplication.run(LoanManagementApplication.class,
                    "--server.port=0",
                    "--spring.profiles.active=test");
            String port = localContext.getEnvironment().getProperty("local.server.port");
            baseUrl = "http://localhost:" + port;
        }
    }

    @AfterAll
    static void stopLocalApp() {
        if (localContext != null) {
            localContext.close();
            localContext = null;
        }
    }

    protected RequestSpecification givenAuth(String username, String password) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .auth().preemptive().basic(username, password)
                .contentType(ContentType.JSON);
    }
}
