package io.github.manojshr.gatherers.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    @Test
    void runsByEmpty() {
        Assertions.assertThat(Stream.<Reading>of()
                .gather(ConsecutiveGatherers.runsBy(Reading::status)))
                .isEmpty();
    }

    @Test
    void runsBySingle() {
        Assertions.assertThat(Stream.of(new Reading(1, "ok"))
                .gather(ConsecutiveGatherers.runsBy(Reading::status)))
                .containsExactly(List.of(new Reading(1, "ok")));
    }

    @Test
    void runsByGroupsConsecutiveSameKey() {
        Assertions.assertThat(Stream.of("a", "a", "b", "b", "b", "a")
                .gather(ConsecutiveGatherers.runsBy(s -> s)))
                .containsExactly(
                        List.of("a", "a"),
                        List.of("b", "b", "b"),
                        List.of("a")
                );
    }

    @Test
    void runsByAllSameKey() {
        Assertions.assertThat(Stream.of("a", "a", "a")
                .gather(ConsecutiveGatherers.runsBy(s -> s)))
                .containsExactly(List.of("a", "a", "a"));
    }

    @Test
    void runsByAllDifferentKeys() {
        Assertions.assertThat(Stream.of("a", "b", "c")
                .gather(ConsecutiveGatherers.runsBy(s -> s)))
                .containsExactly(List.of("a"), List.of("b"), List.of("c"));
    }

    @Test
    void runsByEarlierRunsNotMutatedByLaterRuns() {
        List<List<String>> result = Stream.of("a", "a", "b", "c", "c", "c")
                .gather(ConsecutiveGatherers.runsBy(s -> s))
                .toList();

        Assertions.assertThat(result).containsExactly(
                List.of("a", "a"),
                List.of("b"),
                List.of("c", "c", "c")
        );
    }

    @Test
    void runsByShortCircuitDownstream() {
        Assertions.assertThat(Stream.iterate(0, i -> i + 1)
                .gather(ConsecutiveGatherers.runsBy(i -> i / 2))
                .limit(3))
                .hasSize(3);
    }

    @Test
    void runsByOnParallelStreamRunsSequentially() {
        Assertions.assertThat(Stream.of("a", "a", "b", "b", "c").parallel()
                .gather(ConsecutiveGatherers.runsBy(s -> s)))
                .containsExactly(
                        List.of("a", "a"),
                        List.of("b", "b"),
                        List.of("c")
                );
    }

    @Test
    void runsByThrowsOnNullKey() {
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> Stream.of("a", "b")
                        .gather(ConsecutiveGatherers.runsBy(s -> (String) null))
                        .toList());
    }

    @Test
    void runsEmpty() {
        Assertions.assertThat(Stream.<Integer>of()
                .gather(ConsecutiveGatherers.runs()))
                .isEmpty();
    }

    @Test
    void runsGroupsConsecutiveEqual() {
        Assertions.assertThat(Stream.of(1, 1, 2, 2, 2, 3, 1)
                .gather(ConsecutiveGatherers.runs()))
                .containsExactly(
                        List.of(1, 1),
                        List.of(2, 2, 2),
                        List.of(3),
                        List.of(1)
                );
    }

    @Test
    void splitOnEmptyStreamProducesNoOutput() {
        Assertions.assertThat(Stream.<String>of()
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .isEmpty();
    }

    @Test
    void splitOnNoSeparatorsYieldsSingleGroup() {
        Assertions.assertThat(Stream.of("a", "b", "c")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(List.of("a", "b", "c"));
    }

    @Test
    void splitOnDropsSeparators() {
        Assertions.assertThat(Stream.of("a", "b", "", "c", "d", "", "e")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(
                        List.of("a", "b"),
                        List.of("c", "d"),
                        List.of("e")
                );
    }

    @Test
    void splitOnLeadingSeparatorYieldsLeadingEmptyGroup() {
        Assertions.assertThat(Stream.of("", "a", "b")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(List.of(), List.of("a", "b"));
    }

    @Test
    void splitOnTrailingSeparatorYieldsTrailingEmptyGroup() {
        Assertions.assertThat(Stream.of("a", "b", "")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(List.of("a", "b"), List.of());
    }

    @Test
    void splitOnConsecutiveSeparatorsYieldEmptyGroupsBetween() {
        Assertions.assertThat(Stream.of("a", "", "", "b")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(List.of("a"), List.of(), List.of("b"));
    }

    @Test
    void splitOnAllSeparators() {
        Assertions.assertThat(Stream.of("", "")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(List.of(), List.of(), List.of());
    }

    @Test
    void splitOnEarlierGroupsNotMutatedByLaterGroups() {
        List<List<String>> result = Stream.of("a", "a", "", "b", "", "c", "c")
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty))
                .toList();

        Assertions.assertThat(result).containsExactly(
                List.of("a", "a"),
                List.of("b"),
                List.of("c", "c")
        );
    }

    @Test
    void splitOnShortCircuitDownstream() {
        Assertions.assertThat(Stream.iterate(0, i -> i + 1)
                .gather(ConsecutiveGatherers.splitOn(i -> i % 3 == 0))
                .limit(3))
                .hasSize(3);
    }

    @Test
    void splitOnParallelStreamRunsSequentially() {
        Assertions.assertThat(Stream.of("a", "", "b", "", "c").parallel()
                .gather(ConsecutiveGatherers.splitOn(String::isEmpty)))
                .containsExactly(List.of("a"), List.of("b"), List.of("c"));
    }
}
