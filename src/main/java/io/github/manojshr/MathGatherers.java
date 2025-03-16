package io.github.manojshr;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class MathGatherers {

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
                case null  -> throw new RuntimeException("Invalid state");
                default -> sumAccum.doubleValue() + incomingValue.doubleValue();
            };
        }
    }

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
            if (!downstream.isRejecting()) {
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
