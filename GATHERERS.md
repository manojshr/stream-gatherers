# How Gatherers Work

Think of a `Gatherer` as the middle piece of a stream pipeline — sits between the source and the terminal operation, same slot as `filter` or `map`, but you define exactly what it does.

```
Stream.of(...)
      .gather(myGatherer)   // ← this is where a Gatherer lives
      .collect(...)
```

If you know `Collector`, this is the same idea but for the middle instead of the end.

---

A Gatherer is made of up to four pieces. You don't always need all four.

### 1. Initializer
Creates the state the gatherer holds while elements flow through. Skip this if the gatherer doesn't need state.

```java
Supplier<Map<K, Long>> initializer = HashMap::new;
```

### 2. Integrator
Called once per element — this is where the actual work happens.
Return `true` to keep going, `false` to stop early.

```java
Gatherer.Integrator<Map<K, Long>, K, Map.Entry<K, Long>> integrator =
    (state, element, downstream) -> {
        if (downstream.isRejecting()) return false;
        state.merge(element, 1L, Long::sum);
        return true;
    };
```

### 3. Combiner
Only needed if the gatherer runs in parallel. Merges two independent pieces of state back together.
No combiner → use `Gatherer.ofSequential`. With combiner → use `Gatherer.of`.

```java
BinaryOperator<Map<K, Long>> combiner = (left, right) -> {
    right.forEach((k, v) -> left.merge(k, v, Long::sum));
    return left;
};
```

### 4. Finisher
Runs once after all elements are done. Useful for pushing out anything still sitting in state (a partial window, a final total, etc.). Skip this if there's nothing to flush at the end.

```java
BiConsumer<Map<K, Long>, Gatherer.Downstream<? super Map.Entry<K, Long>>> finisher =
    (state, downstream) -> state.entrySet().forEach(downstream::push);
```

---

## Sequential vs parallel

Use `Gatherer.ofSequential` when order matters or state can't be split (e.g. a sliding window).
Use `Gatherer.of` when you can merge two independent states (e.g. a frequency count).

---

## How to write a gatherer

Each gatherer here defines its pieces as named locals first, then assembles them at the end:

```java
public static <K> Gatherer<K, Map<K, Long>, Map.Entry<K, Long>> frequency() {
    Supplier<...>             initializer = ...;
    Gatherer.Integrator<...>  integrator  = ...;
    BinaryOperator<...>       combiner    = ...;
    BiConsumer<...>           finisher    = ...;

    return Gatherer.of(initializer, integrator, combiner, finisher);
}
```
