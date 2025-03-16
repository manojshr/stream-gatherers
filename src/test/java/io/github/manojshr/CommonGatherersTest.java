package io.github.manojshr;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

class CommonGatherersTest {

    @Test
    void frequency() {
        Assertions.assertThat(Stream.of("a", "b", "a", "c", "b", "a", "d").parallel()
                .gather(CommonGatherers.frequency()))
                .containsExactly(
                        Map.entry("a", 3L),
                        Map.entry("b", 2L),
                        Map.entry("c", 1L),
                        Map.entry("d", 1L)
                );
    }
}