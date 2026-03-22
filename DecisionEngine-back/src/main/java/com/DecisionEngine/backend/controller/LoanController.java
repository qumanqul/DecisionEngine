package com.DecisionEngine.backend.controller;

import com.DecisionEngine.backend.model.LoanRequest;
import com.DecisionEngine.backend.model.LoanResponse;
import com.DecisionEngine.backend.service.DecisionEngineService;
import com.DecisionEngine.backend.util.MaskingUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loan")
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private final DecisionEngineService decisionEngineService;

    public LoanController(DecisionEngineService decisionEngineService) {
        this.decisionEngineService = decisionEngineService;
    }

    @PostMapping("/decision")
    public ResponseEntity<LoanResponse> getLoanDecision(@Valid @RequestBody LoanRequest request) {
        logger.info("Received loan request: personalCode={}, amount={}, period={}",
                MaskingUtil.maskPersonalCode(request.getPersonalCode()),
                request.getLoanAmount(),
                request.getLoanPeriod());

        LoanResponse response = decisionEngineService.calculateDecision(request);

        logger.info("Decision result: decision={}, amount={}, period={}",
                response.getDecision(), response.getAmount(), response.getPeriod());

        return ResponseEntity.ok(response);
    }

}