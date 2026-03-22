package com.DecisionEngine.backend.service;

import com.DecisionEngine.backend.model.Decision;
import com.DecisionEngine.backend.model.LoanRequest;
import com.DecisionEngine.backend.model.LoanResponse;
import com.DecisionEngine.backend.util.MaskingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Service
public class DecisionEngineService {

    private static final Logger logger = LoggerFactory.getLogger(DecisionEngineService.class);

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("2000.00");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");
    private static final int MIN_PERIOD = 12;
    private static final int MAX_PERIOD = 60;

    private static final Map<String, Integer> CREDIT_MODIFIERS = Map.of(
            "49002010965", 0,
            "49002010976", 100,
            "49002010987", 300,
            "49002010998", 1000
    );

    public LoanResponse calculateDecision(LoanRequest request) {
        String personalCode = request.getPersonalCode();
        String maskedCode = MaskingUtil.maskPersonalCode(personalCode);

        Integer creditModifier = CREDIT_MODIFIERS.get(personalCode);

        if (creditModifier == null) {
            logger.warn("Personal code not found in the system: {}", maskedCode);
            return new LoanResponse(Decision.NEGATIVE, null, null,
                    "Personal code not found in the system");
        }

        if (creditModifier == 0) {
            logger.warn("Loan denied due to existing debt for personalCode: {}", maskedCode);
            return new LoanResponse(Decision.NEGATIVE, null, null,
                    "Loan denied due to existing debt");
        }

        int requestedPeriod = request.getLoanPeriod();

        logger.debug("Calculating decision for personalCode={}, creditModifier={}, period={}",
                maskedCode, creditModifier, requestedPeriod);

        Optional<BigDecimal> approvedAmount = findMaxApprovedAmount(creditModifier, requestedPeriod);

        if (approvedAmount.isPresent()) {
            logger.info("Loan approved: period={}, amount={}", requestedPeriod, approvedAmount.get());
            return new LoanResponse(Decision.POSITIVE, approvedAmount.get(), requestedPeriod,
                    "Loan approved");
        }

        logger.debug("No suitable amount for period={}, searching for alternative period", requestedPeriod);

        for (int period = MIN_PERIOD; period <= MAX_PERIOD; period++) {
            Optional<BigDecimal> amountForPeriod = findMaxApprovedAmount(creditModifier, period);
            if (amountForPeriod.isPresent()) {
                logger.info("Loan approved with adjusted period={}, amount={}", period, amountForPeriod.get());
                return new LoanResponse(Decision.POSITIVE, amountForPeriod.get(), period,
                        "Loan approved with adjusted period");
            }
        }

        logger.warn("No suitable loan found for personalCode={}", maskedCode);
        return new LoanResponse(Decision.NEGATIVE, null, null,
                "No suitable loan amount or period found");
    }

    private Optional<BigDecimal> findMaxApprovedAmount(int creditModifier, int period) {
        BigDecimal maxApprovable = BigDecimal.valueOf((long) creditModifier * period);
        BigDecimal approvedAmount = maxApprovable.min(MAX_AMOUNT);

        if (approvedAmount.compareTo(MIN_AMOUNT) >= 0) {
            return Optional.of(approvedAmount.setScale(2, RoundingMode.HALF_UP));
        }

        return Optional.empty();
    }
}