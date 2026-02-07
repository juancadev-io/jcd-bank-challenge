package com.bank.onboarding.backend.dto;

import jakarta.validation.constraints.NotNull;

public class AccountCreateDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    public AccountCreateDTO() {
    }

    public AccountCreateDTO(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
