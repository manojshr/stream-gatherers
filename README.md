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

### `DeduplicationGatherers.distinctBy(keyFn)`

Keeps the first element seen for each key, drops the rest.

```java
record Person(String name, int age) {}

Stream.of(new Person("Alice", 30), new Person("Bob", 25), new Person("Alice", 40))
      .gather(DeduplicationGatherers.distinctBy(Person::name))
      .forEach(System.out::println);
// Person[name=Alice, age=30]
// Person[name=Bob, age=25]
```

Runs sequentially even on parallel streams. The combiner can only merge *state*, not already-emitted elements — two chunks would each emit their own first-seen element for the same key, leaving duplicates the combiner can't undo. So `distinctBy` stays sequential.

### `IndexGatherers.zipWithIndex()`

Pairs each element with its zero-based position in the stream.

```java
Stream.of("a", "b", "c")
      .gather(IndexGatherers.zipWithIndex())
      .forEach(System.out::println);
// Indexed[index=0, value=a]
// Indexed[index=1, value=b]
// Indexed[index=2, value=c]
```

Runs sequentially even on parallel streams. Index reflects encounter order in the original stream, which can't be reconstructed once parallel chunks split the work.

## License

Apache 2.0
