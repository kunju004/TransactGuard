# Contributing

TransactGuard is a portfolio-grade Spring Boot service, so contributions should keep the code easy to reason about and test.

## Local workflow

```bash
mvn verify
mvn spring-boot:run
```

## Pull request checklist

- Keep changes focused and reviewable.
- Add or update tests for behavior changes.
- Prefer explicit validation and clear error responses.
- Keep domain logic in application/domain layers instead of controllers.
- Update README or docs when API behavior changes.
- Run `mvn verify` before opening a PR.

## Code style

- Use Java 21 language features conservatively.
- Keep controller classes thin.
- Prefer constructor injection.
- Make transactional boundaries explicit.
- Avoid logging sensitive request values.
