package com.DecisionEngine.backend.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class LoanRequest {

    @NotBlank(message = "Personal code is required")
    private String personalCode;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "2000.00", message = "Minimum loan amount is 2000")
    @DecimalMax(value = "10000.00", message = "Maximum loan amount is 10000")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan period is required")
    @Min(value = 12, message = "Minimum loan period is 12 months")
    @Max(value = 60, message = "Maximum loan period is 60 months")
    private Integer loanPeriod;

    public String getPersonalCode() { return personalCode; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public Integer getLoanPeriod() { return loanPeriod; }

    public void setPersonalCode(String personalCode) { this.personalCode = personalCode; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public void setLoanPeriod(Integer loanPeriod) { this.loanPeriod = loanPeriod; }
}