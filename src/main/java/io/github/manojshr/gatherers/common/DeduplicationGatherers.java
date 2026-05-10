package io.github.manojshr.gatherers.common;

import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

/**
 * Gatherers that filter out duplicate elements from a stream.
 *
 * @since 1.1.0
 */
public class DeduplicationGatherers {

    /**
     * Filters elements so only the first occurrence of each key is emitted.
     * Subsequent elements producing a key already seen are dropped.
     *
     * <p>This gatherer runs sequentially even when the upstream stream is
     * parallel</p>
     *
     * <p>Example:
     * {@snippet :
     * record Person(String name, int age) {}
     *
     * Stream.of(new Person("Alice", 30), new Person("Bob", 25), new Person("Alice", 40))
     *       .gather(DeduplicationGatherers.distinctBy(Person::name))
     *       .forEach(System.out::println);
     * // Person[name=Alice, age=30]
     * // Person[name=Bob, age=25]
     * }
     *
     * @param keyFn extracts the key used for equality; equality is determined
     *              by the key's {@code equals}/{@code hashCode}
     * @param <T>   element type
     * @param <K>   key type
     * @return a sequential gatherer that emits the first element seen
     *         for each distinct key
     * @since 1.1.0
     */
    public static <T, K> Gatherer<T, ?, T> distinctBy(Function<? super T, ? extends K> keyFn) {
        Supplier<HashSet<K>> initializer = HashSet::new;

        Gatherer.Integrator<HashSet<K>, T, T> integrator = (state, element, downstream) -> {
            K key = keyFn.apply(element);
            if (state.add(key)) {
                return downstream.push(element);
            }
            return true;
        };

        return Gatherer.ofSequential(initializer, integrator);
    }
}
