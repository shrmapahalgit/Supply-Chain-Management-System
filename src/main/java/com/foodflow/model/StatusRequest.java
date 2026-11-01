package com.foodflow.model;

import jakarta.validation.constraints.NotBlank;

public class StatusRequest {
    @NotBlank private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
