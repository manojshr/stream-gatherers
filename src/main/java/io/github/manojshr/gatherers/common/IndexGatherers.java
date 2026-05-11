package io.github.manojshr.gatherers.common;

import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Gatherers that attach positional information to stream elements.
 *
 * @since 1.1.0
 */
public class IndexGatherers {

    /**
     * An element paired with its zero-based position in the stream.
     *
     * @param index zero-based position in the original stream
     * @param value the element at that position
     * @param <T>   element type
     * @since 1.1.0
     */
    public record Indexed<T>(long index, T value) {}

    private static final class Counter {
        long value;
        long incrementAndGet() {
            return value++;
        }
    }

    /**
     * Pairs each element with its zero-based position in the stream.
     *
     * <p>This gatherer runs sequentially even when the upstream stream is parallel</p>
     *
     * <p>Example:
     * {@snippet :
     * Stream.of("a", "b", "c")
     *       .gather(IndexGatherers.zipWithIndex())
     *       .forEach(System.out::println);
     * // Indexed[index=0, value=a]
     * // Indexed[index=1, value=b]
     * // Indexed[index=2, value=c]
     * }
     *
     * @param <T> element type
     * @return a sequential gatherer that emits each element paired with its index
     * @since 1.1.0
     */
    public static <T> Gatherer<T, ?, Indexed<T>> zipWithIndex() {
        Supplier<Counter> initializer = Counter::new;

        Gatherer.Integrator<Counter, T, Indexed<T>> integrator = (state, element, downstream) ->
                downstream.push(new Indexed<>(state.incrementAndGet(), element));

        return Gatherer.ofSequential(initializer, integrator);
    }
}
