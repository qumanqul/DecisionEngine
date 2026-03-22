package com.DecisionEngine.backend.service;

import com.DecisionEngine.backend.model.Decision;
import com.DecisionEngine.backend.model.LoanRequest;
import com.DecisionEngine.backend.model.LoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DecisionEngineServiceTest {

    private DecisionEngineService service;

    @BeforeEach
    void setUp() {
        service = new DecisionEngineService();
    }

    // ── Debt ──────────────────────────────────────────────────────────────

    @Test
    void whenPersonalCodeHasDebt_thenDecisionIsNegative() {
        LoanRequest request = buildRequest("49002010965", "5000", 24);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.NEGATIVE, response.getDecision());
        assertNull(response.getAmount());
        assertNull(response.getPeriod());
    }

    // ── Unknown personal code ─────────────────────────────────────────────

    @Test
    void whenPersonalCodeIsUnknown_thenDecisionIsNegative() {
        LoanRequest request = buildRequest("00000000000", "5000", 24);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.NEGATIVE, response.getDecision());
        assertNull(response.getAmount());
    }

    // ── Segment 1 (modifier = 100) ─────────────────────────────────────────

    @Test
    void whenSegment1_thenReturnsMaxApprovedAmount() {
        // modifier=100, period=24 → max = 100*24 = 2400
        LoanRequest request = buildRequest("49002010976", "1000", 24);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.POSITIVE, response.getDecision());
        assertEquals(new BigDecimal("2400.00"), response.getAmount());
        assertEquals(24, response.getPeriod());
    }

    @Test
    void whenSegment1AndPeriodTooShort_thenFindsAlternativePeriod() {
        // modifier=100, period=12 → max = 100*12 = 1200 < 2000 → not enough
        // period=20 → max = 100*20 = 2000 → approved
        LoanRequest request = buildRequest("49002010976", "5000", 12);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.POSITIVE, response.getDecision());
        assertTrue(response.getPeriod() > 12);
        assertNotNull(response.getAmount());
    }

    // ── Segment 2 (modifier = 300) ─────────────────────────────────────────

    @Test
    void whenSegment2_thenReturnsMaxApprovedAmount() {
        // modifier=300, period=24 → max = 300*24 = 7200
        LoanRequest request = buildRequest("49002010987", "4000", 24);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.POSITIVE, response.getDecision());
        assertEquals(new BigDecimal("7200.00"), response.getAmount());
        assertEquals(24, response.getPeriod());
    }

    @Test
    void whenSegment2AndAmountExceedsCap_thenReturnsCappedAmount() {
        // modifier=300, period=60 → max = 300*60 = 18000 → capped at 10000
        LoanRequest request = buildRequest("49002010987", "5000", 60);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.POSITIVE, response.getDecision());
        assertEquals(new BigDecimal("10000.00"), response.getAmount());
    }

    // ── Segment 3 (modifier = 1000) ────────────────────────────────────────

    @Test
    void whenSegment3_thenAlwaysApprovesMaxAmount() {
        // modifier=1000, period=12 → max = 1000*12 = 12000 → capped at 10000
        LoanRequest request = buildRequest("49002010998", "2000", 12);
        LoanResponse response = service.calculateDecision(request);

        assertEquals(Decision.POSITIVE, response.getDecision());
        assertEquals(new BigDecimal("10000.00"), response.getAmount());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private LoanRequest buildRequest(String personalCode, String amount, int period) {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode(personalCode);
        request.setLoanAmount(new BigDecimal(amount));
        request.setLoanPeriod(period);
        return request;
    }
}