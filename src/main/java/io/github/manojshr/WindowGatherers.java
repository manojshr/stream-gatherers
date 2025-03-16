package io.github.manojshr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class WindowGatherers {

    public static <T> Gatherer<T, ?, List<T>> fixedWindow(int limit) {
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

    public static <T> Gatherer<T, ?, List<T>> slidingWindow(int limit) {
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
