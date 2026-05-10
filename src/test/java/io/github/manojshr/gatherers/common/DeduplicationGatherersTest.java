package io.github.manojshr.gatherers.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class DeduplicationGatherersTest {

    record Person(String name, int age) {}

    @Test
    void distinctByEmpty() {
        Assertions.assertThat(Stream.<Person>of()
                .gather(DeduplicationGatherers.distinctBy(Person::name)))
                .isEmpty();
    }

    @Test
    void distinctBySingle() {
        Assertions.assertThat(Stream.of(new Person("Alice", 30))
                .gather(DeduplicationGatherers.distinctBy(Person::name)))
                .containsExactly(new Person("Alice", 30));
    }

    @Test
    void distinctByFirstWins() {
        Assertions.assertThat(Stream.of(
                        new Person("Alice", 30),
                        new Person("Bob", 25),
                        new Person("Alice", 40),
                        new Person("Bob", 35))
                .gather(DeduplicationGatherers.distinctBy(Person::name)))
                .containsExactly(
                        new Person("Alice", 30),
                        new Person("Bob", 25)
                );
    }

    @Test
    void distinctByAllUnique() {
        Assertions.assertThat(Stream.of("a", "b", "c", "d")
                .gather(DeduplicationGatherers.distinctBy(s -> s)))
                .containsExactly("a", "b", "c", "d");
    }

    @Test
    void distinctByAllDuplicate() {
        Assertions.assertThat(Stream.of("a", "a", "a", "a")
                .gather(DeduplicationGatherers.distinctBy(s -> s)))
                .containsExactly("a");
    }

    @Test
    void distinctByOnParallelStreamRunsSequentially() {
        Assertions.assertThat(Stream.of("a", "b", "c", "a", "b", "d").parallel()
                .gather(DeduplicationGatherers.distinctBy(s -> s)))
                .containsExactly("a", "b", "c", "d");
    }

    @Test
    void distinctByShortCircuitDownstream() {
        Assertions.assertThat(Stream.iterate(0, i -> i + 1)
                .gather(DeduplicationGatherers.distinctBy(i -> i % 10))
                .limit(3))
                .hasSize(3);
    }

    @Test
    void distinctByKeyExtractorMappingMultipleToSameKey() {
        Assertions.assertThat(Stream.of("apple", "ant", "banana", "berry", "cherry")
                .gather(DeduplicationGatherers.distinctBy(s -> s.charAt(0))))
                .containsExactly("apple", "banana", "cherry");
    }
}
