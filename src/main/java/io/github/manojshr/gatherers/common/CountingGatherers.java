package io.github.manojshr.gatherers.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Gatherers that count elements as they flow through a stream.
 *
 * @since 1.0.0
 */
public class CountingGatherers {

    /**
     * Counts how many times each element appears, emitting one
     * {@code Map.Entry<K, Long>} per distinct element after the input
     * stream is exhausted. Emission order is unspecified.
     *
     * <p>Example:
     * {@snippet :
     * Stream.of("a", "b", "a", "c", "b", "a")
     *       .gather(CountingGatherers.frequency())
     *       .forEach(System.out::println);
     * // a=3
     * // b=2
     * // c=1
     * }
     *
     * @param <K> element type; equality is determined by {@code equals}/{@code hashCode}
     * @return a parallel-capable gatherer producing one entry per distinct element
     * @since 1.0.0
     */
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
            for (Map.Entry<K, Long> entry : state.entrySet()) {
                if (downstream.isRejecting()) {
                    return;
                }
                downstream.push(entry);
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
