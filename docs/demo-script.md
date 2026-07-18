# Demo Script

Use this script when showing TransactGuard to a recruiter, hiring manager, or interviewer.

## 1. Start the service

```bash
docker compose up --build
```

## 2. Show Swagger

Open:

```text
http://localhost:8080/swagger-ui.html
```

Explain that the API is documented through OpenAPI and supports authorization, retrieval, and account transaction history.

## 3. Submit an authorization

```bash
curl -i -X POST http://localhost:8080/api/v1/transactions/authorize \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-request-001" \
  -d '{
    "accountId": "acct_market_001",
    "cardToken": "tok_urbanRoast0001",
    "merchantId": "merchant_urban_roast",
    "merchantCategoryCode": "5812",
    "merchantCountry": "US",
    "currency": "USD",
    "amount": 42.25,
    "idempotencyKey": "idem-demo-0001"
  }'
```

## 4. Demonstrate idempotency

Run the same request again. The service should return the same transaction decision instead of creating a duplicate authorization.

## 5. Demonstrate validation

Change `cardToken` to a raw-looking value and show the validation failure.

## 6. Show observability

Open:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/prometheus
http://localhost:9090
http://localhost:3000
```

## 7. Show tests

```bash
mvn verify
```
