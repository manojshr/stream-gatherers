# stream-gatherers

Reusable Java Stream `Gatherer`s ([JEP 485](https://openjdk.org/jeps/485)).

`Gatherer` is Java 24's API for custom intermediate Stream operations — the same idea as `Collector` but for the middle of a pipeline, not the end. The JDK ships five built-ins; this library adds the ones that come up often in practice.

> New to Gatherers? See [GATHERERS.md](GATHERERS.md) for a quick overview of how they are composed.

## Requirements

Java 24+

## Install

```xml
<dependency>
    <groupId>io.github.manojshr</groupId>
    <artifactId>stream-gatherers</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Gatherers

### `CountingGatherers.frequency()`

```java
Stream.of("a", "b", "a", "c", "b", "a")
      .gather(CountingGatherers.frequency())
      .forEach(System.out::println);
// a=3
// b=2
// c=1
```

### `WindowGatherers.fixedWindow(n)`

Non-overlapping windows of size `n`. Trailing window may be smaller.

```java
Stream.of(1, 2, 3, 4, 5, 6, 7)
      .gather(WindowGatherers.fixedWindow(3))
      .forEach(System.out::println);
// [1, 2, 3]
// [4, 5, 6]
// [7]
```

### `WindowGatherers.slidingWindow(n)`

Overlapping windows of size `n`, advancing one element at a time.

```java
Stream.of(1, 2, 3, 4, 5)
      .gather(WindowGatherers.slidingWindow(3))
      .forEach(System.out::println);
// [1, 2, 3]
// [2, 3, 4]
// [3, 4, 5]
```

### `MathGatherers.runningSum()` / `MathGatherers.sum()`

```java
Stream.<Number>of(1, 2, 3, 4).gather(MathGatherers.runningSum()).forEach(System.out::println);
// 1, 3, 6, 10

Stream.<Number>of(1, 2, 3, 4).gather(MathGatherers.sum()).findFirst();
// Optional[10]
```

## License

Apache 2.0
