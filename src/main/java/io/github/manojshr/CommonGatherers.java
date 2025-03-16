package io.github.manojshr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class CommonGatherers {

    public static <K> Gatherer<K, Map<K, Long>, Map.Entry<K, Long>> frequency() {
        Supplier<Map<K, Long>> initializer = HashMap::new;
        Gatherer.Integrator<Map<K, Long>, K, Map.Entry<K, Long>> integrator = (state, element, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            }
            Long counter = state.getOrDefault(element, 0L) + 1;
            state.put(element, counter);
            return true;
        };

        BinaryOperator<Map<K, Long>> combiner = (left, right) -> {
            right.forEach((key, value) -> left.merge(key, value, Long::sum));
            return left;
        };


        BiConsumer<Map<K, Long>, Gatherer.Downstream<? super Map.Entry<K, Long>>> finisher = (state, downstream) -> {
           if (!downstream.isRejecting() && !state.isEmpty()) {
               state.entrySet().forEach(downstream::push);
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
