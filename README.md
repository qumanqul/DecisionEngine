# Loan Decision Engine

A full-stack web application that determines the maximum loan amount a person is eligible for, based on their credit score and requested loan period.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.2, Maven
- **Frontend:** React 19, Vite, Axios

## Getting Started

### Prerequisites
- Java 21
- Node.js 18+
- Maven

### Run the backend
```bash
cd DecisionEngine-back
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Run the frontend
```bash
cd DecisionEngine-front
npm install
npm run dev
```

The UI will be available at `http://localhost:5173`

## API

### POST /api/loan/decision

**Request:**
```json
{
  "personalCode": "49002010987",
  "loanAmount": 4000.00,
  "loanPeriod": 24
}
```

**Response:**
```json
{
  "decision": "POSITIVE",
  "amount": 7200.00,
  "period": 24,
  "message": "Loan approved"
}
```

**Constraints:**
- Loan amount: €2000 - €10000
- Loan period: 12 - 60 months

## How the Decision Engine Works

The engine does not simply approve or reject the requested amount. Instead, it always tries to find the **maximum approvable amount** regardless of what the customer requested.

**Credit score formula:**
```
credit_score = (credit_modifier / loan_amount) * loan_period
```

A loan is approved when `credit_score >= 1`, which means:
```
(credit_modifier / amount) * period >= 1
→ amount <= credit_modifier * period
```

So the maximum approvable amount is `credit_modifier * period`, capped between €2000 and €10000.

**Decision flow:**
1. Look up the personal code - if not found, deny
2. If the person has debt (`credit_modifier = 0`), deny immediately
3. Calculate the maximum approvable amount for the requested period
4. If no suitable amount exists for that period, search across all periods (12–60)
5. Return the best result found, or deny if nothing qualifies

**Mocked personal codes** (as per task requirements):
| Personal Code | Status |
|---|---|
| 49002010965 | Debt — always denied |
| 49002010976 | Segment 1 (modifier: 100) |
| 49002010987 | Segment 2 (modifier: 300) |
| 49002010998 | Segment 3 (modifier: 1000) |

In a real system this would be replaced by an integration with an external credit registry.

## Key Technical Decisions

**BigDecimal for monetary values**
`double` and `float` can produce rounding errors (e.g. `0.1 + 0.2 = 0.30000000000000004`). For financial calculations, `BigDecimal` with explicit `RoundingMode` is the correct choice.

**Mathematical derivation instead of brute force**
Rather than iterating through all possible amounts to find the maximum, the maximum is derived algebraically from the formula. This is both more efficient and clearer in intent.

**Decision enum instead of String**
Using `Decision.POSITIVE` / `Decision.NEGATIVE` instead of raw strings `"positive"` / `"negative"` eliminates the risk of typos and makes the contract between backend and frontend explicit.

**Constructor injection over @Autowired**
Constructor injection makes dependencies explicit, supports immutability, and is easier to test - you can instantiate the class directly with `new` without needing a Spring context.

**PII masking in logs**
Personal codes are sensitive identifiers. Logging them in full would violate data privacy practices, so only the last 4 digits are shown (e.g. `****0987`). In production, a more robust masking strategy or structured logging with field-level redirection would be used.

**Validation on both frontend and backend**
Frontend validation provides immediate feedback without a network round trip. Backend validation is the authoritative layer - it protects the API even if requests bypass the UI entirely.

## What I Would Improve

If I were to extend this beyond the scope of the assignment:

- **Replace the hardcoded mock with a real integration** - connect to an external credit registry to build a comprehensive user profile
- **Add a correlation ID to logs** - tracing requests across service boundaries becomes much easier when every log entry carries a shared request ID
- **Expand test coverage** - add integration tests for the controller layer and edge cases around boundary values (exactly €2000, exactly 60 months)
- **Introduce rate limiting** - a decision engine API is a natural target for abuse; limiting requests per IP would be a basic but important safeguard

Additionally, the assignment could be improved by clarifying the expected behavior regarding the requested loan amount. It is currently ambiguous whether the system should prioritize returning the requested amount or always maximize the approved amount. Providing explicit examples and clearly defined edge cases would help eliminate this ambiguity.
