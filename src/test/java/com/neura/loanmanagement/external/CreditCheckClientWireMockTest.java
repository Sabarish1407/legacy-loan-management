package com.neura.loanmanagement.external;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.neura.loanmanagement.exception.ExternalServiceException;
import com.neura.loanmanagement.integration.credit.CreditCheckClient;
import com.neura.loanmanagement.integration.credit.CreditScoreResponse;
import org.junit.jupiter.api.*;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("External Credit Bureau Integration WireMock Tests")
class CreditCheckClientWireMockTest {

    private static WireMockServer wireMockServer;
    private CreditCheckClient creditCheckClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        String url = "http://localhost:" + wireMockServer.port() + "/credit-bureau/score";
        creditCheckClient = new CreditCheckClient(
                new RestTemplateBuilder(),
                url,
                1000, // 1 sec timeout
                3,    // 3 retry attempts
                50    // 50ms backoff for fast tests
        );
    }

    @Test
    @DisplayName("Should successfully look up credit score on successful 200 response")
    void testSuccessfulCreditScoreLookup() {
        wireMockServer.stubFor(post(urlEqualTo("/credit-bureau/score"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"nationalId\":\"NAT-999\",\"creditScore\":780,\"riskRating\":\"LOW\",\"status\":\"ACTIVE\"}")));

        CreditScoreResponse response = creditCheckClient.fetchCreditScore("NAT-999", "user@example.com");

        assertNotNull(response);
        assertEquals("NAT-999", response.getNationalId());
        assertEquals(780, response.getCreditScore());
        assertEquals("LOW", response.getRiskRating());

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/credit-bureau/score")));
    }

    @Test
    @DisplayName("Should retry on transient 503 error and succeed on second attempt")
    void testRetryBehaviorOnTransientError() {
        // First attempt 503, second attempt 200
        wireMockServer.stubFor(post(urlEqualTo("/credit-bureau/score"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willSetStateTo("SecondAttempt")
                .willReturn(aResponse().withStatus(503)));

        wireMockServer.stubFor(post(urlEqualTo("/credit-bureau/score"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("SecondAttempt")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"nationalId\":\"NAT-RETRY\",\"creditScore\":720,\"riskRating\":\"MEDIUM\",\"status\":\"ACTIVE\"}")));

        CreditScoreResponse response = creditCheckClient.fetchCreditScore("NAT-RETRY", "retry@example.com");

        assertNotNull(response);
        assertEquals(720, response.getCreditScore());
        wireMockServer.verify(2, postRequestedFor(urlEqualTo("/credit-bureau/score")));
    }

    @Test
    @DisplayName("Should handle timeout error and exhaust retries throwing ExternalServiceException")
    void testTimeoutHandlingAndExhaustRetries() {
        wireMockServer.stubFor(post(urlEqualTo("/credit-bureau/score"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000))); // Exceeds 1000ms timeout

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () ->
                creditCheckClient.fetchCreditScore("NAT-TIMEOUT", "timeout@example.com"));

        assertTrue(ex.getMessage().contains("Failed to fetch credit score after 3 attempts"));
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/credit-bureau/score")));
    }

    @Test
    @DisplayName("Should handle 4xx client error immediately without unnecessary retries")
    void testClientErrorHandlingWithoutRetry() {
        wireMockServer.stubFor(post(urlEqualTo("/credit-bureau/score"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"error\":\"Invalid national ID format\"}")));

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () ->
                creditCheckClient.fetchCreditScore("INVALID-ID", "bad@example.com"));

        assertTrue(ex.getMessage().contains("Client error from credit bureau"));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/credit-bureau/score")));
    }
}
