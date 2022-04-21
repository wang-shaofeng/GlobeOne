/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ph.com.globe.globeonesuperapp.utils.navigation.backstack.BackStack;
import ph.com.globe.globeonesuperapp.utils.navigation.history.History;
import ph.com.globe.globeonesuperapp.utils.navigation.keys.IScreenKey;


/**
 * Helps with dispatching events and managing listeners.
 * It provides a way to detect re-entrant calls by using {@link #isDispatchingTo(BackStack.NavigationListener)}
 * method.
 */
public class NavigationListenersHelper<Key extends IScreenKey> {
    private final Map<BackStack.NavigationListener<Key>, ListenerWrapper> listeners = new ConcurrentHashMap<>();
    private final BackStack<Key, ?> backStack;
    /**
     * Listeners that we are currently dispatching to.
     */
    @SuppressWarnings("JdkObsolete") // LinkedList allocates less in case of 1 element
    private final Deque<ListenerWrapper> dispatchingListeners = new LinkedList<>();

    public NavigationListenersHelper(BackStack<Key, ?> backStack) {
        this.backStack = backStack;
    }

    /**
     * Check if we are currently dispatching an event to passed listener
     */
    public boolean isDispatchingTo(BackStack.NavigationListener<Key> navigationListener) {
        if (dispatchingListeners.peek() == null) {
            return false;
        }
        return listeners.get(navigationListener) == dispatchingListeners.peek();
    }

    /**
     * Add listener to listen to navigation events.
     *
     * @return same listener you passed in. Useful for lambdas as listeners
     */
    public BackStack.NavigationListener<Key> addNavigationListener(BackStack.NavigationListener<Key> listener) {
        ListenerWrapper wrapper = new ListenerWrapper(listener);
        addListenerInternal(listener, wrapper);
        return listener;
    }

    /**
     * Register listener that will be automatically removed when activity/fragment is destroyed.
     * Current state is immediately dispatched.
     *
     * @return listener just added so you cen remove it after if you're using lambdas
     */
    public BackStack.NavigationListener<Key> addNavigationListener(LifecycleOwner lifecycleOwner,
                                                            BackStack.NavigationListener<Key> listener) {
        ListenerWrapper wrapper = new LifecycleListenerWrapper(lifecycleOwner, listener);
        addListenerInternal(listener, wrapper);
        return listener;
    }

    /**
     * Similar to {@link #isDispatchingTo(BackStack.NavigationListener)} but check for every listener.
     */
    public boolean isDispatching() {
        return !dispatchingListeners.isEmpty();
    }

    public final Collection<? extends BackStack.NavigationListener<Key>> listeners() {
        return Collections.unmodifiableCollection(listeners.values());
    }

    private void addListenerInternal(BackStack.NavigationListener<Key> listener, ListenerWrapper wrapper) {
        if (listeners.put(listener, wrapper) != null) {
            throw new IllegalStateException("Added same listener twice: " + listener);
        }
        wrapper.attach();
        dispatchToListener(wrapper, null);
    }

    public void removeNavigationListener(BackStack.NavigationListener<Key> navListener) {
        ListenerWrapper wrapper = listeners.remove(navListener);
        if (wrapper != null) {
            wrapper.detach();
        }
    }

    private void dispatchToListener(ListenerWrapper listener,
                                    @Nullable History<Key> previous) {
        dispatchingListeners.push(listener);
        try {
            listener.onNavigated(previous, backStack.history());
        } finally {
            dispatchingListeners.pop();
        }
    }

    public void dispatchNavigationChanged(History<Key> previousState) {
        if (previousState.equals(backStack.history())) {
            return;
        }
        for (ListenerWrapper listener : listeners.values()) {
            dispatchToListener(listener, previousState);
        }
    }

    /**
     * Wraps listener client want to register, and exposes attach/detach methods that should be
     * called when registering/deregistering
     */
    private class ListenerWrapper implements BackStack.NavigationListener<Key> {
        final BackStack.NavigationListener<Key> real;

        private ListenerWrapper(BackStack.NavigationListener<Key> real) {
            this.real = real;
        }

        @Override
        public void onNavigated(@Nullable History<Key> previousState, History<Key> history) {
            real.onNavigated(previousState, history);
        }

        void attach() {
            // no-op
        }

        void detach() {
            // no-op
        }

    }

    /**
     * {@link ListenerWrapper} that knows to unregister itself by using
     * {@link androidx.lifecycle.Lifecycle}.
     */
    private class LifecycleListenerWrapper extends ListenerWrapper implements DefaultLifecycleObserver {
        private final LifecycleOwner lifecycleOwner;

        LifecycleListenerWrapper(LifecycleOwner lifecycleOwner, BackStack.NavigationListener<Key> real) {
            super(real);
            this.lifecycleOwner = lifecycleOwner;
        }

        @Override
        void attach() {
            lifecycleOwner.getLifecycle().addObserver(this);
        }

        @Override
        void detach() {
            lifecycleOwner.getLifecycle().removeObserver(this);
        }

        @Override
        public void onNavigated(History<Key> previousState, History<Key> history) {
            real.onNavigated(previousState, history);
        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            lifecycleOwner.getLifecycle().removeObserver(this);
            listeners.remove(real);
        }
    }
}
