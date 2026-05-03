# Contributing

## How to contribute

1. Open an issue before non-trivial work — saves wasted effort if the idea isn't a fit.
2. Fork, branch off `main`, open a PR.
3. One change per PR. New gatherers and bug fixes are separate PRs.

## Pull request checklist

- [ ] Tests pass: `mvn verify`
- [ ] New public API has Javadoc with a `@snippet` example and `@since` tag
- [ ] Tests cover at least: empty input, single element, expected boundary, parallel (if `Gatherer.of`), short-circuit downstream
- [ ] `CHANGELOG.md` updated under `[Unreleased]`
- [ ] Commit messages are short and imperative (`fix:`, `feat:`, `docs:`, `refactor:`, `chore:`)

## Style

Match the existing code. Some pointers:

- Explicit typed locals (`Supplier<...> initializer = ...;`) before `Gatherer.of(...)` rather than inline lambdas.
- Nested records inside the factory method when state is local to that gatherer.
- Choose `Gatherer.of` (parallel-capable) vs `Gatherer.ofSequential` deliberately per gatherer.
- No comments unless the *why* is non-obvious — Javadoc covers the *what*.
- AssertJ + JUnit 5 for tests.

## Review

We review and merge every PR. Direct pushes to `main` are blocked, including for us — everything goes through PR + CI.

## Reporting security issues

**Do not open a public issue** for security vulnerabilities. See [SECURITY.md](SECURITY.md).
