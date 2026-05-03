package io.github.manojshr.gatherers.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

class CountingGatherersTest {

    @Test
    void frequencyParallel() {
        Assertions.assertThat(Stream.of("a", "b", "a", "c", "b", "a", "d").parallel()
                .gather(CountingGatherers.frequency()))
                .containsExactlyInAnyOrder(
                        Map.entry("a", 3L),
                        Map.entry("b", 2L),
                        Map.entry("c", 1L),
                        Map.entry("d", 1L)
                );
    }

    @Test
    void frequencySequential() {
        Assertions.assertThat(Stream.of("a", "b", "a", "c", "b", "a", "d")
                .gather(CountingGatherers.frequency()))
                .containsExactlyInAnyOrder(
                        Map.entry("a", 3L),
                        Map.entry("b", 2L),
                        Map.entry("c", 1L),
                        Map.entry("d", 1L)
                );
    }

    @Test
    void frequencyEmpty() {
        Assertions.assertThat(Stream.<String>of().gather(CountingGatherers.frequency()))
                .isEmpty();
    }

    @Test
    void frequencySingle() {
        Assertions.assertThat(Stream.of("a").gather(CountingGatherers.frequency()))
                .containsExactly(Map.entry("a", 1L));
    }

    @Test
    void frequencyShortCircuitDownstream() {
        Assertions.assertThat(Stream.of("a", "b", "c", "d", "e")
                .gather(CountingGatherers.frequency())
                .limit(2))
                .hasSize(2);
    }

    @Test
    void frequencyParallelStress() {
        int count = 10_000;
        Stream<String> stream = Stream.concat(
                Stream.generate(() -> "a").limit(count),
                Stream.generate(() -> "b").limit(count)
        ).parallel();

        Assertions.assertThat(stream.gather(CountingGatherers.frequency()))
                .containsExactlyInAnyOrder(
                        Map.entry("a", (long) count),
                        Map.entry("b", (long) count)
                );
    }
}
