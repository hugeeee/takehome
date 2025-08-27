package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

// API Response wrapper matching the external API structure
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
    // Getters and Setters
    private T data;
    private Status status;
    private String error;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(T data, Status status, String error) {
        this.data = data;
        this.status = status;
        this.error = error;
    }

    // Helper methods
    public boolean isSuccessful() {
        return status == Status.HANDLED;
    }

    public boolean hasError() {
        return status == Status.ERROR || error != null;
    }

    @Override
    public String toString() {
        return "ApiResponse{" + "data=" + data + ", status=" + status + ", error='" + error + '\'' + '}';
    }

    @Getter
    public enum Status {
        @JsonProperty("Successfully processed request.")
        HANDLED("Successfully processed request."),

        @JsonProperty("Failed to process request.")
        ERROR("Failed to process request.");

        @JsonValue
        private final String value;

        Status(String value) {
            this.value = value;
        }
    }
}
