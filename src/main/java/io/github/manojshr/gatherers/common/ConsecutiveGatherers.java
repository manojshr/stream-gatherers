package io.github.manojshr.gatherers.common;

import java.util.Objects;
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
}
