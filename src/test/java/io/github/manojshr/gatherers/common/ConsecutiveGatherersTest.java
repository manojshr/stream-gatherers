package io.github.manojshr.gatherers.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

class ConsecutiveGatherersTest {

    record Reading(int id, String status) {}

    @Test
    void distinctUntilChangedEmpty() {
        Assertions.assertThat(Stream.<Reading>of()
                .gather(ConsecutiveGatherers.distinctUntilChanged(Reading::status)))
                .isEmpty();
    }

    @Test
    void distinctUntilChangedSingle() {
        Assertions.assertThat(Stream.of(new Reading(1, "ok"))
                .gather(ConsecutiveGatherers.distinctUntilChanged(Reading::status)))
                .containsExactly(new Reading(1, "ok"));
    }

    @Test
    void distinctUntilChangedCollapsesConsecutiveSameKey() {
        Assertions.assertThat(Stream.of(
                        new Reading(1, "ok"),
                        new Reading(2, "ok"),
                        new Reading(3, "err"),
                        new Reading(4, "ok"))
                .gather(ConsecutiveGatherers.distinctUntilChanged(Reading::status)))
                .containsExactly(
                        new Reading(1, "ok"),
                        new Reading(3, "err"),
                        new Reading(4, "ok")
                );
    }

    @Test
    void distinctUntilChangedAllSameKey() {
        Assertions.assertThat(Stream.of(
                        new Reading(1, "ok"),
                        new Reading(2, "ok"),
                        new Reading(3, "ok"))
                .gather(ConsecutiveGatherers.distinctUntilChanged(Reading::status)))
                .containsExactly(new Reading(1, "ok"));
    }

    @Test
    void distinctUntilChangedAllDifferentKeys() {
        Assertions.assertThat(Stream.of(
                        new Reading(1, "a"),
                        new Reading(2, "b"),
                        new Reading(3, "c"))
                .gather(ConsecutiveGatherers.distinctUntilChanged(Reading::status)))
                .containsExactly(
                        new Reading(1, "a"),
                        new Reading(2, "b"),
                        new Reading(3, "c")
                );
    }

    @Test
    void distinctUntilChangedKeyMayReappearLater() {
        Assertions.assertThat(Stream.of("a", "a", "b", "a", "a")
                .gather(ConsecutiveGatherers.distinctUntilChanged(s -> s)))
                .containsExactly("a", "b", "a");
    }

    @Test
    void distinctUntilChangedShortCircuitDownstream() {
        Assertions.assertThat(Stream.iterate(0, i -> i + 1)
                .gather(ConsecutiveGatherers.distinctUntilChanged(i -> i / 2))
                .limit(3))
                .hasSize(3);
    }

    @Test
    void distinctUntilChangedOnParallelStreamRunsSequentially() {
        Assertions.assertThat(Stream.of("a", "a", "b", "b", "c", "a").parallel()
                .gather(ConsecutiveGatherers.distinctUntilChanged(s -> s)))
                .containsExactly("a", "b", "c", "a");
    }

    @Test
    void distinctUntilChangedThrowsOnNullKey() {
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> Stream.of("a", "b")
                        .gather(ConsecutiveGatherers.distinctUntilChanged(s -> (String) null))
                        .toList());
    }

    @Test
    void distinctConsecutiveEmpty() {
        Assertions.assertThat(Stream.<String>of()
                .gather(ConsecutiveGatherers.distinctConsecutive()))
                .isEmpty();
    }

    @Test
    void distinctConsecutiveCollapsesRuns() {
        Assertions.assertThat(Stream.of("a", "a", "b", "b", "b", "a", "c", "c")
                .gather(ConsecutiveGatherers.distinctConsecutive()))
                .containsExactly("a", "b", "a", "c");
    }

    @Test
    void distinctConsecutiveKeepsNonAdjacentDuplicates() {
        Assertions.assertThat(Stream.of(1, 1, 2, 1, 1, 2, 2)
                .gather(ConsecutiveGatherers.distinctConsecutive()))
                .containsExactly(1, 2, 1, 2);
    }
}
