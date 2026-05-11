# Changelog

## [1.1.0] - Unreleased

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
