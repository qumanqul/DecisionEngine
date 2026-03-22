package com.DecisionEngine.backend.model;

import java.math.BigDecimal;

public class LoanResponse {

    private Decision decision;
    private BigDecimal amount;
    private Integer period;
    private String message;

    public LoanResponse(Decision decision, BigDecimal amount, Integer period, String message) {
        this.decision = decision;
        this.amount = amount;
        this.period = period;
        this.message = message;
    }

    public Decision getDecision() { return decision; }
    public BigDecimal getAmount() { return amount; }
    public Integer getPeriod() { return period; }
    public String getMessage() { return message; }
}
