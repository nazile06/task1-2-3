package com.example.client.config;

import com.example.client.exception.DownstreamClientException;
import com.example.client.exception.DownstreamServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder storageErrorDecoder(ObjectMapper mapper) {
        return new StorageErrorDecoder(mapper);
    }

    /**
     * Translates HTTP error responses from storage-service into typed exceptions,
     * keeping the original status code and message (error propagation).
     */
    static class StorageErrorDecoder implements ErrorDecoder {

        private static final Logger log = LoggerFactory.getLogger(StorageErrorDecoder.class);
        private final ObjectMapper mapper;

        StorageErrorDecoder(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Exception decode(String methodKey, Response response) {
            int status = response.status();
            String message = extractMessage(response);
            log.warn("storage-service responded {} for {}: {}", status, methodKey, message);
            return status >= 500
                    ? new DownstreamServerException(status, message)
                    : new DownstreamClientException(status, message);
        }

        private String extractMessage(Response response) {
            try {
                if (response.body() == null) {
                    return "No response body from storage-service";
                }
                String body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String message = mapper.readTree(body).path("message").asText();
                return message.isBlank() ? "storage-service error" : message;
            } catch (IOException | RuntimeException e) {
                return "storage-service error (unreadable response body)";
            }
        }
    }
}
