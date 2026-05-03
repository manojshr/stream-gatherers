package io.github.manojshr.gatherers.window;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Gatherers that group consecutive elements into list-shaped windows.
 *
 * @since 1.0.0
 */
public class WindowGatherers {

    /**
     * Groups elements into non-overlapping fixed-size windows.
     * The final window is emitted even if it is shorter than {@code limit}.
     *
     * <p>Example:
     * {@snippet :
     * Stream.of(1, 2, 3, 4, 5, 6, 7)
     *       .gather(WindowGatherers.fixedWindow(3))
     *       .forEach(System.out::println);
     * // [1, 2, 3]
     * // [4, 5, 6]
     * // [7]
     * }
     *
     * @param <T> element type
     * @param limit window size; must be positive
     * @return a sequential gatherer emitting fixed-size windows in input order
     * @throws IllegalArgumentException if {@code limit <= 0}
     * @since 1.0.0
     */
    public static <T> Gatherer<T, ?, List<T>> fixedWindow(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("window size must be positive, got " + limit);
        }
        record FixedWindow<T>(List<T> window) {
            public FixedWindow(int limit) {
                this(new ArrayList<>(limit));
            }
        }

        Supplier<FixedWindow<T>> initializer = () -> new FixedWindow<>(limit);
        Gatherer.Integrator<FixedWindow<T>, T, List<T>> integrator = (state, element, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            }
            state.window.add(element);
            if (state.window.size() == limit) {
                downstream.push(List.copyOf(state.window));
                state.window.clear();
            }
            return true;
        };
        BiConsumer<FixedWindow<T>, Gatherer.Downstream<? super List<T>>> finisher = (state, downstream) -> {
            if (!downstream.isRejecting() && !state.window.isEmpty()) {
                downstream.push(List.copyOf(state.window));
            }
        };
        return Gatherer.ofSequential(
                initializer,
                integrator,
                finisher
        );
    }

    /**
     * Groups elements into overlapping windows that slide one element at a time.
     * No window is emitted until {@code limit} elements have been seen, so a
     * stream shorter than {@code limit} produces no output.
     *
     * <p>Example:
     * {@snippet :
     * Stream.of(1, 2, 3, 4, 5)
     *       .gather(WindowGatherers.slidingWindow(3))
     *       .forEach(System.out::println);
     * // [1, 2, 3]
     * // [2, 3, 4]
     * // [3, 4, 5]
     * }
     *
     * @param <T> element type
     * @param limit window size; must be positive
     * @return a sequential gatherer emitting overlapping windows in input order
     * @throws IllegalArgumentException if {@code limit <= 0}
     * @since 1.0.0
     */
    public static <T> Gatherer<T, ?, List<T>> slidingWindow(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("window size must be positive, got " + limit);
        }
        record SlidingWindow<T>(List<T> window) {
            public SlidingWindow() {
                this(new LinkedList<>());
            }
        }
        Supplier<SlidingWindow<T>> initializer = SlidingWindow::new;
        Gatherer.Integrator<SlidingWindow<T>, T, List<T>> integrator = (state, element, downstream) -> {
            if (downstream.isRejecting()) {
                return false;
            }
            state.window.add(element);
            if (state.window.size() == limit) {
                downstream.push(List.copyOf(state.window));
                state.window.removeFirst();
            }
            return true;
        };
        return Gatherer.ofSequential(
                initializer,
                integrator
        );
    }
}
