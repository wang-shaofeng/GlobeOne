/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation.history;

import android.os.Bundle;

import androidx.annotation.CheckResult;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import ph.com.globe.globeonesuperapp.utils.navigation.keys.IScreenKey;


/**
 * Immutable stack of keys. All operations that would mutate stack return new instance of the
 * History.
 *
 * @param <T> key type parameter, extends {@link IScreenKey}
 */
@MainThread
@SuppressWarnings("JdkObsolete")// LinkedList support equals while ArrayDeque does not
public class History<T extends IScreenKey> implements Iterable<T> {
    private static final String STATE_KEY = "navigation:HistoryState";
    final Deque<T> keys;

    History(Deque<T> keys) {
        // LinkedList support equals while ArrayDeque does not, so don't change it
        this.keys = new LinkedList<>(keys);
        checkStackSame(this.keys, keys);
    }

    static void checkStackSame(Deque<?> keys, Deque<?> keys1) {
        if (!keys.equals(keys1)) {
            throw new IllegalStateException("not same: " + keys + ", k2 " + keys1);
        }
    }

    public History() {
        this.keys = new LinkedList<>();
    }

    public static <T extends IScreenKey> History<T> singleton(T screenKey) {
        History<T> history = new History<>();
        history.keys.push(screenKey);
        return history;
    }

    public static <T extends IScreenKey> History<T> empty() {
        return new History<>();
    }

    /**
     * Creates new history from array. Last in the array will be at the top of the stack
     */
    @SafeVarargs
    public static <T extends IScreenKey> History<T> from(T... keys) {
        History<T> history = new History<>();
        for (T key : keys) {
            history.keys.push(key);
        }
        return history;
    }

    /**
     * @return Key at the top of the stack
     */
    public T peek() {
        return keys.peek();
    }

    /**
     * @return Key at the bottom of the stack
     */
    public T getLast() {
        return keys.peekLast();
    }

    @CheckResult
    public History<T> pop() {
        return this.toBuilder().pop().build();
    }

    @CheckResult
    public History<T> push(T key) {
        return this.toBuilder().push(key).build();
    }

    public int size() {
        return keys.size();
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    /**
     * Elements in this history but not in other. It goes from top of this history and pops
     * elements until same key is found
     * <p>
     * So if this history is {1,2 ,3} where 3 is top of the stack and other history is {1} then
     * returned history will be {2,3}
     */
    public History<T> topDifference(History<T> other) {
        if (other.isEmpty() || isEmpty()) {
            return this;
        }

        HistoryBuilder<T> diff = new HistoryBuilder<>();
        HistoryBuilder<T> builder = toBuilder();
        T peek;
        while ((peek = builder.peek()) != null && !peek.equals(other.peek())) {
            builder.pop();
            diff.pushLast(peek);
        }
        return diff.build();
    }

    /**
     * Save History state to Bundle.
     * You should not assume anything about how state is stored, and state should always be
     * restored by calling {@link #restoreFromState(Bundle)}.
     * @param bundle Bundle in which to save state.
     */
    public void saveToBundle(Bundle bundle) {
        ArrayList<T> ts = new ArrayList<>(keys);
        Collections.reverse(ts);
        bundle.putParcelableArrayList(STATE_KEY, ts);
    }

    /**
     * Given Bundle restore History object with same structure and elements as in moment when
     * save in {@link #saveToBundle(Bundle)}.
     *
     * @return History restored from Bundle
     */
    public static <M extends IScreenKey> History<M> restoreFromState(Bundle bundle) {
        ArrayList<M> parcelableArrayList = bundle.getParcelableArrayList(STATE_KEY);
        return new HistoryBuilder<M>().replace(parcelableArrayList).build();
    }

    public HistoryBuilder<T> toBuilder() {
        return new HistoryBuilder<>(this);
    }

    @Override
    public String toString() {
        return "History{" +
                "keys=" + keys +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        History<?> history = (History<?>) o;

        return keys.equals(history.keys);
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }

    @CheckResult
    public History<T> bringToTop(T key) {
        LinkedList<T> newkeys = new LinkedList<>(keys);
        boolean removed = newkeys.removeFirstOccurrence(key);
        if (!removed) {
            throw new IllegalStateException();
        }
        newkeys.push(key);
        return new History<>(newkeys);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            final Iterator<T> real = keys.iterator();

            @Override
            public boolean hasNext() {
                return real.hasNext();
            }

            @Override
            public T next() {
                return real.next();
            }
        };
    }

    public boolean contains(T key) {
        return keys.contains(key);
    }

}

