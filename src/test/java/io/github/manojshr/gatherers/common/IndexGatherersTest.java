package io.github.manojshr.gatherers.common;

import io.github.manojshr.gatherers.common.IndexGatherers.Indexed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class IndexGatherersTest {

    @Test
    void zipWithIndexEmpty() {
        Assertions.assertThat(Stream.<String>of()
                .gather(IndexGatherers.zipWithIndex()))
                .isEmpty();
    }

    @Test
    void zipWithIndexSingle() {
        Assertions.assertThat(Stream.of("a")
                .gather(IndexGatherers.zipWithIndex()))
                .containsExactly(new Indexed<>(0L, "a"));
    }

    @Test
    void zipWithIndexMultiple() {
        Assertions.assertThat(Stream.of("a", "b", "c")
                .gather(IndexGatherers.zipWithIndex()))
                .containsExactly(
                        new Indexed<>(0L, "a"),
                        new Indexed<>(1L, "b"),
                        new Indexed<>(2L, "c")
                );
    }

    @Test
    void zipWithIndexShortCircuitDownstream() {
        Assertions.assertThat(Stream.iterate(0, i -> i + 1)
                .gather(IndexGatherers.zipWithIndex())
                .limit(3))
                .containsExactly(
                        new Indexed<>(0L, 0),
                        new Indexed<>(1L, 1),
                        new Indexed<>(2L, 2)
                );
    }

    @Test
    void zipWithIndexOnParallelStreamRunsSequentially() {
        Assertions.assertThat(Stream.of("a", "b", "c", "d").parallel()
                .gather(IndexGatherers.zipWithIndex()))
                .containsExactly(
                        new Indexed<>(0L, "a"),
                        new Indexed<>(1L, "b"),
                        new Indexed<>(2L, "c"),
                        new Indexed<>(3L, "d")
                );
    }
}
