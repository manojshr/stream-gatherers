package io.github.manojshr.gatherers.window;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class WindowGatherersTest {

    @Test
    void verifyFixedWindow() {
        assertThat(
                Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                        .gather(WindowGatherers.fixedWindow(3))
        ).containsExactly(
                List.of(1, 2, 3),
                List.of(4, 5, 6),
                List.of(7, 8, 9),
                List.of(10)
        );

        assertThat(Stream.of(1, 2).gather(WindowGatherers.fixedWindow(3)))
                .containsExactly(List.of(1, 2));

        assertThat(Stream.of(1, 2).gather(WindowGatherers.fixedWindow(2)))
                .containsExactly(List.of(1, 2));
    }


    @Test
    void slidingWindow() {
        assertThat(
                Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                        .gather(WindowGatherers.slidingWindow(3))
        ).containsExactly(
                List.of(1, 2, 3),
                List.of(2, 3, 4),
                List.of(3, 4, 5),
                List.of(4, 5, 6),
                List.of(5, 6, 7),
                List.of(6, 7, 8),
                List.of(7, 8, 9),
                List.of(8, 9, 10)
        );

        assertThat(Stream.of(1, 2).gather(WindowGatherers.slidingWindow(3)))
                .isEmpty();

        assertThat(Stream.of(1, 2).gather(WindowGatherers.slidingWindow(2)))
                .containsExactly(List.of(1, 2));
    }

    @Test
    void fixedWindowOnEmptyStream() {
        assertThat(Stream.<Integer>of().gather(WindowGatherers.fixedWindow(3)))
                .isEmpty();
    }

    @Test
    void slidingWindowOnEmptyStream() {
        assertThat(Stream.<Integer>of().gather(WindowGatherers.slidingWindow(3)))
                .isEmpty();
    }

    @Test
    void fixedWindowShortCircuitDownstream() {
        assertThat(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(WindowGatherers.fixedWindow(2))
                .limit(2))
                .containsExactly(List.of(1, 2), List.of(3, 4));
    }

    @Test
    void slidingWindowShortCircuitDownstream() {
        assertThat(Stream.of(1, 2, 3, 4, 5)
                .gather(WindowGatherers.slidingWindow(2))
                .limit(2))
                .containsExactly(List.of(1, 2), List.of(2, 3));
    }

    @Test
    void fixedWindowRejectsNonPositiveSize() {
        assertThatIllegalArgumentException().isThrownBy(() -> WindowGatherers.fixedWindow(0));
        assertThatIllegalArgumentException().isThrownBy(() -> WindowGatherers.fixedWindow(-1));
    }

    @Test
    void slidingWindowRejectsNonPositiveSize() {
        assertThatIllegalArgumentException().isThrownBy(() -> WindowGatherers.slidingWindow(0));
        assertThatIllegalArgumentException().isThrownBy(() -> WindowGatherers.slidingWindow(-1));
    }
}
