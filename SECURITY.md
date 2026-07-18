# Security Policy

TransactGuard is an educational portfolio project. Please do not submit real payment data, card numbers, secrets, credentials, or production data.

## Secure-by-design choices

- API accepts tokenized card references only.
- Raw PAN-like card input is rejected by validation.
- Idempotency keys are hashed before storage.
- Request fingerprints are used to detect replay drift.
- Security headers and correlation IDs are applied through request hardening.
- Events are published after transaction commit.

## Reporting issues

For this portfolio project, open a GitHub issue with a clear reproduction path. Do not include secrets or real customer data.
