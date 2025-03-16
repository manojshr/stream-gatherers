package io.github.manojshr;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class MathGatherersTest {

    @ParameterizedTest
    @MethodSource("runningSumGathererMethodSource")
    void runningSum(List<Number> inputList, List<Number> expectedRunningSum) {
        Assertions.assertThat(inputList.stream().gather(MathGatherers.runningSum()))
                .containsExactly(expectedRunningSum.toArray(Number[]::new));
    }
    public static Stream<Arguments> runningSumGathererMethodSource() {
        return Stream.of(
                Arguments.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), List.of(1, 3, 6, 10, 15, 21, 28, 36, 45, 55)),
                Arguments.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10), List.of(1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 65, 75)),
                Arguments.of(List.of(1L, (long) Integer.MAX_VALUE, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L),
                        List.of(1L, 2147483648L, 2147483650L, 2147483653L, 2147483657L, 2147483662L, 2147483668L, 2147483675L, 2147483683L, 2147483692L, 2147483702L)),
                Arguments.of(List.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f), List.of(1.0f, 3.0f, 6.0f, 10.0f, 15.0f, 21.0f, 28.0f, 36.0f, 45.0f, 55.0f)),
                Arguments.of(List.of(1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d, 7.0d, 8.0d, 9.0d, 10.0d), List.of(1.0d, 3.0d, 6.0d, 10.0d, 15.0d, 21.0d, 28.0d, 36.0d, 45.0d, 55.0d))
        );
    }

    @ParameterizedTest
    @MethodSource("sumGathererMethodSource")
    void sum(List<Number> inputList, Number expectedSum) {
        Assertions.assertThat(inputList.stream().parallel().gather(MathGatherers.sum()))
                .contains(expectedSum);
        Assertions.assertThat(inputList.stream().gather(MathGatherers.sum())).contains(expectedSum);
    }

    public static Stream<Arguments> sumGathererMethodSource() {
        return Stream.of(
                Arguments.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 55),
                Arguments.of(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10), 75),
                Arguments.of(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, (long) Integer.MAX_VALUE), 2147483702L),
                Arguments.of(List.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f), 55.0f),
                Arguments.of(List.of(1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d, 7.0d, 8.0d, 9.0d, 10.0d), 55.0d)
        );
    }
}