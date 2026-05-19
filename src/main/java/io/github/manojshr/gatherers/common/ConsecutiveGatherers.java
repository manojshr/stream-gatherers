package io.github.manojshr.gatherers.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Gatherers that act on runs of consecutive elements — collapsing or grouping
 * them by a key or a predicate. All operate on adjacency, not the whole stream.
 *
 * @since 1.2.0
 */
public class ConsecutiveGatherers {

    private static final class LastKey<K> {
        K key;
    }

    /**
     * Drops an element when its key matches the previous emitted element's
     * key. Only consecutive runs collapse, so the same key can appear again
     * later in the stream.
     *
     * <p>This gatherer runs sequentially even when the upstream stream is
     * parallel — "differs from the previous element" depends on encounter
     * order, which cannot be preserved across parallel chunks.
     *
     * <p>Example:
     * {@snippet :
     * record Reading(int id, String status) {}
     *
     * Stream.of(new Reading(1, "ok"), new Reading(2, "ok"),
     *           new Reading(3, "err"), new Reading(4, "ok"))
     *       .gather(ConsecutiveGatherers.distinctUntilChanged(Reading::status))
     *       .forEach(System.out::println);
     * // Reading[id=1, status=ok]
     * // Reading[id=3, status=err]
     * // Reading[id=4, status=ok]
     * }
     *
     * @param keyFn extracts the key used for equality; must not return {@code null}
     * @param <T>   element type
     * @param <K>   key type
     * @return a sequential gatherer that drops consecutive same-key elements
     * @throws NullPointerException if {@code keyFn} returns {@code null} for any element
     * @since 1.2.0
     */
    public static <T, K> Gatherer<T, ?, T> distinctUntilChanged(Function<? super T, ? extends K> keyFn) {
        Supplier<LastKey<K>> initializer = LastKey::new;

        Gatherer.Integrator<LastKey<K>, T, T> integrator = (state, element, downstream) -> {
            K key = keyFn.apply(element);
            Objects.requireNonNull(key, "keyFn returned null");
            if (!key.equals(state.key)) {
                state.key = key;
                return downstream.push(element);
            }
            return true;
        };

        return Gatherer.ofSequential(initializer, integrator);
    }

    /**
     * Drops an element when it equals the previous emitted element. Only
     * consecutive runs collapse; a value can appear again later. Same as
     * {@link #distinctUntilChanged} with the identity function.
     *
     * <p>This gatherer runs sequentially even when the upstream stream is parallel.
     *
     * <p>Example:
     * {@snippet :
     * Stream.of("a", "a", "b", "b", "b", "a", "c", "c")
     *       .gather(ConsecutiveGatherers.distinctConsecutive())
     *       .forEach(System.out::println);
     * // a
     * // b
     * // a
     * // c
     * }
     *
     * @param <T> element type
     * @return a sequential gatherer that drops consecutive equal elements
     * @since 1.2.0
     */
    public static <T> Gatherer<T, ?, T> distinctConsecutive() {
        return distinctUntilChanged(Function.identity());
    }

    private static final class Run<T, K> {
        private List<T> elements = new ArrayList<>();
        private K key;

        boolean isEmpty() {
            return elements.isEmpty();
        }

        boolean matches(K candidate) {
            return candidate.equals(key);
        }

        void openWith(T element, K elementKey) {
            elements.add(element);
            key = elementKey;
        }

        void add(T element) {
            elements.add(element);
        }

        List<T> flush() {
            // swap in a fresh list so the returned one can't be mutated after it's pushed
            List<T> completed = elements;
            elements = new ArrayList<>();
            return completed;
        }
    }

    /**
     * Groups maximal runs of consecutive elements that produce an equal key.
     * Each run is emitted as a {@code List} when the key changes; the final
     * run is emitted when the stream ends.
     *
     * <p>This gatherer runs sequentially even when the upstream stream is
     * parallel — run boundaries depend on encounter order, which cannot be
     * preserved across parallel chunks.
     *
     * <p>Example:
     * {@snippet :
     * Stream.of("a", "a", "b", "b", "b", "a")
     *       .gather(ConsecutiveGatherers.runsBy(s -> s))
     *       .forEach(System.out::println);
     * // [a, a]
     * // [b, b, b]
     * // [a]
     * }
     *
     * @param keyFn extracts the key used for equality; must not return {@code null}
     * @param <T>   element type
     * @param <K>   key type
     * @return a sequential gatherer emitting one list per run of equal-key elements
     * @throws NullPointerException if {@code keyFn} returns {@code null} for any element
     * @since 1.2.0
     */
    public static <T, K> Gatherer<T, ?, List<T>> runsBy(Function<? super T, ? extends K> keyFn) {
        Supplier<Run<T, K>> initializer = Run::new;

        Gatherer.Integrator<Run<T, K>, T, List<T>> integrator = (state, element, downstream) -> {
            K key = keyFn.apply(element);
            Objects.requireNonNull(key, "keyFn returned null");

            if (state.isEmpty()) {
                state.openWith(element, key);
                return true;
            }
            if (state.matches(key)) {
                state.add(element);
                return true;
            }
            boolean ok = downstream.push(state.flush());
            state.openWith(element, key);
            return ok;
        };

        BiConsumer<Run<T, K>, Gatherer.Downstream<? super List<T>>> finisher = (state, downstream) -> {
            if (!state.isEmpty() && !downstream.isRejecting()) {
                downstream.push(state.flush());
            }
        };

        return Gatherer.ofSequential(
                initializer,
                integrator,
                finisher
        );
    }

    /**
     * Groups maximal runs of consecutive equal elements (by {@code equals})
     * into lists. Same as {@link #runsBy} with the identity function.
     *
     * <p>This gatherer runs sequentially even when the upstream stream is parallel.
     *
     * <p>Example:
     * {@snippet :
     * Stream.of(1, 1, 2, 2, 2, 3, 1)
     *       .gather(ConsecutiveGatherers.runs())
     *       .forEach(System.out::println);
     * // [1, 1]
     * // [2, 2, 2]
     * // [3]
     * // [1]
     * }
     *
     * @param <T> element type
     * @return a sequential gatherer emitting one list per run of equal elements
     * @since 1.2.0
     */
    public static <T> Gatherer<T, ?, List<T>> runs() {
        return runsBy(Function.identity());
    }
}
