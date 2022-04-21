/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation.history;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import ph.com.globe.globeonesuperapp.utils.navigation.keys.IScreenKey;

@SuppressWarnings("JdkObsolete")// LinkedList support equals while ArrayDeque does not
public class HistoryBuilder<T extends IScreenKey> {
    public final Deque<T> keys = new LinkedList<>();

    public HistoryBuilder(History<T> history) {
        Iterator<T> tIterator = history.keys.descendingIterator();
        while (tIterator.hasNext()) {
            keys.push(tIterator.next());
        }
        History.checkStackSame(keys, history.keys);
    }

    public HistoryBuilder() {
    }

    public HistoryBuilder<T> push(T key) {
        keys.push(key);
        return this;
    }

    public HistoryBuilder<T> pop() {
        keys.pop();
        return this;
    }

    @SafeVarargs
    public final HistoryBuilder<T> replace(T... keys) {
        for (T key : keys) {
            this.keys.push(key);
        }
        return this;
    }

    public HistoryBuilder<T> replace(ArrayList<T> keys) {
        if (keys != null && !keys.isEmpty()) {
            for (T key : keys) {
                this.keys.push(key);
            }
        }
        return this;
    }

    public History<T> build() {
        return new History<>(keys);
    }

    public T peek() {
        return keys.peek();
    }

    public HistoryBuilder<T> pushLast(T t) {
        keys.addLast(t);
        return this;
    }
}
