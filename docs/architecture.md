# Architecture Notes

## Request flow

1. Client submits a tokenized authorization request.
2. Controller validates request shape through Bean Validation.
3. Authorization service hashes the idempotency key and request fingerprint.
4. Service checks for an existing idempotent decision.
5. Account row is loaded with a write lock.
6. Risk scoring evaluates account, merchant, amount, country, and category signals.
7. Approved transactions debit the account.
8. Transaction and outbox event are committed together.
9. After commit, an event relay publishes through the configured publisher.

## Why an outbox?

A direct publish inside the transaction can create inconsistent behavior if the database rolls back after a message is emitted. The outbox keeps the decision and event record in the same transaction, then relays after commit.

## Why idempotency?

Payment clients retry requests after timeouts. Without idempotency, the same transaction can be processed more than once. TransactGuard stores a hashed idempotency key and request fingerprint so exact retries return the original decision while changed payloads are rejected.

## Why account locking?

Concurrent authorization requests can race on account balance. Account-level locking makes the balance update deterministic and protects the invariant that approved debits should not exceed available balance.
