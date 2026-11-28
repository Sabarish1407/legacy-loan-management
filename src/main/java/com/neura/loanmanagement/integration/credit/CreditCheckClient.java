package com.neura.loanmanagement.integration.credit;

import com.neura.loanmanagement.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class CreditCheckClient {

    private static final Logger log = LoggerFactory.getLogger(CreditCheckClient.class);

    private final RestTemplate restTemplate;
    private final String bureauUrl;
    private final int retryAttempts;
    private final long retryBackoffMs;

    public CreditCheckClient(RestTemplateBuilder restTemplateBuilder,
                             @Value("${loan.credit-bureau.url:http://localhost:8089/credit-bureau/score}") String bureauUrl,
                             @Value("${loan.credit-bureau.timeout-ms:3000}") long timeoutMs,
                             @Value("${loan.credit-bureau.retry-attempts:3}") int retryAttempts,
                             @Value("${loan.credit-bureau.retry-backoff-ms:500}") long retryBackoffMs) {
        this.bureauUrl = bureauUrl;
        this.retryAttempts = Math.max(1, retryAttempts);
        this.retryBackoffMs = retryBackoffMs;
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .readTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public CreditScoreResponse fetchCreditScore(String nationalId, String email) {
        CreditScoreRequest request = new CreditScoreRequest(nationalId, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreditScoreRequest> entity = new HttpEntity<>(request, headers);

        int attempts = 0;
        Exception lastException = null;

        while (attempts < retryAttempts) {
            attempts++;
            try {
                log.info("Querying credit bureau (attempt {} of {})...", attempts, retryAttempts);
                ResponseEntity<CreditScoreResponse> response = restTemplate.postForEntity(
                        bureauUrl, entity, CreditScoreResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Successfully fetched credit score: {}", response.getBody().getCreditScore());
                    return response.getBody();
                } else {
                    throw new ExternalServiceException("CreditBureau",
                            "Unexpected response code: " + response.getStatusCode());
                }
            } catch (ResourceAccessException ex) {
                // Timeout or network unreachable
                lastException = ex;
                log.warn("Credit bureau request timed out or network error on attempt {}: {}", attempts, ex.getMessage());
            } catch (HttpStatusCodeException ex) {
                // HTTP error from external service
                lastException = ex;
                log.warn("Credit bureau returned HTTP {} on attempt {}", ex.getStatusCode(), attempts);
                if (ex.getStatusCode().is4xxClientError()) {
                    // Do not retry 4xx errors (client errors)
                    throw new ExternalServiceException("CreditBureau",
                            "Client error from credit bureau: " + ex.getResponseBodyAsString(), ex);
                }
            } catch (Exception ex) {
                lastException = ex;
                log.warn("General error connecting to credit bureau on attempt {}: {}", attempts, ex.getMessage());
            }

            if (attempts < retryAttempts) {
                try {
                    Thread.sleep(retryBackoffMs * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ExternalServiceException("CreditBureau", "Interrupted during retry backoff", ie);
                }
            }
        }

        throw new ExternalServiceException("CreditBureau",
                "Failed to fetch credit score after " + retryAttempts + " attempts", lastException);
    }
}
