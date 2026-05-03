package io.github.manojshr.gatherers.math;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Gatherers that perform arithmetic over a stream of {@code Number}s.
 *
 * <p>When the input mixes numeric types (e.g. {@code Integer} with {@code Long}),
 * the running value is widened to {@code double}. Stick to a single numeric type
 * for exact results.
 *
 * @since 1.0.0
 */
public class MathGatherers {

    /**
     * Internal accumulator used by {@link #sum()} and {@link #runningSum()}.
     * Exposed as part of the gatherer's state type signature; not intended for
     * direct use and may become package-private in a future major release.
     *
     * @since 1.0.0
     */
    public static class AdditionProcessor {
        private Number sum = null;
        public Number add(Number incomingValue) {
            if (incomingValue == null) {
                return sum;
            }
            if (this.sum == null) {
                this.sum = incomingValue;
            } else {
                this.sum  = addNumbers(this.sum, incomingValue);
            }
            return this.sum;
        }

        private Number addNumbers(Number sumAccum, Number incomingValue) {
            return switch (sumAccum) {
                case Long sumValue when incomingValue instanceof Long value -> sumValue + value;
                case Integer sumValue when incomingValue instanceof Integer value -> sumValue + value;
                case Float sumValue when incomingValue instanceof Float value -> sumValue + value;
                default -> sumAccum.doubleValue() + incomingValue.doubleValue();
            };
        }
    }

    /**
     * Emits the running sum after each element. An empty input produces no output.
     *
     * <p>Example:
     * {@snippet :
     * Stream.<Number>of(1, 2, 3, 4)
     *       .gather(MathGatherers.runningSum())
     *       .forEach(System.out::println);
     * // 1
     * // 3
     * // 6
     * // 10
     * }
     *
     * @return a sequential gatherer emitting one running total per input element
     * @since 1.0.0
     */
    public static Gatherer<Number, AdditionProcessor, Number> runningSum() {
        Supplier<AdditionProcessor> initializer = AdditionProcessor::new;
        Gatherer.Integrator<AdditionProcessor, Number, Number> integrator = (state, element, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            }
            downstream.push(state.add(element));
            return true;
        };

        return Gatherer.ofSequential(
                initializer,
                integrator
        );
    }

    /**
     * Emits a single total after the input stream is exhausted.
     * An empty input produces no output.
     *
     * <p>Example:
     * {@snippet :
     * Number total = Stream.<Number>of(1, 2, 3, 4)
     *       .gather(MathGatherers.sum())
     *       .findFirst()
     *       .orElse(0);
     * // total = 10
     * }
     *
     * @return a parallel-capable gatherer that emits one total at end of stream
     * @since 1.0.0
     */
    public static Gatherer<Number, AdditionProcessor, Number> sum() {
        Supplier<AdditionProcessor> initializer = AdditionProcessor::new;
        Gatherer.Integrator<AdditionProcessor, Number, Number> integrator = (state, element, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            }
            state.add(element);
            return true;
        };
        BinaryOperator<AdditionProcessor> combiner = (left, right) -> {
            if (left.sum == null) {
                return right;
            }
            if (right.sum == null) {
                return left;
            }
            left.sum = left.add(right.sum);
            return left;
        };

        BiConsumer<AdditionProcessor, Gatherer.Downstream<? super Number>> finisher = (state, downstream) -> {
            if (state.sum != null && !downstream.isRejecting()) {
                downstream.push(state.sum);
            }
        };

        return Gatherer.of(
                initializer,
                integrator,
                combiner,
                finisher
        );
    }
}
