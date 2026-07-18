#!/usr/bin/env bash
set -euo pipefail

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
