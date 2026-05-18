# Changelog

## [1.2.0] - Unreleased

### Added
- `ConsecutiveGatherers.distinctUntilChanged(keyFn)` — drops consecutive same-key elements. Sequential.
- `ConsecutiveGatherers.distinctConsecutive()` — drops consecutive equal elements. Sequential.

## [1.1.1] - 2026-05-12

### Dependencies
- Bumped JUnit Jupiter 5.12.0 → 6.0.3.
- Bumped AssertJ 3.27.3 → 3.27.7.
- Bumped maven-compiler-plugin 3.13.0 → 3.15.0.
- Bumped maven-surefire-plugin 3.5.2 → 3.5.5.
- Bumped maven-source-plugin 3.3.0 → 3.4.0.
- Bumped central-publishing-maven-plugin 0.7.0 → 0.10.0.

## [1.1.0] - 2026-05-11

### Added
- `DeduplicationGatherers.distinctBy(keyFn)` — keeps the first element seen per key. Sequential.
- `IndexGatherers.zipWithIndex()` — pairs each element with its zero-based position. Sequential. Output type: `Indexed<T>(long index, T value)`.

## [1.0.1] - 2026-05-03

### Added
- `CountingGatherers.frequency()` — count occurrences per distinct element.
- `WindowGatherers.fixedWindow(n)` — non-overlapping fixed-size windows.
- `WindowGatherers.slidingWindow(n)` — overlapping sliding windows.
- `MathGatherers.runningSum()` — running total after each element.
- `MathGatherers.sum()` — single total at end of stream.

### Notes
- `MathGatherers.AdditionProcessor` is exposed in the gatherer's state type. May become package-private in 2.0.
- Mixed numeric types widen to `double`. Stick to one type for exact results.
