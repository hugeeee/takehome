package com.reliaquest.api.config;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();

        // Add error handler to handle HTTP errors gracefully
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(final ClientHttpResponse response) throws IOException {
                final HttpStatusCode statusCode = response.getStatusCode();
                return statusCode.is4xxClientError() || statusCode.is5xxServerError();
            }

            @Override
            public void handleError(final ClientHttpResponse response) throws IOException {
                final HttpStatusCode statusCode = response.getStatusCode();
                final String statusText = response.getStatusText();

                // Log the error for debugging
                System.err.println("HTTP Error: " + statusCode + " " + statusText);
            }
        });

        return restTemplate;
    }
}
