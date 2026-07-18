INSERT INTO accounts (account_id, active, available_balance, created_at, currency, updated_at, version)
VALUES ('acct_test_001', true, 1000.00, now(), 'USD', now(), 0)
ON CONFLICT (account_id) DO NOTHING;

INSERT INTO accounts (account_id, active, available_balance, created_at, currency, updated_at, version)
VALUES ('acct_high_risk_001', true, 2500.00, now(), 'USD', now(), 0)
ON CONFLICT (account_id) DO NOTHING;
